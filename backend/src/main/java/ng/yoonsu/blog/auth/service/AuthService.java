package ng.yoonsu.blog.auth.service;

import ng.yoonsu.blog.auth.dto.login.LoginRequestDto;
import ng.yoonsu.blog.auth.exception.AuthErrorCode;
import ng.yoonsu.blog.auth.security.AuthenticatedUser;
import ng.yoonsu.blog.common.exception.BusinessException;
import ng.yoonsu.blog.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthenticatedUser login(LoginRequestDto request) {
        String username = request.username();
        String password = request.password();

        var user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_USERNAME_OR_PASSWORD));

        var matched = passwordEncoder.matches(password, user.getPassword());
        if (!matched) {
            throw new BusinessException(AuthErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }

        return new AuthenticatedUser(user.getId(), user.getUsername());
    }
}
