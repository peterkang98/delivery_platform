package xyz.sparta_project.manjok.global.infrastructure.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventLog;
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventErrorCode;
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventException;
import xyz.sparta_project.manjok.global.infrastructure.event.repository.EventLogRepository;

/**
 * 이벤트 발행자
 * - 이벤트를 EventLog로 저장하고 spring 이벤트로 발행
 * */
@Slf4j
@Component
@RequiredArgsConstructor

public class EventPublisher {

    private final EventLogRepository eventLogRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * 이벤트 발행
     * @param event 발행할 이벤트 객체
     * @throws EventException 이벤트 직렬화 실패 시
     * @throws IllegalArgumentException event가 null인 경우
     * */
    @Transactional
    public void publish(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("이벤트는 null일 수 없습니다.");
        }

        try {
            // 이벤트 직렬화
            String eventName = event.getClass().getSimpleName();
            String payload = objectMapper.writeValueAsString(event);

            // EventLog 생성 및 저장
            EventLog eventLog = EventLog.of(eventName, payload);
            eventLogRepository.save(eventLog);

            log.info("이벤트 로그 저장 완료: eventName={}, eventLogId={}", eventName, eventLog.getId());

            // spring 이벤트 발행
            applicationEventPublisher.publishEvent(event);

        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화: {}", event.getClass().getSimpleName(), e);
            throw new EventException(EventErrorCode.EVENT_SERIALIZATION_FAILED);
        }
    }
}
