package co.inspien.assignment.order.repository;
import co.inspien.assignment.order.util.IdGenerator;
import co.inspien.assignment.order.dto.OrderRecord;

import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private static final int MAX_RETRY = 5;
    private static final String INSERT_SQL =
            "INSERT INTO ORDER_TB (ORDER_ID, APPLICANT_KEY, USER_ID, ITEM_ID, NAME, ADDRESS, ITEM_NAME, PRICE, STATUS) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'N')";

    private final JdbcTemplate jdbc;
    private final IdGenerator idGenerator;

    /**
     * 주문 레코드를 ORDER_TB에 적재한다. STATUS는 'N'으로 고정(FR-S1-03-a).
     * @return 생성된 ORDER_ID 목록
     */
    public List<String> saveAll(List<OrderRecord> records, String applicantKey) {
        List<String> orderIds = new ArrayList<>();
        for (OrderRecord r : records) {
            orderIds.add(insertWithRetry(r, applicantKey));
        }
        return orderIds;
    }

    private String insertWithRetry(OrderRecord r, String applicantKey) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            String orderId = idGenerator.generate();
            try {
                jdbc.update(INSERT_SQL,
                        orderId, applicantKey,
                        r.userId(), r.itemId(),
                        r.name(), r.address(),
                        r.itemName(), r.price());
                return orderId;
            } catch (DuplicateKeyException ignored) {
                // 복합 PK 충돌 시 다른 ID로 재시도 (NFR-ID-02)
            }
        }
        throw new InspienException(ErrorCode.ORDER_PERSISTENCE_ERROR,
                "채번 재시도 " + MAX_RETRY + "회 초과");
    }
}
