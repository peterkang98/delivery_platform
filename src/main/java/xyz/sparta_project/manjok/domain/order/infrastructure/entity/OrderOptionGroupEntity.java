package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderOptionGroup;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderOptionGroup Entity
 */
@Entity
@Table(name = "p_order_item_option_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderOptionGroupEntity extends BaseEntity {

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "group_total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal groupTotalPrice;

    // OrderItem과의 다대일 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItemEntity orderItem;

    // OrderOption은 여전히 ElementCollection으로 유지 (단일 레벨)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "p_order_item_options",
            joinColumns = @JoinColumn(name = "option_group_id")
    )
    @OrderColumn(name = "option_order")
    @Builder.Default
    private List<OrderOptionVO> options = new ArrayList<>();

    /**
     * 도메인 → 엔티티
     */
    public static OrderOptionGroupEntity from(OrderOptionGroup domain, OrderItemEntity orderItem) {
        List<OrderOptionVO> optionVOs = domain.getOptions().stream()
                .map(OrderOptionVO::from)
                .collect(Collectors.toList());

        return OrderOptionGroupEntity.builder()
                .groupName(domain.getGroupName())
                .groupTotalPrice(domain.getGroupTotalPrice())
                .orderItem(orderItem)
                .options(optionVOs)
                .build();
    }

    /**
     * 엔티티 → 도메인
     */
    public OrderOptionGroup toDomain() {
        return OrderOptionGroup.builder()
                .groupName(this.groupName)
                .groupTotalPrice(this.groupTotalPrice)
                .options(this.options.stream()
                        .map(OrderOptionVO::toDomain)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 연관관계 편의 메서드 - OrderItem 설정
     */
    public void setOrderItem(OrderItemEntity orderItem) {
        this.orderItem = orderItem;
    }

    /**
     * 도메인으로부터 엔티티 업데이트
     */
    public void updateFromDomain(OrderOptionGroup domain) {
        this.groupName = domain.getGroupName();
        this.groupTotalPrice = domain.getGroupTotalPrice();

        // 옵션 업데이트 (ElementCollection은 clear 후 addAll)
        this.options.clear();
        this.options.addAll(domain.getOptions().stream()
                .map(OrderOptionVO::from)
                .collect(Collectors.toList()));
    }
}