package xyz.sparta_project.manjok.domain.payment.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 토스 결제 조회 API 응답 DTO
 * https://docs.tosspayments.com/reference#payment-객체
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentResponse {

    /**
     * 결제 키
     */
    @JsonProperty("paymentKey")
    private String paymentKey;

    /**
     * 주문 ID
     */
    @JsonProperty("orderId")
    private String orderId;

    /**
     * 결제 상태
     * READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED
     */
    @JsonProperty("status")
    private String status;

    /**
     * 총 결제 금액
     */
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    /**
     * 결제 승인 시각
     */
    @JsonProperty("approvedAt")
    private LocalDateTime approvedAt;

    /**
     * 결제 방법
     */
    @JsonProperty("method")
    private String method;

    /**
     * 결제 수단 정보 (카드, 계좌이체 등)
     */
    @JsonProperty("card")
    private CardInfo card;

    /**
     * 카드 정보
     */
    @Getter
    @NoArgsConstructor
    public static class CardInfo {
        @JsonProperty("company")
        private String company;

        @JsonProperty("number")
        private String number;

        @JsonProperty("approveNo")
        private String approveNo;
    }

    /**
     * 결제가 완료된 상태인지 확인
     */
    public boolean isDone() {
        return "DONE".equals(status);
    }
}