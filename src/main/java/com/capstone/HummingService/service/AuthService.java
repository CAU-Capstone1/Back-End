package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.enums.Role;
import com.capstone.HummingService.domain.User;
import com.capstone.HummingService.dto.request.LoginRequest;
import com.capstone.HummingService.dto.request.RegisterRequest;
import com.capstone.HummingService.dto.response.LoginResponse;
import com.capstone.HummingService.repository.UserRepository;
import com.capstone.HummingService.support.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // 의존성 주입 (생성자 인젝션)
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String usernameToUse = request.getEmail();

        if (userRepository.existsByUsername(usernameToUse)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + usernameToUse);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getEmail(),     // email
                usernameToUse,          // username (email과 동일)
                encodedPassword,        // password
                request.getName(),      // name
                Role.USER               // role
        );

        return userRepository.save(user);
    }

    public LoginResponse authenticate(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(), // username (여기서는 email)
                request.getPassword() // raw password
        );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        String accessToken = jwtTokenProvider.createAccessToken(authentication);

        User userEntity = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + request.getEmail()));

        return new LoginResponse(accessToken, "Bearer", userEntity);
    }
}