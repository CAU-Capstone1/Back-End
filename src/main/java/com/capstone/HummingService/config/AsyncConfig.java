package com.capstone.HummingService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    // AI 작곡 전용 스레드 풀: 요청 스레드와 분리하고, 동시 생성 수를 제한해 AI 서버/메모리 과부하를 막는다
    @Bean(name = "composeExecutor")
    public ThreadPoolTaskExecutor composeExecutor(@Value("${compose.executor.pool-size:2}") int poolSize,
                                                  @Value("${compose.executor.queue-capacity:20}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("compose-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
