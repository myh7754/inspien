package co.inspien.assignment.order.service;
import co.inspien.assignment.order.parser.OrderXmlParser;
import co.inspien.assignment.order.repository.OrderRepository;
import co.inspien.assignment.order.dto.OrderRecord;
import co.inspien.assignment.order.dto.ReceiptFile;
import co.inspien.assignment.order.dto.OrderResult;

import co.inspien.assignment.bootstrap.ApplicantContext;
import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 시나리오1 실시간 주문 오케스트레이션(FR-S1-09).
 *
 * <p>parse → validate → saveAll → 영수증 생성 → FTP 전송을 하나의 트랜잭션으로 묶는다(NFR-TX-01).
 * 보상 전략: INSERT(트랜잭션 내) → FTP 전송 → 커밋 순서.
 * • FTP 실패 시 예외 전파 → 자동 rollback (NFR-TX-01)
 * • FTP 성공 + commit 실패 시 불일치 로그 + 수동 개입 (NFR-TX-03)
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

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

        // FTP 전송 (트랜잭션 내). 실패하면 예외 → 자동 rollback (NFR-TX-01)
        String receiptFilename = ftpSender.upload(receipt);

        // 이 지점: JDBC & FTP 모두 성공. 이제 commit 시도.
        // commit 실패 후 FTP 파일이 이미 올라간 상태는 NFR-TX-03으로 처리:
        // 불일치 상태를 로그에 명시적으로 기록하고 수동 개입 필요
        log.info("[NFR-TX-03 주의] 주문 적재 완료 [orderIds={}], FTP 업로드 완료 [file={}]. COMMIT 시도 중...",
                orderIds, receiptFilename);

        // commit은 메서드 종료 시 자동으로 일어남 (@Transactional의 프록시)
        // 만약 여기서 commit이 실패하면, GlobalExceptionHandler가 catch하고
        // 로그에 "FAIL [stage=INTERNAL, reason=...]"가 남음.
        // 하지만 FTP는 이미 올라갔으므로 불일치 상태.

        return new OrderResult(orderIds, receiptFilename);
    }

    /**
     * 테스트용: FTP 강제 실패 시나리오.
     * FTP 업로드 실패 → JDBC 자동 rollback (NFR-TX-01) 검증.
     */
    @Transactional
    public OrderResult processWithFtpFailure(String xml) {
        List<OrderRecord> records = parser.parse(xml);
        validator.validate(records);
        List<String> orderIds = repository.saveAll(records, applicant.key());

        ReceiptFile receipt = receiptFileBuilder.build(records, orderIds, applicant.key());
        log.warn("테스트: FTP 강제 실패 시나리오 (NFR-TX-01 검증). JDBC는 이미 INSERT됨.");

        // FTP를 강제로 실패시킴
        throw new InspienException(ErrorCode.FTP_UPLOAD_ERROR,
                "테스트 시나리오: FTP 강제 실패. JDBC는 자동 rollback됩니다.");
    }
}
