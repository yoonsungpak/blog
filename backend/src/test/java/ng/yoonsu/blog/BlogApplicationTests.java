package ng.yoonsu.blog;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(BlogApplicationTests.FailingController.class)
class BlogApplicationTests {

    @LocalServerPort
    private int port;

    @Test
    void healthReturnsUp() throws Exception {
        var response = get("/api/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"code\":\"HEALTH_UP\"", "\"status\":\"UP\"");
    }

    @Test
    void missingRouteReturnsNotFound() throws Exception {
        var response = get("/api/missing");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("\"code\":\"NOT_FOUND\"");
    }

    @Test
    void unexpectedExceptionReturnsInternalServerError() throws Exception {
        var response = get("/api/test-error");

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.body())
                .contains("\"code\":\"INTERNAL_SERVER_ERROR\"")
                .doesNotContain("Test exception details");
    }

    private HttpResponse<String> get(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @RestController
    static class FailingController {
        @GetMapping("/api/test-error")
        void fail() {
            throw new IllegalStateException("Test exception details");
        }
    }
}
