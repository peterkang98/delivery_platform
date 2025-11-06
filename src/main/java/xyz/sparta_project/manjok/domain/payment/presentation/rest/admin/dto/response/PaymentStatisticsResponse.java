package xyz.sparta_project.manjok.domain.payment.presentation.rest.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 결제 통계 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatisticsResponse {

    private long totalCount;
    private long approvedCount;
    private long cancelledCount;
    private long failedCount;
    private BigDecimal totalAmount;
    private BigDecimal approvedAmount;
    private BigDecimal cancelledAmount;
}