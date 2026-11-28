package ng.yoonsu.blog.board.repository;

import ng.yoonsu.blog.board.domain.Board;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {
    public boolean existsByUrl(String url);
    public Optional<Board> findById(Long id);
    public void deleteById(@NonNull Long id);
}
