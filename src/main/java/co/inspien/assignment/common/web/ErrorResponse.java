package co.inspien.assignment.common.web;

import co.inspien.assignment.common.exception.ErrorCode;

/**
 * 실패 응답(PRD §6.4): {@code { "result": "FAIL", "stage": "...", "reason": "..." }}
 * stage는 실패 단계(VALIDATION/JDBC/FTP 등)로 {@link ErrorCode}가 보유한다.
 */
public record ErrorResponse(String result, String stage, String reason) {

    public static ErrorResponse fail(ErrorCode errorCode, String reason) {
        return new ErrorResponse("FAIL", errorCode.getStage(), reason);
    }
}
