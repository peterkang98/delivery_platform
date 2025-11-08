package xyz.sparta_project.manjok.domain.payment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentCancellation Entity 테스트")
class PaymentCancellationTest {

    @Nested
    @DisplayName("취소 생성 테스트")
    class CreateCancellation {

        @Test
        @DisplayName("정상적인 파라미터로 취소 생성 성공")
        void create_success() {
            // given
            String paymentId = "PAY-001";
            CancellationType cancellationType = CancellationType.USER_REQUEST;
            String reason = "고객 변심";
            String requestedBy = "USER-001";
            BigDecimal cancelAmount = BigDecimal.valueOf(10000);
            LocalDateTime cancelledAt = LocalDateTime.now();

            // when
            PaymentCancellation cancellation = PaymentCancellation.create(
                    paymentId, cancellationType, reason, requestedBy,
                    cancelAmount, cancelledAt
            );

            // then
            assertThat(cancellation.getId()).isNotNull();
            assertThat(cancellation.getId()).startsWith("PC-");
            assertThat(cancellation.getPaymentId()).isEqualTo(paymentId);
            assertThat(cancellation.getCancellationType()).isEqualTo(cancellationType);
            assertThat(cancellation.getReason()).isEqualTo(reason);
            assertThat(cancellation.getRequestedBy()).isEqualTo(requestedBy);
            assertThat(cancellation.getCancelAmount()).isEqualTo(cancelAmount);
            assertThat(cancellation.getCancelledAt()).isEqualTo(cancelledAt);
        }

