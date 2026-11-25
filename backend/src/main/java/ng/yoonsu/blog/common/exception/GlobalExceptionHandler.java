package ng.yoonsu.blog.common.exception;

import ng.yoonsu.blog.auth.exception.AuthErrorCode;
import ng.yoonsu.blog.common.response.ApiResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return errorResponse(exception.getErrorCode());
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException exception) {
        return errorResponse(exception.getErrorCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handleAccessDenied() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthErrorCode error = trustResolver.isAuthenticated(authentication)
                ? AuthErrorCode.ACCESS_DENIED
                : AuthErrorCode.UNAUTHENTICATED;
        return errorResponse(error);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unexpected error while handling a request", exception);
        return errorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception exception, Object body, @NonNull HttpHeaders headers, HttpStatusCode status, @NonNull WebRequest request) {
        if (exception instanceof MethodArgumentNotValidException validationException) {
            var error = CommonErrorCode.INVALID_REQUEST;
            String message = validationException.getAllErrors().stream().findFirst()
                    .map(ObjectError::getDefaultMessage).orElse(error.getMessage());
            body = ApiResponse.error(error.getCode(), message);
        } else if (exception instanceof HttpMessageNotReadableException) {
            body = errorBody(CommonErrorCode.INVALID_REQUEST_BODY);
        }
        if (status.isSameCodeAs(HttpStatus.NOT_FOUND)) {
            body = errorBody(CommonErrorCode.NOT_FOUND);
        }
        if (status.isSameCodeAs(HttpStatus.INTERNAL_SERVER_ERROR)) {
            log.error("Internal error while handling a request", exception);
            body = errorBody(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return super.handleExceptionInternal(exception, body, headers, status, request);
    }

    private ResponseEntity<ApiResponse<Void>> errorResponse(ErrorCode error) {
        return ResponseEntity.status(error.getStatus()).body(errorBody(error));
    }

    private ApiResponse<Void> errorBody(ErrorCode error) {
        return ApiResponse.error(error.getCode(), error.getMessage());
    }
}
