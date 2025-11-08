package xyz.sparta_project.manjok.domain.payment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentStatus Enum 테스트")
class PaymentStatusTest {

    @Nested
    @DisplayName("Enum 값 확인")
    class EnumValues {

        @Test
        @DisplayName("모든 PaymentStatus 값이 존재한다")
        void allValuesExist() {
            PaymentStatus[] statuses = PaymentStatus.values();

            assertThat(statuses).hasSize(5);
            assertThat(statuses).containsExactlyInAnyOrder(
                    PaymentStatus.PENDING,
                    PaymentStatus.APPROVED,
                    PaymentStatus.FAILED,
                    PaymentStatus.PARTIALLY_CANCELLED,
                    PaymentStatus.CANCELLED
            );
        }

        @Test
        @DisplayName("각 상태는 올바른 한글 설명을 가진다")
        void descriptions() {
            assertThat(PaymentStatus.PENDING.getDescription()).isEqualTo("결제 대기");
            assertThat(PaymentStatus.APPROVED.getDescription()).isEqualTo("결제 승인");
            assertThat(PaymentStatus.FAILED.getDescription()).isEqualTo("결제 실패");
            assertThat(PaymentStatus.PARTIALLY_CANCELLED.getDescription()).isEqualTo("부분 취소");
            assertThat(PaymentStatus.CANCELLED.getDescription()).isEqualTo("전액 취소");
        }
    }

    @Nested
    @DisplayName("상태별 특성 확인")
    class StatusCharacteristics {

        @Test
        @DisplayName("PENDING은 초기 결제 대기 상태")
        void pending_characteristics() {
            PaymentStatus status = PaymentStatus.PENDING;

            assertThat(status.getDescription()).contains("대기");
        }

        @Test
        @DisplayName("APPROVED는 결제 승인 완료 상태")
        void approved_characteristics() {
            PaymentStatus status = PaymentStatus.APPROVED;

            assertThat(status.getDescription()).contains("승인");
        }

        @Test
        @DisplayName("FAILED는 결제 실패 상태")
        void failed_characteristics() {
            PaymentStatus status = PaymentStatus.FAILED;

            assertThat(status.getDescription()).contains("실패");
        }

        @Test
        @DisplayName("PARTIALLY_CANCELLED는 부분 취소 상태")
        void partiallyCancelled_characteristics() {
            PaymentStatus status = PaymentStatus.PARTIALLY_CANCELLED;

            assertThat(status.getDescription()).contains("부분");
            assertThat(status.getDescription()).contains("취소");
        }

        @Test
        @DisplayName("CANCELLED는 전액 취소 상태")
        void cancelled_characteristics() {
            PaymentStatus status = PaymentStatus.CANCELLED;

            assertThat(status.getDescription()).contains("전액");
            assertThat(status.getDescription()).contains("취소");
        }
    }

    @Nested
    @DisplayName("Enum 메서드 동작 확인")
    class EnumMethods {

