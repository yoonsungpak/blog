package ng.yoonsu.blog.system;

import java.util.Map;
import ng.yoonsu.blog.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/api/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok("HEALTH_UP", Map.of("status", "UP"));
    }
}
