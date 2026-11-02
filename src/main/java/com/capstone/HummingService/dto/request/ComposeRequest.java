package com.capstone.HummingService.dto.request;


public class ComposeRequest {

        private String style; // 프론트의 style
        private String mood; // 프론트의 mood
        private String instrument; // 프론트의 instrument (단일 악기로 가정)

        // key, tempo, duration은 프론트에서 건너뛸 수 있는 선택 항목 (duration 미입력 시 기본값 사용)
        private String key;

        private String tempo;

        private String duration;

        private String hummingStart;
        private String hummingMain;
        private String hummingEnd;
        private String referenceVisual;



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

        public String getReferenceVisual() {
                return referenceVisual;
        }

        public void setReferenceVisual(String referenceVisual) {
                this.referenceVisual = referenceVisual;
        }
}