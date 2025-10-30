package xyz.sparta_project.manjok.global.presentation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalErrorCode;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorResponse 테스트")
class ErrorResponseTest {
    
    @Test
    @DisplayName("ErrorCode로 ErrorResponse 생성")
    void create_error_response_from_error_code() {
        // given
        GlobalErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        String path = "/api/test";

        // when
        ErrorResponse response = ErrorResponse.of(errorCode, path);

        //then
        assertThat(response.getCode()).isEqualTo("GLOBAL_001");
        assertThat(response.getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getPath()).isEqualTo(path);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("ErrorCode와 커스텀 메시지로 ErrorResponse 생성")
    void create_error_response_with_custom_message() {
        // given
        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_INPUT_VALUE;
        String customMessage = "이메일 형식이 올바르지 않습니다.";
        String path = "/api/users";

        // when
        ErrorResponse response = ErrorResponse.of(errorCode, customMessage, path);

        // then
        assertThat(response.getCode()).isEqualTo("GLOBAL_101");
        assertThat(response.getMessage()).isEqualTo(customMessage);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getPath()).isEqualTo(path);
    }

    @Test
    @DisplayName("직접 파라미터로 ErrorResponse 생성")
    void create_error_response_with_parameters() {
        // given
        String code = "CUSTOM_001";
        String message = "커스텀 에러 메시지";
        int status = 400;
        String path = "/api/custom";

        // when
        ErrorResponse response = ErrorResponse.of(code, message, status, path);

        //then
        assertThat(response.getCode()).isEqualTo(code);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getPath()).isEqualTo(path);
    }

    @Test
    @DisplayName("timestamp는 현재 시간(밀리초)으로 설정됨")
    void timestamp_is_set_to_current_time_millis() {
        // given
        long before = System.currentTimeMillis();

        //When
        ErrorResponse response = ErrorResponse.of(
                GlobalErrorCode.BAD_REQUEST,
                "/api/test"
        );

        long after = System.currentTimeMillis();

        //then
        assertThat(response.getTimestamp()).isBetween(before, after);
    }

    @Test
    @DisplayName("동일한 ErrorCode로 생성된 응답들은 같은 code와 status를 가짐")
    void same_error_code_produces_same_code_and_status() {
        // given
        GlobalErrorCode errorCode = GlobalErrorCode.UNAUTHORIZED;

        // when
        ErrorResponse response1 = ErrorResponse.of(errorCode, "/api/path1");
        ErrorResponse response2 = ErrorResponse.of(errorCode, "/api/path2");

        // then
        assertThat(response1.getCode()).isEqualTo(response2.getCode());
        assertThat(response1.getStatus()).isEqualTo(response2.getStatus());
        assertThat(response1.getMessage()).isEqualTo(response2.getMessage());
    }

    @Test
    @DisplayName("커스텀 메시지는 ErrorCode의 기본 메시지를 덮어씀")
    void custom_message_overrides_default_message() {
        // given
        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_INPUT_VALUE;
        String defaultMessage = errorCode.getMessage();
        String customMessage = "커스텀 에러 메시지";

        // when
        ErrorResponse withDefaultMessage = ErrorResponse.of(errorCode, "/api/test1");
        ErrorResponse withCustomMessage = ErrorResponse.of(errorCode, customMessage, "/api/test1");

        // then
        assertThat(withDefaultMessage.getMessage()).isEqualTo(defaultMessage);
        assertThat(withCustomMessage.getMessage()).isEqualTo(customMessage);
        assertThat(withCustomMessage.getMessage()).isNotEqualTo(defaultMessage);
    }

}