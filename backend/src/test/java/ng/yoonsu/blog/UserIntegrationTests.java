package ng.yoonsu.blog;

import java.util.Optional;
import ng.yoonsu.blog.user.domain.Role;
import ng.yoonsu.blog.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserIntegrationTests extends ApiIntegrationTestSupport {

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
}
