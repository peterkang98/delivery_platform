package xyz.sparta_project.manjok.domain.payment.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentMethod;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentStatus;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Payment JPA Entity
 */
@Entity
@Table(name = "p_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentEntity extends BaseEntity {

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "orderer_id", nullable = false, length = 36)
    private String ordererId;

    @Column(name = "toss_payment_key", nullable = false, unique = true, length = 200)
    private String tossPaymentKey;

    @Column(name = "pay_token", nullable = false, length = 500)
    private String payToken;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "p_payment_cancellations",
            joinColumns = @JoinColumn(name = "payment_id")
    )
    @OrderColumn(name = "cancellation_order")
    @Builder.Default
    private List<PaymentCancellationVO> cancellations = new ArrayList<>();

    // 감사 필드
    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    // 소프트 삭제
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    /**
     * 도메인 → 엔티티
     */
    public static PaymentEntity from(Payment domain) {
        List<PaymentCancellationVO> cancellationVOs = domain.getCancellations().stream()
                .map(PaymentCancellationVO::from)
                .collect(Collectors.toList());

        return PaymentEntity.builder()
                .orderId(domain.getOrderId())
                .ordererId(domain.getOrdererId())
                .tossPaymentKey(domain.getTossPaymentKey())
                .payToken(domain.getPayToken())
                .amount(domain.getAmount())
                .paymentMethod(domain.getPaymentMethod())
                .paymentStatus(domain.getPaymentStatus())
                .approvedAt(domain.getApprovedAt())
                .cancellations(cancellationVOs)
                .createdBy(domain.getCreatedBy())
                .updatedAt(domain.getUpdatedAt())
                .updatedBy(domain.getUpdatedBy())
                .isDeleted(domain.getIsDeleted())
                .deletedAt(domain.getDeletedAt())
                .deletedBy(domain.getDeletedBy())
                .build();
    }

    /**
     * 엔티티 → 도메인
     */
    public Payment toDomain() {
        return Payment.builder()
                .id(this.getId())
                .orderId(this.orderId)
                .ordererId(this.ordererId)
                .tossPaymentKey(this.tossPaymentKey)
                .payToken(this.payToken)
                .amount(this.amount)
                .paymentMethod(this.paymentMethod)
                .paymentStatus(this.paymentStatus)
                .approvedAt(this.approvedAt)
                .cancellations(this.cancellations.stream()
                        .map(PaymentCancellationVO::toDomain)
                        .collect(Collectors.toList()))
                .createdAt(this.getCreatedAt())
                .createdBy(this.createdBy)
                .updatedAt(this.updatedAt)
                .updatedBy(this.updatedBy)
                .isDeleted(this.isDeleted)
                .deletedAt(this.deletedAt)
                .deletedBy(this.deletedBy)
                .build();
    }

    /**
     * 도메인으로 엔티티 업데이트 (영속성 컨텍스트 활용)
     */
    public void updateFromDomain(Payment domain) {
        // ElementCollection은 clear 후 addAll
        this.cancellations.clear();
        this.cancellations.addAll(domain.getCancellations().stream()
                .map(PaymentCancellationVO::from)
                .collect(Collectors.toList()));

        // 필드 업데이트
        this.paymentStatus = domain.getPaymentStatus();
        this.approvedAt = domain.getApprovedAt();
        this.updatedAt = domain.getUpdatedAt();
        this.updatedBy = domain.getUpdatedBy();
        // 소프트 삭제 필드 업데이트
        this.isDeleted = domain.getIsDeleted();
        this.deletedAt = domain.getDeletedAt();
        this.deletedBy = domain.getDeletedBy();
    }

    /**
     * 소프트 삭제 업데이트
     */
    public void markAsDeleted(String deletedBy, LocalDateTime deletedAt, LocalDateTime updatedAt, String updatedBy) {
        this.isDeleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }
}