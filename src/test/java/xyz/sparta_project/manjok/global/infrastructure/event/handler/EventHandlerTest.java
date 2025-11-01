package xyz.sparta_project.manjok.global.infrastructure.event.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

class EventHandlerTest {

    @Test
    @DisplayName("EventHandler 어노테이션이 클래스에 적용 가능하다.")
    void event_handler_can_be_applied_to_class() {
        // given
        Target target = EventHandler.class.getAnnotation(Target.class);

        // when & then
        assertThat(target.value()).contains(ElementType.TYPE);
    }

    @Test
    @DisplayName("EventHandler 어느테이션은 런타임에 유지된다.")
    void event_handler_is_retained_at_runtime() {
        // given
        Retention retention = EventHandler.class.getAnnotation(Retention.class);

        // when & then
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
    }

    @Test
    @DisplayName("EventHandler 어노테이션은 Component를 포함한다.")
    void event_handler_includes_component() {
        // given & when
        boolean hasComponent = EventHandler.class.isAnnotationPresent(Component.class);

        // then
        assertThat(hasComponent).isTrue();
    }

    @Test
    @DisplayName("eventType 속성을 가지고 있다.")
    void event_handler_has_event_type_attribute() throws NoSuchMethodException {
        // given & when
        var method = EventHandler.class.getMethod("eventType");

        // then
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Class.class);
    }

}