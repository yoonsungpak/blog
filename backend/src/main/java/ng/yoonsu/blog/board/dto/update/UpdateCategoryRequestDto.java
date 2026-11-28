package ng.yoonsu.blog.board.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequestDto(
        @NotBlank @Size(max = 255) String name
) {}
