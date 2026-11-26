package ng.yoonsu.blog.common.security.exception;

import ng.yoonsu.blog.common.exception.BusinessException;
import ng.yoonsu.blog.common.exception.CommonErrorCode;

public class NotAdminException extends BusinessException {
    public NotAdminException() {
        super(CommonErrorCode.ACCESS_DENIED);
    }
}
