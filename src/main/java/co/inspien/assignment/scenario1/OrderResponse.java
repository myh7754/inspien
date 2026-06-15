package co.inspien.assignment.scenario1;

import java.util.List;

/**
 * 주문 성공 응답(FR-S1-09-a, PRD §6.4):
 * {@code { "result": "SUCCESS", "orderIds": [...], "ftpFile": "..." }}
 */
public record OrderResponse(String result, List<String> orderIds, String ftpFile) {

    public static OrderResponse success(OrderResult result) {
        return new OrderResponse("SUCCESS", result.orderIds(), result.receiptFilename());
    }
}
