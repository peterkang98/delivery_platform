package xyz.sparta_project.manjok.domain.order.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;

/**
 * OrderOption (Value Object)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"optionName"})
public class OrderOption {

    private String optionName;
    private String description;
    private BigDecimal additionalPrice;
    private Integer quantity;

    /**
     * 팩토리 메서드 - 옵션 생성
     */
    public static OrderOption create(String optionName, String description,
                                     BigDecimal additionalPrice, Integer quantity) {
        validateOption(optionName, additionalPrice, quantity);

        return OrderOption.builder()
                .optionName(optionName)
                .description(description)
                .additionalPrice(additionalPrice)
                .quantity(quantity)
                .build();
    }

    /**
     * 옵션 유효성 검증
     */
    private static void validateOption(String optionName, BigDecimal additionalPrice, Integer quantity) {
        if (optionName == null || optionName.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEMS, "옵션명은 필수입니다.");
        }
        if (additionalPrice == null || additionalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderException(OrderErrorCode.INVALID_PRICE, "옵션 가격은 0 이상이어야 합니다.");
        }
        if (quantity == null || quantity < 1) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEM_QUANTITY, "옵션 수량은 1개 이상이어야 합니다.");
        }
    }

    /**
     * 옵션 총 가격 계산
     */
    public BigDecimal calculateOptionTotalPrice() {
        return additionalPrice.multiply(BigDecimal.valueOf(quantity));
    }
}