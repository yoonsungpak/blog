package ng.yoonsu.blog.board.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequestDto(
        @NotBlank @Size(max = 255) String name
) {}
