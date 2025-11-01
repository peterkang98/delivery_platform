package xyz.sparta_project.manjok.global.infrastructure.event.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 이벤트 스케줄러 설정
 * - 스케줄링 활성화
 * - 비동기 처리 활성화
 */
@Configuration
@EnableScheduling
@EnableAsync
public class EventSchedulerConfig {
}
