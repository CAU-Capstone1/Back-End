package com.capstone.HummingService.service;

import com.capstone.HummingService.dto.request.ComposeRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ComposeWorkerMelodyTest {

    @Test
    void 메인_허밍을_우선_사용하고_없으면_시작_끝_순서로_대체한다() {
        assertThat(ComposeWorker.selectMelody(request("start.mp3", "main.mp3", "end.mp3"))).isEqualTo("main.mp3");
        assertThat(ComposeWorker.selectMelody(request("start.mp3", null, "end.mp3"))).isEqualTo("start.mp3");
        assertThat(ComposeWorker.selectMelody(request(null, " ", "end.mp3"))).isEqualTo("end.mp3");
    }

    @Test
    void 허밍이_하나도_없으면_예외가_발생한다() {
        assertThatThrownBy(() -> ComposeWorker.selectMelody(request(null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ComposeRequest request(String start, String main, String end) {
        ComposeRequest request = new ComposeRequest();
        request.setHummingStart(start);
        request.setHummingMain(main);
        request.setHummingEnd(end);
        return request;
    }
}
