package xyz.sparta_project.manjok.global.infrastructure.event.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

/**
 * 이벤트 시스템 관련 에러 코드
 * */
@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements ErrorCode {
    EVENT_PUBLISHER_NOT_INITIALIZED("EVENT_001", "이벤트 발행자가 초기화되지 않았습니다.", 500),
    EVENT_SERIALIZATION_FAILED("EVENT_002", "이벤트 직렬화에 실패했습니다.", 500),
    EVENT_PROCESSING_FAILED("EVENT_003", "이벤트 처리 중 오류가 발생했습니다.", 500),
    EVENT_RETRY_FAILED("EVENT_004", "이벤트 재시도 중 오류가 발생했습니다.", 500),

    // 이벤트 입력 검증 에러 (400번대)
    INVALID_EVENT("EVENT_100", "유효하지 않은 이벤트입니다.", 400),
    EVENT_NOT_FOUND("EVENT_101", "이벤트를 찾을 수 없습니다.", 404);

    private final String code;
    private final String message;
    private final int status;
}
