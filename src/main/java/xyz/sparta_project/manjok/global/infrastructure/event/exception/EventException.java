package xyz.sparta_project.manjok.global.infrastructure.event.exception;

import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalException;

/**
 * 이벤트 시스템 커스텀 예외
 * GlobalException을 상속받아 일관된 예외 처리 구조 유지
 * */
public class EventException extends GlobalException {
    
    /**
     * ErrorCode만으로 예외 발생
     * */
    public EventException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    /**
     * ErrorCode와 커스텀 메시지로 예외 생성
     * */
    public EventException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    /**
     * ErrorCode와 원인 예외로 예외 생성
     * */
    public EventException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    /**
     * ErrorCode, 커스텀 메시지, 원인 예외로 예외 생성
     * */
    public EventException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
