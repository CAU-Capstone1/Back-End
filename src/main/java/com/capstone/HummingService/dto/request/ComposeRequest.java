package com.capstone.HummingService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * AI 음악 생성을 위한 요청 데이터 모델 (프론트엔드 -> 백엔드)
 * * - songForm: 선택된 곡 구조 순서 (예: "Verse", "Chorus" 등)
 * - hummingFiles: 각 구조에 1:1 매칭되는 다중 허밍 오디오 파일
 * - imageFile: 선택적 이미지 파일
 * - 기타: 장르, 길이, 키, 템포, 악기 등 음악 설정 정보
 */
public class ComposeRequest {

        @NotNull(message = "곡 구성을 선택해 주세요.")
        private List<String> songForm;

        @NotNull(message = "허밍 파일을 최소 하나 이상 업로드해 주세요.")
        private List<MultipartFile> hummingFiles; // 송폼의 개수와 일치해야 함

        private MultipartFile imageFile; // 이미지 업로드 요구사항 반영

        @NotBlank(message = "장르를 선택해 주세요.")
        private String genre;

        @NotNull(message = "길이를 입력해 주세요.")
        @Min(value = 5, message = "최소 5초 이상이어야 합니다.")
        private Integer durationSec;

        @NotBlank(message = "키 정보를 입력해 주세요.")
        private String key;

        @NotBlank(message = "템포 정보를 선택해 주세요.")
        private String tempo;

        //@NotNull(message = "사용할 악기를 최소 하나 이상 선택해 주세요.")
        private List<String> instruments;



        public List<String> getSongForm() {
                return songForm;
        }

        public void setSongForm(List<String> songForm) {
                this.songForm = songForm;
        }

        public List<MultipartFile> getHummingFiles() {
                return hummingFiles;
        }

        public void setHummingFiles(List<MultipartFile> hummingFiles) {
                this.hummingFiles = hummingFiles;
        }

        public MultipartFile getImageFile() {
                return imageFile;
        }

        public void setImageFile(MultipartFile imageFile) {
                this.imageFile = imageFile;
        }

        public String getGenre() {
                return genre;
        }

        public void setGenre(String genre) {
                this.genre = genre;
        }

        public Integer getDurationSec() {
                return durationSec;
        }

        public void setDurationSec(Integer durationSec) {
                this.durationSec = durationSec;
        }

        public String getKey() {
                return key;
        }

        public void setKey(String key) {
                this.key = key;
        }

        public String getTempo() {
                return tempo;
        }

        public void setTempo(String tempo) {
                this.tempo = tempo;
        }

        public List<String> getInstruments() {
                return instruments;
        }

        public void setInstruments(List<String> instruments) {
                this.instruments = instruments;
        }
}