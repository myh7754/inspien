package co.inspien.assignment.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    XML_PARSE_ERROR(HttpStatus.BAD_REQUEST, "주문 XML 파싱에 실패했습니다."),
    ORDER_PERSISTENCE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "주문 저장에 실패했습니다."),
    FTP_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "영수증 파일 전송에 실패했습니다."),
    PROVISIONING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "접속 정보 수신에 실패했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
