package ng.yoonsu.blog.common.security.aspect;

import ng.yoonsu.blog.auth.security.AuthenticatedUser;
import ng.yoonsu.blog.common.security.exception.NotAdminException;
import ng.yoonsu.blog.user.service.UserService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminOnlyAspect {
    private final UserService userService;

    public AdminOnlyAspect(UserService userService) {
        this.userService = userService;
    }

    @Before("@annotation(ng.yoonsu.blog.common.security.annotation.AdminOnly)")
    public void adminOnly() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new NotAdminException();
        }

        if (!userService.isAdmin(authenticatedUser.userId())) {
            throw new NotAdminException();
        }
    }
}
