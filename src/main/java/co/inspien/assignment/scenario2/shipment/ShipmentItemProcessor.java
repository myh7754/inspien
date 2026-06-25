package co.inspien.assignment.scenario2.shipment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 운송 '한 건'을 하나의 트랜잭션으로 처리한다.
 *
 * <p>운송 적재(insertShipment)와 주문 상태 전이(markSent)를 한 트랜잭션으로 묶어,
 * 둘 중 하나라도 실패하면 함께 롤백되게 한다. 이렇게 하면 "적재는 됐는데 상태 전이가 실패"한
 * 경우에도 주문이 STATUS='N'으로 남아 다음 배치에서 깨끗하게 재시도되고, 운송이 중복 생성되지 않는다.
 *
 * <p>배치 루프와 별도 빈으로 분리한 이유: 같은 빈 안에서 {@code @Transactional} 메서드를
 * 자기호출하면 Spring 프록시를 거치지 않아 트랜잭션이 적용되지 않는다(self-invocation).
 * 별도 빈으로 두어 프록시 경유 호출이 되도록 한다.
 */
@Service
@RequiredArgsConstructor
public class ShipmentItemProcessor {

    private final ShipmentRepository repository;

    /**
     * 운송 적재 + 주문 상태 전이를 한 트랜잭션으로 처리한다.
     * @return 생성된 SHIPMENT_ID
     */
    @Transactional
    public String processOne(ShipmentRecord target, String applicantKey) {
        String shipmentId = repository.insertShipment(target, applicantKey);
        repository.markSent(target.orderId(), applicantKey);
        return shipmentId;
    }
}
