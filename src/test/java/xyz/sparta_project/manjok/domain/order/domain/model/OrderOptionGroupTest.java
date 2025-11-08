package xyz.sparta_project.manjok.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderOptionGroup Value Object 테스트")
class OrderOptionGroupTest {

    private OrderOption createOption(String name, int price, int quantity) {
        return OrderOption.create(
                name,
                name + " 설명",
                BigDecimal.valueOf(price),
                quantity
        );
    }

    @Nested
    @DisplayName("create() - 옵션 그룹 생성")
    class CreateOptionGroup {

        @Test
        @DisplayName("정상적인 값으로 OrderOptionGroup 생성에 성공한다")
        void create_success() {
            // given
            OrderOption option1 = createOption("곱빼기", 2000, 1);
            OrderOption option2 = createOption("치즈추가", 500, 2);

            // when
            OrderOptionGroup group = OrderOptionGroup.create("추가 옵션", List.of(option1, option2));

            // then
            assertThat(group.getGroupName()).isEqualTo("추가 옵션");
            assertThat(group.getOptions()).hasSize(2);
            assertThat(group.getGroupTotalPrice()).isEqualByComparingTo("3000"); // 2000 + (500*2)
        }

        @Test
        @DisplayName("옵션 그룹명이 null 또는 공백이면 예외가 발생한다")
        void create_fail_invalidGroupName() {
            OrderOption option = createOption("곱빼기", 2000, 1);

            assertThatThrownBy(() ->
                    OrderOptionGroup.create("   ", List.of(option))
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    OrderOptionGroup.create(null, List.of(option))
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("옵션 리스트가 null 이여도 생성은 가능하다 (빈 리스트로 처리)")
        void create_success_nullOptionsHandledAsEmpty() {
            // when
            OrderOptionGroup group = OrderOptionGroup.create("추가 옵션", null);

            // then
            assertThat(group.getOptions()).isEmpty();
            assertThat(group.getGroupTotalPrice()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("calculateGroupTotalPrice() - 총 가격 계산")
    class CalculateTotalPrice {

        @Test
        @DisplayName("옵션 총 가격을 정확히 계산한다")
        void calculate_success() {
            // given
            OrderOption option1 = createOption("곱빼기", 2000, 1);
            OrderOption option2 = createOption("치즈추가", 500, 3);
            OrderOptionGroup group = OrderOptionGroup.create("추가 옵션", List.of(option1, option2));

            // when
            var total = group.calculateGroupTotalPrice();

            // then
            assertThat(total).isEqualByComparingTo("3500"); // 2000 + (500*3)
        }
    }

    @Nested
    @DisplayName("addOption() - 옵션 추가")
    class AddOption {

        @Test
        @DisplayName("옵션 추가 시 그룹 총 가격이 다시 계산된다")
        void addOption_recalculatesPrice() {
            // given
            OrderOption option1 = createOption("곱빼기", 2000, 1);
            OrderOptionGroup group = OrderOptionGroup.create("추가 옵션", List.of(option1));

            OrderOption option2 = createOption("치즈추가", 500, 2);

            // when
            group.addOption(option2);

            // then
            assertThat(group.getOptions()).hasSize(2);
            assertThat(group.getGroupTotalPrice()).isEqualByComparingTo("3000"); // 2000 + (500*2)
        }
    }
}
