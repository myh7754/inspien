package co.inspien.assignment.common.web;

import co.inspien.assignment.common.exception.ErrorCode;

public record ErrorResponse(String code, String message) {

    public static ErrorResponse from(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message);
    }
}
