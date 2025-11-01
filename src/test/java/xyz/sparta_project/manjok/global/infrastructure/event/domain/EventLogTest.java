package xyz.sparta_project.manjok.global.infrastructure.event.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EventLog 테스트")
class EventLogTest {

    @Test
    @DisplayName("이벤트 생성 시 필수 필드가 정상적으로 초기화된다.")
    void createEventLog_success() {
        // given
        String eventName = "OrderCompleted";
        String payload = "{\"orderId\":\"1234\"}";

        // when
        EventLog log = EventLog.of(eventName, payload);

        // then
        assertThat(log.getEventName()).isEqualTo(eventName);
        assertThat(log.getPayload()).isEqualTo(payload);
        assertThat(log.getStatus()).isEqualTo(EventStatus.PENDING);
        assertThat(log.getCreatedAt()).isNotNull();
        assertThat(log.getRetryCount()).isZero();
    }

    @Test
    @DisplayName("이벤트 이름이 null이면 예외가 발생한다.")
    void createEventLog_fail_null_eventName() {
        // given
        String payload = "{\"id\":\"1\"}";

        // when & then
        assertThatThrownBy(() -> EventLog.of(null, payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이벤트 이름은 필수 입력 값입니다.");
    }

    @Test
    @DisplayName("이벤트 페이로드가 null이면 예외가 발생한다.")
    void createEventLog_fail_null_payload() {
        // given
        String eventName = "PaymentCreated";

        // when & then
        assertThatThrownBy(() -> EventLog.of(eventName, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이벤트 페이로드는 필수 입력 값입니다.");
    }

    @Test
    @DisplayName("이벤트 상태를 업데이트할 수 있다.")
    void updateEventStatus_sucess() {
        // given
        EventLog log = EventLog.of("OrderCreated", "{}");

        // when
        log.updateStatus(EventStatus.SUCCESS);

        // then
        assertThat(log.getStatus()).isEqualTo(EventStatus.SUCCESS);
        assertThat(log.getUpdatedAt()).isAfterOrEqualTo(log.getCreatedAt());
    }

    @Test
    @DisplayName("재시도 시 retryCount가 1 증가한다.")
    void increaseRetryCount_success() {
        // given
        EventLog log = EventLog.of("OrderCreated", "{}");

        // when
        log.increaseRetryCount();

        // then
        assertThat(log.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("재시도 횟수가 음수일 수 없다.")
    void retryCount_cannot_be_negative() {
        // given
        EventLog log = EventLog.of("OrderCreated", "{}");

        // when & then
        assertThatThrownBy(() -> log.setRetryCount(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재시도 횟수는 음수가 될 수 없습니다.");
    }
}