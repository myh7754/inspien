package co.inspien.assignment.scenario1;

import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import co.inspien.assignment.common.web.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("주문 REST 엔드포인트")
class OrderControllerTest {

    private static final String XML = "<HEADER>...</HEADER><ITEM>...</ITEM>";

    @Autowired MockMvc mockMvc;
    @MockitoBean OrderService orderService;

    @Test
    @DisplayName("성공: 200 + {result:SUCCESS, orderIds, ftpFile}")
    void success_returnsSuccessJson() throws Exception {
        when(orderService.process(anyString()))
                .thenReturn(new OrderResult(List.of("A113", "B114"),
                        "INSPIEN_문영훈_20260615120000.txt"));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_XML).content(XML))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.orderIds[0]").value("A113"))
                .andExpect(jsonPath("$.orderIds[1]").value("B114"))
                .andExpect(jsonPath("$.ftpFile").value("INSPIEN_문영훈_20260615120000.txt"));
    }

    @Test
    @DisplayName("검증 실패: 400 + {result:FAIL, stage:VALIDATION, reason}")
    void validationFailure_returnsFailJsonWithValidationStage() throws Exception {
        when(orderService.process(anyString()))
                .thenThrow(new InspienException(ErrorCode.VALIDATION_ERROR, "NAME 필드가 비어 있습니다"));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_XML).content(XML))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("FAIL"))
                .andExpect(jsonPath("$.stage").value("VALIDATION"))
                .andExpect(jsonPath("$.reason").value("NAME 필드가 비어 있습니다"));
    }

    @Test
    @DisplayName("FTP 실패: 500 + {result:FAIL, stage:FTP, reason}")
    void ftpFailure_returnsFailJsonWithFtpStage() throws Exception {
        when(orderService.process(anyString()))
                .thenThrow(new InspienException(ErrorCode.FTP_UPLOAD_ERROR, "영수증 업로드 실패"));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_XML).content(XML))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.result").value("FAIL"))
                .andExpect(jsonPath("$.stage").value("FTP"))
                .andExpect(jsonPath("$.reason").value("영수증 업로드 실패"));
    }
}
