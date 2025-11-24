package com.capstone.HummingService.config;

import com.capstone.HummingService.service.UserDetailsServiceCustom;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// ** [추가된 Import 문] **
import java.util.Arrays;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceCustom userDetailsServiceCustom;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsServiceCustom userDetailsServiceCustom) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsServiceCustom = userDetailsServiceCustom;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // 이 설정은 아래 corsConfigurationSource Bean을 사용합니다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/test",
                                "/callback/**",
                                "/jobs/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",

                                "/api/upload",
                                "/api/compose/full-upload",
                                "/api/compose"
                        ).permitAll()

                        // 일반 API 경로는 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().permitAll()
                )

                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())


                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    // ------------------------------------------------------------------
    // ⭐ 프론트엔드 연결을 위한 CORS 설정 Bean 추가 ⭐
    // ------------------------------------------------------------------
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ⚠️ [필수 변경] 프론트엔드가 실행되는 도메인을 여기에 추가해야 합니다.
        // 임시로 로컬 개발 주소를 모두 허용합니다.
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",      // React/Vue/Angular 기본
                "http://127.0.0.1:3000",
                "http://localhost:5173",      // Vite 기본
                "http://[EC2_공인_IP]",       // 프론트엔드가 EC2에서 직접 서빙될 경우 EC2 IP
                "*" // 💡 개발 완료 후에는 이 '모든 허용'(*)을 제거하고 특정 주소만 남겨야 합니다.
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // JWT와 같은 자격 증명(쿠키, 인증 헤더) 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로("/**")에 이 CORS 설정을 적용합니다.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증 제공자(Authentication Provider)를 명시적으로 정의하여 비밀번호 인코더를 설정
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsServiceCustom);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}