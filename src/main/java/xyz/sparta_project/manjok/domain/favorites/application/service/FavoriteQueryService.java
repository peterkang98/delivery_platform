// FavoriteQueryService.java
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

import java.util.List;

/**
 * 찜하기 쿼리 서비스
 * - 찜하기 조회 등 읽기 작업 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteQueryService {

    private final FavoriteRepository favoriteRepository;

    /**
     * 찜하기 상세 조회
     */
    public Favorite getFavorite(String favoriteId) {
        log.debug("찜하기 조회 - FavoriteId: {}", favoriteId);

        return favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> {
                    log.warn("찜하기를 찾을 수 없음 - FavoriteId: {}", favoriteId);
                    return new FavoriteException(FavoriteErrorCode.FAVORITE_NOT_FOUND);
                });
    }

    /**
     * 고객의 전체 찜하기 목록 조회
     */
    public List<Favorite> getCustomerFavorites(String customerId) {
        log.debug("고객의 전체 찜하기 조회 - CustomerId: {}", customerId);

        List<Favorite> favorites = favoriteRepository.findByCustomerId(customerId);

        log.debug("고객의 찜하기 조회 완료 - CustomerId: {}, Count: {}", customerId, favorites.size());

        return favorites;
    }

    /**
     * 고객의 타입별 찜하기 목록 조회
     */
    public List<Favorite> getCustomerFavoritesByType(String customerId, FavoriteType type) {
        log.debug("고객의 타입별 찜하기 조회 - CustomerId: {}, Type: {}", customerId, type);

        List<Favorite> favorites = favoriteRepository.findByCustomerIdAndType(customerId, type);

        log.debug("고객의 타입별 찜하기 조회 완료 - CustomerId: {}, Type: {}, Count: {}",
                customerId, type, favorites.size());

        return favorites;
    }

    /**
     * 고객의 레스토랑 찜하기 목록 조회
     */
    public List<Favorite> getCustomerRestaurantFavorites(String customerId) {
        log.debug("고객의 레스토랑 찜하기 조회 - CustomerId: {}", customerId);

        return getCustomerFavoritesByType(customerId, FavoriteType.RESTAURANT);
    }

    /**
     * 고객의 메뉴 찜하기 목록 조회
     */
    public List<Favorite> getCustomerMenuFavorites(String customerId) {
        log.debug("고객의 메뉴 찜하기 조회 - CustomerId: {}", customerId);

        return getCustomerFavoritesByType(customerId, FavoriteType.MENU);
    }

    /**
     * 레스토랑의 찜하기 목록 조회
     */
    public List<Favorite> getRestaurantFavorites(String restaurantId) {
        log.debug("레스토랑의 찜하기 조회 - RestaurantId: {}", restaurantId);

        List<Favorite> favorites = favoriteRepository.findByRestaurantId(restaurantId);

        log.debug("레스토랑의 찜하기 조회 완료 - RestaurantId: {}, Count: {}", restaurantId, favorites.size());

        return favorites;
    }

    /**
     * 찜하기 여부 확인
     */
    public boolean isFavorite(String customerId, FavoriteType type, String restaurantId, String menuId) {
        log.debug("찜하기 여부 확인 - CustomerId: {}, Type: {}, RestaurantId: {}, MenuId: {}",
                customerId, type, restaurantId, menuId);

        return favoriteRepository.existsByCustomerAndTarget(customerId, type, restaurantId, menuId);
    }

    /**
     * 레스토랑 찜하기 여부 확인
     */
    public boolean isRestaurantFavorite(String customerId, String restaurantId) {
        log.debug("레스토랑 찜하기 여부 확인 - CustomerId: {}, RestaurantId: {}", customerId, restaurantId);

        return favoriteRepository.existsByCustomerAndTarget(
                customerId,
                FavoriteType.RESTAURANT,
                restaurantId,
                null
        );
    }

    /**
     * 메뉴 찜하기 여부 확인
     */
    public boolean isMenuFavorite(String customerId, String restaurantId, String menuId) {
        log.debug("메뉴 찜하기 여부 확인 - CustomerId: {}, RestaurantId: {}, MenuId: {}",
                customerId, restaurantId, menuId);

        return favoriteRepository.existsByCustomerAndTarget(
                customerId,
                FavoriteType.MENU,
                restaurantId,
                menuId
        );
    }

    /**
     * 고객의 찜하기 통계 조회
     */
    public FavoriteStatistics getCustomerFavoriteStatistics(String customerId) {
        log.debug("고객의 찜하기 통계 조회 - CustomerId: {}", customerId);

        long totalCount = favoriteRepository.countByCustomerId(customerId);
        long restaurantCount = favoriteRepository.countByCustomerIdAndType(customerId, FavoriteType.RESTAURANT);
        long menuCount = favoriteRepository.countByCustomerIdAndType(customerId, FavoriteType.MENU);

        return new FavoriteStatistics(totalCount, restaurantCount, menuCount);
    }

    /**
     * 레스토랑의 찜하기 통계 조회
     */
    public long getRestaurantFavoriteCount(String restaurantId) {
        log.debug("레스토랑의 찜하기 개수 조회 - RestaurantId: {}", restaurantId);

        return favoriteRepository.countByRestaurantId(restaurantId);
    }

    /**
     * 찜하기 통계 내부 클래스
     */
    public static class FavoriteStatistics {
        private final long totalCount;
        private final long restaurantCount;
        private final long menuCount;

        public FavoriteStatistics(long totalCount, long restaurantCount, long menuCount) {
            this.totalCount = totalCount;
            this.restaurantCount = restaurantCount;
            this.menuCount = menuCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public long getRestaurantCount() {
            return restaurantCount;
        }

        public long getMenuCount() {
            return menuCount;
        }
    }
}