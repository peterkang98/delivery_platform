package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.model.Order;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderStatus;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order JPA Entity
 */
@Entity
@Table(name = "p_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderEntity extends BaseEntity {

    @Embedded
    private OrdererVO orderer;

    // OrderItem과의 일대다 연관관계 (ElementCollection → OneToMany 변경)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "item_order")
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Embedded
    private PaymentVO payment;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "payment_completed_at")
    private LocalDateTime paymentCompletedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

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
    public static OrderEntity from(Order domain) {
        OrderEntity entity = OrderEntity.builder()
                .orderer(OrdererVO.from(domain.getOrderer()))
                .status(domain.getStatus())
                .payment(PaymentVO.from(domain.getPayment()))
                .totalPrice(domain.getTotalPrice())
                .requestedAt(domain.getRequestedAt())
                .paymentCompletedAt(domain.getPaymentCompletedAt())
                .confirmedAt(domain.getConfirmedAt())
                .completedAt(domain.getCompletedAt())
                .canceledAt(domain.getCanceledAt())
                .cancelReason(domain.getCancelReason())
                .createdBy(domain.getCreatedBy())
                .updatedAt(domain.getUpdatedAt())
                .updatedBy(domain.getUpdatedBy())
                .isDeleted(domain.getIsDeleted())
                .deletedAt(domain.getDeletedAt())
                .deletedBy(domain.getDeletedBy())
                .build();

        // OrderItem 변환 및 연관관계 설정
        domain.getItems().forEach(item -> {
            OrderItemEntity itemEntity = OrderItemEntity.from(item, entity);
            entity.addItem(itemEntity);
        });

        return entity;
    }

    /**
     * 엔티티 → 도메인
     */
    public Order toDomain() {
        return Order.builder()
                .id(this.getId())
                .orderer(this.orderer.toDomain())
                .items(this.items.stream()
                        .map(OrderItemEntity::toDomain)
                        .collect(Collectors.toList()))
                .status(this.status)
                .payment(this.payment.toDomain())
                .totalPrice(this.totalPrice)
                .requestedAt(this.requestedAt)
                .paymentCompletedAt(this.paymentCompletedAt)
                .confirmedAt(this.confirmedAt)
                .completedAt(this.completedAt)
                .canceledAt(this.canceledAt)
                .cancelReason(this.cancelReason)
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
     * 연관관계 편의 메서드 - OrderItem 추가
     */
    public void addItem(OrderItemEntity item) {
        this.items.add(item);
        item.setOrder(this);
    }

    /**
     * 연관관계 편의 메서드 - OrderItem 제거
     */
    public void removeItem(OrderItemEntity item) {
        this.items.remove(item);
        item.setOrder(null);
    }

    /**
     * 연관관계 편의 메서드 - 모든 OrderItem 제거
     */
    public void clearItems() {
        this.items.clear();
    }

    /**
     * 도메인으로 엔티티 업데이트 (영속성 컨텍스트 활용)
     */
    public void updateFromDomain(Order domain) {
        // OrderItem 업데이트 (기존 제거 후 재설정)
        this.clearItems();
        domain.getItems().forEach(item -> {
            OrderItemEntity itemEntity = OrderItemEntity.from(item, this);
            this.addItem(itemEntity);
        });

        // 필드 업데이트
        this.orderer = OrdererVO.from(domain.getOrderer());
        this.status = domain.getStatus();
        this.payment = PaymentVO.from(domain.getPayment());
        this.totalPrice = domain.getTotalPrice();
        this.paymentCompletedAt = domain.getPaymentCompletedAt();
        this.confirmedAt = domain.getConfirmedAt();
        this.completedAt = domain.getCompletedAt();
        this.canceledAt = domain.getCanceledAt();
        this.cancelReason = domain.getCancelReason();
        this.updatedAt = domain.getUpdatedAt();
        this.updatedBy = domain.getUpdatedBy();

        // 소프트 삭제 필드 업데이트 (추가됨)
        this.isDeleted = domain.getIsDeleted();
        this.deletedBy = domain.getDeletedBy();
        this.deletedAt = domain.getDeletedAt();
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