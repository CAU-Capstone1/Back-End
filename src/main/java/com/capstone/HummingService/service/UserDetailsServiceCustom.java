package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.User;
import com.capstone.HummingService.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserDetailsServiceCustom implements UserDetailsService {

    private final UserRepository userRepository;

    // 표준 Java 생성자 주입
    public UserDetailsServiceCustom(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. UserRepository를 통해 DB에서 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 2. 조회된 User 엔티티 정보를 Spring Security가 이해하는 UserDetails 객체로 변환

        // 사용자의 Role을 Spring Security의 GrantedAuthority 리스트로 변환
        List<GrantedAuthority> authorities = Collections.singletonList(
                // DB에 저장된 Role 이름을 'ROLE_' 접두사를 붙여 권한으로 사용
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        // Spring Security의 내장 User 객체를 사용하여 UserDetails 반환
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // DB에 저장된 암호화된 비밀번호
                authorities         // 권한 리스트
        );
    }
}