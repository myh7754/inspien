package co.inspien.assignment.scenario2.shipment;

/**
 * ORDER_TB(STATUS='N')에서 읽어와 SHIPMENT_TB로 보낼 배송 대상 1행.
 * SHIPMENT_ID(채번)와 APPLICANT_KEY(고정키)는 적재 시점에 부여되므로 제외.
 * orderId는 채번 대상이 아니라 ORDER_TB에서 읽어온 기존 값을 그대로 보존한다.
 */
public record ShipmentRecord(
        String orderId,
        String itemId,
        String address
) {}
