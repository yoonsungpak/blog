package ng.yoonsu.blog.board.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import ng.yoonsu.blog.board.domain.Board;
import ng.yoonsu.blog.board.dto.create.CreateBoardRequestDto;
import ng.yoonsu.blog.board.dto.create.CreateBoardResponseDto;
import ng.yoonsu.blog.board.dto.delete.DeleteBoardResponseDto;
import ng.yoonsu.blog.board.dto.update.UpdateBoardRequestDto;
import ng.yoonsu.blog.board.exception.BoardErrorCode;
import ng.yoonsu.blog.board.repository.BoardRepository;
import ng.yoonsu.blog.common.exception.BusinessException;
import ng.yoonsu.blog.common.exception.CommonErrorCode;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional
    public void update(String rawBoardId, @Valid UpdateBoardRequestDto request) {
        if (request.name() == null && request.url() == null && request.isPublic() == null) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        long boardId;
        try {
            boardId = Long.parseLong(rawBoardId);
        } catch (NumberFormatException e) {
            throw new BusinessException(BoardErrorCode.BOARD_NOT_FOUND);
        }

        var board = boardRepository.findById(boardId).orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        if (request.name() != null) {
            if (request.name().trim().isEmpty())
                throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
            board.setName(request.name());
        }


        if (request.url() != null) {
            if (boardRepository.existsByUrl(request.url()))
                throw new BusinessException(BoardErrorCode.DUPLICATE_BOARD_URL);

            board.setUrl(request.url());
        }

        if (request.isPublic() != null) {
           board.setPublic(request.isPublic());
        }

        boardRepository.save(board);
    }

    public DeleteBoardResponseDto delete(Long boardId) {
        boardRepository.deleteById(boardId);
        return new DeleteBoardResponseDto(true);
    }

    @Transactional
    public void reorder(List<Long> boardIds) {
        var boardsById = boardRepository.findAll().stream()
                .collect(Collectors.toMap(Board::getId, board -> board));
        if (boardIds.size() != boardsById.size() || !boardsById.keySet().equals(new HashSet<>(boardIds))) {
            throw new BusinessException(BoardErrorCode.BOARD_ORDER_MISMATCH);
        }

        for (int index = 0; index < boardIds.size(); index++) {
            boardsById.get(boardIds.get(index)).setDisplayOrder(index + 1);
        }
    }
}
