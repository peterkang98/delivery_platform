package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderOption;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderOptionGroup;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderOptionGroupEntity 변환 테스트")
class OrderOptionGroupEntityTest {

    @Test
    @DisplayName("도메인 -> 엔티티 변환이 정상적으로 이루어진다")
    void fromDomain_success() {
        // given
        OrderOption option1 = OrderOption.create("치즈 추가", "고소한 치즈", new BigDecimal("1000"), 1);
        OrderOption option2 = OrderOption.create("베이컨 추가", "바삭한 베이컨", new BigDecimal("1500"), 1);
        OrderOptionGroup domain = OrderOptionGroup.create("토핑", List.of(option1, option2));

        OrderItemEntity mockOrderItem = OrderItemEntity.builder()
                .orderItemNumber("OI-123")
                .menuId("menu-1")
                .menuName("버거")
                .basePrice(new BigDecimal("5000"))
                .quantity(1)
                .totalPrice(new BigDecimal("5000"))
                .build();

        // when
        OrderOptionGroupEntity entity = OrderOptionGroupEntity.from(domain, mockOrderItem);

        // then
        assertThat(entity.getGroupName()).isEqualTo("토핑");
        assertThat(entity.getGroupTotalPrice()).isEqualByComparingTo(new BigDecimal("2500"));
        assertThat(entity.getOptions()).hasSize(2);
        assertThat(entity.getOrderItem()).isEqualTo(mockOrderItem);
    }

    @Test
    @DisplayName("엔티티 -> 도메인 변환이 정상적으로 이루어진다")
    void toDomain_success() {
        // given
        OrderOptionVO optionVO1 = OrderOptionVO.builder()
                .optionName("피클 추가")
                .description("신선한 피클")
                .additionalPrice(new BigDecimal("500"))
                .quantity(2)
                .build();

        OrderOptionGroupEntity entity = OrderOptionGroupEntity.builder()
                .groupName("야채")
                .groupTotalPrice(new BigDecimal("1000"))
                .options(List.of(optionVO1))
                .build();

        // when
        OrderOptionGroup domain = entity.toDomain();

        // then
        assertThat(domain.getGroupName()).isEqualTo("야채");
        assertThat(domain.getGroupTotalPrice()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(domain.getOptions()).hasSize(1);
    }

    @Test
    @DisplayName("도메인 -> 엔티티 -> 도메인 변환 시 값이 동일해야 한다")
    void roundTripConversion() {
        // given
        OrderOption option = OrderOption.create("소스 추가", "특제 소스", new BigDecimal("800"), 1);
        OrderOptionGroup original = OrderOptionGroup.create("소스", List.of(option));

        OrderItemEntity mockOrderItem = OrderItemEntity.builder()
                .orderItemNumber("OI-456")
                .menuId("menu-2")
                .menuName("샐러드")
                .basePrice(new BigDecimal("7000"))
                .quantity(1)
                .totalPrice(new BigDecimal("7000"))
                .build();

        // when
        OrderOptionGroup converted = OrderOptionGroupEntity.from(original, mockOrderItem).toDomain();

        // then
        assertThat(converted)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }
}