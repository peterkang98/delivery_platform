package xyz.sparta_project.manjok.global.infrastructure.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventLog;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventStatus;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerRegistry;
import xyz.sparta_project.manjok.global.infrastructure.event.repository.EventLogRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventRetryService 테스트")
class EventRetryServiceTest {

    @Mock
    private EventLogRepository eventLogRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private EventHandlerRegistry eventHandlerRegistry;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventRetryService eventRetryService;

    @Test
    @DisplayName("재시도 횟수가 3회 미만인 FAILED 이벤트를 RETRYING 상태로 변경하고 재발생한다")
    void retry_failed_event_under_max_retry() {
        // given
        EventLog eventLog = EventLog.of("TestEvent","{\"data\":\"test\"}");
        eventLog.updateStatus(EventStatus.FAILED);
        eventLog.setRetryCount(2);

        when(eventLogRepository.findAllByStatus(EventStatus.FAILED))
                .thenReturn(List.of(eventLog));
        when(eventHandlerRegistry.getRegisteredEventNames())
                .thenReturn(Set.of("TestEvent"));

        // when
        eventRetryService.retryFailedEvents();

        // then
        assertThat(eventLog.getStatus()).isEqualTo(EventStatus.RETRYING);
        assertThat(eventLog.getRetryCount()).isEqualTo(3);

        verify(applicationEventPublisher).publishEvent(any(EventRetryService.RetryEvent.class));
    }

    @Test
    @DisplayName("재시도 횟수가 3회 이상인 FAILED 이벤트느 DEAD_LETTER로 전환한다")
    void move_to_dead_letter_when_max_retry_exceeded() {
        // given
        EventLog eventLog = EventLog.of("TestEvent", "{\"data\":\"test\"}");
        eventLog.updateStatus(EventStatus.FAILED);
        eventLog.setRetryCount(3);

        when(eventLogRepository.findAllByStatus(EventStatus.FAILED))
                .thenReturn(List.of(eventLog));
        when(eventHandlerRegistry.getRegisteredEventNames())
                .thenReturn(Set.of("TestEvent"));

        // when
        eventRetryService.retryFailedEvents();

        // then
        assertThat(eventLog.getStatus()).isEqualTo(EventStatus.DEAD_LETTER);

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("FAILED 상태의 이벤트가 없으면 아무 동작도 하지 않는다")
    void do_noting_when_no_failed_events() {
        // given
        when(eventLogRepository.findAllByStatus(EventStatus.FAILED))
                .thenReturn(Collections.emptyList());

        // when
        eventRetryService.retryFailedEvents();

        // then
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("여러 FAILED 이벤트를 한 번에 처리할 수 있다")
    void retry_multiple_failed_events() {
        //given
        EventLog eventLog1 = EventLog.of("TestEvent1", "{\"data\":\"test1\"}");
        eventLog1.updateStatus(EventStatus.FAILED);
        eventLog1.setRetryCount(1);

        EventLog eventLog2 = EventLog.of("TestEvent2", "{\"data\":\"test2\"}");
        eventLog2.updateStatus(EventStatus.FAILED);
        eventLog2.setRetryCount(2);

        when(eventLogRepository.findAllByStatus(EventStatus.FAILED))
                .thenReturn(List.of(eventLog1, eventLog2));
        when(eventHandlerRegistry.getRegisteredEventNames())
                .thenReturn(Set.of("TestEvent1","TestEvent2"));

        // when
        eventRetryService.retryFailedEvents();

        // then
        assertThat(eventLog1.getStatus()).isEqualTo(EventStatus.RETRYING);
        assertThat(eventLog1.getRetryCount()).isEqualTo(2);

        assertThat(eventLog2.getStatus()).isEqualTo(EventStatus.RETRYING);
        assertThat(eventLog2.getRetryCount()).isEqualTo(3);

        verify(applicationEventPublisher, times(2)).publishEvent(any(EventRetryService.RetryEvent.class));

    }

}