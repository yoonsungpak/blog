package ng.yoonsu.blog.user.dto.update;

import ng.yoonsu.blog.user.domain.Role;

public record UpdateUserRequestDto(
        String password,
        String oldPassword,
        Role role
) {}
