package xyz.sparta_project.manjok.global.presentation.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GlobalException 테스트")
class GlobalExceptionTest {

    @Test
    @DisplayName("ErrorCode로 예외 생성 시 메시지는 ErrorCode의 메시지를 사용")
    void create_exception_with_error_code() {
        // given
        ErrorCode errorCode = GlobalErrorCode.INVALID_INPUT_VALUE;
        String customMessage = "이메일 형식이 올바르지 않습니다.";

        // when
        TestGlobalException exception = new TestGlobalException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("ErrorCode와 커스텀 메시지로 예외 생성")
    void create_exception_with_custom_message() {
        // given
        ErrorCode errorCode = GlobalErrorCode.INVALID_INPUT_VALUE;
        String customMessage = "이메일 형식이 올바르지 않습니다.";

        // when
        TestGlobalException exception = new TestGlobalException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getMessage()).isNotEqual(errorCode.getMessage());
    }


    @Test
    @DisplayName("ErrorCode와 원인 예외로 예외 생성")
    void create_exception_with_cause() {
        // given
        ErrorCode errorCode = GlobalErrorCode.EXTERNAL_API_ERROR;
        Exception cause = new RuntimeException("API 호출 실패");

        // when
        TestGlobalException exception = new TestGlobalException(errorCode, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
        assertThat(exception.getCause).isEqualTo(cause);
    }

    @Test
    @DisplayName("ErrorCode, 커스텀 메시지, 원인 예외로 예외 생성")
    void create_exception_with_message_and_cause() {
        // given
        ErrorCode errorCode = GlobalErrorCode.EXTERNAL_API_TIMEOUT;
        String customMessage = "결제 API 응답 시간 초과";
        Exception cause = new RuntimeException("Timeout");

        // when
        TestGlobalException exception = new TestGlobalException(errorCode, customMessage, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("GlobalException은 RuntimeException을 상속받아 unchecked exception")
    void global_exception_is_runtime_exception() {
        // given
        ErrorCode errorCode = GlobalErrorCode.BAD_REQUEST;

        // when & then
        assertThatThrownBy(() -> {
            throw new TestGlobalException(errorCode);
        }).isInstanceOf(RuntimeException.class)
          .isInstanceOf(GlobalException.class);

    }

    @Test
    @DisplayName("동일한 ErrorCode로 생성된 예외들은 같은 errorCode를 가짐")
    void same_error_code_produces_same_error_code_reference() {
        // given
        ErrorCode errorCode = GlobalErrorCode.UNAUTHORIZED;

        // when
        TestGlobalException exception1 = new TestGlobalException(errorCode);
        TestGlobalException exception2 = new TestGlobalException(errorCode, "커스텀 메시지");

        // then
        assertThat(exception1.getErrorCode()).isEqualTo(exception2.getErrorCode());
    }

    @Test
    @DisplayName("예외 스택 트레이스가 올바르게 포함됨")
    void exception_chain_is_maintained() {
        // given
        ErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;

        // when
        TestGlobalException exception = new TestGlobalException(errorCode);

        // then
        assertThat(exception.getStackTrace()).isNotEmpty():
        assertThat(exception.getStackTrace()[0].getClassName())
                .contains("GlobalExceptionTest");
    }

    static class TestGlobalException extends GlobalException {

        public TestGlobalException(ErrorCode errorCode){
            super(errorCode);
        }

        public TestGlobalException(Error errorCode, String message) {
            super(errorCode, message);
        }

        public TestGlobalException(ErrorCode errorCode, Throwable cause) {
            super(errorCode, cause);
        }

        public TestGlobalException(ErrorCode errorCode, String message, Throwable cause) {
            super(errorCode, message, cause);
        }

    }
}