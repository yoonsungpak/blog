package ng.yoonsu.blog.board.controller;

import jakarta.validation.Valid;
import ng.yoonsu.blog.board.dto.create.CreateCategoryRequestDto;
import ng.yoonsu.blog.board.dto.create.CreateCategoryResponseDto;
import ng.yoonsu.blog.board.dto.update.ReorderCategoriesRequestDto;
import ng.yoonsu.blog.board.dto.update.UpdateCategoryRequestDto;
import ng.yoonsu.blog.board.service.CategoryService;
import ng.yoonsu.blog.common.response.ApiResponse;
import ng.yoonsu.blog.common.security.annotation.AdminOnly;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @AdminOnly
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateCategoryResponseDto> create(@RequestBody @Valid CreateCategoryRequestDto request) {
        return ApiResponse.ok("BOARD_CATEGORY_CREATED", categoryService.create(request));
    }

    @AdminOnly
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{categoryId}")
    public ApiResponse<Void> update(
            @RequestBody @Valid UpdateCategoryRequestDto request,
            @PathVariable long categoryId
    ) {
        categoryService.update(categoryId, request);
        return ApiResponse.ok("BOARD_CATEGORY_UPDATED");
    }

    @AdminOnly
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/order")
    public ApiResponse<Void> reorder(@RequestBody @Valid ReorderCategoriesRequestDto request) {
        categoryService.reorder(request.categoryIds());
        return ApiResponse.ok("BOARD_CATEGORY_ORDER_UPDATED");
    }

    @AdminOnly
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> delete(@PathVariable long categoryId) {
        categoryService.delete(categoryId);
        return ApiResponse.ok("BOARD_CATEGORY_DELETED");
    }
}
