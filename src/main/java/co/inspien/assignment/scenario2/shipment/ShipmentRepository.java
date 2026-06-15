package co.inspien.assignment.scenario2.shipment;

import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import co.inspien.assignment.order.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ShipmentRepository {

    private static final int MAX_RETRY = 5;

    private static final String SELECT_UNSENT_SQL =
            "SELECT ORDER_ID, ITEM_ID, ADDRESS FROM ORDER_TB " +
            "WHERE APPLICANT_KEY = ? AND STATUS = 'N'";

    private static final String INSERT_SQL =
            "INSERT INTO SHIPMENT_TB (SHIPMENT_ID, APPLICANT_KEY, ORDER_ID, ITEM_ID, ADDRESS) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_STATUS_SQL =
            "UPDATE ORDER_TB SET STATUS = 'Y' WHERE ORDER_ID = ? AND APPLICANT_KEY = ?";

    private final JdbcTemplate jdbc;
    private final IdGenerator idGenerator;

    /** ORDER_TB에서 아직 운송 전송되지 않은(STATUS='N') 주문을 조회한다. */
    public List<ShipmentRecord> findUnsent(String applicantKey) {
        return jdbc.query(SELECT_UNSENT_SQL,
                (rs, rowNum) -> new ShipmentRecord(
                        rs.getString("ORDER_ID"),
                        rs.getString("ITEM_ID"),
                        rs.getString("ADDRESS")),
                applicantKey);
    }

    /**
     * SHIPMENT_TB에 적재한다. SHIPMENT_ID는 채번하며, 복합 PK 충돌 시 재채번 재시도(NFR-ID-02).
     * @return 생성된 SHIPMENT_ID
     */
    public String insertShipment(ShipmentRecord record, String applicantKey) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            String shipmentId = idGenerator.generate();
            try {
                jdbc.update(INSERT_SQL,
                        shipmentId, applicantKey,
                        record.orderId(), record.itemId(), record.address());
                return shipmentId;
            } catch (DuplicateKeyException ignored) {
                // 복합 PK 충돌 시 다른 ID로 재시도
            }
        }
        throw new InspienException(ErrorCode.SHIPMENT_PERSISTENCE_ERROR,
                "SHIPMENT_ID 채번 재시도 " + MAX_RETRY + "회 초과");
    }

    /** 운송 적재에 성공한 주문을 STATUS='Y'로 전이한다(S1↔S2 연결고리). */
    public void markSent(String orderId, String applicantKey) {
        jdbc.update(UPDATE_STATUS_SQL, orderId, applicantKey);
    }
}
