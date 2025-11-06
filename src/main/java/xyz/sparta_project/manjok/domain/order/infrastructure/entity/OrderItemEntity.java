package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderItem;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderItem Entity
 */
@Entity
@Table(name = "p_order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderItemEntity extends BaseEntity {

    @Column(name = "order_item_number", nullable = false, length = 50)
    private String orderItemNumber;

    @Column(name = "menu_id", nullable = false, length = 36)
    private String menuId;

    @Column(name = "menu_name", nullable = false, length = 200)
    private String menuName;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Embedded
    private RestaurantVO restaurant;

    // Order와의 다대일 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    // OrderOptionGroup과의 일대다 연관관계
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "group_order")
    @Builder.Default
    private List<OrderOptionGroupEntity> optionGroups = new ArrayList<>();

    /**
     * 도메인 → 엔티티
     */
    public static OrderItemEntity from(OrderItem domain, OrderEntity order) {
        OrderItemEntity entity = OrderItemEntity.builder()
                .orderItemNumber(domain.getOrderItemNumber())
                .menuId(domain.getMenuId())
                .menuName(domain.getMenuName())
                .basePrice(domain.getBasePrice())
                .quantity(domain.getQuantity())
                .totalPrice(domain.getTotalPrice())
                .restaurant(RestaurantVO.from(domain.getRestaurant()))
                .order(order)
                .build();

        // 옵션 그룹 변환 및 연관관계 설정
        List<OrderOptionGroupEntity> optionGroupEntities = domain.getOptionGroups().stream()
                .map(optionGroup -> OrderOptionGroupEntity.from(optionGroup, entity))
                .collect(Collectors.toList());

        entity.optionGroups.addAll(optionGroupEntities);

        return entity;
    }

    /**
     * 엔티티 → 도메인
     */
    public OrderItem toDomain() {
        return OrderItem.builder()
                .orderItemNumber(this.orderItemNumber)
                .menuId(this.menuId)
                .menuName(this.menuName)
                .basePrice(this.basePrice)
                .quantity(this.quantity)
                .totalPrice(this.totalPrice)
                .restaurant(this.restaurant.toDomain())
                .optionGroups(this.optionGroups.stream()
                        .map(OrderOptionGroupEntity::toDomain)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 연관관계 편의 메서드 - Order 설정
     */
    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    /**
     * 연관관계 편의 메서드 - OptionGroup 추가
     */
    public void addOptionGroup(OrderOptionGroupEntity optionGroup) {
        this.optionGroups.add(optionGroup);
        optionGroup.setOrderItem(this);
    }

    /**
     * 연관관계 편의 메서드 - OptionGroup 제거
     */
    public void removeOptionGroup(OrderOptionGroupEntity optionGroup) {
        this.optionGroups.remove(optionGroup);
        optionGroup.setOrderItem(null);
    }

    /**
     * 연관관계 편의 메서드 - 모든 OptionGroup 제거
     */
    public void clearOptionGroups() {
        this.optionGroups.clear();
    }

    /**
     * 도메인으로부터 엔티티 업데이트
     */
    public void updateFromDomain(OrderItem domain) {
        this.menuName = domain.getMenuName();
        this.basePrice = domain.getBasePrice();
        this.quantity = domain.getQuantity();
        this.totalPrice = domain.getTotalPrice();
        this.restaurant = RestaurantVO.from(domain.getRestaurant());

        // 옵션 그룹 업데이트 (기존 제거 후 재설정)
        this.clearOptionGroups();
        domain.getOptionGroups().forEach(optionGroup -> {
            OrderOptionGroupEntity optionGroupEntity = OrderOptionGroupEntity.from(optionGroup, this);
            this.addOptionGroup(optionGroupEntity);
        });
    }
}