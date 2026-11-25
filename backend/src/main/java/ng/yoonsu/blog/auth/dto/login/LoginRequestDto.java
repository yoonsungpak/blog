package ng.yoonsu.blog.auth.dto.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "아이디를 입력해 주세요.") String username,
        @NotBlank(message = "비밀번호를 입력해 주세요.") String password
) {}
