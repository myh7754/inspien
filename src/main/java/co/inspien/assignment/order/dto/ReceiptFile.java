package co.inspien.assignment.order.dto;

/**
 * 생성된 영수증 파일 한 건 — 파일명과 내용(바이트).
 * 내용은 인코딩이 적용된 byte[]이며, FTP 전송 시 그대로 업로드한다.
 */
public record ReceiptFile(String filename, byte[] content) {
}
