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
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventLog;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventStatus;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.TestEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerProcessor;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerRegistry;
import xyz.sparta_project.manjok.global.infrastructure.event.repository.EventLogRepository;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventConsumer 테스트")
class EventConsumerTest {

    @Mock
    private EventLogRepository eventLogRepository;

    @Mock
    private EventHandlerRegistry eventHandlerRegistry;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventConsumer eventConsumer;

    @Mock
    private EventHandlerProcessor<TestEvent> mockHandler;

    private EventLog testEventLog;

    @BeforeEach
    void setUp() {
        testEventLog = EventLog.of("TestEvent", "{\"data\":\"test\"}");
    }

    @Test
    @DisplayName("이벤트 처리 성공 시 EventLog 상태를 SUCCESS로 업데이트한다")
    void handle_event_success_updates_status() throws Exception {
        // given
        TestEvent event = new TestEvent("test-data");
        EventLog eventLog = EventLog.of("TestEvent", "{\"data\":\"test\"}");

        when(eventHandlerRegistry.getHandler(TestEvent.class))
                .thenReturn(mockHandler);
        when(eventLogRepository.findAllByStatus(EventStatus.PENDING))
                .thenReturn(List.of(eventLog));

        // when
        eventConsumer.handleEvent(event);

        // then
        verify(mockHandler).handle(event);
        assertThat(eventLog.getStatus()).isEqualTo(EventStatus.SUCCESS);
    }

    @Test
    @DisplayName("EventLog를 찾을 수 없는 경우 예외를 던지지 않는다")
    void handle_event_without_log_does_not_throw() throws Exception {
        // given
        TestEvent event = new TestEvent("test-data");

        when(eventHandlerRegistry.getHandler(TestEvent.class))
                .thenReturn(mockHandler);
        when(eventLogRepository.findAllByStatus(EventStatus.PENDING))
                .thenReturn(Collections.emptyList());

        // when & then - 예외 없이 정상 실행
        eventConsumer.handleEvent(event);

        verify(mockHandler).handle(event);
    }

    @Test
    @DisplayName("RetryEvent는 handleEvent에서 무시된다")
    void handle_event_ignores_retry_event() {
        // given
        EventRetryService.RetryEvent retryEvent = new EventRetryService.RetryEvent("TestEvent", "{\"data\":\"test\"}");

        // when
        eventConsumer.handleEvent(retryEvent);

        // then
        verify(eventHandlerRegistry, never()).getHandler(any());
        verify(eventLogRepository, never()).findAllByStatus(any());
    }

    @Test
    @DisplayName("재시도 이벤트 처리 성공 시 RETRYING 상태를 SUCCESS로 업데이트한다")
    void handle_retry_event_success_updates_status() throws Exception {
        // given
        String eventName = "TestEvent";
        String payload = "{\"data\":\"test\"}";
        EventRetryService.RetryEvent retryEvent = new EventRetryService.RetryEvent(eventName, payload);

        EventLog eventLog = EventLog.of(eventName, payload);
        eventLog.updateStatus(EventStatus.RETRYING);

        TestEvent originalEvent = new TestEvent("test");

        when(eventHandlerRegistry.getEventClassByName(eventName))
                .thenReturn((Class) TestEvent.class);
        when(objectMapper.readValue(payload, TestEvent.class))
                .thenReturn(originalEvent);
        when(eventHandlerRegistry.getHandlerByName(eventName))
                .thenReturn((EventHandlerProcessor) mockHandler);
        when(eventLogRepository.findAllByStatus(EventStatus.RETRYING))
                .thenReturn(List.of(eventLog));

        // when
        eventConsumer.handleRetryEvent(retryEvent);

        // then
        verify(mockHandler).handle(originalEvent);
        assertThat(eventLog.getStatus()).isEqualTo(EventStatus.SUCCESS);
    }

    @Test
    @DisplayName("재시도 이벤트 처리 실패 시 RETRYING 상태를 FAILED로 업데이트한다")
    void handle_retry_event_failure_updates_status() throws Exception {
        // given
        String eventName = "TestEvent";
        String payload = "{\"data\":\"test\"}";
        EventRetryService.RetryEvent retryEvent = new EventRetryService.RetryEvent(eventName, payload);

        EventLog eventLog = EventLog.of(eventName, payload);
        eventLog.updateStatus(EventStatus.RETRYING);

        TestEvent originalEvent = new TestEvent("test");

        when(eventHandlerRegistry.getEventClassByName(eventName))
                .thenReturn((Class) TestEvent.class);
        when(objectMapper.readValue(payload, TestEvent.class))
                .thenReturn(originalEvent);
        when(eventHandlerRegistry.getHandlerByName(eventName))
                .thenReturn((EventHandlerProcessor) mockHandler);
        doThrow(new RuntimeException("처리 실패"))
                .when(mockHandler).handle(originalEvent);
        when(eventLogRepository.findAllByStatus(EventStatus.RETRYING))
                .thenReturn(List.of(eventLog));

        // when
        eventConsumer.handleRetryEvent(retryEvent);

        // then
        verify(mockHandler).handle(originalEvent);
        assertThat(eventLog.getStatus()).isEqualTo(EventStatus.FAILED);
    }

    @Test
    @DisplayName("재시도 이벤트 역직렬화 실패 시 FAILED 상태로 업데이트한다")
    void handle_retry_event_deserialization_failure_updates_status() throws Exception {
        // given
        String eventName = "TestEvent";
        String payload = "{invalid json}";
        EventRetryService.RetryEvent retryEvent = new EventRetryService.RetryEvent(eventName, payload);

        EventLog eventLog = EventLog.of(eventName, payload);
        eventLog.updateStatus(EventStatus.RETRYING);

        when(eventHandlerRegistry.getEventClassByName(eventName))
                .thenReturn((Class) TestEvent.class);
        when(objectMapper.readValue(payload, TestEvent.class))
                .thenThrow(new RuntimeException("역직렬화 실패"));
        when(eventLogRepository.findAllByStatus(EventStatus.RETRYING))
                .thenReturn(List.of(eventLog));

        // when
        eventConsumer.handleRetryEvent(retryEvent);

        // then
        verify(mockHandler, never()).handle(any());
        assertThat(eventLog.getStatus()).isEqualTo(EventStatus.FAILED);
    }
}