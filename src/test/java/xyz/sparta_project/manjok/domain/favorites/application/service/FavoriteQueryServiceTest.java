package xyz.sparta_project.manjok.domain.favorites.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import xyz.sparta_project.manjok.domain.favorites.domain.exception.FavoriteException;
import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;
import xyz.sparta_project.manjok.domain.favorites.domain.repository.FavoriteRepository;
import xyz.sparta_project.manjok.domain.favorites.infrastructure.repository.FavoriteRepositoryImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
        FavoriteRepositoryImpl.class,
        FavoriteQueryService.class
})
@DisplayName("FavoriteQueryService 테스트")
class FavoriteQueryServiceTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private FavoriteQueryService queryService;

    private Favorite createRestaurant(String customerId, String restaurantId) {
        return Favorite.createRestaurantFavorite(customerId, restaurantId, customerId);
    }

    private Favorite createMenu(String customerId, String restaurantId, String menuId) {
        return Favorite.createMenuFavorite(customerId, restaurantId, menuId, customerId);
    }

    @BeforeEach
    void setup() {
        // userA
        favoriteRepository.save(createRestaurant("userA", "rest1"));
        favoriteRepository.save(createMenu("userA", "rest1", "menu1"));

        // userB
        favoriteRepository.save(createRestaurant("userB", "rest2"));
        favoriteRepository.save(createMenu("userB", "rest2", "menu2"));
    }

    @Test
    @DisplayName("favoriteId 로 찜 상세를 조회할 수 있다")
    void getFavorite() {
        // given
        Favorite saved = favoriteRepository.save(createRestaurant("userX", "restX"));

        // when
        Favorite result = queryService.getFavorite(saved.getId());

        // then
        assertThat(result.getCustomerId()).isEqualTo("userX");
        assertThat(result.getRestaurantId()).isEqualTo("restX");
        assertThat(result.getType()).isEqualTo(FavoriteType.RESTAURANT);
    }

    @Test
    @DisplayName("존재하지 않는 FavoriteId 조회 시 예외가 발생한다")
    void getFavorite_NotFound() {
        assertThatThrownBy(() -> queryService.getFavorite("not_exist"))
                .isInstanceOf(FavoriteException.class);
    }

    @Test
    @DisplayName("고객의 전체 찜 목록을 조회할 수 있다")
    void getCustomerFavorites() {
        List<Favorite> list = queryService.getCustomerFavorites("userA");
        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("고객의 레스토랑 찜 목록만 조회할 수 있다")
    void getCustomerRestaurantFavorites() {
        List<Favorite> list = queryService.getCustomerRestaurantFavorites("userA");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getType()).isEqualTo(FavoriteType.RESTAURANT);
    }

    @Test
    @DisplayName("고객의 메뉴 찜 목록만 조회할 수 있다")
    void getCustomerMenuFavorites() {
        List<Favorite> list = queryService.getCustomerMenuFavorites("userA");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getType()).isEqualTo(FavoriteType.MENU);
    }

    @Test
    @DisplayName("특정 대상에 대해 찜 여부를 확인할 수 있다")
    void isFavorite() {
        boolean result = queryService.isFavorite("userA", FavoriteType.MENU, "rest1", "menu1");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("고객의 찜하기 통계를 조회할 수 있다")
    void getCustomerFavoriteStatistics() {
        FavoriteQueryService.FavoriteStatistics stats = queryService.getCustomerFavoriteStatistics("userA");

        assertThat(stats.getTotalCount()).isEqualTo(2);
        assertThat(stats.getRestaurantCount()).isEqualTo(1);
        assertThat(stats.getMenuCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("레스토랑의 찜 개수를 조회할 수 있다")
    void getRestaurantFavoriteCount() {
        long count = queryService.getRestaurantFavoriteCount("rest1");
        assertThat(count).isEqualTo(2); // REST + MENU
    }
}
