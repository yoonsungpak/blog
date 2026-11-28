package ng.yoonsu.blog.board.dto.create;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ng.yoonsu.blog.board.domain.Board;

public record CreateBoardResponseDto(
        @JsonIgnoreProperties("displayOrder") Board board
) {}
