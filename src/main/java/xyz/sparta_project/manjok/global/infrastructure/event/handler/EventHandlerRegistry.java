package xyz.sparta_project.manjok.global.infrastructure.event.handler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventErrorCode;
import xyz.sparta_project.manjok.global.infrastructure.event.exception.EventException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 이벤트 핸들러 레지스트리
 * - 애플리케이션 시작 시 @EventHandler가 붙은 핸들러들을 스캔하여 등록
 * - 이벤트 타입별로 핸들러 매핑
 * */
@Slf4j
@Component
public class EventHandlerRegistry {
    private final ApplicationContext applicationContext;
    private final Map<Class<?>, EventHandlerProcessor<?>> handlers = new HashMap<>();
    private final Map<String, Class<?>> eventNameToClassMap = new HashMap<>();

    public EventHandlerRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 애플리케이션 시작 시 @EventHandler 어노테이션이 붙은 빈들을 스캔하여 등록
     * */
    @PostConstruct
    public void init() {
        log.info("이벤트 핸들러 스캔 시작");

        Map<String, Object> eventHandlers = applicationContext.getBeansWithAnnotation(EventHandler.class);

        for (Map.Entry<String, Object> entry : eventHandlers.entrySet()) {
            Object handler = entry.getValue();
            EventHandler annotation = handler.getClass().getAnnotation(EventHandler.class);

            if (handler instanceof EventHandlerProcessor) {
                Class<?> eventType = annotation.eventType();
                handlers.put(eventType, (EventHandlerProcessor<?>) handler);
                eventNameToClassMap.put(eventType.getSimpleName(), eventType);

                log.info("이벤트 핸들러 등록: {} -> {}",
                        eventType.getSimpleName(),
                        handler.getClass().getSimpleName());
            } else {
                log.warn("@EventHandler가 붙었지만 EventHandlerProcessor를 구현하지 않음: {}",
                        handler.getClass().getSimpleName());
            }
        }

        log.info("이벤트 핸들러 스캔 완료. 총 {} 개 핸들러 등록됨", handlers.size());
    }

    /**
     * 이벤트 타입에 해당하는 핸들러 조회
     *
     * @param eventType 이벤트 타입
     * @return 이벤트 핸들러
     * @throws EventException 핸들러가 등록되지 않은 경우
     * */
    @SuppressWarnings("unchecked")
    public <T> EventHandlerProcessor<T> getHandler(Class<T> eventType) {
        EventHandlerProcessor<?> handler = handlers.get(eventType);

        if (handler == null) {
            throw new EventException(
                    EventErrorCode.EVENT_PROCESSING_FAILED,
                    "등록된 핸들러가 없습니다: " + eventType.getSimpleName()
                    );
        }

        return (EventHandlerProcessor<T>) handler;
    }

    /**
     * 이벤트 타입에 해당하는 핸들러가 등록되어 있는지 확인
     * @param eventType 이벤트 타입
     * @return 핸들러 존재 여부
     * */
    public boolean hasHandler(Class<?> eventType) {
        return handlers.containsKey(eventType);
    }

    /**
     * 등록된 모든 이벤트 이름 목록 반환
     *
     * @return 이벤트 이름(SimpleName) Set
     */
    public Set<String> getRegisteredEventNames() {
        return handlers.keySet().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toSet());
    }

    /**
     * 이벤트 이름으로 Class 조회
     *
     * @param eventName 이벤트 SimpleName (예: "OrderCompletedEvent")
     * @return 이벤트 Class
     */
    public Class<?> getEventClassByName(String eventName) {
        Class<?> eventClass = eventNameToClassMap.get(eventName);

        if (eventClass == null) {
            throw new EventException(
                    EventErrorCode.EVENT_PROCESSING_FAILED,
                    "등록된 이벤트 클래스가 없습니다: " + eventName
            );
        }

        return eventClass;
    }

    /**
     * 이벤트 이름으로 핸들러 조회
     *
     * @param eventName 이벤트 SimpleName
     * @return 이벤트 핸들러
     */
    @SuppressWarnings("unchecked")
    public EventHandlerProcessor<?> getHandlerByName(String eventName) {
        Class<?> eventClass = getEventClassByName(eventName);
        return getHandler(eventClass);
    }
}
