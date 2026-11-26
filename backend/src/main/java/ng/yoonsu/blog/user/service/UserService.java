package ng.yoonsu.blog.user.service;

import jakarta.transaction.Transactional;
import ng.yoonsu.blog.auth.dto.register.RegisterRequestDto;
import ng.yoonsu.blog.auth.dto.register.RegisterResponseDto;
import ng.yoonsu.blog.common.exception.BusinessException;
import ng.yoonsu.blog.common.exception.CommonErrorCode;
import ng.yoonsu.blog.user.domain.Role;
import ng.yoonsu.blog.user.domain.User;
import ng.yoonsu.blog.user.dto.update.UpdateUserRequestDto;
import ng.yoonsu.blog.user.dto.update.UpdateUserResponseDto;
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

    public User getUserById(String userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    public boolean isAdmin(String userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .filter(user -> user.getRole() == Role.ADMIN)
                .isPresent();
    }

    @Transactional
    public UpdateUserResponseDto update(
            String userId,
            UpdateUserRequestDto request,
            boolean adminAction
    ) {
        var user = getUserById(userId);
        String newPassword = request.password();

        if (!adminAction && newPassword != null) {
            String oldPassword = request.oldPassword();

            if (newPassword.isBlank() || oldPassword == null || oldPassword.isBlank()) {
                throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
            }

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
            }

            user.changePassword(passwordEncoder.encode(newPassword));
        }

        if (request.role() != null) {
            if (!adminAction) {
                throw new BusinessException(CommonErrorCode.ACCESS_DENIED);
            }

            user.setRole(request.role());
        }

        return new UpdateUserResponseDto(true);
    }
}
