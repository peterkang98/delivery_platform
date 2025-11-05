// Favorite.java
package xyz.sparta_project.manjok.domain.favorites.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.favorites.domain.exception.FavoriteErrorCode;
import xyz.sparta_project.manjok.domain.favorites.domain.exception.FavoriteException;

import java.time.LocalDateTime;

/**
 * 찜하기 Aggregate Root
 * - 찜하기의 모든 정보와 비즈니스 규칙을 관리
 * - 순수 도메인 모델
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Favorite {

    // 식별자
    private String id;
    private LocalDateTime createdAt;

    // 소유자 정보
    private String customerId;

    // 찜하기 타입
    private FavoriteType type;

    // 대상 정보
    private String restaurantId;
    private String menuId;

    // 감사 필드
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    // ==================== 생성 ====================

    /**
     * 레스토랑 찜하기 생성
     */
    public static Favorite createRestaurantFavorite(String customerId, String restaurantId, String createdBy) {
        Favorite favorite = Favorite.builder()
                .customerId(customerId)
                .type(FavoriteType.RESTAURANT)
                .restaurantId(restaurantId)
                .menuId(null)
                .createdBy(createdBy)
                .build();

        favorite.validate();
        return favorite;
    }

    /**
     * 메뉴 찜하기 생성
     */
    public static Favorite createMenuFavorite(String customerId, String restaurantId, String menuId, String createdBy) {
        Favorite favorite = Favorite.builder()
                .customerId(customerId)
                .type(FavoriteType.MENU)
                .restaurantId(restaurantId)
                .menuId(menuId)
                .createdBy(createdBy)
                .build();

        favorite.validate();
        return favorite;
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 레스토랑 찜하기인지 확인
     */
    public boolean isRestaurantFavorite() {
        return type == FavoriteType.RESTAURANT;
    }

    /**
     * 메뉴 찜하기인지 확인
     */
    public boolean isMenuFavorite() {
        return type == FavoriteType.MENU;
    }

    /**
     * 동일한 찜하기인지 확인
     */
    public boolean isSameFavorite(Favorite other) {
        if (this.type != other.type) return false;
        if (!this.customerId.equals(other.customerId)) return false;
        if (!this.restaurantId.equals(other.restaurantId)) return false;

        if (this.type == FavoriteType.MENU) {
            return this.menuId != null && this.menuId.equals(other.menuId);
        }

        return true;
    }

    /**
     * 소유자 확인
     */
    public boolean isOwnedBy(String customerId) {
        return this.customerId.equals(customerId);
    }

    /**
     * 소유자 검증
     */
    public void validateOwner(String customerId) {
        if (!isOwnedBy(customerId)) {
            throw new FavoriteException(FavoriteErrorCode.FORBIDDEN_FAVORITE_ACCESS);
        }
    }

    // ==================== 유틸리티 메서드 ====================

    /**
     * 검증
     */
    public void validate() {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_CUSTOMER_ID);
        }
        if (type == null) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_FAVORITE_TYPE);
        }
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_RESTAURANT_ID);
        }
        if (type == FavoriteType.MENU && (menuId == null || menuId.trim().isEmpty())) {
            throw new FavoriteException(FavoriteErrorCode.MENU_ID_REQUIRED);
        }
        if (type == FavoriteType.RESTAURANT && menuId != null) {
            throw new FavoriteException(FavoriteErrorCode.MENU_ID_NOT_ALLOWED);
        }
    }
}