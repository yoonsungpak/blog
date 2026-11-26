package ng.yoonsu.blog.board.service;

import jakarta.transaction.Transactional;
import ng.yoonsu.blog.board.domain.Board;
import ng.yoonsu.blog.board.dto.create.CreateBoardRequestDto;
import ng.yoonsu.blog.board.dto.create.CreateBoardResponseDto;
import ng.yoonsu.blog.board.dto.delete.DeleteBoardResponseDto;
import ng.yoonsu.blog.board.exception.BoardErrorCode;
import ng.yoonsu.blog.board.repository.BoardRepository;
import ng.yoonsu.blog.common.exception.BusinessException;
import org.hibernate.sql.Delete;
import org.springframework.stereotype.Service;

@Service
public class BoardService {
    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional
    public CreateBoardResponseDto create(CreateBoardRequestDto request, String userId) {
        var name = request.name();
        var url = request.url();
        var isPublic = request.isPublic();

        if (boardRepository.existsByUrl(url))
            throw new BusinessException(BoardErrorCode.DUPLICATE_BOARD_URL);

        Board board = new Board(name, url, userId, isPublic);
        boardRepository.save(board);

        return new CreateBoardResponseDto(board);
    }

    public DeleteBoardResponseDto delete(Long boardId) {
        boardRepository.deleteById(boardId);
        return new DeleteBoardResponseDto(true);
    }
}
