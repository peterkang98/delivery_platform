package xyz.sparta_project.manjok.domain.restaurant.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 완료 이벤트
 * - 도메인 이벤트 (DTO 역할)
 */
@Getter
@RequiredArgsConstructor
public class OrderCompletedEvent {
    private final String orderId;
    private final String restaurantId;
    private final List<OrderMenuItem> menuItems;  // 주문한 메뉴 정보
    private final BigDecimal totalAmount;
    private final LocalDateTime completedAt;

    @Getter
    @RequiredArgsConstructor
    public static class OrderMenuItem {
        private final String menuId;
        private final Integer quantity;
    }
}