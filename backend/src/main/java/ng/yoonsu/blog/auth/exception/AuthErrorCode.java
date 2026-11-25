package ng.yoonsu.blog.auth.exception;

import ng.yoonsu.blog.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "로그인이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "이미 로그인된 사용자입니다."),
    INVALID_USERNAME_OR_PASSWORD(HttpStatus.FORBIDDEN, "INVALID_USERNAME_OR_PASSWORD", "올바르지 않은 아이디 또는 비밀번호입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
