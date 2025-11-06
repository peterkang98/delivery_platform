package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderOption;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderOptionVO Value Object 변환 테스트")
class OrderOptionVOTest {

    @Test
    @DisplayName("도메인 -> VO 변환이 정상적으로 이루어진다")
    void fromDomain_success() {
        // given
        OrderOption domain = OrderOption.create(
                "치즈 추가",
                "고소한 체다 치즈",
                new BigDecimal("1000"),
                2
        );

        // when
        OrderOptionVO vo = OrderOptionVO.from(domain);

        // then
        assertThat(vo.getOptionName()).isEqualTo("치즈 추가");
        assertThat(vo.getDescription()).isEqualTo("고소한 체다 치즈");
        assertThat(vo.getAdditionalPrice()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(vo.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("VO -> 도메인 변환이 정상적으로 이루어진다")
    void toDomain_success() {
        // given
        OrderOptionVO vo = OrderOptionVO.builder()
                .optionName("베이컨 추가")
                .description("바삭한 베이컨")
                .additionalPrice(new BigDecimal("1500"))
                .quantity(1)
                .build();

        // when
        OrderOption domain = vo.toDomain();

        // then
        assertThat(domain.getOptionName()).isEqualTo("베이컨 추가");
        assertThat(domain.getDescription()).isEqualTo("바삭한 베이컨");
        assertThat(domain.getAdditionalPrice()).isEqualByComparingTo(new BigDecimal("1500"));
        assertThat(domain.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("도메인 -> VO -> 도메인 변환 시 값이 동일해야 한다")
    void roundTripConversion() {
        // given
        OrderOption original = OrderOption.create(
                "피클 추가",
                "신선한 피클",
                new BigDecimal("500"),
                3
        );

        // when
        OrderOption converted = OrderOptionVO.from(original).toDomain();

        // then
        assertThat(converted)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }
}