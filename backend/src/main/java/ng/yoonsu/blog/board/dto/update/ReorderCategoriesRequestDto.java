package ng.yoonsu.blog.board.dto.update;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record ReorderCategoriesRequestDto(
        @NotEmpty List<@NotNull @Positive Long> categoryIds
) {}
