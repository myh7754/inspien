package co.inspien.assignment.scenario1;

import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("주문 오케스트레이션")
class OrderServiceTest {

    private static final String APPLICANT_KEY = "KEY999";

    @Mock OrderXmlParser parser;
    @Mock OrderValidator validator;
    @Mock OrderRepository repository;
    @Mock ReceiptFileBuilder receiptFileBuilder;
    @Mock ReceiptFtpSender ftpSender;

    OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(parser, validator, repository, receiptFileBuilder, ftpSender, APPLICANT_KEY);
    }

    @Test
    @DisplayName("정상 흐름: 적재된 ORDER_ID와 전송된 영수증 파일명을 돌려준다")
    void process_happyPath_returnsOrderIdsAndReceiptFilename() {
        String xml = "<HEADER>...</HEADER><ITEM>...</ITEM>";
        List<OrderRecord> records = List.of(
                new OrderRecord("USER1", "홍길동", "서울특별시 금천구", "ITEM1", "청바지", 21000));
        List<String> orderIds = List.of("A113");
        ReceiptFile receipt = new ReceiptFile("INSPIEN_문영훈_20260615120000.txt", new byte[]{1, 2, 3});

        when(parser.parse(xml)).thenReturn(records);
        when(repository.saveAll(records, APPLICANT_KEY)).thenReturn(orderIds);
        when(receiptFileBuilder.build(records, orderIds, APPLICANT_KEY)).thenReturn(receipt);
        when(ftpSender.upload(receipt)).thenReturn(receipt.filename());

        OrderResult result = service.process(xml);

        assertThat(result.orderIds()).isEqualTo(orderIds);
        assertThat(result.receiptFilename()).isEqualTo("INSPIEN_문영훈_20260615120000.txt");
        verify(validator).validate(records);
    }

    @Test
    @DisplayName("FTP 전송 실패는 그대로 전파된다 — saveAll(트랜잭션 내 INSERT)은 이미 수행되어 롤백 대상이 된다")
    void process_ftpFailure_propagatesAfterSaveAll() {
        String xml = "<HEADER>...</HEADER><ITEM>...</ITEM>";
        List<OrderRecord> records = List.of(
                new OrderRecord("USER1", "홍길동", "서울특별시 금천구", "ITEM1", "청바지", 21000));
        List<String> orderIds = List.of("A113");
        ReceiptFile receipt = new ReceiptFile("INSPIEN_문영훈_20260615120000.txt", new byte[]{1, 2, 3});

        when(parser.parse(xml)).thenReturn(records);
        when(repository.saveAll(records, APPLICANT_KEY)).thenReturn(orderIds);
        when(receiptFileBuilder.build(records, orderIds, APPLICANT_KEY)).thenReturn(receipt);
        when(ftpSender.upload(receipt))
                .thenThrow(new InspienException(ErrorCode.FTP_UPLOAD_ERROR, "업로드 실패"));

        assertThatThrownBy(() -> service.process(xml))
                .isInstanceOf(InspienException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.FTP_UPLOAD_ERROR);

        // INSERT는 FTP 전송 전에 트랜잭션 안에서 일어났어야 한다(보상 = 롤백 대상 확보)
        verify(repository).saveAll(records, APPLICANT_KEY);
    }

    @Test
    @DisplayName("검증 실패는 적재/전송 이전에 단락된다 — saveAll·upload는 호출되지 않는다")
    void process_validationFailure_shortCircuitsBeforePersistence() {
        String xml = "<HEADER>...</HEADER>";
        List<OrderRecord> records = List.of(
                new OrderRecord("USER1", "홍길동", "서울특별시 금천구", "ITEM1", "청바지", 21000));

        when(parser.parse(xml)).thenReturn(records);
        doThrow(new InspienException(ErrorCode.VALIDATION_ERROR, "검증 실패"))
                .when(validator).validate(records);

        assertThatThrownBy(() -> service.process(xml))
                .isInstanceOf(InspienException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(repository, never()).saveAll(any(), any());
        verify(ftpSender, never()).upload(any());
    }
}
