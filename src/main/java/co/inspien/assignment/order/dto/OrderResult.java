package co.inspien.assignment.order.dto;

import java.util.List;

/**
 * 주문 처리 결과 — 적재된 ORDER_ID 목록과 전송된 영수증 파일명.
 * 컨트롤러가 성공 응답(JSON)으로 직렬화한다(FR-S1-09-a).
 */
public record OrderResult(List<String> orderIds, String receiptFilename) {}
