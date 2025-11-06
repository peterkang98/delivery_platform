package xyz.sparta_project.manjok.domain.order.presentation.rest.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 확인 응답 (Owner용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmOrderResponse {
    private String orderId;
    private String message;
}