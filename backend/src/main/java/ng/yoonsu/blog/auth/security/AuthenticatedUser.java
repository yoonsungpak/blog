package ng.yoonsu.blog.auth.security;

import java.io.Serializable;
import org.springframework.security.core.AuthenticatedPrincipal;

public record AuthenticatedUser(String userId, String username)
        implements AuthenticatedPrincipal, Serializable {
    @Override
    public String getName() {
        return username;
    }
}
