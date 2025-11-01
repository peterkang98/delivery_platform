package xyz.sparta_project.manjok.global.infrastructure.event.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventErrorCode;
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventException;

/**
 * 이벤트 발행을 위한 글로벌 진입점
 * - ApplicationEventPublisher를 래핑한 정적 유틸리티 클래스
 * - 비즈니스 로직에서 의존성 주입 없이 이벤트 발행 가능
 * */
@Slf4j
public class Events {

    private static ApplicationEventPublisher publisher;

    /**
     * ApplicationEventPublisher 설정 (EventConfig에서 호출)
     * */
    public static void setPublisher (ApplicationEventPublisher publisher) {
        Events.publisher = publisher;
        log.info("ApplicationEventPublisher가 Events에 설정되어있습니다.");
    }

    /**
     * 이벤트 발행
     * @Param event 발행할 이벤트 객체
     * @throws EventException publisher가 초기화되지 않은 경우
     * @throws IllegalArgumentException event가 null인 경우
     * */
    public static void raise(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("이벤트는 null일 수 없습니다.");
        }

        if (publisher == null) {
            throw new EventException(EventErrorCode.EVENT_PUBLISHER_NOT_INITIALIZED);
        }

        log.debug("이벤트 발행: {}", event.getClass().getSimpleName());
        publisher.publishEvent(event);
    }

    /**
     * 테스트를 위한 publisher 초기화 메서드
     * */
    public static void reset() {
        publisher = null;
    }

}
