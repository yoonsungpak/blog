package ng.yoonsu.blog.board.controller;

import jakarta.validation.Valid;
import ng.yoonsu.blog.auth.security.AuthenticatedUser;
import ng.yoonsu.blog.board.dto.create.CreateBoardRequestDto;
import ng.yoonsu.blog.board.dto.create.CreateBoardResponseDto;
import ng.yoonsu.blog.board.dto.delete.DeleteBoardResponseDto;
import ng.yoonsu.blog.board.service.BoardService;
import ng.yoonsu.blog.common.response.ApiResponse;
import ng.yoonsu.blog.common.security.annotation.AdminOnly;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @AdminOnly
    @PreAuthorize("isAuthenticated()")
    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateBoardResponseDto> create(
            @RequestBody @Valid CreateBoardRequestDto request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        var dto = boardService.create(request, user.userId());
        return ApiResponse.ok("BOARD_CREATED", dto);
    }

    @AdminOnly
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{boardId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<DeleteBoardResponseDto> delete(@PathVariable Long boardId) {
        var dto = boardService.delete(boardId);
        return ApiResponse.ok("BOARD_DELETED", dto);
    }
}
