package xyz.sparta_project.manjok.global.infrastructure.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventException;

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
        // given
        TestEventHandler handler = new TestEventHandler();
        Map<String, Object> handlers = new HashMap<>();
        handlers.put("testEventHandler", handler);

        when(applicationContext.getBeansWithAnnotation(EventHandler.class))
                .thenReturn(handlers);

        // when
        registry.init();

        // then
        assertThat(registry.hasHandler(TestEvent.class)).isTrue();
        assertThat(registry.getHandler(TestEvent.class)).isEqualTo(handler);
    }

    @Test
    @DisplayName("등록된 핸들러를 이벤트 타입으로 조회할 수 있다.")
    void get_handler_returns_registered_handler() {
        // given
        TestEventHandler handler = new TestEventHandler();
        Map<String, Object> handlers = new HashMap<>();
        handlers.put("testEventHandler", handler);

        when(applicationContext.getBeansWithAnnotation(EventHandler.class))
                .thenReturn(handlers);

        registry.init();

        // when
        EventHandlerProcessor<TestEvent> result = registry.getHandler(TestEvent.class);

        // then
        assertThat(result).isEqualTo(handler);

    }

    @Test
    @DisplayName("등록되지 않은 이벤트 타입으로 조회 시 예외가 발생한다.")
    void get_handler_throws_exception_for_unregistered_type() {
        // given
        when(applicationContext.getBeansWithAnnotation(EventHandler.class))
                .thenReturn(new HashMap<>());

        registry.init();

        // when & then
        assertThatThrownBy(() -> registry.getHandler(UnregisteredEvent.class))
                .isInstanceOf(EventException.class)
                .hasMessageContaining("등록된 핸들러가 없습니다");
    }

    @Test
    @DisplayName("EventHandlerProcessor를 구현하지 않은 클래스는 등록되지 않는다.")
    void init_ignores_handlers_not_implementing_processor() {
        // given
        InvalidHandler invalidHandler = new InvalidHandler();
        Map<String, Object> handlers = new HashMap<>();
        handlers.put("invalidHandler", invalidHandler);

         when(applicationContext.getBeansWithAnnotation(EventHandler.class))
                 .thenReturn(handlers);

         // when
        registry.init();

        // then
        assertThat(registry.hasHandler(TestEvent.class)).isFalse();
    }

    @Test
    @DisplayName("여러 핸들러를 동시에 등록할 수 있다.")
    void init_registers_multiple_handlers() {
        // given
        TestEventHandler handler1 = new TestEventHandler();
        AnotherEventHandler handler2 = new AnotherEventHandler();

        Map<String, Object> handlers = new HashMap<>();
        handlers.put("testEventHandler", handler1);
        handlers.put("anotherEventHandler", handler2);

        when(applicationContext.getBeansWithAnnotation(EventHandler.class))
                .thenReturn(handlers);

        // when
        registry.init();

        // then
        assertThat(registry.hasHandler(TestEvent.class)).isTrue();
        assertThat(registry.hasHandler(AnotherEvent.class)).isTrue();
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