package co.inspien.assignment.order.service;
import co.inspien.assignment.order.config.FtpProperties;
import co.inspien.assignment.order.dto.ReceiptFile;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 FTP 서버에 영수증 파일을 올리는 통합테스트.
 *
 * 주의: 제공 FTP 계정은 업로드 전용(STOR만 허용). RETR/DELE는 550, 덮어쓰기는 553으로 거부되므로
 * 내용 재다운로드·사후 삭제·동일명 재업로드가 모두 불가능하다. 대신 디렉터리 목록(LIST는 허용)에
 * 업로드한 파일명이 나타나는지로 검증하고, 재실행 충돌(553)을 피하려 매 실행 유니크 파일명을 쓴다.
 *
 * 실행: ./gradlew integrationTest
 */
@Tag("integration")
@SpringBootTest
@DisplayName("영수증 FTP 전송 통합")
class ReceiptFtpSenderIT {

    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    @Autowired ReceiptFtpSender sender;
    @Autowired FtpProperties ftp;

    @Test
    @DisplayName("영수증 파일을 FTP에 업로드하면 서버 디렉터리에 나타난다")
    void uploads_receipt_to_ftp() throws Exception {
        String filename = "INSPIEN_문영훈_IT_" + System.currentTimeMillis() + ".txt";
        byte[] content = "A113^USER1^ITEM1^KEY999^홍길동^서울특별시 금천구^청바지^21000\n".getBytes(EUC_KR);
        ReceiptFile file = new ReceiptFile(filename, content);

        String uploaded = sender.upload(file);

        assertThat(uploaded).isEqualTo(filename);
        assertThat(listFilenames()).contains(filename);
    }

    /** 검증용 raw 클라이언트로 현재 디렉터리 파일명 목록을 가져온다. */
    private List<String> listFilenames() throws Exception {
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
