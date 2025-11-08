package xyz.sparta_project.manjok.domain.payment.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.payment.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentEntity 변환 테스트")
class PaymentEntityTest {

    @Test
    @DisplayName("도메인 -> 엔티티 변환 성공")
    void from_domain_to_entity() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Payment domain = Payment.builder()
                .id("PAY-001")
                .orderId("ORD-001")
                .ordererId("USER-001")
                .tossPaymentKey("TOSS-KEY-001")
                .payToken("PAY-TOKEN-001")
                .amount(BigDecimal.valueOf(10000))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.APPROVED)
                .approvedAt(now)
                .cancellations(List.of())
                .createdAt(now)
                .createdBy("system")
                .updatedAt(now)
                .updatedBy("system")
                .isDeleted(false)
                .deletedAt(null)
                .deletedBy(null)
                .build();

        // when
        PaymentEntity entity = PaymentEntity.from(domain);

        // then
        assertThat(entity.getOrderId()).isEqualTo("ORD-001");
        assertThat(entity.getOrdererId()).isEqualTo("USER-001");
        assertThat(entity.getTossPaymentKey()).isEqualTo("TOSS-KEY-001");
        assertThat(entity.getPayToken()).isEqualTo("PAY-TOKEN-001");
        assertThat(entity.getAmount()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(entity.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(entity.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(entity.getApprovedAt()).isEqualTo(now);
        assertThat(entity.getCancellations()).isEmpty();
        assertThat(entity.getCreatedBy()).isEqualTo("system");
        assertThat(entity.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("취소 내역이 있는 도메인 -> 엔티티 변환 성공")
    void from_domain_with_cancellations_to_entity() {
        // given
        LocalDateTime now = LocalDateTime.now();
        PaymentCancellation cancellation = PaymentCancellation.builder()
                .id("PC-001")
                .paymentId("PAY-001")
                .cancellationType(CancellationType.USER_REQUEST)
                .reason("고객 변심")
                .requestedBy("USER-001")
                .cancelAmount(BigDecimal.valueOf(3000))
                .cancelledAt(now)
                .build();

        Payment domain = Payment.builder()
                .id("PAY-001")
                .orderId("ORD-001")
                .ordererId("USER-001")
                .tossPaymentKey("TOSS-KEY-001")
                .payToken("PAY-TOKEN-001")
                .amount(BigDecimal.valueOf(10000))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.PARTIALLY_CANCELLED)
                .approvedAt(now)
                .cancellations(List.of(cancellation))
                .createdAt(now)
                .createdBy("system")
                .updatedAt(now)
                .updatedBy("system")
                .isDeleted(false)
                .build();

        // when
        PaymentEntity entity = PaymentEntity.from(domain);

        // then
        assertThat(entity.getCancellations()).hasSize(1);
        assertThat(entity.getCancellations().get(0).getCancellationId()).isEqualTo("PC-001");
        assertThat(entity.getCancellations().get(0).getCancellationType()).isEqualTo(CancellationType.USER_REQUEST);
        assertThat(entity.getCancellations().get(0).getReason()).isEqualTo("고객 변심");
        assertThat(entity.getCancellations().get(0).getCancelAmount()).isEqualTo(BigDecimal.valueOf(3000));
    }

    @Test
    @DisplayName("엔티티 -> 도메인 변환 성공")
    void from_entity_to_domain() {
        // given
        LocalDateTime now = LocalDateTime.now();
        PaymentEntity entity = PaymentEntity.builder()
                .orderId("ORD-001")
                .ordererId("USER-001")
                .tossPaymentKey("TOSS-KEY-001")
                .payToken("PAY-TOKEN-001")
                .amount(BigDecimal.valueOf(10000))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.APPROVED)
                .approvedAt(now)
                .cancellations(List.of())
                .createdBy("system")
                .updatedAt(now)
                .updatedBy("system")
                .isDeleted(false)
                .build();

        // when
        Payment domain = entity.toDomain();

        // then
        assertThat(domain.getOrderId()).isEqualTo("ORD-001");
        assertThat(domain.getOrdererId()).isEqualTo("USER-001");
        assertThat(domain.getTossPaymentKey()).isEqualTo("TOSS-KEY-001");
        assertThat(domain.getPayToken()).isEqualTo("PAY-TOKEN-001");
        assertThat(domain.getAmount()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(domain.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(domain.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(domain.getCancellations()).isEmpty();
        assertThat(domain.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("엔티티 -> 도메인 -> 엔티티 변환 일관성 검증")
    void entity_domain_entity_consistency() {
        // given
        LocalDateTime now = LocalDateTime.now();
        PaymentCancellationVO cancellationVO = PaymentCancellationVO.builder()
                .cancellationId("PC-001")
                .cancellationType(CancellationType.USER_REQUEST)
                .reason("고객 변심")
                .requestedBy("USER-001")
                .cancelAmount(BigDecimal.valueOf(3000))
                .cancelledAt(now)
                .build();

        PaymentEntity originalEntity = PaymentEntity.builder()
                .orderId("ORD-001")
                .ordererId("USER-001")
                .tossPaymentKey("TOSS-KEY-001")
                .payToken("PAY-TOKEN-001")
                .amount(BigDecimal.valueOf(10000))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.PARTIALLY_CANCELLED)
                .approvedAt(now)
                .cancellations(List.of(cancellationVO))
                .createdBy("system")
                .updatedAt(now)
                .updatedBy("system")
                .isDeleted(false)
                .build();

        // when
        Payment domain = originalEntity.toDomain();
        PaymentEntity newEntity = PaymentEntity.from(domain);

        // then
        assertThat(newEntity.getOrderId()).isEqualTo(originalEntity.getOrderId());
        assertThat(newEntity.getPaymentStatus()).isEqualTo(originalEntity.getPaymentStatus());
        assertThat(newEntity.getCancellations()).hasSize(1);
        assertThat(newEntity.getCancellations().get(0).getReason()).isEqualTo("고객 변심");
    }

    @Test
    @DisplayName("소프트 삭제 마킹 성공")
    void markAsDeleted() {
        // given
        LocalDateTime now = LocalDateTime.now();
        PaymentEntity entity = PaymentEntity.builder()
                .orderId("ORD-001")
                .ordererId("USER-001")
                .tossPaymentKey("TOSS-KEY-001")
                .payToken("PAY-TOKEN-001")
                .amount(BigDecimal.valueOf(10000))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.CANCELLED)
                .approvedAt(now)
                .cancellations(List.of())
                .createdBy("system")
                .updatedAt(now)
                .updatedBy("system")
                .isDeleted(false)
                .build();

        // when
        entity.markAsDeleted("deleter", now, now, "deleter");

        // then
        assertThat(entity.getIsDeleted()).isTrue();
        assertThat(entity.getDeletedBy()).isEqualTo("deleter");
        assertThat(entity.getDeletedAt()).isEqualTo(now);
    }
}
