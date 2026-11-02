package com.capstone.HummingService.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AudioServiceTest {

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private AudioService audioService;

    @Test
    void 허밍_파일은_사용자별_경로에_업로드된다() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hum.mp3", "audio/mpeg", new byte[]{1});
        when(s3Service.uploadFile(any(), eq("humming/7/"))).thenReturn("humming/7/1_hum.mp3");

        String key = audioService.saveAudio(file, 7L);

        assertThat(key).isEqualTo("humming/7/1_hum.mp3");
        verify(s3Service).uploadFile(file, "humming/7/");
    }

    @Test
    void mp3가_아닌_파일은_업로드를_거부한다() {
        MockMultipartFile file = new MockMultipartFile("file", "hum.wav", "audio/wav", new byte[]{1});

        assertThatThrownBy(() -> audioService.saveAudio(file, 7L))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(s3Service);
    }

    @Test
    void 본인이_업로드한_key만_소유한_것으로_판단한다() {
        assertThat(audioService.isUploadedBy("humming/7/1_hum.mp3", 7L)).isTrue();
        assertThat(audioService.isUploadedBy("image/7/1_cover.png", 7L)).isTrue();

        assertThat(audioService.isUploadedBy("humming/8/1_hum.mp3", 7L)).isFalse();
        assertThat(audioService.isUploadedBy("humming/77/1_hum.mp3", 7L)).isFalse(); // prefix 부분 일치 방지
        assertThat(audioService.isUploadedBy("final-bgm/job_abc.wav", 7L)).isFalse();
        assertThat(audioService.isUploadedBy("humming/7/../8/1_hum.mp3", 7L)).isFalse();
        assertThat(audioService.isUploadedBy(null, 7L)).isFalse();
    }
}
