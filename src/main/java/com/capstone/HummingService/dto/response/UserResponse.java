package com.capstone.HummingService.dto.response;

import com.capstone.HummingService.domain.User;
import com.capstone.HummingService.domain.enums.Role;

// 클라이언트에 노출해도 되는 사용자 정보만 담는다 (비밀번호 해시 제외)
public record UserResponse(Long id, String email, String username, String name, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getUsername(), user.getName(), user.getRole());
    }
}