        @Test
        @DisplayName("paymentId가 null이면 예외 발생")
        void create_fail_nullPaymentId() {
            // when / then
            assertThatThrownBy(() ->
                    PaymentCancellation.create(
                            null,
                            CancellationType.USER_REQUEST,
                            "취소 사유",
                            "USER-001",
                            BigDecimal.valueOf(10000),
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("paymentId가 빈 문자열이면 예외 발생")
        void create_fail_emptyPaymentId() {
            // when / then
            assertThatThrownBy(() ->
                    PaymentCancellation.create(
                            "",
                            CancellationType.USER_REQUEST,
                            "취소 사유",
                            "USER-001",
                            BigDecimal.valueOf(10000),
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("cancellationType이 null이면 예외 발생")
        void create_fail_nullCancellationType() {
            // when / then
            assertThatThrownBy(() ->
                    PaymentCancellation.create(
                            "PAY-001",
                            null,
                            "취소 사유",
                            "USER-001",
                            BigDecimal.valueOf(10000),
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("reason이 null이면 예외 발생")
        void create_fail_nullReason() {
            // when / then
            assertThatThrownBy(() ->
                    PaymentCancellation.create(
                            "PAY-001",
                            CancellationType.USER_REQUEST,
                            null,
                            "USER-001",
                            BigDecimal.valueOf(10000),
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("reason이 빈 문자열이면 예외 발생")
        void create_fail_emptyReason() {
            // when / then
            assertThatThrownBy(() ->
                    PaymentCancellation.create(
                            "PAY-001",
                            CancellationType.USER_REQUEST,
                            "",
                            "USER-001",
                            BigDecimal.valueOf(10000),
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("requestedBy가 null이면 예외 발생")
        void create_fail_nullRequestedBy() {
            // when / then
            assertThatThrownBy(() ->
                    PaymentCancellation.create(
                            "PAY-001",
                            CancellationType.USER_REQUEST,
                            "취소 사유",
                            null,
                            BigDecimal.valueOf(10000),
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("requestedBy가 빈 문자열이면 예외 발생")
        void create_fail_emptyRequestedBy() {
            // when / then
            assertThatThrownBy(() ->
                    PaymentCancellation.create(
                            "PAY-001",
                            CancellationType.USER_REQUEST,
                            "취소 사유",
                            "",
                            BigDecimal.valueOf(10000),
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("cancelAmount가 null이면 예외 발생")
        void create_fail_nullCancelAmount() {
            // when / then
            assertThatThrownBy(() ->
                    PaymentCancellation.create(
                            "PAY-001",
                            CancellationType.USER_REQUEST,
                            "취소 사유",
                            "USER-001",
                            null,
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }

        @Test
        @DisplayName("cancelAmount가 0 이하면 예외 발생")
        void create_fail_zeroOrNegativeCancelAmount() {
            // when / then
            assertThatThrownBy(() ->
                    PaymentCancellation.create(
                            "PAY-001",
                            CancellationType.USER_REQUEST,
                            "취소 사유",
                            "USER-001",
                            BigDecimal.ZERO,
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);

            assertThatThrownBy(() ->
                    PaymentCancellation.create(
                            "PAY-001",
                            CancellationType.USER_REQUEST,
                            "취소 사유",
                            "USER-001",
                            BigDecimal.valueOf(-1000),
                            LocalDateTime.now()
                    ))
                    .isInstanceOf(PaymentException.class);
        }
    }

    @Nested
    @DisplayName("취소 유형 확인 테스트")
    class CancellationTypeCheck {

        @Test
        @DisplayName("USER_REQUEST 타입은 사용자 취소로 판단")
        void isUserCancellation_true() {
            // given
            PaymentCancellation cancellation = PaymentCancellation.create(
                    "PAY-001",
                    CancellationType.USER_REQUEST,
                    "고객 변심",
                    "USER-001",
                    BigDecimal.valueOf(10000),
                    LocalDateTime.now()
            );

            // when / then
            assertThat(cancellation.isUserCancellation()).isTrue();
            assertThat(cancellation.isSystemCancellation()).isFalse();
            assertThat(cancellation.isAdminCancellation()).isFalse();
        }

        @Test
        @DisplayName("SYSTEM_ERROR 타입은 시스템 취소로 판단")
        void isSystemCancellation_true() {
            // given
            PaymentCancellation cancellation = PaymentCancellation.create(
                    "PAY-001",
                    CancellationType.SYSTEM_ERROR,
                    "시스템 오류",
                    "SYSTEM",
                    BigDecimal.valueOf(10000),
                    LocalDateTime.now()
            );

            // when / then
            assertThat(cancellation.isSystemCancellation()).isTrue();
            assertThat(cancellation.isUserCancellation()).isFalse();
            assertThat(cancellation.isAdminCancellation()).isFalse();
        }

        @Test
        @DisplayName("ADMIN_CANCEL 타입은 관리자 취소로 판단")
        void isAdminCancellation_true() {
            // given
            PaymentCancellation cancellation = PaymentCancellation.create(
                    "PAY-001",
                    CancellationType.ADMIN_CANCEL,
                    "관리자 강제 취소",
                    "ADMIN-001",
                    BigDecimal.valueOf(10000),
                    LocalDateTime.now()
            );

            // when / then
            assertThat(cancellation.isAdminCancellation()).isTrue();
            assertThat(cancellation.isUserCancellation()).isFalse();
            assertThat(cancellation.isSystemCancellation()).isFalse();
        }

        @Test
        @DisplayName("FRAUD_DETECTION 타입 취소 생성 가능")
        void fraudDetection_create() {
            // when
            PaymentCancellation cancellation = PaymentCancellation.create(
                    "PAY-001",
                    CancellationType.FRAUD_DETECTION,
                    "부정 거래 감지",
                    "SYSTEM",
                    BigDecimal.valueOf(10000),
                    LocalDateTime.now()
            );

            // then
            assertThat(cancellation.getCancellationType()).isEqualTo(CancellationType.FRAUD_DETECTION);
            assertThat(cancellation.isUserCancellation()).isFalse();
            assertThat(cancellation.isSystemCancellation()).isFalse();
            assertThat(cancellation.isAdminCancellation()).isFalse();
        }

        @Test
        @DisplayName("TIMEOUT 타입 취소 생성 가능")
        void timeout_create() {
            // when
            PaymentCancellation cancellation = PaymentCancellation.create(
                    "PAY-001",
                    CancellationType.TIMEOUT,
                    "결제 시간 초과",
                    "SYSTEM",
                    BigDecimal.valueOf(10000),
                    LocalDateTime.now()
            );

            // then
            assertThat(cancellation.getCancellationType()).isEqualTo(CancellationType.TIMEOUT);
        }
    }

    @Test
    @DisplayName("취소 ID는 'PC-'로 시작하는 UUID 형식이다")
    void cancellationId_format() {
        // when
        PaymentCancellation cancellation = PaymentCancellation.create(
                "PAY-001",
                CancellationType.USER_REQUEST,
                "취소",
                "USER-001",
                BigDecimal.valueOf(10000),
                LocalDateTime.now()
        );

        // then
        assertThat(cancellation.getId()).startsWith("PC-");
        assertThat(cancellation.getId()).hasSize(39); // "PC-" + UUID 길이
    }

    @Test
    @DisplayName("동일한 취소는 ID로 구분된다")
    void cancellation_equality() {
        // given
        PaymentCancellation cancellation1 = PaymentCancellation.create(
                "PAY-001",
                CancellationType.USER_REQUEST,
                "취소1",
                "USER-001",
                BigDecimal.valueOf(5000),
                LocalDateTime.now()
        );

        PaymentCancellation cancellation2 = PaymentCancellation.create(
                "PAY-001",
                CancellationType.USER_REQUEST,
                "취소2",
                "USER-001",
                BigDecimal.valueOf(5000),
                LocalDateTime.now()
        );

        // then - ID가 다르므로 다른 객체
        assertThat(cancellation1).isNotEqualTo(cancellation2);
        assertThat(cancellation1.getId()).isNotEqualTo(cancellation2.getId());
    }
}