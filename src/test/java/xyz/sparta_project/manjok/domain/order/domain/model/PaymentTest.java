package xyz.sparta_project.manjok.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payment Value Object 테스트")
class PaymentTest {

    @Nested
    @DisplayName("create() - 결제 정보 생성")
    class CreatePayment {

        @Test
        @DisplayName("정상적인 값으로 Payment 생성에 성공한다")
        void create_success() {
            // given
            String paymentId = "PAY001";

            // when
            Payment payment = Payment.create(paymentId, true);

            // then
            assertThat(payment.getPaymentId()).isEqualTo(paymentId);
            assertThat(payment.getIsPaid()).isTrue();
        }

        @Test
        @DisplayName("paymentId 가 null 또는 공백이면 예외가 발생한다")
        void create_fail_invalidPaymentId() {
            assertThatThrownBy(() ->
                    Payment.create("   ", true)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("isPaid 가 null 이면 예외가 발생한다")
        void create_fail_isPaidNull() {
            assertThatThrownBy(() ->
                    Payment.create("PAY001", null)
            ).isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("createPending() - 미결제 상태 생성")
    class CreatePendingPayment {

        @Test
        @DisplayName("미결제 상태로 Payment 생성 시 isPaid = false 이다")
        void createPending_success() {
            // when
            Payment payment = Payment.createPending("PAY002");

            // then
            assertThat(payment.getPaymentId()).isEqualTo("PAY002");
            assertThat(payment.getIsPaid()).isFalse();
        }
    }

    @Nested
    @DisplayName("isCompleted() - 결제 완료 여부 확인")
    class PaymentCompletionCheck {

        @Test
        @DisplayName("isPaid = true 이면 결제 완료 상태이다")
        void isCompleted_true() {
            Payment payment = Payment.create("PAY003", true);
            assertThat(payment.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isPaid = false 이면 결제 미완료 상태이다")
        void isCompleted_false() {
            Payment payment = Payment.create("PAY004", false);
            assertThat(payment.isCompleted()).isFalse();
        }
    }
}
