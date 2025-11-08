package xyz.sparta_project.manjok.domain.restaurant.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.OrderCompletedEvent;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Menu;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Restaurant;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandler;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerProcessor;

/**
 * 주문 완료 이벤트 핸들러
 * - 주문 완료 시 레스토랑 및 메뉴 통계 업데이트
 * - @EventHandler 어노테이션으로 자동 등록
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EventHandler(eventType = OrderCompletedEvent.class)
public class OrderEventHandler implements EventHandlerProcessor<OrderCompletedEvent> {

    private final RestaurantRepository restaurantRepository;

    /**
     * 주문 완료 이벤트 처리
     * - 레스토랑 구매수 증가
     * - 메뉴별 구매수 증가
     */
    @Override
    @Transactional
    public void handle(OrderCompletedEvent event) throws Exception {
        log.info("주문 완료 이벤트 처리 시작: orderId={}, restaurantId={}",
                event.getOrderId(), event.getRestaurantId());

        // 1. Restaurant 조회 (메뉴 포함)
        Restaurant restaurant = restaurantRepository
                .findByIdWithMenus(event.getRestaurantId())
                .orElseThrow(() -> new RestaurantException(
                        RestaurantErrorCode.RESTAURANT_NOT_FOUND,
                        "레스토랑을 찾을 수 없습니다: " + event.getRestaurantId()
                ));

        // 2. 레스토랑 구매수 증가
        restaurant.incrementPurchaseCount();
        log.debug("레스토랑 구매수 증가: restaurantId={}, newCount={}",
                event.getRestaurantId(), restaurant.getPurchaseCount());

        // 3. 각 메뉴의 구매수 증가
        event.getMenuItems().forEach(orderItem -> {
            try {
                Menu menu = restaurant.findMenuById(orderItem.getMenuId());

                // 주문 수량만큼 증가
                for (int i = 0; i < orderItem.getQuantity(); i++) {
                    menu.incrementPurchaseCount();
                }

                log.debug("메뉴 구매수 증가: menuId={}, quantity={}, newCount={}",
                        orderItem.getMenuId(), orderItem.getQuantity(), menu.getPurchaseCount());

            } catch (RestaurantException e) {
                // 메뉴를 찾을 수 없는 경우 (이미 삭제된 메뉴 등) - 로그만 남기고 계속 진행
                log.warn("메뉴 통계 업데이트 실패: menuId={}, error={}",
                        orderItem.getMenuId(), e.getMessage());
            }
        });

        // 4. 변경사항 저장 (더티체킹 + Cascade로 하위 엔티티 자동 저장)
        restaurantRepository.save(restaurant);

        log.info("주문 완료 이벤트 처리 성공: orderId={}, restaurantId={}",
                event.getOrderId(), event.getRestaurantId());
    }
}