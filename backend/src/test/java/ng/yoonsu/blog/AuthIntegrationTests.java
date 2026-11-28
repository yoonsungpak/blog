package ng.yoonsu.blog;

import java.util.Optional;
import ng.yoonsu.blog.auth.security.AuthenticatedUser;
import ng.yoonsu.blog.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTests extends ApiIntegrationTestSupport {

    @Autowired
    private PasswordEncoder passwordEncoder;

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
}
