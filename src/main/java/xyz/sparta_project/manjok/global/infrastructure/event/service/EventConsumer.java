package xyz.sparta_project.manjok.global.infrastructure.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventLog;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventStatus;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerProcessor;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerRegistry;
import xyz.sparta_project.manjok.global.infrastructure.event.repository.EventLogRepository;

import java.util.List;

/**
 * 이벤트 소비자
 * - 발행된 이벤트를 수신하고 처리
 * - 처리 결과에 따라 EventLog 상태 업데이트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EventLogRepository eventLogRepository;
    private final EventHandlerRegistry eventHandlerRegistry;
    private final ObjectMapper objectMapper;

    /**
     * 모든 이벤트를 수신하는 리스너
     * - RetryEvent는 제외
     *
     * @param event 수신된 이벤트
     */
    @Async
    @EventListener
    @Transactional
    public void handleEvent(Object event) {
        // RetryEvent는 별도 핸들러에서 처리
        if (event instanceof EventRetryService.RetryEvent) {
            return;
        }

        String eventName = event.getClass().getSimpleName();
        log.info("이벤트 수신: {}", eventName);

        try {
            // 실제 이벤트 처리 로직
            processEvent(event);

            // 처리 성공 시 EventLog 상태 업데이트
            updateEventLogStatus(eventName, EventStatus.SUCCESS, EventStatus.PENDING);

            log.info("이벤트 처리 성공: {}", eventName);

        } catch (Exception e) {
            log.error("이벤트 처리 실패: {}", eventName, e);

            // 처리 실패 시 EventLog 상태 업데이트
            updateEventLogStatus(eventName, EventStatus.FAILED, EventStatus.PENDING);
        }
    }

    /**
     * 재시도 이벤트 전용 핸들러
     * - payload를 원본 이벤트로 역직렬화하여 직접 처리
     *
     * @param retryEvent 재시도 이벤트
     */
    @Async
    @EventListener
    @Transactional
    public void handleRetryEvent(EventRetryService.RetryEvent retryEvent) {
        String eventName = retryEvent.getEventName();
        log.info("재시도 이벤트 수신: {}", eventName);

        try {
            // eventName으로 Class 조회
            Class<?> eventClass = eventHandlerRegistry.getEventClassByName(eventName);

            // payload를 원본 이벤트로 역직렬화
            Object originalEvent = objectMapper.readValue(retryEvent.getPayload(), eventClass);

            // 직접 핸들러 조회 및 실행 (재발행 안 함!)
            EventHandlerProcessor handler = eventHandlerRegistry.getHandlerByName(eventName);
            handler.handle(originalEvent);

            // RETRYING → SUCCESS
            updateEventLogStatus(eventName, EventStatus.SUCCESS, EventStatus.RETRYING);

            log.info("재시도 이벤트 처리 성공: {}", eventName);

        } catch (Exception e) {
            log.error("재시도 이벤트 처리 실패: {}", eventName, e);

            // RETRYING → FAILED
            updateEventLogStatus(eventName, EventStatus.FAILED, EventStatus.RETRYING);
        }
    }

    /**
     * 실제 이벤트 처리 로직
     * - EventHandlerRegistry에서 해당 이벤트 타입의 핸들러를 조회하여 실행
     *
     * @param event 처리할 이벤트
     * @throws Exception 처리 중 발생한 예외
     */
    @SuppressWarnings("unchecked")
    private void processEvent(Object event) throws Exception {
        Class<?> eventType = event.getClass();

        log.debug("이벤트 핸들러 조회: {}", eventType.getSimpleName());

        // 핸들러 조회 및 실행
        EventHandlerProcessor handler = eventHandlerRegistry.getHandler(eventType);
        handler.handle(event);

        log.debug("이벤트 처리 완료: {}", eventType.getSimpleName());
    }

    /**
     * EventLog 상태 업데이트 (통합)
     *
     * @param eventName 이벤트 이름
     * @param newStatus 새로운 상태
     * @param currentStatus 현재 상태 (PENDING 또는 RETRYING)
     */
    private void updateEventLogStatus(String eventName, EventStatus newStatus, EventStatus currentStatus) {
        List<EventLog> logs = eventLogRepository.findAllByStatus(currentStatus);

        logs.stream()
                .filter(log -> log.getEventName().equals(eventName))
                .findFirst()
                .ifPresentOrElse(
                        eventLog -> {
                            eventLog.updateStatus(newStatus);
                            log.info("EventLog 상태 업데이트: eventName={}, {} → {}",
                                    eventName, currentStatus, newStatus);
                        },
                        () -> log.warn("EventLog를 찾을 수 없습니다: eventName={}, currentStatus={}",
                                eventName, currentStatus)
                );
    }
}