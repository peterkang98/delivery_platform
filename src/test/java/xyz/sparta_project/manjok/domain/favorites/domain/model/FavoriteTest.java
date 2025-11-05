package xyz.sparta_project.manjok.domain.favorites.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.favorites.domain.exception.FavoriteErrorCode;
import xyz.sparta_project.manjok.domain.favorites.domain.exception.FavoriteException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Favorite 도메인 모델 테스트")
class FavoriteTest {

    @Test
    @DisplayName("레스토랑 찜하기를 생성할 수 있다")
    void createRestaurantFavorite() {
        // given
        String customerId = "user123";
        String restaurantId = "rest001";
        String createdBy = "user123";

        // when
        Favorite favorite = Favorite.createRestaurantFavorite(customerId, restaurantId, createdBy);

        // then
        assertThat(favorite.getCustomerId()).isEqualTo(customerId);
        assertThat(favorite.getType()).isEqualTo(FavoriteType.RESTAURANT);
        assertThat(favorite.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(favorite.getMenuId()).isNull();
        assertThat(favorite.isRestaurantFavorite()).isTrue();
        assertThat(favorite.isMenuFavorite()).isFalse();
    }

    @Test
    @DisplayName("메뉴 찜하기를 생성할 수 있다")
    void createMenuFavorite() {
        // given
        String customerId = "user123";
        String restaurantId = "rest001";
        String menuId = "menu001";
        String createdBy = "user123";

        // when
        Favorite favorite = Favorite.createMenuFavorite(customerId, restaurantId, menuId, createdBy);

        // then
        assertThat(favorite.getCustomerId()).isEqualTo(customerId);
        assertThat(favorite.getType()).isEqualTo(FavoriteType.MENU);
        assertThat(favorite.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(favorite.getMenuId()).isEqualTo(menuId);
        assertThat(favorite.isMenuFavorite()).isTrue();
        assertThat(favorite.isRestaurantFavorite()).isFalse();
    }

    @Test
    @DisplayName("레스토랑 찜하기 생성 시 menuId 가 존재하면 예외가 발생한다")
    void createRestaurantFavorite_InvalidMenuId() {
        // given
        Favorite favorite = Favorite.builder()
                .customerId("user123")
                .type(FavoriteType.RESTAURANT)
                .restaurantId("rest001")
                .menuId("menu001") // 잘못된 입력
                .createdBy("user123")
                .build();

        // when & then
        assertThatThrownBy(favorite::validate)
                .isInstanceOf(FavoriteException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteErrorCode.MENU_ID_NOT_ALLOWED);
    }

    @Test
    @DisplayName("메뉴 찜하기 생성 시 menuId 가 없으면 예외가 발생한다")
    void createMenuFavorite_MenuIdMissing() {
        // given
        Favorite favorite = Favorite.builder()
                .customerId("user123")
                .restaurantId("rest001")
                .type(FavoriteType.MENU)
                .menuId(null)
                .createdBy("user123")
                .build();

        // when & then
        assertThatThrownBy(favorite::validate)
                .isInstanceOf(FavoriteException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteErrorCode.MENU_ID_REQUIRED);
    }

    @Test
    @DisplayName("소유자 검증에 실패하면 예외가 발생한다")
    void validateOwner() {
        // given
        Favorite favorite = Favorite.createRestaurantFavorite("ownerA", "rest001", "ownerA");

        // when & then
        assertThatThrownBy(() -> favorite.validateOwner("otherUser"))
                .isInstanceOf(FavoriteException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteErrorCode.FORBIDDEN_FAVORITE_ACCESS);
    }

    @Test
    @DisplayName("동일한 찜하기 판단이 올바르게 동작한다")
    void isSameFavorite() {
        // given
        Favorite f1 = Favorite.createMenuFavorite("user123", "rest001", "menu001", "user123");
        Favorite f2 = Favorite.createMenuFavorite("user123", "rest001", "menu001", "user123");

        // when
        boolean result = f1.isSameFavorite(f2);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("레스토랑 찜하기와 메뉴 찜하기는 동일하지 않다")
    void isSameFavorite_DifferentType() {
        // given
        Favorite f1 = Favorite.createRestaurantFavorite("user123", "rest001", "user123");
        Favorite f2 = Favorite.createMenuFavorite("user123", "rest001", "menu001", "user123");

        // when
        boolean result = f1.isSameFavorite(f2);

        // then
        assertThat(result).isFalse();
    }
}
