package ng.yoonsu.blog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import ng.yoonsu.blog.auth.security.AuthenticatedUser;
import ng.yoonsu.blog.board.repository.BoardRepository;
import ng.yoonsu.blog.user.domain.Role;
import ng.yoonsu.blog.user.domain.User;
import ng.yoonsu.blog.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:sqlite::memory:",
                "spring.datasource.hikari.maximum-pool-size=1",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
@Import(BlogApplicationTests.TestController.class)
class BlogApplicationTests {

    @LocalServerPort
    private int port;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BoardRepository boardRepository;

    @MockitoBean
    private UserRepository userRepository;

    private MockMvc mvc;

    @BeforeEach
    void setUpMockMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void jpaConnectsToSqlite() {
        var version = entityManager.createNativeQuery("select sqlite_version()").getSingleResult();

        assertThat(version).asString().startsWith("3.");
    }

    @Test
    void healthReturnsUp() throws Exception {
        var response = getOverHttp("/api/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"code\":\"HEALTH_UP\"", "\"status\":\"UP\"");
    }

    @Test
    void missingRouteReturnsNotFound() throws Exception {
        mvc.perform(get("/api/missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("\"code\":\"NOT_FOUND\"")));
    }

    @Test
    void unexpectedExceptionReturnsInternalServerError() throws Exception {
        mvc.perform(get("/api/test-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(IllegalStateException.class))
                .andExpect(content().string(containsString("\"code\":\"INTERNAL_SERVER_ERROR\"")))
                .andExpect(content().string(not(containsString("Test exception details"))));
    }

    @Test
    void apiMethodSecurityRequiresAuthenticationAndReturnsJson() throws Exception {
        var response = getOverHttp("/api/test-protected");

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("\"code\":\"UNAUTHENTICATED\"");
        assertThat(response.headers().firstValue("Location")).isEmpty();
        mvc.perform(get("/api/test-protected").with(user("tester")))
                .andExpect(status().isOk());
    }

    @Test
    void methodSecurityReturnsForbiddenForAuthenticatedUsersWithoutPermission() throws Exception {
        mvc.perform(get("/api/test-admin").with(user("tester")))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().string(containsString("\"code\":\"ACCESS_DENIED\"")));
    }

    @Test
    void logoutOnlyInvalidatesSessionOnPost() throws Exception {
        var session = new MockHttpSession();
        mvc.perform(get("/logout").with(user("tester")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/auth/logout").session(session).with(user("tester")))
                .andExpect(status().isNotFound());
        assertThat(session.isInvalid()).isFalse();

        mvc.perform(post("/api/auth/logout").session(session).with(user("tester")))
                .andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void loginValidationReturnsDtoMessagesAndMalformedJsonError() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"  \", \"password\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("아이디를 입력해 주세요."));
        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("비밀번호를 입력해 주세요."));
        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"INVALID_REQUEST_BODY\"")));
    }

    @Test
    void registrationValidationReturnsConfiguredPatternMessage() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\", \"password\":\"test_password\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("아이디는 영문, 숫자, 언더바(_)으로 구성된 4~12자리여야 합니다."));
    }

    @Test
    void loginPersistsIdentityForSubsequentRequests() throws Exception {
        var account = mock(User.class);
        when(account.getId()).thenReturn("test-id");
        when(account.getUsername()).thenReturn("tester");
        when(account.getPassword()).thenReturn(passwordEncoder.encode("test-password"));
        when(userRepository.findByUsernameAndDeletedAtIsNull("tester")).thenReturn(Optional.of(account));

        var session = new MockHttpSession();
        String previousId = session.getId();
        mvc.perform(post("/api/auth/login").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"userId\":\"test-id\"")));

        assertThat(session.getId()).isNotEqualTo(previousId);
        assertThat(session.getAttribute("userId")).isEqualTo("test-id");
        assertThat(session.getAttribute("username")).isEqualTo("tester");
        var authentication = ((SecurityContext) session.getAttribute(SPRING_SECURITY_CONTEXT_KEY)).getAuthentication();
        assertThat(authentication.getPrincipal()).isEqualTo(new AuthenticatedUser("test-id", "tester"));
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.getAuthorities()).isEmpty();
        mvc.perform(get("/api/test-protected").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserCannotAccessAdminUpdate() throws Exception {
        mvc.perform(patch("/api/users/target").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"WRITER\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        verifyNoInteractions(userRepository);
    }

    @Test
    void nonAdminCannotChangeUserRole() throws Exception {
        when(userRepository.findByIdAndDeletedAtIsNull("actor"))
                .thenReturn(Optional.of(new User("actor", "test-hash")));

        mvc.perform(patch("/api/users/target").with(loggedInAs("actor"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void adminCanChangeRoleUsingDatabaseRoleAndLosesAccessAfterDemotion() throws Exception {
        var actor = new User("actor", "test-hash");
        actor.setRole(Role.ADMIN);
        var target = new User("target", "test-hash");
        when(userRepository.findByIdAndDeletedAtIsNull("actor")).thenReturn(Optional.of(actor));
        when(userRepository.findByIdAndDeletedAtIsNull("target")).thenReturn(Optional.of(target));

        var sessionAuthentication = loggedInAs("actor");
        mvc.perform(patch("/api/users/target").with(sessionAuthentication)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"WRITER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));
        assertThat(target.getRole()).isEqualTo(Role.WRITER);

        actor.setRole(Role.VISITOR);
        mvc.perform(patch("/api/users/target").with(sessionAuthentication)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
        assertThat(target.getRole()).isEqualTo(Role.WRITER);
    }

    @Test
    void adminCreatesPublicBoardByDefaultAndDuplicateUrlReturnsConflict() throws Exception {
        var admin = mockAdmin();

        mvc.perform(post("/api/boards/").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"개발 기록","url":"development"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("BOARD_CREATED"))
                .andExpect(jsonPath("$.data.board.name").value("개발 기록"))
                .andExpect(jsonPath("$.data.board.public").value(true))
                .andExpect(jsonPath("$.data.board.createdBy").value("admin"));

        mvc.perform(post("/api/boards/").with(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"다른 게시판","url":"development"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_BOARD_URL"));
    }

    @Test
    void adminCanCreatePrivateBoard() throws Exception {
        mvc.perform(post("/api/boards/").with(mockAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"비공개 기록","url":"private-notes","isPublic":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.board.public").value(false))
                .andExpect(jsonPath("$.data.board.createdAt").exists());

        var createdAt = entityManager.createNativeQuery("select created_at from boards where url = :url")
                .setParameter("url", "private-notes")
                .getSingleResult();
        assertThat(createdAt).isNotNull();
    }

    @Test
    void boardCreationRejectsMissingUrlAndNonAdmin() throws Exception {
        mvc.perform(post("/api/boards/").with(mockAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"개발 기록"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        when(userRepository.findByIdAndDeletedAtIsNull("visitor"))
                .thenReturn(Optional.of(new User("visitor", "test-hash")));
        mvc.perform(post("/api/boards/").with(loggedInAs("visitor")).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"개발 기록","url":"forbidden-board"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertThat(boardRepository.existsByUrl("forbidden-board")).isFalse();
    }

    @Test
    void boardCreationRejectsUrlsOutsideLowercaseLettersAndHyphens() throws Exception {
        var admin = mockAdmin();
        for (String url : List.of("", "Dev-notes", "dev_notes", "dev1", "개발")) {
            mvc.perform(post("/api/boards/").with(admin).contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"개발 기록","url":"%s"}
                                    """.formatted(url)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("게시판 URL에는 영문 소문자와 하이픈(-)만 사용할 수 있습니다."));
        }
    }

    private RequestPostProcessor mockAdmin() {
        var admin = new User("admin", "test-hash");
        admin.setRole(Role.ADMIN);
        when(userRepository.findByIdAndDeletedAtIsNull("admin")).thenReturn(Optional.of(admin));
        return loggedInAs("admin");
    }

    private static RequestPostProcessor loggedInAs(String userId) {
        return authentication(UsernamePasswordAuthenticationToken.authenticated(
                new AuthenticatedUser(userId, userId), null, List.of()
        ));
    }

    private HttpResponse<String> getOverHttp(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
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