        @Test
        @DisplayName("valueOf()로 문자열을 Enum으로 변환 가능")
        void valueOf_success() {
            PaymentStatus status = PaymentStatus.valueOf("PENDING");

            assertThat(status).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("존재하지 않는 값으로 valueOf() 호출 시 예외 발생")
        void valueOf_fail() {
            assertThatThrownBy(() ->
                    PaymentStatus.valueOf("INVALID_STATUS"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("name()으로 Enum 상수명 조회")
        void name_method() {
            assertThat(PaymentStatus.PENDING.name()).isEqualTo("PENDING");
            assertThat(PaymentStatus.APPROVED.name()).isEqualTo("APPROVED");
            assertThat(PaymentStatus.FAILED.name()).isEqualTo("FAILED");
            assertThat(PaymentStatus.PARTIALLY_CANCELLED.name()).isEqualTo("PARTIALLY_CANCELLED");
            assertThat(PaymentStatus.CANCELLED.name()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("ordinal()로 정의 순서 조회")
        void ordinal_method() {
            assertThat(PaymentStatus.PENDING.ordinal()).isEqualTo(0);
            assertThat(PaymentStatus.APPROVED.ordinal()).isEqualTo(1);
            assertThat(PaymentStatus.FAILED.ordinal()).isEqualTo(2);
            assertThat(PaymentStatus.PARTIALLY_CANCELLED.ordinal()).isEqualTo(3);
            assertThat(PaymentStatus.CANCELLED.ordinal()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("상태 비교 및 동등성")
    class StatusComparison {

        @Test
        @DisplayName("같은 상태는 동등하다")
        void equality() {
            PaymentStatus status1 = PaymentStatus.APPROVED;
            PaymentStatus status2 = PaymentStatus.APPROVED;

            assertThat(status1).isEqualTo(status2);
            assertThat(status1 == status2).isTrue();
        }

        @Test
        @DisplayName("다른 상태는 동등하지 않다")
        void inequality() {
            PaymentStatus status1 = PaymentStatus.APPROVED;
            PaymentStatus status2 = PaymentStatus.PENDING;

            assertThat(status1).isNotEqualTo(status2);
            assertThat(status1 == status2).isFalse();
        }

        @Test
        @DisplayName("switch 문에서 사용 가능")
        void switchStatement() {
            PaymentStatus status = PaymentStatus.APPROVED;

            String result = switch (status) {
                case PENDING -> "대기중";
                case APPROVED -> "승인됨";
                case FAILED -> "실패";
                case PARTIALLY_CANCELLED -> "부분취소";
                case CANCELLED -> "전체취소";
            };

            assertThat(result).isEqualTo("승인됨");
        }
    }

    @Nested
    @DisplayName("상태별 비즈니스 의미")
    class BusinessMeaning {

        @Test
        @DisplayName("취소 관련 상태 확인")
        void cancelledStates() {
            assertThat(PaymentStatus.PARTIALLY_CANCELLED.getDescription()).contains("취소");
            assertThat(PaymentStatus.CANCELLED.getDescription()).contains("취소");

            // PENDING, APPROVED, FAILED는 취소 상태가 아님
            assertThat(PaymentStatus.PENDING.getDescription()).doesNotContain("취소");
            assertThat(PaymentStatus.APPROVED.getDescription()).doesNotContain("취소");
            assertThat(PaymentStatus.FAILED.getDescription()).doesNotContain("취소");
        }

        @Test
        @DisplayName("정상 진행 상태 확인")
        void normalStates() {
            // PENDING과 APPROVED는 정상 진행 상태
            assertThat(PaymentStatus.PENDING.getDescription())
                    .doesNotContain("실패")
                    .doesNotContain("취소");
            assertThat(PaymentStatus.APPROVED.getDescription())
                    .doesNotContain("실패")
                    .doesNotContain("취소");
        }

        @Test
        @DisplayName("최종 상태 확인")
        void finalStates() {
            // FAILED와 CANCELLED는 최종 상태로 볼 수 있음
            assertThat(PaymentStatus.FAILED.getDescription()).contains("실패");
            assertThat(PaymentStatus.CANCELLED.getDescription()).contains("전액");
        }
    }

    @Test
    @DisplayName("결제 상태 흐름 시나리오")
    void paymentStatusFlow() {
        // 결제 생성 -> 대기
        PaymentStatus initial = PaymentStatus.PENDING;
        assertThat(initial).isEqualTo(PaymentStatus.PENDING);

        // 승인
        PaymentStatus approved = PaymentStatus.APPROVED;
        assertThat(approved).isEqualTo(PaymentStatus.APPROVED);

        // 부분 취소
        PaymentStatus partiallyCancelled = PaymentStatus.PARTIALLY_CANCELLED;
        assertThat(partiallyCancelled).isEqualTo(PaymentStatus.PARTIALLY_CANCELLED);

        // 전액 취소
        PaymentStatus cancelled = PaymentStatus.CANCELLED;
        assertThat(cancelled).isEqualTo(PaymentStatus.CANCELLED);

        // 모든 상태가 서로 다름
        assertThat(initial).isNotEqualTo(approved);
        assertThat(approved).isNotEqualTo(partiallyCancelled);
        assertThat(partiallyCancelled).isNotEqualTo(cancelled);
    }
}