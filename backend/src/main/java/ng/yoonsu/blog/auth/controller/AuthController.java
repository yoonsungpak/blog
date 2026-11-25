package ng.yoonsu.blog.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import ng.yoonsu.blog.auth.dto.login.LoginRequestDto;
import ng.yoonsu.blog.auth.dto.login.LoginResponseDto;
import ng.yoonsu.blog.auth.dto.register.RegisterRequestDto;
import ng.yoonsu.blog.auth.dto.register.RegisterResponseDto;
import ng.yoonsu.blog.auth.service.AuthService;
import ng.yoonsu.blog.common.response.ApiResponse;
import ng.yoonsu.blog.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;
    private final UserService userService;

    AuthController(AuthService authService, SecurityContextRepository securityContextRepository, UserService userService) {
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
        this.userService = userService;
    }

    @PostMapping("/login")
    @PreAuthorize("isAnonymous()")
    public ApiResponse<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto credentials,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var user = authService.login(credentials);
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        var authentication = UsernamePasswordAuthenticationToken.authenticated(user, null, List.of());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        request.getSession().setAttribute("username", user.username());

        return ApiResponse.ok("LOGIN_SUCCESS", new LoginResponseDto(user.userId()));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAnonymous()")
    public ApiResponse<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        var dto = userService.register(request);
        return ApiResponse.ok("REGISTER_SUCCESS", dto);
    }
}
