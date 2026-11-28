package ng.yoonsu.blog.board.dto.create;

import ng.yoonsu.blog.board.domain.Category;

public record CreateCategoryResponseDto(
        Long id,
        String name,
        long displayOrder
) {
    public CreateCategoryResponseDto(Category category) {
        this(category.getId(), category.getName(), category.getDisplayOrder());
    }
}
