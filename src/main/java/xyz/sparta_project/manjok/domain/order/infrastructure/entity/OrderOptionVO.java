package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderOption;

import java.math.BigDecimal;

/**
 * OrderOption Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderOptionVO {

    @Column(name = "option_name", nullable = false, length = 100)
    private String optionName;

    @Column(name = "option_description", length = 500)
    private String description;

    @Column(name = "additional_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal additionalPrice;

    @Column(name = "option_quantity", nullable = false)
    private Integer quantity;

    /**
     * 도메인 → VO
     */
    public static OrderOptionVO from(OrderOption domain) {
        return OrderOptionVO.builder()
                .optionName(domain.getOptionName())
                .description(domain.getDescription())
                .additionalPrice(domain.getAdditionalPrice())
                .quantity(domain.getQuantity())
                .build();
    }

    /**
     * VO → 도메인
     */
    public OrderOption toDomain() {
        return OrderOption.builder()
                .optionName(this.optionName)
                .description(this.description)
                .additionalPrice(this.additionalPrice)
                .quantity(this.quantity)
                .build();
    }
}