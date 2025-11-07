// FavoriteCommandService.java
package xyz.sparta_project.manjok.domain.favorites.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.favorites.domain.exception.FavoriteErrorCode;
import xyz.sparta_project.manjok.domain.favorites.domain.exception.FavoriteException;
import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;
import xyz.sparta_project.manjok.domain.favorites.domain.repository.FavoriteRepository;
import xyz.sparta_project.manjok.domain.restaurant.domain.event.WishlistChangedEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.infrastructure.Events;

/**
 * 찜하기 커맨드 서비스
 * - 찜하기 생성, 삭제 등 쓰기 작업 처리
 * - 찜하기 변경 시 이벤트 발행
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteCommandService {

    private final FavoriteRepository favoriteRepository;

    /**
     * 레스토랑 찜하기 추가
     */
    public Favorite addRestaurantFavorite(String customerId, String restaurantId) {
        log.info("레스토랑 찜하기 추가 시도 - CustomerId: {}, RestaurantId: {}", customerId, restaurantId);

        // 중복 체크
        if (favoriteRepository.existsByCustomerAndTarget(
                customerId,
                FavoriteType.RESTAURANT,
                restaurantId,
                null
        )) {
            log.warn("이미 찜한 레스토랑 - CustomerId: {}, RestaurantId: {}", customerId, restaurantId);
            throw new FavoriteException(FavoriteErrorCode.ALREADY_FAVORITED);
        }

        try {
            // 도메인 생성
            Favorite favorite = Favorite.createRestaurantFavorite(
                    customerId,
                    restaurantId,
                    customerId
            );

            // 저장
            Favorite savedFavorite = favoriteRepository.save(favorite);

            log.info("레스토랑 찜하기 추가 성공 - FavoriteId: {}, CustomerId: {}, RestaurantId: {}",
                    savedFavorite.getId(), customerId, restaurantId);

            // 이벤트 발행
            publishWishlistAddedEvent(restaurantId, null);

            return savedFavorite;

        } catch (FavoriteException e) {
            throw e;
        } catch (Exception e) {
            log.error("레스토랑 찜하기 추가 실패 - CustomerId: {}, RestaurantId: {}", customerId, restaurantId, e);
            throw new FavoriteException(FavoriteErrorCode.FAVORITE_SAVE_FAILED, e);
        }
    }

    /**
     * 메뉴 찜하기 추가
     */
    public Favorite addMenuFavorite(String customerId, String restaurantId, String menuId) {
        log.info("메뉴 찜하기 추가 시도 - CustomerId: {}, RestaurantId: {}, MenuId: {}",
                customerId, restaurantId, menuId);

        // 중복 체크
        if (favoriteRepository.existsByCustomerAndTarget(
                customerId,
                FavoriteType.MENU,
                restaurantId,
                menuId
        )) {
            log.warn("이미 찜한 메뉴 - CustomerId: {}, RestaurantId: {}, MenuId: {}",
                    customerId, restaurantId, menuId);
            throw new FavoriteException(FavoriteErrorCode.ALREADY_FAVORITED);
        }

        try {
            // 도메인 생성
            Favorite favorite = Favorite.createMenuFavorite(
                    customerId,
                    restaurantId,
                    menuId,
                    customerId
            );

            // 저장
            Favorite savedFavorite = favoriteRepository.save(favorite);

            log.info("메뉴 찜하기 추가 성공 - FavoriteId: {}, CustomerId: {}, RestaurantId: {}, MenuId: {}",
                    savedFavorite.getId(), customerId, restaurantId, menuId);

            // 이벤트 발행
            publishWishlistAddedEvent(restaurantId, menuId);

            return savedFavorite;

        } catch (FavoriteException e) {
            throw e;
        } catch (Exception e) {
            log.error("메뉴 찜하기 추가 실패 - CustomerId: {}, RestaurantId: {}, MenuId: {}",
                    customerId, restaurantId, menuId, e);
            throw new FavoriteException(FavoriteErrorCode.FAVORITE_SAVE_FAILED, e);
        }
    }

    /**
     * 찜하기 취소 (하드 삭제)
     */
    public void removeFavorite(String customerId, String favoriteId) {
        log.info("찜하기 삭제 시도 - CustomerId: {}, FavoriteId: {}", customerId, favoriteId);

        // 찜하기 조회
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> {
                    log.warn("찜하기를 찾을 수 없음 - FavoriteId: {}", favoriteId);
                    return new FavoriteException(FavoriteErrorCode.FAVORITE_NOT_FOUND);
                });

        // 본인 확인
        favorite.validateOwner(customerId);

        try {
            // 삭제 전 정보 저장 (이벤트 발행용)
            String restaurantId = favorite.getRestaurantId();
            String menuId = favorite.getMenuId();

            // 삭제
            favoriteRepository.delete(favoriteId);

            log.info("찜하기 삭제 성공 - FavoriteId: {}, CustomerId: {}", favoriteId, customerId);

            // 이벤트 발행
            publishWishlistRemovedEvent(restaurantId, menuId);

        } catch (FavoriteException e) {
            throw e;
        } catch (Exception e) {
            log.error("찜하기 삭제 실패 - FavoriteId: {}, CustomerId: {}", favoriteId, customerId, e);
            throw new FavoriteException(FavoriteErrorCode.FAVORITE_DELETE_FAILED, e);
        }
    }

    /**
     * 찜하기 추가 이벤트 발행
     */
    private void publishWishlistAddedEvent(String restaurantId, String menuId) {
        try {
            WishlistChangedEvent event = new WishlistChangedEvent(
                    restaurantId,
                    menuId,
                    WishlistChangedEvent.WishlistAction.ADDED
            );

            Events.raise(event);

            log.info("찜하기 추가 이벤트 발행 완료 - RestaurantId: {}, MenuId: {}", restaurantId, menuId);

        } catch (Exception e) {
            log.error("찜하기 추가 이벤트 발행 실패 - RestaurantId: {}, MenuId: {}", restaurantId, menuId, e);
            // 이벤트 발행 실패는 비즈니스 로직에 영향을 주지 않도록 예외를 던지지 않음
        }
    }

    /**
     * 찜하기 제거 이벤트 발행
     */
    private void publishWishlistRemovedEvent(String restaurantId, String menuId) {
        try {
            WishlistChangedEvent event = new WishlistChangedEvent(
                    restaurantId,
                    menuId,
                    WishlistChangedEvent.WishlistAction.REMOVED
            );

            Events.raise(event);

            log.info("찜하기 제거 이벤트 발행 완료 - RestaurantId: {}, MenuId: {}", restaurantId, menuId);

        } catch (Exception e) {
            log.error("찜하기 제거 이벤트 발행 실패 - RestaurantId: {}, MenuId: {}", restaurantId, menuId, e);
            // 이벤트 발행 실패는 비즈니스 로직에 영향을 주지 않도록 예외를 던지지 않음
        }
    }
}