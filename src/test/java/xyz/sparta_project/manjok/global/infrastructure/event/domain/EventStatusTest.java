package xyz.sparta_project.manjok.global.infrastructure.event.domain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EventStatus 테스트")
class EventStatusTest {

    @Test
    @DisplayName("EventStatus Enum 값이 정상적으로 매핑된다")
    void eventStatus_enum_values_are_valid() {
        // given & when
        EventStatus[] statuses = EventStatus.values();

        // then
        assertThat(statuses).containsExactly(
                EventStatus.PENDING,
                EventStatus.SUCCESS,
                EventStatus.FAILED,
                EventStatus.RETRYING,
                EventStatus.DEAD_LETTER
        );
    }

    @Test
    @DisplayName("문자열을 통해 Enum으로 변환할 수 있다.")
    void valueOf_string_to_enum() {
        // given
        String name = "SUCCESS";

        // when
        EventStatus status = EventStatus.valueOf(name);

        // then
        assertThat(status).isEqualTo(EventStatus.SUCCESS);
    }

    @Test
    @DisplayName("존재하지 않는 Enum 이름을 변환하면 예외가 발생한다.")
    void valueOf_invalid_string_throws_exception() {
        // given
        String invalid = "INVALID_STATUS";

        // when & then
        assertThatThrownBy(() -> EventStatus.valueOf(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant");
    }

    @Test
    @DisplayName("FAILED 상태는 재시도(RETRY)로 전이 가능하다.")
    void failed_can_transition_to_retrying() {
        // given
        EventStatus current = EventStatus.FAILED;

        // when
        boolean canRetry = current.canTransitionTo(EventStatus.RETRYING);
        boolean canFail = current.canTransitionTo(EventStatus.FAILED);

        // then
        assertThat(canRetry).isTrue();
        assertThat(canFail).isFalse();
    }

    @Test
    @DisplayName("RETRYING 상태에서 SUCCESS로 전이 가능하다.")
    void retrying_can_transition_to_success() {
        // given
        EventStatus current = EventStatus.RETRYING;

        // when
        boolean canSuccess = current.canTransitionTo(EventStatus.SUCCESS);

        // then
        assertThat(canSuccess).isTrue();
    }

}