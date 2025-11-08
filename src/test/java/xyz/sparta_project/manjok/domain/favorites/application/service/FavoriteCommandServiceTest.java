package xyz.sparta_project.manjok.domain.favorites.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import xyz.sparta_project.manjok.domain.favorites.domain.exception.FavoriteException;
import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;
import xyz.sparta_project.manjok.domain.favorites.domain.repository.FavoriteRepository;
import xyz.sparta_project.manjok.domain.favorites.infrastructure.repository.FavoriteRepositoryImpl;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.WishlistChangedEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.infrastructure.Events;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import({
        FavoriteRepositoryImpl.class,
        FavoriteCommandService.class
})
@DisplayName("FavoriteCommandService 테스트")
class FavoriteCommandServiceTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private FavoriteCommandService commandService;

    @BeforeEach
    void setup() {
        // userA 기본 데이터 준비
        favoriteRepository.save(Favorite.createRestaurantFavorite("userA", "rest1", "userA"));
        favoriteRepository.save(Favorite.createMenuFavorite("userA", "rest1", "menu1", "userA"));
    }

    @Test
    @DisplayName("레스토랑 찜하기 추가 시 DB에 저장되고 이벤트가 발행된다")
    void addRestaurantFavorite() {
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {

            // when
            Favorite result = commandService.addRestaurantFavorite("userB", "rest2");

            // then: DB 검증
            assertThat(result.getCustomerId()).isEqualTo("userB");
            assertThat(result.getRestaurantId()).isEqualTo("rest2");
            assertThat(result.getType()).isEqualTo(FavoriteType.RESTAURANT);

            // then: 이벤트 발행 검증
            eventsMock.verify(() -> Events.raise(any(WishlistChangedEvent.class)), times(1));
        }
    }

    @Test
    @DisplayName("이미 찜한 레스토랑이면 예외가 발생한다")
    void addRestaurantFavorite_Duplicate() {
        // userA 이미 rest1을 찜함
        assertThatThrownBy(() -> commandService.addRestaurantFavorite("userA", "rest1"))
                .isInstanceOf(FavoriteException.class);
    }

    @Test
    @DisplayName("메뉴 찜하기 추가 시 DB에 저장되고 이벤트가 발행된다")
    void addMenuFavorite() {
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {

            // when
            Favorite result = commandService.addMenuFavorite("userB", "rest3", "menuX");

            // then: DB 검증
            assertThat(result.getCustomerId()).isEqualTo("userB");
            assertThat(result.getRestaurantId()).isEqualTo("rest3");
            assertThat(result.getMenuId()).isEqualTo("menuX");
            assertThat(result.getType()).isEqualTo(FavoriteType.MENU);

            // then: 이벤트 발행 검증
            eventsMock.verify(() -> Events.raise(any(WishlistChangedEvent.class)), times(1));
        }
    }

    @Test
    @DisplayName("찜하기 삭제 시 DB에서 삭제되고 이벤트가 발행된다")
    void removeFavorite() {
        Favorite favorite = favoriteRepository.save(
                Favorite.createRestaurantFavorite("userX", "rest9", "userX")
        );

        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {

            // when
            commandService.removeFavorite("userX", favorite.getId());

            // then: DB에서 사라져야 함
            assertThat(favoriteRepository.findById(favorite.getId())).isEmpty();

            // then: 이벤트 발행 검증
            eventsMock.verify(() -> Events.raise(any(WishlistChangedEvent.class)), times(1));
        }
    }

    @Test
    @DisplayName("본인이 아닌 찜을 삭제하려고 하면 예외가 발생한다")
    void removeFavorite_Forbidden() {
        Favorite favorite = favoriteRepository.save(
                Favorite.createRestaurantFavorite("ownerUser", "rest100", "ownerUser")
        );

        assertThatThrownBy(() -> commandService.removeFavorite("otherUser", favorite.getId()))
                .isInstanceOf(FavoriteException.class);
    }
}
