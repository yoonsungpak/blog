package ng.yoonsu.blog.auth.dto.register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequestDto(
        @NotBlank(message = "아이디를 입력해 주세요.")
        @Pattern(
                regexp = "^[a-z0-9_]{4,12}$",
                message = "아이디는 영문, 숫자, 언더바(_)으로 구성된 4~12자리여야 합니다."
        )
        String username,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]{10,50}$",
                message = "비밀번호는 영문, 숫자, 언더바(_)로 구성된 10~50자리여야 합니다."
        )
        String password
) {}
