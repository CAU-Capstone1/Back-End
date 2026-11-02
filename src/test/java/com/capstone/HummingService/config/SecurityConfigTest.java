package com.capstone.HummingService.config;

import com.capstone.HummingService.controller.AudioController;
import com.capstone.HummingService.controller.AuthController;
import com.capstone.HummingService.service.AudioService;
import com.capstone.HummingService.service.AuthService;
import com.capstone.HummingService.service.CurrentUserService;
import com.capstone.HummingService.service.UserDetailsServiceCustom;
import com.capstone.HummingService.support.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 보안 설정(인증 필요 경로, 401/403 응답)을 실제 필터 체인으로 검증
@WebMvcTest(controllers = {AudioController.class, AuthController.class})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private UserDetailsServiceCustom userDetailsServiceCustom;
    @MockitoBean
    private AudioService audioService;
    @MockitoBean
    private CurrentUserService currentUserService;
    @MockitoBean
    private AuthService authService;

    @Test
    void 토큰_없이_업로드하면_401을_반환한다() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hum.mp3", "audio/mpeg", new byte[]{1});

        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "a@b.com")
    void 다른_사용자의_파일로_signed_url을_요청하면_403을_반환한다() throws Exception {
        when(currentUserService.getUserId(any())).thenReturn(1L);
        when(audioService.isUploadedBy("humming/2/a.mp3", 1L)).thenReturn(false);

        mockMvc.perform(get("/api/audio/signed-url").param("key", "humming/2/a.mp3"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없는 파일입니다."));
    }

    @Test
    @WithMockUser(username = "a@b.com")
    void 본인_파일이면_signed_url을_반환한다() throws Exception {
        when(currentUserService.getUserId(any())).thenReturn(1L);
        when(audioService.isUploadedBy("humming/1/a.mp3", 1L)).thenReturn(true);
        when(audioService.generateSignedUrl("humming/1/a.mp3")).thenReturn("https://signed");

        mockMvc.perform(get("/api/audio/signed-url").param("key", "humming/1/a.mp3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signedUrl").value("https://signed"));
    }

    @Test
    void 비밀번호가_틀리면_401을_반환한다() throws Exception {
        when(authService.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }
}
