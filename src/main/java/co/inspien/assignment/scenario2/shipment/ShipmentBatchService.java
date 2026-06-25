package co.inspien.assignment.scenario2.shipment;

import co.inspien.assignment.bootstrap.ApplicantContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 운송 배치 오케스트레이션(FR-S2). STATUS='N' 미전송 주문을 조회해 건별로 처리한다.
 * 건별 적재+상태전이는 {@link ShipmentItemProcessor}가 하나의 트랜잭션으로 수행하므로,
 * 한 건 안에서 일부만 반영되는 일이 없다. 한 건 실패가 전체를 막지 않으며(건별 격리),
 * 실패 건은 STATUS='N'을 유지해 다음 배치에서 재시도된다(FR-S2-03-a).
 */
@Service
@RequiredArgsConstructor
public class ShipmentBatchService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentBatchService.class);

    private final ShipmentRepository repository;
    private final ShipmentItemProcessor processor;
    private final ApplicantContext applicant;

    public BatchResult run() {
        String applicantKey = applicant.key();
        List<ShipmentRecord> targets = repository.findUnsent(applicantKey);
        log.info("운송 배치 시작 — 미전송 대상 {}건", targets.size());

        int success = 0;
        int failed = 0;
        for (ShipmentRecord target : targets) {
            try {
                String shipmentId = processor.processOne(target, applicantKey);
                success++;
                log.info("운송 적재 성공 — ORDER_ID={} → SHIPMENT_ID={}", target.orderId(), shipmentId);
            } catch (Exception e) {
                failed++;
                log.error("운송 적재 실패 — ORDER_ID={} (STATUS='N' 유지, 다음 배치 재시도): {}",
                        target.orderId(), e.getMessage());
            }
        }

        BatchResult result = new BatchResult(targets.size(), success, failed);
        log.info("운송 배치 종료 — {}", result);
        return result;
    }

    public record BatchResult(int total, int success, int failed) {}
}
