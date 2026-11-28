package ng.yoonsu.blog.board.dto.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreateBoardRequestDto(
        @NotNull(message = "게시판 이름은 필수입니다.")
        String name,

        @NotNull(message = "게시판 URL은 필수입니다.")
        @Pattern(regexp = "[a-z-]+", message = "게시판 URL에는 영문 소문자와 하이픈(-)만 사용할 수 있습니다.")
        String url,

        Boolean isPublic,
        @Positive Long categoryId
) {
        public CreateBoardRequestDto {
                if (isPublic == null)
                        isPublic = true;
        }
}
