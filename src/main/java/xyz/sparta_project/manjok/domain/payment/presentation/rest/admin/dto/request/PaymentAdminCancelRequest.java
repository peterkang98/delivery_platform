package xyz.sparta_project.manjok.domain.payment.presentation.rest.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.payment.domain.model.CancellationType;

import java.math.BigDecimal;

/**
 * 관리자 결제 취소 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAdminCancelRequest {

    @NotBlank(message = "결제 ID는 필수입니다.")
    private String paymentId;

    @NotNull(message = "취소 유형은 필수입니다.")
    private CancellationType cancellationType;

    @NotBlank(message = "취소 사유는 필수입니다.")
    private String cancelReason;

    @Positive(message = "취소 금액은 0보다 커야 합니다.")
    private BigDecimal cancelAmount; // null이면 전액 취소
}