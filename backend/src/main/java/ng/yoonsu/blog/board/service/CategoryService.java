package ng.yoonsu.blog.board.service;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import ng.yoonsu.blog.board.domain.Category;
import ng.yoonsu.blog.board.dto.create.CreateCategoryRequestDto;
import ng.yoonsu.blog.board.dto.create.CreateCategoryResponseDto;
import ng.yoonsu.blog.board.dto.update.UpdateCategoryRequestDto;
import ng.yoonsu.blog.board.exception.BoardErrorCode;
import ng.yoonsu.blog.board.repository.CategoryRepository;
import ng.yoonsu.blog.board.repository.BoardRepository;
import ng.yoonsu.blog.common.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final BoardRepository boardRepository;

    public CategoryService(CategoryRepository categoryRepository, BoardRepository boardRepository) {
        this.categoryRepository = categoryRepository;
        this.boardRepository = boardRepository;
    }

    @Transactional
    public CreateCategoryResponseDto create(CreateCategoryRequestDto request) {
        var category = categoryRepository.save(new Category(request.name().strip()));
        return new CreateCategoryResponseDto(category);
    }

    @Transactional
    public void update(long id, UpdateCategoryRequestDto request) {
        find(id).setName(request.name().strip());
    }

    @Transactional
    public void reorder(List<Long> categoryIds) {
        var categoriesById = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, category -> category));
        if (categoryIds.size() != categoriesById.size() || !categoriesById.keySet().equals(new HashSet<>(categoryIds))) {
            throw new BusinessException(BoardErrorCode.BOARD_CATEGORY_ORDER_MISMATCH);
        }

        for (int index = 0; index < categoryIds.size(); index++) {
            categoriesById.get(categoryIds.get(index)).setDisplayOrder(index + 1);
        }
    }

    @Transactional
    public void delete(long id) {
        var category = find(id);
        boardRepository.findByCategory(category).forEach(board -> board.moveToCategory(null));
        boardRepository.flush();
        categoryRepository.delete(category);
    }

    public Category find(long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_CATEGORY_NOT_FOUND));
    }
}
