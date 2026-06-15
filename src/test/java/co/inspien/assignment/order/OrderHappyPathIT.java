package co.inspien.assignment.order;

import co.inspien.assignment.bootstrap.ApplicantContext;
import co.inspien.assignment.order.config.FtpProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 시나리오1 happy-path 통합테스트: XML POST → 실 ORDER_TB 적재 + 실 FTP 업로드 → SUCCESS.
 *
 * <p>목킹 없이 전체 빈 그래프(컨트롤러→서비스→리포지토리→FTP)를 실 제공 API·Oracle·FTP에 태운다.
 * 따라서 {@code test} 프로파일을 켜지 않는다(실 applicantKey 필요).
 *
 * <p>주의: ORDER_TB·FTP 모두 DELETE 권한이 없어 실행할 때마다 행/파일이 누적된다(정리 불가).
 * 데모(AC-07)에서 내 APPLICANT_KEY 행 조회·FTP 파일 확인의 근거가 된다.
 *
 * 실행: ./gradlew integrationTest
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("주문 happy-path 통합 (XML POST → DB+FTP → SUCCESS)")
class OrderHappyPathIT {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ApplicantContext applicant;
    @Autowired FtpProperties ftp;

    @Test
    @DisplayName("주문 2건을 POST하면 ORDER_TB에 적재되고 영수증이 FTP에 올라간다")
    void post_order_persists_to_db_and_uploads_receipt_to_ftp() throws Exception {
        String xml = """
                <HEADER>
                    <USER_ID>ITUSER1</USER_ID>
                    <NAME>문영훈</NAME>
                    <ADDRESS>서울특별시 금천구</ADDRESS>
                    <STATUS>N</STATUS>
                </HEADER>
                <ITEM>
                    <USER_ID>ITUSER1</USER_ID>
                    <ITEM_ID>ITEM01</ITEM_ID>
                    <ITEM_NAME>청바지</ITEM_NAME>
                    <PRICE>21000</PRICE>
                </ITEM>
                <ITEM>
                    <USER_ID>ITUSER1</USER_ID>
                    <ITEM_ID>ITEM02</ITEM_ID>
                    <ITEM_NAME>티셔츠</ITEM_NAME>
                    <PRICE>15800</PRICE>
                </ITEM>
                """;

        MvcResult result = mockMvc.perform(post("/orders")
                        .contentType(MediaType.valueOf("application/xml;charset=UTF-8"))
                        .content(xml.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.orderIds.length()").value(2))
                .andExpect(jsonPath("$.ftpFile").exists())
                .andReturn();

        JsonNode json = MAPPER.readTree(result.getResponse().getContentAsString());
        List<String> orderIds = new ArrayList<>();
        json.get("orderIds").forEach(n -> orderIds.add(n.asText()));
        String ftpFile = json.get("ftpFile").asText();

        // DB 검증: 반환된 각 ORDER_ID가 내 APPLICANT_KEY 스코프로 STATUS='N' 적재됨
        for (String orderId : orderIds) {
            Map<String, Object> row = jdbc.queryForMap(
                    "SELECT USER_ID, ITEM_ID, STATUS FROM ORDER_TB WHERE ORDER_ID = ? AND APPLICANT_KEY = ?",
                    orderId, applicant.key());
            assertThat(row.get("USER_ID")).isEqualTo("ITUSER1");
            assertThat(row.get("STATUS")).isEqualTo("N");
        }

        // FTP 검증: 반환된 영수증 파일명이 서버 디렉터리에 존재(계정은 업로드 전용 → listNames로만 확인)
        assertThat(listFtpFilenames()).contains(ftpFile);
    }

    private List<String> listFtpFilenames() throws Exception {
        FTPClient client = new FTPClient();
        client.setControlEncoding("EUC-KR");
        try {
            client.connect(ftp.url(), ftp.port());
            client.login(ftp.username(), ftp.password());
            client.enterLocalPassiveMode();
            if (ftp.path() != null && !ftp.path().isBlank()) {
                client.changeWorkingDirectory(ftp.path());
            }
            String[] names = client.listNames();
            return names == null ? List.of() : Arrays.asList(names);
        } finally {
            if (client.isConnected()) {
                client.disconnect();
            }
        }
    }
}
