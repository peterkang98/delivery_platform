package xyz.sparta_project.manjok.global.infrastructure.event.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventErrorCode;
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("Events 테스트")
class EventsTest {

    private ApplicationEventPublisher mockPublisher;

    @BeforeEach
    void setUp() {
        mockPublisher = mock(ApplicationEventPublisher.class);
        Events.reset();
    }

    @AfterEach
    void tearDown() {
        Events.reset();
    }

    @Test
    @DisplayName("publisher가 초기화되지 않은 상태에서 이벤트 발생 시 예외가 발생한다.")
    void raise_without_initialization_throws_exception() {
        // given
        TestEvent event = new TestEvent("test");

        // when & then
        assertThatThrownBy(() -> Events.raise(event))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_PUBLISHER_NOT_INITIALIZED);
    }

    @Test
    @DisplayName("Publisher 초기화 후 이벤트를 장성적으로 발행할 수 있다.")
    void raise_with_initialized_publisher_publishes_event() {
        // given
        Events.setPublisher(mockPublisher);
        TestEvent event = new TestEvent("test");

        // when
        Events.raise(event);

        // then
        verify(mockPublisher, times(1)).publishEvent(event);
    }

    @Test
    @DisplayName("null 이벤트 발행 시 예외가 발생한다.")
    void rais_null_event_throws_exception() {
        // given
        Events.setPublisher(mockPublisher);

        // when & then
        assertThatThrownBy(() -> Events.raise(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이벤트는 null일 수 없습니다.");
    }

    // 테스트용 이벤트 클래스
    private static class TestEvent extends BaseEntity {
        private final String data;

        public TestEvent(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

}