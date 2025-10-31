package xyz.sparta_project.manjok.global.infrastructure.event.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventException 테스트")
class EventExceptionTest {

    @Test
    @DisplayName("GlobalException을 상속 받는다.")
    void extends_global_exception() {
        // given
        EventException exception = new EventException(EventErrorCode.EVENT_PUBLISHER_NOT_INITIALIZED);

        // when & then
        assertThat(exception).isInstanceOf(GlobalException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ErrorCode로 예외를 생성할 수 있다.")
    void create_exception_with_error_code() {
        // given
        EventErrorCode errorCode = EventErrorCode.EVENT_PUBLISHER_NOT_INITIALIZED;

        // when
        EventException exception = new EventException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
        assertThat(exception.getErrorCode().getCode()).isEqualTo("EVENT_001");
        assertThat(exception.getErrorCode().getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("커스텀 메시지와 함께 예외를 생성 할 수 있다.")
    void create_exception_with_custom_message() {
        // given
        EventErrorCode errorCode = EventErrorCode.EVENT_PROCESSING_FAILED;
        String customMessage = "주문 완료 이벤트 처리 중 데이터베이스 연결 오류 발생";

        // when
        EventException exception = new EventException(errorCode, customMessage);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getErrorCode().getCode()).isEqualTo("EVENT_003");
    }

    @Test
    @DisplayName("원인 예외와 함께 예외를 생성할 수 있다.")
    void create_exception_with_cause() {
        // given
        EventErrorCode errorCode = EventErrorCode.EVENT_SERIALIZATION_FAILED;
        RuntimeException cause = new RuntimeException("JSON 파싱 실패");

        // when
        EventException exception = new EventException(errorCode, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("커스텀 메시지와 원인 예외를 함께 포함하여 예외를 생성할 수 있다.")
    void create_exception_with_message_and_cause() {
        // given
        EventErrorCode errorCode = EventErrorCode.EVENT_RETRY_FAILED;
        String customMessage = "3회 재시도 후에도 외부 API 호출 실패";
        Exception cause = new Exception("Connection timeout");

        // when
        EventException exception = new EventException(errorCode, customMessage, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getErrorCode().getCode()).isEqualTo("EVENT_004");
    }

    @Test
    @DisplayName("GlobalExceptionHandler에서 처리 가능하도록 ErrorCode를 반환한다.")
    void returns_error_code_for_handler() {
        // given
        EventException exception = new EventException(EventErrorCode.INVALID_EVENT);

        // when
        var errorCode = exception.getErrorCode();

        // then
        assertThat(errorCode.getCode()).isNotNull();
        assertThat(errorCode.getMessage()).isNotNull();
        assertThat(errorCode.getStatus()).isGreaterThan(0);
    }
}