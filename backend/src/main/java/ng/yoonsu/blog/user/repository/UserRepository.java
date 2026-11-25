package ng.yoonsu.blog.user.repository;

import ng.yoonsu.blog.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    boolean existsUserByUsername(String username);

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
}