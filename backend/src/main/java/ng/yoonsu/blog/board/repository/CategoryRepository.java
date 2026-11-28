package ng.yoonsu.blog.board.repository;

import ng.yoonsu.blog.board.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}
