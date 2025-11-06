package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.model.Payment;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentVO Value Object 변환 테스트")
class PaymentVOTest {

    @Test
    @DisplayName("도메인 -> VO 변환이 정상적으로 이루어진다")
    void fromDomain_success() {
        // given
        Payment domain = Payment.create("PAY-12345", true);

        // when
        PaymentVO vo = PaymentVO.from(domain);

        // then
        assertThat(vo.getPaymentId()).isEqualTo("PAY-12345");
        assertThat(vo.getIsPaid()).isTrue();
    }

    @Test
    @DisplayName("VO -> 도메인 변환이 정상적으로 이루어진다")
    void toDomain_success() {
        // given
        PaymentVO vo = PaymentVO.builder()
                .paymentId("PAY-99999")
                .isPaid(false)
                .build();

        // when
        Payment domain = vo.toDomain();

        // then
        assertThat(domain.getPaymentId()).isEqualTo("PAY-99999");
        assertThat(domain.getIsPaid()).isFalse();
    }

    @Test
    @DisplayName("도메인 -> VO -> 도메인 변환 시 값이 동일해야 한다")
    void roundTripConversion() {
        // given
        Payment original = Payment.create("PAY-77777", true);

        // when
        Payment converted = PaymentVO.from(original).toDomain();

        // then
        assertThat(converted)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }
}
