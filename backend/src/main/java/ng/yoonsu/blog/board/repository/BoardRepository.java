package ng.yoonsu.blog.board.repository;

import ng.yoonsu.blog.board.domain.Board;
import ng.yoonsu.blog.board.domain.Category;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {
    public boolean existsByUrl(String url);
    public Optional<Board> findById(Long id);
    public void deleteById(@NonNull Long id);
    List<Board> findByCategory(Category category);
}
