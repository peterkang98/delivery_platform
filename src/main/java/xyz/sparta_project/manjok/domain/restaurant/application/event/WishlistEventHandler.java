package xyz.sparta_project.manjok.domain.restaurant.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.event.WishlistChangedEvent;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Menu;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Restaurant;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandler;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerProcessor;

/**
 * 찜 변경 이벤트 핸들러
 * - 찜 추가/제거 시 레스토랑 및 메뉴 통계 업데이트
 * - @EventHandler 어노테이션으로 자동 등록
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EventHandler(eventType = WishlistChangedEvent.class)
public class WishlistEventHandler implements EventHandlerProcessor<WishlistChangedEvent> {

    private final RestaurantRepository restaurantRepository;

    /**
     * 찜 추가/제거 이벤트 처리
     */
    @Override
    @Transactional
    public void handle(WishlistChangedEvent event) throws Exception {
        log.info("찜 변경 이벤트 처리 시작: restaurantId={}, menuId={}, action={}",
                event.getRestaurantId(), event.getMenuId(), event.getAction());

        if (event.getMenuId() != null) {
            // 메뉴 찜
            handleMenuWishlist(event);
        } else {
            // 레스토랑 찜
            handleRestaurantWishlist(event);
        }

        log.info("찜 변경 이벤트 처리 성공: restaurantId={}, menuId={}, action={}",
                event.getRestaurantId(), event.getMenuId(), event.getAction());
    }

    /**
     * 레스토랑 찜 처리
     */
    private void handleRestaurantWishlist(WishlistChangedEvent event) {
        // 1. Restaurant 조회 (기본 정보만)
        Restaurant restaurant = restaurantRepository
                .findById(event.getRestaurantId())
                .orElseThrow(() -> new RestaurantException(
                        RestaurantErrorCode.RESTAURANT_NOT_FOUND,
                        "레스토랑을 찾을 수 없습니다: " + event.getRestaurantId()
                ));

        // 2. 찜 통계 업데이트
        if (event.getAction() == WishlistChangedEvent.WishlistAction.ADDED) {
            restaurant.incrementWishlistCount();
            log.debug("레스토랑 찜 추가: restaurantId={}, newCount={}",
                    event.getRestaurantId(), restaurant.getWishlistCount());
        } else {
            restaurant.decrementWishlistCount();
            log.debug("레스토랑 찜 제거: restaurantId={}, newCount={}",
                    event.getRestaurantId(), restaurant.getWishlistCount());
        }

        // 3. 변경사항 저장 (더티체킹)
        restaurantRepository.save(restaurant);
    }

    /**
     * 메뉴 찜 처리
     */
    private void handleMenuWishlist(WishlistChangedEvent event) {
        // 1. Restaurant 조회 (메뉴 포함)
        Restaurant restaurant = restaurantRepository
                .findByIdWithMenus(event.getRestaurantId())
                .orElseThrow(() -> new RestaurantException(
                        RestaurantErrorCode.RESTAURANT_NOT_FOUND,
                        "레스토랑을 찾을 수 없습니다: " + event.getRestaurantId()
                ));

        // 2. Menu 조회
        Menu menu;
        try {
            menu = restaurant.findMenuById(event.getMenuId());
        } catch (RestaurantException e) {
            throw new RestaurantException(
                    MenuErrorCode.MENU_NOT_FOUND,
                    "메뉴를 찾을 수 없습니다: " + event.getMenuId(),
                    e
            );
        }

        // 3. 찜 통계 업데이트
        if (event.getAction() == WishlistChangedEvent.WishlistAction.ADDED) {
            menu.incrementWishlistCount();
            log.debug("메뉴 찜 추가: menuId={}, newCount={}",
                    event.getMenuId(), menu.getWishlistCount());
        } else {
            menu.decrementWishlistCount();
            log.debug("메뉴 찜 제거: menuId={}, newCount={}",
                    event.getMenuId(), menu.getWishlistCount());
        }

        // 4. 변경사항 저장 (더티체킹 + Cascade로 하위 엔티티 자동 저장)
        restaurantRepository.save(restaurant);
    }
}