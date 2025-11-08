package xyz.sparta_project.manjok.domain.order.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * OrderItem
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"orderItemNumber"})
public class OrderItem {

    private String orderItemNumber;  // 주문 아이템 고유 번호
    private String menuId;
    private String menuName;
    private BigDecimal basePrice;
    private Integer quantity;
    private BigDecimal totalPrice;

    private Restaurant restaurant;

    @Builder.Default
    private List<OrderOptionGroup> optionGroups = new ArrayList<>();

    /**
     * 팩토리 메서드 - 주문 아이템 생성
     */
    public static OrderItem create(String menuId, String menuName, BigDecimal basePrice,
                                   Integer quantity, Restaurant restaurant,
                                   List<OrderOptionGroup> optionGroups) {
        validateOrderItem(menuId, menuName, basePrice, quantity, restaurant);

        List<OrderOptionGroup> groupsCopy = optionGroups != null ?
                new ArrayList<>(optionGroups) : new ArrayList<>();

        BigDecimal calculatedTotalPrice = calculateTotalPriceStatic(basePrice, quantity, groupsCopy);

        return OrderItem.builder()
                .orderItemNumber(generateOrderItemNumber())  // 고유 번호 자동 생성
                .menuId(menuId)
                .menuName(menuName)
                .basePrice(basePrice)
                .quantity(quantity)
                .restaurant(restaurant)
                .optionGroups(groupsCopy)
                .totalPrice(calculatedTotalPrice)
                .build();
    }

    /**
     * 주문 아이템 고유 번호 생성
     */
    private static String generateOrderItemNumber() {
        return "OI-" + UUID.randomUUID().toString();
    }

    /**
     * 주문 아이템 유효성 검증
     */
    private static void validateOrderItem(String menuId, String menuName,
                                          BigDecimal basePrice, Integer quantity,
                                          Restaurant restaurant) {
        if (menuId == null || menuId.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEMS, "메뉴 ID는 필수입니다.");
        }
        if (menuName == null || menuName.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEMS, "메뉴 이름은 필수입니다.");
        }
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderException(OrderErrorCode.INVALID_PRICE);
        }
        if (quantity == null || quantity < 1) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEM_QUANTITY);
        }
        if (restaurant == null) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEMS, "레스토랑 정보는 필수입니다.");
        }
    }

    /**
     * 아이템 총 가격 계산 (static)
     */
    private static BigDecimal calculateTotalPriceStatic(BigDecimal basePrice, Integer quantity,
                                                        List<OrderOptionGroup> optionGroups) {
        BigDecimal optionsPrice = optionGroups.stream()
                .map(OrderOptionGroup::calculateGroupTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return basePrice.add(optionsPrice).multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * 아이템 총 가격 계산 (인스턴스 메서드)
     */
    public BigDecimal calculateTotalPrice() {
        return calculateTotalPriceStatic(this.basePrice, this.quantity, this.optionGroups);
    }

    /**
     * 옵션 그룹 추가
     */
    public void addOptionGroup(OrderOptionGroup optionGroup) {
        if (optionGroup != null) {
            this.optionGroups.add(optionGroup);
            this.totalPrice = calculateTotalPrice();
        }
    }

    /**
     * optionGroups의 불변성을 위한 방어적 복사
     */
    public List<OrderOptionGroup> getOptionGroups() {
        return new ArrayList<>(optionGroups);
    }

    /**
     * restaurantId를 반환하는 헬퍼 메서드
     */
    public String getRestaurantId() {
        return restaurant != null ? restaurant.getRestaurantId() : null;
    }
}