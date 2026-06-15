package co.inspien.assignment.common.exception;

public class InspienException extends RuntimeException {

    private final ErrorCode errorCode;

    public InspienException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public InspienException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
    }

    public InspienException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public InspienException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
