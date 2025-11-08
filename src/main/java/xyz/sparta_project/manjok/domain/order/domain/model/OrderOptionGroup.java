package xyz.sparta_project.manjok.domain.order.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderOptionGroup (Value Object)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"groupName"})
public class OrderOptionGroup {

    private String groupName;

    @Builder.Default
    private List<OrderOption> options = new ArrayList<>();

    private BigDecimal groupTotalPrice;

    /**
     * 팩토리 메서드 - 옵션 그룹 생성
     */
    public static OrderOptionGroup create(String groupName, List<OrderOption> options) {
        validateOptionGroup(groupName, options);

        List<OrderOption> optionsCopy = options != null ? new ArrayList<>(options) : new ArrayList<>();
        BigDecimal calculatedGroupTotalPrice = calculateGroupTotalPriceStatic(optionsCopy);

        return OrderOptionGroup.builder()
                .groupName(groupName)
                .options(optionsCopy)
                .groupTotalPrice(calculatedGroupTotalPrice)
                .build();
    }

    /**
     * 옵션 그룹 유효성 검증
     */
    private static void validateOptionGroup(String groupName, List<OrderOption> options) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEMS, "옵션 그룹명은 필수입니다.");
        }
    }

    /**
     * 그룹 내 모든 옵션의 총 가격 계산 (static)
     */
    private static BigDecimal calculateGroupTotalPriceStatic(List<OrderOption> options) {
        return options.stream()
                .map(option -> option.getAdditionalPrice()
                        .multiply(BigDecimal.valueOf(option.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 그룹 내 모든 옵션의 총 가격 계산 (인스턴스 메서드)
     */
    public BigDecimal calculateGroupTotalPrice() {
        return calculateGroupTotalPriceStatic(this.options);
    }

    /**
     * 옵션 추가
     */
    public void addOption(OrderOption option) {
        if (option != null) {
            this.options.add(option);
            this.groupTotalPrice = calculateGroupTotalPrice();
        }
    }

    /**
     * options의 불변성을 위한 방어적 복사
     */
    public List<OrderOption> getOptions() {
        return new ArrayList<>(options);
    }
}