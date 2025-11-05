package xyz.sparta_project.manjok.domain.favorites.domain.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;
import xyz.sparta_project.manjok.domain.favorites.infrastructure.repository.FavoriteRepositoryImpl;
import xyz.sparta_project.manjok.domain.favorites.infrastructure.jpa.FavoriteJpaRepository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@Import(FavoriteRepositoryImpl.class) // RepositoryImpl 주입
@DisplayName("FavoriteRepository 테스트")
class FavoriteRepositoryTest {

    @Autowired
    private FavoriteRepositoryImpl repository;

    @Autowired
    private FavoriteJpaRepository jpaRepository;

    private Favorite createRestaurantFavorite(String customerId, String restaurantId) {
        return Favorite.createRestaurantFavorite(customerId, restaurantId, customerId);
    }

    private Favorite createMenuFavorite(String customerId, String restaurantId, String menuId) {
        return Favorite.createMenuFavorite(customerId, restaurantId, menuId, customerId);
    }

    @Test
    @DisplayName("찜하기를 저장할 수 있다")
    void saveFavorite() {
        // given
        Favorite favorite = createRestaurantFavorite("user1", "rest1");

        // when
        Favorite saved = repository.save(favorite);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerId()).isEqualTo("user1");
        assertThat(saved.getRestaurantId()).isEqualTo("rest1");
        assertThat(saved.getType()).isEqualTo(FavoriteType.RESTAURANT);
        assertThat(repository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("ID로 찜하기를 조회할 수 있다")
    void findById() {
        // given
        Favorite favorite = repository.save(createMenuFavorite("user1", "rest1", "menu1"));

        // when
        Optional<Favorite> result = repository.findById(favorite.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMenuId()).isEqualTo("menu1");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 Optional.empty() 가 반환된다")
    void findById_NotFound() {
        // when
        Optional<Favorite> result = repository.findById("not-exist");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("고객 ID로 찜하기 목록을 조회할 수 있다")
    void findByCustomerId() {
        // given
        repository.save(createRestaurantFavorite("userX", "rest1"));
        repository.save(createMenuFavorite("userX", "rest1", "menu1"));

        // when
        List<Favorite> list = repository.findByCustomerId("userX");

        // then
        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("고객 ID와 타입으로 찜하기 목록을 조회할 수 있다")
    void findByCustomerIdAndType() {
        // given
        repository.save(createRestaurantFavorite("user1", "restA"));
        repository.save(createMenuFavorite("user1", "restA", "menuA"));

        // when
        List<Favorite> restaurantFavorites = repository.findByCustomerIdAndType("user1", FavoriteType.RESTAURANT);
        List<Favorite> menuFavorites = repository.findByCustomerIdAndType("user1", FavoriteType.MENU);

        // then
        assertThat(restaurantFavorites).hasSize(1);
        assertThat(menuFavorites).hasSize(1);
    }

    @Test
    @DisplayName("찜하기 삭제 시 실제로 삭제된다")
    void deleteFavorite() {
        // given
        Favorite favorite = repository.save(createRestaurantFavorite("user1", "rest1"));

        // when
        repository.delete(favorite.getId());

        // then
        assertThat(repository.existsById(favorite.getId())).isFalse();
        assertThat(repository.findById(favorite.getId())).isEmpty();
    }

    @Test
    @DisplayName("특정 대상에 대해 찜하기가 존재하는지 확인할 수 있다")
    void existsByCustomerAndTarget() {
        // given
        repository.save(createMenuFavorite("user1", "rest1", "menu1"));

        // when
        boolean exists = repository.existsByCustomerAndTarget("user1", FavoriteType.MENU, "rest1", "menu1");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("고객의 총 찜하기 개수를 조회할 수 있다")
    void countByCustomerId() {
        // given
        repository.save(createRestaurantFavorite("user1", "rest1"));
        repository.save(createMenuFavorite("user1", "rest1", "menu1"));

        // when
        long count = repository.countByCustomerId("user1");

        // then
        assertThat(count).isEqualTo(2);
    }
}
