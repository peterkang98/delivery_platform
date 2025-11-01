package xyz.sparta_project.manjok.global.infrastructure.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("EventHandlerRegistry 테스트")
class EventHandlerRegistryTest {

    private ApplicationContext applicationContext;
    private EventHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        registry = new EventHandlerRegistry(applicationContext);
    }

    @Test
    @DisplayName("애플리케이션 시작 시 @EventHandler가 붙은 핸들러를 스캔하여 등록한다.")
    void init_scans_and_registers_event_handler() {

    }

    @Test
    @DisplayName("등록된 핸들러를 이벤트 타입으로 조회할 수 있다.")
    void get_handler_returns_registered_handler() {

    }

    @Test
    @DisplayName("등록되지 않은 이벤트 타입으로 조회 시 예외가 발생한다.")
    void get_handler_throws_exception_for_unregistered_type() {

    }

    @Test
    @DisplayName("EventHandlerProcessor를 구현하지 않은 클래스는 등록되지 않는다.")
    void init_ignores_handlers_not_implementing_processor() {

    }

    @Test
    @DisplayName("여러 핸들러를 동시에 등록할 수 있다.")
    void init_registers_multiple_handlers() {

    }

    //테스트용 이벤트 클래스
    private static class TestEvent {
        private String data;
        public TestEvent(String data) { this.data = data; }
    }

    private static class AnotherEvent {
        private String data;
        public AnotherEvent(String data) { this.data = data; }
    }

    private static class UnregisteredEvent {
        private String data;
    }

    //테스트용 핸들러
    @EventHandler(eventType = TestEvent.class)
    private static class TestEventHandler implements EventHandlerProcessor<TestEvent> {
        @Override
        public void handle(TestEvent event) throws Exception {
            // 테스트용
        }
    }

    @EventHandler(eventType = AnotherEvent.class)
    private static class AnotherEventHandler implements EventHandlerProcessor<AnotherEvent> {
        @Override
        public void handle(AnotherEvent event) throws Exception {
            // 테스트용
        }
    }

    // 테스트용 핸들러 (비정상 - 인터페이스 미구현)
    @EventHandler(eventType = TestEvent.class)
    private static class InvalidHandler {
        // EventHandlerProcessor 미구현
    }

}