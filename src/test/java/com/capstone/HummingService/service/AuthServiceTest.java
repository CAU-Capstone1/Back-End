package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.User;
import com.capstone.HummingService.domain.enums.Role;
import com.capstone.HummingService.dto.request.LoginRequest;
import com.capstone.HummingService.dto.response.LoginResponse;
import com.capstone.HummingService.repository.UserRepository;
import com.capstone.HummingService.support.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void 로그인_응답_JSON에_비밀번호_해시가_포함되지_않는다() throws Exception {
        User user = new User("a@b.com", "a@b.com", "$2a$10$hashedPasswordValue", "홍길동", Role.USER);
        Authentication authentication = new UsernamePasswordAuthenticationToken("a@b.com", null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.createAccessToken(authentication)).thenReturn("token");
        when(userRepository.findByUsername("a@b.com")).thenReturn(Optional.of(user));

        LoginResponse res = authService.authenticate(new LoginRequest("a@b.com", "password1"));
        String json = new ObjectMapper().writeValueAsString(res);

        assertThat(json).contains("\"email\":\"a@b.com\"", "\"accessToken\":\"token\"");
        assertThat(json).doesNotContain("password", "$2a$10$hashedPasswordValue");
    }
}
