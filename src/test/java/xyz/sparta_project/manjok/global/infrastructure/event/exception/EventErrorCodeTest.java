package xyz.sparta_project.manjok.global.infrastructure.event.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventErrorCode 테스트")
class EventErrorCodeTest {

    @Test
    @DisplayName("모든 에러코드가 정의되어 있다.")
    void all_error_codes_are_defined() {
        // given & when
        EventErrorCode[] errorCodes = EventErrorCode.values();

        // then
        assertThat(errorCodes).containsExactly(
                EventErrorCode.EVENT_PUBLISHER_NOT_INITIALIZED,
                EventErrorCode.EVENT_SERIALIZATION_FAILED,
                EventErrorCode.EVENT_PROCESSING_FAILED,
                EventErrorCode.EVENT_RETRY_FAILED,
                EventErrorCode.INVALID_EVENT,
                EventErrorCode.EVENT_NOT_FOUND
        );
    }

    @Test
    @DisplayName("각 에러 코드는 code, messagem status를 가지고 있다.")
    void each_error_code_has_message() {
        // given & when & then
        for (EventErrorCode errorCode : EventErrorCode.values()) {
            assertThat(errorCode.getCode())
                    .isNotNull()
                    .isNotBlank()
                    .startsWith("EVENT_");

            assertThat(errorCode.getMessage())
                    .isNotNull()
                    .isNotBlank();

            assertThat(errorCode.getStatus())
                    .isGreaterThanOrEqualTo(400)
                    .isLessThan(600);
        }
    }

    @Test
    @DisplayName("ErrorCode 인터페이스를 구현한다.")
    void implements_error_code_interface() {
        // given
        EventErrorCode errorCode = EventErrorCode.EVENT_PUBLISHER_NOT_INITIALIZED;

        // when & then
        assertThat(errorCode).isInstanceOf(ErrorCode.class);
    }

    @Test
    @DisplayName("500번대 에러 코드들은 올바른 상태 코드를 가진다.")
    void server_error_codes_have_correct_status() {
        //given & when & then
        assertThat(EventErrorCode.EVENT_PUBLISHER_NOT_INITIALIZED.getStatus()).isEqualTo(500);
        assertThat(EventErrorCode.EVENT_SERIALIZATION_FAILED.getStatus()).isEqualTo(500);
        assertThat(EventErrorCode.EVENT_PROCESSING_FAILED.getStatus()).isEqualTo(500);
        assertThat(EventErrorCode.EVENT_RETRY_FAILED.getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("400번대 에러 코드들은 올바른 상태 코드를 가진다.")
    void client_error_codes_have_correct_status() {
        // given & when & then
        assertThat(EventErrorCode.INVALID_EVENT.getStatus()).isEqualTo(400);
        assertThat(EventErrorCode.EVENT_NOT_FOUND.getStatus()).isEqualTo(404);
    }
}