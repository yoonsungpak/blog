package ng.yoonsu.blog.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ApiResponse<T>(
        String status,
        String code,
        @JsonInclude(JsonInclude.Include.NON_NULL) String message,
        @JsonInclude(JsonInclude.Include.NON_NULL) T data
) {
    public static <T> ApiResponse<T> ok(String code, String message, T data) {
        return new ApiResponse<>("ok", code, message, data);
    }

    public static <T> ApiResponse<T> ok(String code) {
        return new ApiResponse<>("ok", code, null, null);
    }

    public static <T> ApiResponse<T> ok(String code, T data) {
        return new ApiResponse<>("ok", code, null, data);
    }

    public static <T> ApiResponse<T> warning(String code, String message, T data) {
        return new ApiResponse<>("warning", code, message, data);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>("error", code, message, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>("error", code, message, data);
    }
}
