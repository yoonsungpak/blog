package ng.yoonsu.blog;

import java.util.List;
import java.util.Optional;
import ng.yoonsu.blog.auth.security.AuthenticatedUser;
import ng.yoonsu.blog.user.domain.Role;
import ng.yoonsu.blog.user.domain.User;
import ng.yoonsu.blog.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:sqlite::memory:",
                "spring.datasource.hikari.maximum-pool-size=1",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
@Import(ApiIntegrationTestSupport.TestController.class)
abstract class ApiIntegrationTestSupport {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    UserRepository userRepository;

    MockMvc mvc;

    @BeforeEach
    void setUpMockMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    RequestPostProcessor mockAdmin() {
        var admin = new User("admin", "test-hash");
        admin.setRole(Role.ADMIN);
        when(userRepository.findByIdAndDeletedAtIsNull("admin")).thenReturn(Optional.of(admin));
        return loggedInAs("admin");
    }

    static RequestPostProcessor loggedInAs(String userId) {
        return authentication(UsernamePasswordAuthenticationToken.authenticated(
                new AuthenticatedUser(userId, userId), null, List.of()
        ));
    }

    @RestController
    static class TestController {
        @GetMapping("/api/test-error")
        void fail() {
            throw new IllegalStateException("Test exception details");
        }

        @PreAuthorize("isAuthenticated()")
        @GetMapping("/api/test-protected")
        public String protectedContent() {
            return "protected";
        }

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/api/test-admin")
        public String adminContent() {
            return "admin";
        }
    }
}
