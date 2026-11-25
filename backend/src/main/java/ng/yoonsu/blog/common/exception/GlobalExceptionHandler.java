package ng.yoonsu.blog.common.exception;

import ng.yoonsu.blog.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode error = exception.getErrorCode();
        return ResponseEntity.status(error.getStatus()).body(errorBody(error));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unexpected error while handling a request", exception);
        CommonErrorCode error = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(error.getStatus()).body(errorBody(error));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (status.isSameCodeAs(HttpStatus.NOT_FOUND)) {
            body = errorBody(CommonErrorCode.NOT_FOUND);
        }
        if (status.isSameCodeAs(HttpStatus.INTERNAL_SERVER_ERROR)) {
            log.error("Internal error while handling a request", exception);
            body = errorBody(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return super.handleExceptionInternal(exception, body, headers, status, request);
    }

    private ApiResponse<Void> errorBody(ErrorCode error) {
        return ApiResponse.error(error.getCode(), error.getMessage());
    }
}
