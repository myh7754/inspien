package co.inspien.assignment.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // stage = 응답 JSON 실패 단계(PRD §6.4). 파싱 실패는 입력 문제이므로 VALIDATION으로 묶는다.
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "주문 데이터 검증에 실패했습니다.", "VALIDATION"),
    XML_PARSE_ERROR(HttpStatus.BAD_REQUEST, "주문 XML 파싱에 실패했습니다.", "VALIDATION"),
    ORDER_PERSISTENCE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "주문 저장에 실패했습니다.", "JDBC"),
    SHIPMENT_PERSISTENCE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "운송 정보 저장에 실패했습니다.", "BATCH"),
    FTP_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "영수증 파일 전송에 실패했습니다.", "FTP"),
    PROVISIONING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "접속 정보 수신에 실패했습니다.", "BOOTSTRAP"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", "INTERNAL");

    private final HttpStatus status;
    private final String defaultMessage;
    private final String stage;

    ErrorCode(HttpStatus status, String defaultMessage, String stage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
        this.stage = stage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getStage() {
        return stage;
    }
}
