package xyz.sparta_project.manjok.global.infrastructure.event.service;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("EventConsumer 테스트")
class EventConsumerTest {

    @Mock
    private EventLogRepository eventLogRepository;

    @Mock
    private EventHandlerRegistry eventHandlerRegistry;

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

    // 테스트용 이벤트 클래스
    private static class TestEvent {
        private String data;

        public TestEvent(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }
}