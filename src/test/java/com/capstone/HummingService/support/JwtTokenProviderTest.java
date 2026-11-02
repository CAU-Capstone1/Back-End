package com.capstone.HummingService.support;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "jwt-token-provider-test-secret-key-that-is-at-least-64-bytes-long!!".getBytes());

    private final Authentication authentication = new UsernamePasswordAuthenticationToken(
            "a@b.com", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    void 발급한_토큰은_검증을_통과하고_사용자_정보를_복원한다() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);
        String token = provider.createAccessToken(authentication);

        assertThat(provider.validateToken(token)).isTrue();
        Authentication restored = provider.getAuthentication(token);
        assertThat(restored.getName()).isEqualTo("a@b.com");
        assertThat(restored.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void 만료된_토큰은_검증에_실패한다() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, -1_000);

        assertThat(provider.validateToken(provider.createAccessToken(authentication))).isFalse();
    }

    @Test
    void 변조되었거나_형식이_잘못된_토큰은_검증에_실패한다() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);
        String token = provider.createAccessToken(authentication);
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        assertThat(provider.validateToken(tampered)).isFalse();
        assertThat(provider.validateToken("not-a-jwt")).isFalse();
        assertThat(provider.validateToken("")).isFalse();
    }
}
