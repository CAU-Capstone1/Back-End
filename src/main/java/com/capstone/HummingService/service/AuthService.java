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

    /**
     * 사용자 회원가입을 처리합니다.
     * @param request 회원가입 요청 DTO
     * @return 저장된 User 엔티티
     */
    @Transactional
    public User register(RegisterRequest request) {
        // 백엔드가 username을 요구하면 이메일로 채워서 보냅니다.
        String usernameToUse = request.getEmail();

        // 1. 사용자 이메일 중복 확인
        if (userRepository.existsByUsername(usernameToUse)) {
            // 이미 사용 중인 이메일인 경우 예외 처리
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + usernameToUse);
        }

        // 2. 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. User 엔티티 생성
        User user = new User(
                request.getEmail(),     // email
                usernameToUse,          // username (email과 동일)
                encodedPassword,        // password
                request.getName(),      // name
                Role.USER               // role
        );

        // 4. 사용자 저장 및 반환
        return userRepository.save(user);
    }

    /**
     * 사용자 로그인을 처리하고 JWT 토큰을 발급합니다.
     * @param request 로그인 요청 DTO
     * @return 액세스 토큰 및 사용자 정보를 포함한 응답 DTO
     */
    public LoginResponse authenticate(LoginRequest request) {
        // 1. UsernamePasswordAuthenticationToken 생성 (이메일, 비밀번호)
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(), // username (여기서는 email)
                request.getPassword() // raw password
        );

        // 2. 인증 시도 (CustomUserDetailsService의 loadUserByUsername 호출)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. Access Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(authentication);

        // 4. 인증된 사용자 Entity 조회
        User userEntity = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + request.getEmail()));

        // 5. LoginResponse 반환 (액세스 토큰과 User 객체 포함)
        return new LoginResponse(accessToken, "Bearer", userEntity);
    }
}