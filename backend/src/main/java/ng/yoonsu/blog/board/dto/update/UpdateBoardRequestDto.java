package ng.yoonsu.blog.board.dto.update;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

public record UpdateBoardRequestDto(
        String name,

        @Pattern(regexp = "[a-z-]+", message = "게시판 URL에는 영문 소문자와 하이픈(-)만 사용할 수 있습니다.")
        String url,

        Boolean isPublic,
        @Valid CategoryChange category
) {
    public record CategoryChange(@Positive Long id) {}
}
