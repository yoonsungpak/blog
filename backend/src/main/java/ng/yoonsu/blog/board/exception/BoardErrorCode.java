package ng.yoonsu.blog.board.exception;

import ng.yoonsu.blog.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum BoardErrorCode implements ErrorCode {
    DUPLICATE_BOARD_URL(HttpStatus.CONFLICT, "DUPLICATE_BOARD_URL", "이미 사용중인 게시판 URL 입니다."),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_NOT_FOUND", "게시판을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    BoardErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
