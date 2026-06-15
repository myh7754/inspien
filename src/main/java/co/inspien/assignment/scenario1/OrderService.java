package co.inspien.assignment.scenario1;

import co.inspien.assignment.bootstrap.ApplicantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 시나리오1 실시간 주문 오케스트레이션(FR-S1-09).
 *
 * <p>parse → validate → saveAll → 영수증 생성 → FTP 전송을 하나의 트랜잭션으로 묶는다(NFR-TX-01).
 * 보상 전략: INSERT(트랜잭션 내) → FTP 전송 → 커밋 순서. FTP 전송이 실패하면 예외가 전파되어
 * 트랜잭션이 롤백되므로 적재된 주문이 함께 취소된다(FTP/DB 모두 DELETE 권한이 없어 삭제 보상은 불가).
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderXmlParser parser;
    private final OrderValidator validator;
    private final OrderRepository repository;
    private final ReceiptFileBuilder receiptFileBuilder;
    private final ReceiptFtpSender ftpSender;
    private final ApplicantContext applicant;

    @Transactional
    public OrderResult process(String xml) {
        List<OrderRecord> records = parser.parse(xml);
        validator.validate(records);
        List<String> orderIds = repository.saveAll(records, applicant.key());

        ReceiptFile receipt = receiptFileBuilder.build(records, orderIds, applicant.key());
        String receiptFilename = ftpSender.upload(receipt);

        return new OrderResult(orderIds, receiptFilename);
    }
}
