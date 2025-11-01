package xyz.sparta_project.manjok.global.infrastructure.event.handler;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이벤트 핸들러를 표시하는 어노테이션
 * - 특정 이벤트 타입을 처리하는 핸들러 클래스에 사용
 * - 애플리케이션 시작 시 스캔되어 EventHandlerRegistry에 등록됨
 * */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface EventHandler {
    /**
     * 처리할 이벤트 타입
     * */
    Class<?> eventType();
}
