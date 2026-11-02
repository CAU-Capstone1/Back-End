package com.capstone.HummingService.controller;

import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.dto.request.CallbackRequest;
import com.capstone.HummingService.service.CallbackService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CallbackControllerTest {

    private final CallbackService callbackService = mock(CallbackService.class);
    private final CallbackRequest request = new CallbackRequest("job_abc", JobStatus.PROCESSING, 50.0, null, null);

    @Test
    void 시크릿이_일치하면_콜백을_처리한다() {
        CallbackController controller = new CallbackController(callbackService, "secret");

        assertThat(controller.handle("secret", request).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(callbackService).handleCallback(request);
    }

    @Test
    void 시크릿이_없거나_틀리면_401을_반환한다() {
        CallbackController controller = new CallbackController(callbackService, "secret");

        assertThat(controller.handle(null, request).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(controller.handle("wrong", request).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(callbackService);
    }

    @Test
    void 서버에_시크릿이_설정되지_않으면_모든_콜백을_거부한다() {
        CallbackController controller = new CallbackController(callbackService, "");

        assertThat(controller.handle("", request).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(callbackService);
    }
}
