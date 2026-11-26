package ng.yoonsu.blog.board.repository;

import ng.yoonsu.blog.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {
    public boolean existsByUrl(String url);
}
