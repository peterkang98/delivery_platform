package xyz.sparta_project.manjok.global.infrastructure.event.handler;

/**
 * 이벤트 핸들러가 구현해야 하는 인터페이스
 *
 * @param <T> 처리할 이벤트 타입
 * */
public interface EventHandlerProcessor<T> {

    /**
     * 이벤트 처리 로직
     *
     * @param event 처리할 이벤트
     * @throws Exception 처리 중 발생한 예외
     * */
    void handle(T event) throws Exception;
}
