package xyz.sparta_project.manjok.global.infrastructure.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventErrorCode;
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventException;
import xyz.sparta_project.manjok.global.infrastructure.event.repository.EventLogRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventPublisher 테스트")
class EventPublisherTest {

    @Mock
    private EventLogRepository eventLogRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private EventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("이벤트를 정상적으로 발행하고 EventLog를 저장한다.")
    void publish_event_successfully() throws Exception {
        // given
        TestEvent event = new TestEvent("test-data");
        EventPublisher publisher = new EventPublisher(eventLogRepository,
                applicationEventPublisher, objectMapper);

        when(eventLogRepository.save(any(EventLog.class)))
                .thenAnswer(invocation
                        -> invocation.getArgument(0));

        // when
        publisher.publish(event);

        // then
        ArgumentCaptor<EventLog> logCaptor = ArgumentCaptor.forClass(EventLog.class);
        verify(eventLogRepository).save(logCaptor.capture());

        EventLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getEventName()).isEqualTo("TestEvent");
        assertThat(savedLog.getStatus()).isEqualTo(EventStatus.PENDING);
        assertThat(savedLog.getPayload()).contains("test-data");

        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    @DisplayName("이벤트 직렬화에 실패하면 예외가 발생한다.")
    void publish_event_serialization_fails() {

        //given
        CircularReferenceEvent event = new CircularReferenceEvent();
        event.self = event; //순환 참조

        EventPublisher publisher = new EventPublisher(eventLogRepository,
                applicationEventPublisher, objectMapper);

        // when & then
        assertThatThrownBy(() -> publisher.publish(event))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_SERIALIZATION_FAILED);

        verify(eventLogRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("null 이벤트 발생 시 예외가 발생한다.")
    void publish_null_event_throws_exception() {

        // given
        EventPublisher publisher = new EventPublisher(eventLogRepository,
                applicationEventPublisher, objectMapper);

        // when & then
        assertThatThrownBy(() -> publisher.publish(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이벤트는 null일 수 없습니다.");
    }
    
    //테스트용 이벤트 클래스
    private static class TestEvent {
        private String data;

        public TestEvent(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    // 순환 참조 테스트용 이벤트 클래스
    private static class CircularReferenceEvent {
        public CircularReferenceEvent self;
    }
}