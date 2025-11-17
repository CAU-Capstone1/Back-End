package com.capstone.HummingService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
// import org.springframework.web.multipart.MultipartFile; // 파일 타입 제거
import java.util.List;

/**
 * AI 음악 생성을 위한 요청 데이터 모델 (프론트엔드 -> 백엔드)
 * - 프론트엔드 CompositionRequestBody와 필드명 및 타입 일치
 * - hummingStart/Main/End 및 referenceVisual은 파일 경로(string)로 간주
 */
public class ComposeRequest {

        // 프론트엔드 필드명과 일치시키기 위한 매핑 (선택적 필드는 @NotBlank 대신 null 허용)
        private String style; // 프론트의 style
        private String mood; // 프론트의 mood
        private String instrument; // 프론트의 instrument (단일 악기로 가정)

        @NotBlank(message = "키 정보를 입력해 주세요.")
        private String key;

        @NotBlank(message = "템포 정보를 선택해 주세요.")
        private String tempo;

        // **변경: 프론트엔드 필드명 'duration'과 타입 'String'으로 통일**
        @NotBlank(message = "길이를 입력해 주세요.") // @NotNull 대신 @NotBlank로 문자열 필수 검사
        private String duration; // Integer durationSec -> String duration

        private String hummingStart;
        private String hummingMain;
        private String hummingEnd;
        private String referenceVisual;


        // =========================================================================
        // 프론트엔드 모델과 일치시키기 위해 일부 필드명 및 타입을 정리했습니다.
        // 백엔드에서 사용하지 않거나, 프론트의 필드로 대체 가능한 필드들은 제거했습니다.
        // 예를 들어, songForm, hummingFiles, imageFile은 제거되었습니다.
        // =론적으로, 프론트엔드 Body와 백엔드 DTO 필드명이 일치하지 않으니,
        // 아래 Getters/Setters를 포함한 전체 DTO를 사용하시길 권장합니다.
        // =========================================================================


        // **Getters and Setters**

        public String getStyle() {
                return style;
        }

        public void setStyle(String style) {
                this.style = style;
        }

        public String getMood() {
                return mood;
        }

        public void setMood(String mood) {
                this.mood = mood;
        }

        public String getInstrument() {
                return instrument;
        }

        public void setInstrument(String instrument) {
                this.instrument = instrument;
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

        public String getDuration() {
                return duration;
        }

        public void setDuration(String duration) {
                this.duration = duration;
        }

        // 허밍 필드 (프론트와 일치)
        public String getHummingStart() {
                return hummingStart;
        }

        public void setHummingStart(String hummingStart) {
                this.hummingStart = hummingStart;
        }

        public String getHummingMain() {
                return hummingMain;
        }

        public void setHummingMain(String hummingMain) {
                this.hummingMain = hummingMain;
        }

        public String getHummingEnd() {
                return hummingEnd;
        }

        public void setHummingEnd(String hummingEnd) {
                this.hummingEnd = hummingEnd;
        }

        // 레퍼런스 비주얼 (프론트와 일치)
        public String getReferenceVisual() {
                return referenceVisual;
        }

        public void setReferenceVisual(String referenceVisual) {
                this.referenceVisual = referenceVisual;
        }
}