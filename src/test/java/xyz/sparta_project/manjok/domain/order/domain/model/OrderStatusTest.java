package xyz.sparta_project.manjok.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderStatus Enum 테스트")
class OrderStatusTest {

    @Nested
    @DisplayName("validateTransition() 상태 전이 검증")
    class ValidateTransition {

        @Test
        @DisplayName("정상적인 순차 전이는 성공한다 (PAYMENT_PENDING → PAYMENT_COMPLETED)")
        void validTransition_success() {
            // given
            OrderStatus current = OrderStatus.PAYMENT_PENDING;
            OrderStatus next = OrderStatus.PAYMENT_COMPLETED;

            // when / then
            assertThatCode(() -> current.validateTransition(next))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("잘못된 순차 전이는 예외가 발생한다 (PAYMENT_PENDING → PREPARING)")
        void invalidTransition_fail() {
            // given
            OrderStatus current = OrderStatus.PAYMENT_PENDING;
            OrderStatus next = OrderStatus.PREPARING;

            // when / then
            assertThatThrownBy(() -> current.validateTransition(next))
                    .isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("취소 가능한 상태에서는 CANCELED 로 전환 가능하다")
        void cancelableStates_canCancel() {
            OrderStatus current = OrderStatus.PENDING;

            assertThatCode(() -> current.validateTransition(OrderStatus.CANCELED))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("취소 불가능한 상태에서는 CANCELED 전환 시 예외 발생 (DELIVERING → CANCELED 불가)")
        void notCancelableStates_failCancel() {
            OrderStatus current = OrderStatus.DELIVERING;

            assertThatThrownBy(() -> current.validateTransition(OrderStatus.CANCELED))
                    .isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("isPaymentCompleted() 결제 완료 상태 확인")
    class PaymentCompletedCheck {

        @Test
        @DisplayName("결제 완료 이후 상태들은 true 를 반환한다")
        void returnTrueForCompletedStates() {
            assertThat(OrderStatus.PAYMENT_COMPLETED.isPaymentCompleted()).isTrue();
            assertThat(OrderStatus.PENDING.isPaymentCompleted()).isTrue();
            assertThat(OrderStatus.CONFIRMED.isPaymentCompleted()).isTrue();
            assertThat(OrderStatus.PREPARING.isPaymentCompleted()).isTrue();
            assertThat(OrderStatus.DELIVERING.isPaymentCompleted()).isTrue();
            assertThat(OrderStatus.COMPLETED.isPaymentCompleted()).isTrue();
        }

        @Test
        @DisplayName("결제 대기 및 취소 상태는 false")
        void returnFalseForOthers() {
            assertThat(OrderStatus.PAYMENT_PENDING.isPaymentCompleted()).isFalse();
            assertThat(OrderStatus.CANCELED.isPaymentCompleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("isCancelable() 취소 가능 여부")
    class CancelableCheck {

        @Test
        @DisplayName("초기 ~ 조리중 이전까지는 취소 가능")
        void cancelableStates() {
            assertThat(OrderStatus.PAYMENT_PENDING.isCancelable()).isTrue();
            assertThat(OrderStatus.PAYMENT_COMPLETED.isCancelable()).isTrue();
            assertThat(OrderStatus.PENDING.isCancelable()).isTrue();
            assertThat(OrderStatus.CONFIRMED.isCancelable()).isTrue();
            assertThat(OrderStatus.PREPARING.isCancelable()).isTrue();
        }

        @Test
        @DisplayName("배달 중 / 완료 / 취소 후는 취소 불가")
        void notCancelableStates() {
            assertThat(OrderStatus.DELIVERING.isCancelable()).isFalse();
            assertThat(OrderStatus.COMPLETED.isCancelable()).isFalse();
            assertThat(OrderStatus.CANCELED.isCancelable()).isFalse();
        }
    }

    @Nested
    @DisplayName("최종 상태 및 진행 상태 확인")
    class StatusProgressCheck {

        @Test
        @DisplayName("완료 및 취소는 최종 상태")
        void finalStatus() {
            assertThat(OrderStatus.COMPLETED.isFinalStatus()).isTrue();
            assertThat(OrderStatus.CANCELED.isFinalStatus()).isTrue();
        }

        @Test
        @DisplayName("그 외 상태는 진행 상태")
        void inProgress() {
            assertThat(OrderStatus.PENDING.isInProgress()).isTrue();
            assertThat(OrderStatus.DELIVERING.isInProgress()).isTrue();
            assertThat(OrderStatus.COMPLETED.isInProgress()).isFalse();
            assertThat(OrderStatus.CANCELED.isInProgress()).isFalse();
        }
    }

    @Nested
    @DisplayName("isRestaurantProcessable() 가게 처리 가능 상태")
    class RestaurantProcessableCheck {

        @Test
        @DisplayName("대기, 확인, 조리 중 상태만 가게 처리 대상")
        void processableStates() {
            assertThat(OrderStatus.PENDING.isRestaurantProcessable()).isTrue();
            assertThat(OrderStatus.CONFIRMED.isRestaurantProcessable()).isTrue();
            assertThat(OrderStatus.PREPARING.isRestaurantProcessable()).isTrue();
        }

        @Test
        @DisplayName("그 외 상태는 처리 불가")
        void notProcessableStates() {
            assertThat(OrderStatus.DELIVERING.isRestaurantProcessable()).isFalse();
            assertThat(OrderStatus.COMPLETED.isRestaurantProcessable()).isFalse();
            assertThat(OrderStatus.CANCELED.isRestaurantProcessable()).isFalse();
        }
    }
}
