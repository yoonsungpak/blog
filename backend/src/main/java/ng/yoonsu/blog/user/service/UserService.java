package ng.yoonsu.blog.user.service;

import jakarta.transaction.Transactional;
import ng.yoonsu.blog.auth.dto.register.RegisterRequestDto;
import ng.yoonsu.blog.auth.dto.register.RegisterResponseDto;
import ng.yoonsu.blog.common.exception.BusinessException;
import ng.yoonsu.blog.user.domain.User;
import ng.yoonsu.blog.user.exception.UserErrorCode;
import ng.yoonsu.blog.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        String username = request.username();
        String password = request.password();

        boolean exists = userRepository.existsUserByUsername(username);
        if (exists) {
            throw new BusinessException(UserErrorCode.DUPLICATE_USERNAME);
        }

        var hashed = passwordEncoder.encode(password);
        var user = new User(username, hashed);
        userRepository.save(user);

        return new RegisterResponseDto(user.getUsername());
    }
}
