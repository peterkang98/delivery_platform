package xyz.sparta_project.manjok.domain.payment.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.payment.domain.model.CancellationType;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentCancellation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentCancellationVO 변환 테스트")
class PaymentCancellationVOTest {

    @Test
    @DisplayName("도메인 -> VO 변환 성공")
    void from_domain_to_vo() {
        // given
        LocalDateTime now = LocalDateTime.now();
        PaymentCancellation domain = PaymentCancellation.builder()
                .id("PC-001")
                .paymentId("PAY-001")
                .cancellationType(CancellationType.USER_REQUEST)
                .reason("고객 변심")
                .requestedBy("USER-001")
                .cancelAmount(BigDecimal.valueOf(3000))
                .cancelledAt(now)
                .build();

        // when
        PaymentCancellationVO vo = PaymentCancellationVO.from(domain);

        // then
        assertThat(vo.getCancellationId()).isEqualTo("PC-001");
        assertThat(vo.getCancellationType()).isEqualTo(CancellationType.USER_REQUEST);
        assertThat(vo.getReason()).isEqualTo("고객 변심");
        assertThat(vo.getRequestedBy()).isEqualTo("USER-001");
        assertThat(vo.getCancelAmount()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(vo.getCancelledAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("VO -> 도메인 변환 성공")
    void from_vo_to_domain() {
        // given
        LocalDateTime now = LocalDateTime.now();
        PaymentCancellationVO vo = PaymentCancellationVO.builder()
                .cancellationId("PC-001")
                .cancellationType(CancellationType.USER_REQUEST)
                .reason("고객 변심")
                .requestedBy("USER-001")
                .cancelAmount(BigDecimal.valueOf(3000))
                .cancelledAt(now)
                .build();

        // when
        PaymentCancellation domain = vo.toDomain();

        // then
        assertThat(domain.getId()).isEqualTo("PC-001");
        assertThat(domain.getCancellationType()).isEqualTo(CancellationType.USER_REQUEST);
        assertThat(domain.getReason()).isEqualTo("고객 변심");
        assertThat(domain.getRequestedBy()).isEqualTo("USER-001");
        assertThat(domain.getCancelAmount()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(domain.getCancelledAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("모든 취소 유형에 대해 변환 성공")
    void all_cancellation_types_conversion() {
        // given
        LocalDateTime now = LocalDateTime.now();
        CancellationType[] types = CancellationType.values();

        for (CancellationType type : types) {
            PaymentCancellation domain = PaymentCancellation.builder()
                    .id("PC-001")
                    .paymentId("PAY-001")
                    .cancellationType(type)
                    .reason("취소 사유")
                    .requestedBy("USER-001")
                    .cancelAmount(BigDecimal.valueOf(1000))
                    .cancelledAt(now)
                    .build();

            // when
            PaymentCancellationVO vo = PaymentCancellationVO.from(domain);
            PaymentCancellation converted = vo.toDomain();

            // then
            assertThat(converted.getCancellationType()).isEqualTo(type);
        }
    }
}