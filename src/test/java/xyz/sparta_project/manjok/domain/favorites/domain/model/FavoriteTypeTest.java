package xyz.sparta_project.manjok.domain.favorites.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FavoriteType 이넘 테스트")
class FavoriteTypeTest {

    @Test
    @DisplayName("대소문자를 구분하지 않고 문자열로 FavoriteType 을 찾을 수 있다")
    void fromString_Valid() {
        // given
        String value1 = "RESTAURANT";
        String value2 = "restaurant";
        String value3 = "Restaurant";

        // when & then
        assertThat(FavoriteType.fromString(value1)).isEqualTo(FavoriteType.RESTAURANT);
        assertThat(FavoriteType.fromString(value2)).isEqualTo(FavoriteType.RESTAURANT);
        assertThat(FavoriteType.fromString(value3)).isEqualTo(FavoriteType.RESTAURANT);
    }

    @Test
    @DisplayName("유효하지 않은 문자열을 입력하면 예외가 발생한다")
    void fromString_Invalid() {
        // given
        String invalid = "INVALID";

        // when & then
        assertThatThrownBy(() -> FavoriteType.fromString(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid FavoriteType");
    }

    @Test
    @DisplayName("RESTAURANT 타입은 isRestaurant() 가 true 이다")
    void isRestaurant() {
        // given
        FavoriteType type = FavoriteType.RESTAURANT;

        // when
        boolean result = type.isRestaurant();

        // then
        assertThat(result).isTrue();
        assertThat(type.isMenu()).isFalse();
    }

    @Test
    @DisplayName("MENU 타입은 isMenu() 가 true 이다")
    void isMenu() {
        // given
        FavoriteType type = FavoriteType.MENU;

        // when
        boolean result = type.isMenu();

        // then
        assertThat(result).isTrue();
        assertThat(type.isRestaurant()).isFalse();
    }
}
