package ng.yoonsu.blog.user.controller;

import jakarta.validation.Valid;
import ng.yoonsu.blog.common.response.ApiResponse;
import ng.yoonsu.blog.common.security.annotation.AdminOnly;
import ng.yoonsu.blog.user.dto.update.UpdateUserRequestDto;
import ng.yoonsu.blog.user.dto.update.UpdateUserResponseDto;
import ng.yoonsu.blog.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @AdminOnly
    public ApiResponse<UpdateUserResponseDto> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequestDto request
    ) {
        var response = userService.update(userId, request, true);
        return ApiResponse.ok("UPDATE_SUCCESS", response);
    }
}
