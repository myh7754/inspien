package co.inspien.assignment.scenario2.shipment;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 운송 배치 트리거(FR-S2-01). 5분 주기로 {@link ShipmentBatchService}를 깨운다.
 * "언제 실행할지"만 책임지고 실제 처리는 서비스에 위임한다.
 */
@Component
@RequiredArgsConstructor
public class ShipmentScheduler {

    private final ShipmentBatchService batchService;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void trigger() {
        batchService.run();
    }
}
