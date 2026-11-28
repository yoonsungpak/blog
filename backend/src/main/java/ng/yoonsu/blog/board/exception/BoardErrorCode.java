package ng.yoonsu.blog.board.exception;

import ng.yoonsu.blog.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum BoardErrorCode implements ErrorCode {
    DUPLICATE_BOARD_URL(HttpStatus.CONFLICT, "DUPLICATE_BOARD_URL", "이미 사용중인 게시판 URL 입니다."),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_NOT_FOUND", "게시판을 찾을 수 없습니다."),
    BOARD_ORDER_MISMATCH(HttpStatus.CONFLICT, "BOARD_ORDER_MISMATCH", "게시판 목록이 달라졌습니다. 새로고침 후 다시 시도해 주세요."),
    BOARD_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_CATEGORY_NOT_FOUND", "게시판 카테고리를 찾을 수 없습니다."),
    BOARD_CATEGORY_ORDER_MISMATCH(HttpStatus.CONFLICT, "BOARD_CATEGORY_ORDER_MISMATCH", "카테고리 목록이 달라졌습니다. 새로고침 후 다시 시도해 주세요.");

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
