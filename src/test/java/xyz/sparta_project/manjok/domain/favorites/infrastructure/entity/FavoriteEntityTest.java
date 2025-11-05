package xyz.sparta_project.manjok.domain.favorites.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FavoriteEntity 테스트")
class FavoriteEntityTest {

    @Test
    @DisplayName("도메인 모델을 엔티티로 변환할 수 있다")
    void fromDomain() {
        // given
        Favorite domain = Favorite.builder()
                .id("FAV123")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .customerId("USER123")
                .type(FavoriteType.MENU)
                .restaurantId("REST001")
                .menuId("MENU001")
                .createdBy("USER123")
                .updatedAt(LocalDateTime.of(2024, 1, 2, 12, 0))
                .updatedBy("USER321")
                .build();

        // when
        FavoriteEntity entity = FavoriteEntity.fromDomain(domain);

        // then
        assertThat(entity.getId()).isEqualTo("FAV123");
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(entity.getCustomerId()).isEqualTo("USER123");
        assertThat(entity.getType()).isEqualTo(FavoriteType.MENU);
        assertThat(entity.getRestaurantId()).isEqualTo("REST001");
        assertThat(entity.getMenuId()).isEqualTo("MENU001");
        assertThat(entity.getCreatedBy()).isEqualTo("USER123");
        assertThat(entity.getUpdatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 12, 0));
        assertThat(entity.getUpdatedBy()).isEqualTo("USER321");
    }

    @Test
    @DisplayName("엔티티를 도메인 모델로 변환할 수 있다")
    void toDomain() {
        // given - 도메인 → 엔티티 변환 (ID, createdAt 자동 반영)
        Favorite original = Favorite.builder()
                .id("FAV456")
                .createdAt(LocalDateTime.of(2024, 1, 1, 9, 30))
                .customerId("USER123")
                .type(FavoriteType.RESTAURANT)
                .restaurantId("REST001")
                .menuId(null)
                .createdBy("USER123")
                .updatedAt(LocalDateTime.of(2024, 1, 3, 15, 0))
                .updatedBy("USER123")
                .build();

        FavoriteEntity entity = FavoriteEntity.fromDomain(original);

        // when
        Favorite converted = entity.toDomain();

        // then
        assertThat(converted).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("도메인 → 엔티티 → 도메인 변환 시 데이터가 변형되지 않는다 (왕복 테스트)")
    void domainToEntityToDomain() {
        // given
        Favorite original = Favorite.builder()
                .id("FAV999")
                .createdAt(LocalDateTime.of(2024, 1, 1, 8, 0))
                .customerId("USER001")
                .type(FavoriteType.MENU)
                .restaurantId("REST100")
                .menuId("MENU100")
                .createdBy("USER001")
                .updatedAt(LocalDateTime.of(2024, 1, 5, 11, 45))
                .updatedBy("USER900")
                .build();

        // when
        FavoriteEntity entity = FavoriteEntity.fromDomain(original);
        Favorite result = entity.toDomain();

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(original);
    }
}
