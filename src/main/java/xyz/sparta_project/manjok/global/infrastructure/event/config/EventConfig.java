package xyz.sparta_project.manjok.global.infrastructure.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.sparta_project.manjok.global.infrastructure.event.infrastructure.Events;

/**
 * 이벤트 시스템 설정
 * - Events 클래스 초기화
 * - ObjectMapper 빈 설정
 * */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class EventConfig {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Events 클래스에 ApplicationEventPublisher 주입
     * */
    @PostConstruct
    public void initializeEvents() {
        Events.setPublisher(applicationEventPublisher);
        log.info("Events 클래스가 초기화되었습니다.");
    }

    /**
     * JSON 직렬화/역직렬화를 위한 ObjectMapper 빈
     * */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
