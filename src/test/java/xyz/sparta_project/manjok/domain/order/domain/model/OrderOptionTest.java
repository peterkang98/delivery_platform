package xyz.sparta_project.manjok.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderOption Value Object 테스트")
class OrderOptionTest {

    @Nested
    @DisplayName("create() - 옵션 생성")
    class CreateOption {

        @Test
        @DisplayName("정상적인 값으로 OrderOption 생성에 성공한다")
        void create_success() {
            // given
            String optionName = "곱빼기";
            String description = "양을 두 배로 증가";
            BigDecimal additionalPrice = new BigDecimal("2000");
            Integer quantity = 2;

            // when
            OrderOption option = OrderOption.create(optionName, description, additionalPrice, quantity);

            // then
            assertThat(option.getOptionName()).isEqualTo("곱빼기");
            assertThat(option.getDescription()).isEqualTo("양을 두 배로 증가");
            assertThat(option.getAdditionalPrice()).isEqualByComparingTo("2000");
            assertThat(option.getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("옵션명이 null 또는 공백이면 예외가 발생한다")
        void create_fail_invalidName() {
            assertThatThrownBy(() ->
                    OrderOption.create(null, "desc", BigDecimal.ZERO, 1)
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    OrderOption.create("   ", "desc", BigDecimal.ZERO, 1)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("추가 금액이 null 이거나 0 미만이면 예외가 발생한다")
        void create_fail_invalidPrice() {
            assertThatThrownBy(() ->
                    OrderOption.create("곱빼기", "desc", null, 1)
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    OrderOption.create("곱빼기", "desc", new BigDecimal("-1"), 1)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("수량이 null 이거나 1 미만이면 예외가 발생한다")
        void create_fail_invalidQuantity() {
            assertThatThrownBy(() ->
                    OrderOption.create("곱빼기", "desc", BigDecimal.ZERO, null)
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    OrderOption.create("곱빼기", "desc", BigDecimal.ZERO, 0)
            ).isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("calculateOptionTotalPrice() - 옵션 총 가격 계산")
    class CalculateTotalPrice {

        @Test
        @DisplayName("옵션 총 가격을 정확히 계산한다")
        void calculate_success() {
            // given
            OrderOption option = OrderOption.create(
                    "치즈 추가",
                    "치즈 한 장 추가",
                    new BigDecimal("500"),
                    3
            );

            // when
            BigDecimal result = option.calculateOptionTotalPrice();

            // then
            assertThat(result).isEqualByComparingTo("1500");
        }
    }
}
