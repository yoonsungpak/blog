package ng.yoonsu.blog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BlogApplicationTests extends ApiIntegrationTestSupport {

    @LocalServerPort
    private int port;

    @PersistenceContext
    private EntityManager entityManager;

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

    private HttpResponse<String> getOverHttp(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
