package xyz.sparta_project.manjok.domain.payment.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payment Aggregate Root 테스트")
class PaymentTest {

    private String orderId;
    private String ordererId;
    private String tossPaymentKey;
    private String payToken;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String createdBy;
    private LocalDateTime createdAt;

    @BeforeEach
    void setup() {
        orderId = "ORD-001";
        ordererId = "USER-001";
        tossPaymentKey = "TOSS-KEY-001";
        payToken = "PAY-TOKEN-001";
        amount = BigDecimal.valueOf(10000);
        paymentMethod = PaymentMethod.CARD;
        createdBy = "system";
        createdAt = LocalDateTime.now();
    }

    @Nested
    @DisplayName("결제 생성 테스트")
    class CreatePayment {

        @Test
        @DisplayName("정상적인 파라미터로 결제 생성 시 초기 상태가 PENDING이다")
        void create_success() {
            // when
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );

            // then
            assertThat(payment.getOrderId()).isEqualTo(orderId);
            assertThat(payment.getOrdererId()).isEqualTo(ordererId);
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getAmount()).isEqualTo(amount);
            assertThat(payment.getPaymentMethod()).isEqualTo(paymentMethod);
            assertThat(payment.getIsDeleted()).isFalse();
            assertThat(payment.getCancellations()).isEmpty();
        }

        @Test
        @DisplayName("orderId가 null이면 예외 발생")
        void create_fail_nullOrderId() {
            // when / then
            assertThatThrownBy(() ->
                    Payment.create(null, ordererId, tossPaymentKey, payToken,
                            amount, paymentMethod, createdBy, createdAt))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("orderId가 빈 문자열이면 예외 발생")
        void create_fail_emptyOrderId() {
            // when / then
            assertThatThrownBy(() ->
                    Payment.create("", ordererId, tossPaymentKey, payToken,
                            amount, paymentMethod, createdBy, createdAt))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("ordererId가 null이면 예외 발생")
        void create_fail_nullOrdererId() {
            // when / then
            assertThatThrownBy(() ->
                    Payment.create(orderId, null, tossPaymentKey, payToken,
                            amount, paymentMethod, createdBy, createdAt))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("tossPaymentKey가 null이면 예외 발생")
        void create_fail_nullTossPaymentKey() {
            // when / then
            assertThatThrownBy(() ->
                    Payment.create(orderId, ordererId, null, payToken,
                            amount, paymentMethod, createdBy, createdAt))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("payToken이 null이면 예외 발생")
        void create_fail_nullPayToken() {
            // when / then
            assertThatThrownBy(() ->
                    Payment.create(orderId, ordererId, tossPaymentKey, null,
                            amount, paymentMethod, createdBy, createdAt))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("amount가 null이면 예외 발생")
        void create_fail_nullAmount() {
            // when / then
            assertThatThrownBy(() ->
                    Payment.create(orderId, ordererId, tossPaymentKey, payToken,
                            null, paymentMethod, createdBy, createdAt))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("amount가 0 이하면 예외 발생")
        void create_fail_zeroOrNegativeAmount() {
            // when / then
            assertThatThrownBy(() ->
                    Payment.create(orderId, ordererId, tossPaymentKey, payToken,
                            BigDecimal.ZERO, paymentMethod, createdBy, createdAt))
                    .isInstanceOf(PaymentException.class);

            assertThatThrownBy(() ->
                    Payment.create(orderId, ordererId, tossPaymentKey, payToken,
                            BigDecimal.valueOf(-1000), paymentMethod, createdBy, createdAt))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("paymentMethod가 null이면 예외 발생")
        void create_fail_nullPaymentMethod() {
            // when / then
            assertThatThrownBy(() ->
                    Payment.create(orderId, ordererId, tossPaymentKey, payToken,
                            amount, null, createdBy, createdAt))
                    .isInstanceOf(PaymentException.class);
        }
    }

    @Nested
    @DisplayName("결제 승인 처리")
    class ApprovePayment {

        @Test
        @DisplayName("PENDING 상태에서만 결제 승인이 가능하다")
        void approve_success() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            LocalDateTime approvedAt = LocalDateTime.now();

            // when
            payment.approve(approvedAt, "approver");

            // then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getApprovedAt()).isEqualTo(approvedAt);
        }

        @Test
        @DisplayName("PENDING이 아닌 상태에서 승인하면 예외 발생")
        void approve_fail_notPending() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");

            // when / then
            assertThatThrownBy(() ->
                    payment.approve(LocalDateTime.now(), "approver"))
                    .isInstanceOf(PaymentException.class);
        }
    }

    @Nested
    @DisplayName("결제 실패 처리")
    class FailPayment {

        @Test
        @DisplayName("PENDING 상태에서만 결제 실패 처리가 가능하다")
        void fail_success() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );

            // when
            payment.fail("system");

            // then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("PENDING이 아닌 상태에서 실패 처리하면 예외 발생")
        void fail_fail_notPending() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");

            // when / then
            assertThatThrownBy(() ->
                    payment.fail("system"))
                    .isInstanceOf(PaymentException.class);
        }
    }

    @Nested
    @DisplayName("결제 취소 추가")
    class AddCancellation {

        @Test
        @DisplayName("APPROVED 상태에서 전액 취소 시 상태가 CANCELLED로 변경된다")
        void addCancellation_fullCancel_success() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");

            // when
            payment.addCancellation(
                    CancellationType.USER_REQUEST,
                    "고객 변심",
                    "USER-001",
                    amount,
                    LocalDateTime.now()
            );

            // then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(payment.getCancellations()).hasSize(1);
            assertThat(payment.getTotalCancelledAmount()).isEqualTo(amount);
            assertThat(payment.getRemainingAmount()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("APPROVED 상태에서 부분 취소 시 상태가 PARTIALLY_CANCELLED로 변경된다")
        void addCancellation_partialCancel_success() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");

            BigDecimal partialAmount = BigDecimal.valueOf(3000);

            // when
            payment.addCancellation(
                    CancellationType.USER_REQUEST,
                    "부분 환불",
                    "USER-001",
                    partialAmount,
                    LocalDateTime.now()
            );

            // then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELLED);
            assertThat(payment.getCancellations()).hasSize(1);
            assertThat(payment.getTotalCancelledAmount()).isEqualTo(partialAmount);
            assertThat(payment.getRemainingAmount()).isEqualTo(amount.subtract(partialAmount));
        }

        @Test
        @DisplayName("PARTIALLY_CANCELLED 상태에서 추가 취소 가능")
        void addCancellation_afterPartialCancel_success() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");
            payment.addCancellation(
                    CancellationType.USER_REQUEST,
                    "부분 환불",
                    "USER-001",
                    BigDecimal.valueOf(3000),
                    LocalDateTime.now()
            );

            // when
            payment.addCancellation(
                    CancellationType.USER_REQUEST,
                    "추가 환불",
                    "USER-001",
                    BigDecimal.valueOf(2000),
                    LocalDateTime.now()
            );

            // then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELLED);
            assertThat(payment.getCancellations()).hasSize(2);
            assertThat(payment.getTotalCancelledAmount()).isEqualTo(BigDecimal.valueOf(5000));
        }

        @Test
        @DisplayName("취소 불가능한 상태에서 취소 시 예외 발생")
        void addCancellation_fail_notCancellable() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );

            // when / then - PENDING 상태에서 취소 불가
            assertThatThrownBy(() ->
                    payment.addCancellation(
                            CancellationType.USER_REQUEST,
                            "취소",
                            "USER-001",
                            amount,
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("취소 금액이 null이면 예외 발생")
        void addCancellation_fail_nullAmount() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");

            // when / then
            assertThatThrownBy(() ->
                    payment.addCancellation(
                            CancellationType.USER_REQUEST,
                            "취소",
                            "USER-001",
                            null,
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("취소 금액이 0 이하면 예외 발생")
        void addCancellation_fail_zeroOrNegativeAmount() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");

            // when / then
            assertThatThrownBy(() ->
                    payment.addCancellation(
                            CancellationType.USER_REQUEST,
                            "취소",
                            "USER-001",
                            BigDecimal.ZERO,
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("취소 금액이 남은 금액을 초과하면 예외 발생")
        void addCancellation_fail_exceedsRemaining() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");
            payment.addCancellation(
                    CancellationType.USER_REQUEST,
                    "부분 취소",
                    "USER-001",
                    BigDecimal.valueOf(3000),
                    LocalDateTime.now()
            );

            // when / then
            assertThatThrownBy(() ->
                    payment.addCancellation(
                            CancellationType.USER_REQUEST,
                            "초과 취소",
                            "USER-001",
                            BigDecimal.valueOf(8000), // 남은 금액 7000 초과
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("취소 사유가 null이면 예외 발생")
        void addCancellation_fail_nullReason() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");

            // when / then
            assertThatThrownBy(() ->
                    payment.addCancellation(
                            CancellationType.USER_REQUEST,
                            null,
                            "USER-001",
                            amount,
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("취소 사유가 빈 문자열이면 예외 발생")
        void addCancellation_fail_emptyReason() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");

            // when / then
            assertThatThrownBy(() ->
                    payment.addCancellation(
                            CancellationType.USER_REQUEST,
                            "",
                            "USER-001",
                            amount,
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("취소 사유가 500자를 초과하면 예외 발생")
        void addCancellation_fail_reasonTooLong() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");
            String longReason = "a".repeat(501);

            // when / then
            assertThatThrownBy(() ->
                    payment.addCancellation(
                            CancellationType.USER_REQUEST,
                            longReason,
                            "USER-001",
                            amount,
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }
    }

    @Nested
    @DisplayName("결제 금액 검증")
    class ValidatePaymentAmount {

        @Test
        @DisplayName("결제 금액과 주문 금액이 일치하면 검증 성공")
        void validatePaymentAmount_success() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );

            // when / then
            assertThatCode(() ->
                    payment.validatePaymentAmount(amount))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("결제 금액과 주문 금액이 다르면 예외 발생")
        void validatePaymentAmount_fail() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );

            // when / then
            assertThatThrownBy(() ->
                    payment.validatePaymentAmount(BigDecimal.valueOf(15000)))
                    .isInstanceOf(PaymentException.class);
        }
    }

    @Nested
    @DisplayName("결제자 검증")
    class IsPaidBy {

        @Test
        @DisplayName("결제자와 사용자 ID가 일치하면 true 반환")
        void isPaidBy_true() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );

            // when / then
            assertThat(payment.isPaidBy("USER-001")).isTrue();
        }

        @Test
        @DisplayName("결제자와 사용자 ID가 다르면 false 반환")
        void isPaidBy_false() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );

            // when / then
            assertThat(payment.isPaidBy("USER-002")).isFalse();
        }
    }

    @Nested
    @DisplayName("소프트 삭제")
    class SoftDelete {

        @Test
        @DisplayName("CANCELLED 상태의 결제는 소프트 삭제 가능")
        void softDelete_cancelled_success() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");
            payment.addCancellation(
                    CancellationType.USER_REQUEST,
                    "취소",
                    "USER-001",
                    amount,
                    LocalDateTime.now()
            );

            // when
            payment.softDelete("deleter", LocalDateTime.now());

            // then
            assertThat(payment.getIsDeleted()).isTrue();
            assertThat(payment.getDeletedBy()).isEqualTo("deleter");
            assertThat(payment.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("FAILED 상태의 결제는 소프트 삭제 가능")
        void softDelete_failed_success() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.fail("system");

            // when
            payment.softDelete("deleter", LocalDateTime.now());

            // then
            assertThat(payment.getIsDeleted()).isTrue();
        }

        @Test
        @DisplayName("APPROVED 상태의 결제는 소프트 삭제 불가")
        void softDelete_approved_fail() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");

            // when / then
            assertThatThrownBy(() ->
                    payment.softDelete("deleter", LocalDateTime.now()))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("PENDING 상태의 결제는 소프트 삭제 불가")
        void softDelete_pending_fail() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );

            // when / then
            assertThatThrownBy(() ->
                    payment.softDelete("deleter", LocalDateTime.now()))
                    .isInstanceOf(PaymentException.class);
        }
    }

    @Nested
    @DisplayName("취소 가능 여부 확인")
    class IsCancellable {

        @Test
        @DisplayName("APPROVED 상태는 취소 가능")
        void isCancellable_approved() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");

            // when / then
            assertThat(payment.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_CANCELLED 상태는 취소 가능")
        void isCancellable_partiallyCancelled() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");
            payment.addCancellation(
                    CancellationType.USER_REQUEST,
                    "부분 취소",
                    "USER-001",
                    BigDecimal.valueOf(3000),
                    LocalDateTime.now()
            );

            // when / then
            assertThat(payment.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("PENDING 상태는 취소 불가")
        void isCancellable_pending() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );

            // when / then
            assertThat(payment.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("FAILED 상태는 취소 불가")
        void isCancellable_failed() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.fail("system");

            // when / then
            assertThat(payment.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED 상태는 취소 불가")
        void isCancellable_cancelled() {
            // given
            Payment payment = Payment.create(
                    orderId, ordererId, tossPaymentKey, payToken,
                    amount, paymentMethod, createdBy, createdAt
            );
            payment.approve(LocalDateTime.now(), "approver");
            payment.addCancellation(
                    CancellationType.USER_REQUEST,
                    "전액 취소",
                    "USER-001",
                    amount,
                    LocalDateTime.now()
            );

            // when / then
            assertThat(payment.isCancellable()).isFalse();
        }
    }

    @Test
    @DisplayName("결제 전체 흐름 - 승인부터 부분 취소, 전액 취소까지")
    void fullPaymentFlow() {
        // 결제 생성
        Payment payment = Payment.create(
                orderId, ordererId, tossPaymentKey, payToken,
                amount, paymentMethod, createdBy, createdAt
        );
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);

        // 결제 승인
        payment.approve(LocalDateTime.now(), "approver");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);

        // 부분 취소 1
        payment.addCancellation(
                CancellationType.USER_REQUEST,
                "부분 환불 1",
                "USER-001",
                BigDecimal.valueOf(3000),
                LocalDateTime.now()
        );
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELLED);
        assertThat(payment.getRemainingAmount()).isEqualTo(BigDecimal.valueOf(7000));

        // 부분 취소 2
        payment.addCancellation(
                CancellationType.USER_REQUEST,
                "부분 환불 2",
                "USER-001",
                BigDecimal.valueOf(2000),
                LocalDateTime.now()
        );
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELLED);
        assertThat(payment.getRemainingAmount()).isEqualTo(BigDecimal.valueOf(5000));

        // 전액 취소
        payment.addCancellation(
                CancellationType.ADMIN_CANCEL,
                "관리자 전액 취소",
                "ADMIN-001",
                BigDecimal.valueOf(5000),
                LocalDateTime.now()
        );
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getRemainingAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(payment.getCancellations()).hasSize(3);
    }
}