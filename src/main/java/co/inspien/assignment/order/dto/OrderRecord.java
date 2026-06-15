package co.inspien.assignment.order.dto;

/**
 * XML 1:N 파싱 결과 — HEADER + ITEM join된 flat 레코드 1행.
 * ORDER_TB 한 행에 대응.
 */
public record OrderRecord(
        String userId,
        String name,
        String address,
        String itemId,
        String itemName,
        int price
) {}
