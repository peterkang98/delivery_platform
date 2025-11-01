package xyz.sparta_project.manjok.global.infrastructure.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventLog;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventStatus;
import xyz.sparta_project.manjok.global.infrastructure.event.repository.EventLogRepository;

import java.util.List;

/**
 * 이벤트 재시도 서비스
 * - 실패한 이벤트를 주기적으로 재시도
 * - 재시도 횟수 제한 관리
 * */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventRetryService {
    private static final int MAX_RETRY_COUNT = 3;

    private final EventLogRepository eventLogRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * 실패한 이벤트 재시도(5초마다 실행)
     * */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void retryFailedEvents() {
        log.info("실패한 이벤트 재시도 작업 시작");

        List<EventLog> failedEvents = eventLogRepository.findAllByStatus(EventStatus.FAILED);

        if(failedEvents.isEmpty()) {
            log.info("재시도할 실패 이벤트가 없습니다.");
            return;
        }

        log.info("재시도할 이벤트 수: {}", failedEvents.size());

        for (EventLog eventLog : failedEvents) {
            processRetry(eventLog);
        }

        log.info("실패한 이벤트 재시도 작업 완료");
    }

    /**
     * 개별 이벤트 재시도 처리
     * - @Transactional 메서드 내에서 호출되므로 더티 체킹으로 자동 업데이트
     *
     * @param eventLog 재시도할 EventLog
     * */
    public void processRetry(EventLog eventLog) {
        try {
            if (eventLog.getRetryCount() >= MAX_RETRY_COUNT) {
                log.warn("재시도 횟수 초과로 DEAD_LETTER로 전환: eventLogId={}, retryCount={}",
                        eventLog.getId(), eventLog.getRetryCount());
                eventLog.updateStatus(EventStatus.DEAD_LETTER);

                return;
            }

            if (!eventLog.getStatus().canTransitionTo(EventStatus.RETRYING)) {
                log.warn("RETRYING 상태로 전환 불가: eventLogId={}, currentStatus={}",
                        eventLog.getId(), eventLog.getStatus());
                return;
            }

            eventLog.updateStatus(EventStatus.RETRYING);
            eventLog.increaseRetryCount();

            log.info("이벤트 재시도 시작: eventLogId={}, eventName={}, retryCount={}",
                    eventLog.getId(), eventLog.getEventName(), eventLog.getRetryCount());

            RetryEvent retryEvent = new RetryEvent(eventLog.getEventName(), eventLog.getPayload());
            applicationEventPublisher.publishEvent(retryEvent);

        } catch (Exception e) {
            log.error("이벤트 재시도 중 오류 발생: eventLogId={}", eventLog.getId(), e);
        }
    }

    /**
     * 재시도 이벤트 래퍼
     * - EventConsumer에서 처리할 수 있도록 래핑
     * */
    public static class RetryEvent {
        private final String eventName;
        private final String payload;

        public RetryEvent(String eventName, String payload) {  // ✅ eventName
            this.eventName = eventName;
            this.payload = payload;
        }

        public String getEventName() {
            return eventName;
        }

        public String getPayload() {
            return payload;
        }
    }
}
