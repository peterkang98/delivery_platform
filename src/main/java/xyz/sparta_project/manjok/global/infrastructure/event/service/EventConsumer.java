package xyz.sparta_project.manjok.global.infrastructure.event.service;

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
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EventLogRepository eventLogRepository;
    private final EventHandlerRegistry eventHandlerRegistry;

    /**
     * 모든 이벤트를 수신하는 리스너
     *
     * @param event 수신된 이벤트
     * */
    @Async
    @EventListener
    @Transactional
    public void handleEvent(Object event) {
        String eventName = event.getClass().getSimpleName();
        log.info("이벤트 수신: {}", eventName);

        try {
            // 실제 이벤트 처리 로직
            proccessEvent(event);

            // 처리 성공 시 EventLog 상태 업데이트
            updateEventLogStatus(eventName, EventStatus.SUCCESS);

            log.info("이벤트 처리 성공: {}", eventName);

        } catch (Exception e) {
            log.error("이벤트 처리 실패: {}", eventName, e);

            // 처리 실패 시 EventLog 상태 업데이트
            updateEventLogStatus(eventName, EventStatus.FAILED);
        }
    }

    /**
     * 실제 이벤트 처리 로직
     * - EventHandlerRegistry에서 해당 이벤트 타입의 핸들러를 조회하여 실행
     *
     * @param event 처리할 이벤트
     * @throws Exception 처리 중 발생한 예외
     * */
    @SuppressWarnings("unckecked")
    private void proccessEvent(Object event) throws  Exception {
        Class<?> eventType = event.getClass();

        log.debug("이벤트 핸들러 조회: {}", eventType.getSimpleName());

        // 핸들러 조회 및 실행
        EventHandlerProcessor handler = eventHandlerRegistry.getHandler(eventType);
        handler.handle(event);

        log.debug("이벤트 처리 완료: {}", eventType.getSimpleName());
    }
    
    /**
     * EventLog 상태 업데이트
     * 
     * @param eventName 이벤트 이름
     * @param status 업데이트할 상태
     * */
    private void updateEventLogStatus(String eventName, EventStatus status) {
        List<EventLog> logs = eventLogRepository.findAllByStatus(EventStatus.PENDING);

        logs.stream()
                .filter(log -> log.getEventName().equals(eventName))
                .findFirst()
                .ifPresentOrElse(
                        eventLog -> {
                            eventLog.updateStatus(status);
                            log.info("EventLog 상태 업데이트: eventName={}, status={}", eventName, status);

                        },
                        () -> log.warn("EventLog를 찾을 수 없습니다: eventName={}", eventName)
                );
    }
}
