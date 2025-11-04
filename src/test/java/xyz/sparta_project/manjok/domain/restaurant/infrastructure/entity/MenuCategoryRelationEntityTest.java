package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuCategoryRelation;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenuCategoryRelationEntity 변환 테스트
 */
class MenuCategoryRelationEntityTest {

    @Test
    @DisplayName("도메인 MenuCategoryRelation을 Entity로 변환")
    void fromDomain_ShouldConvertRelationToEntity() {
        // given
        MenuCategoryRelation domain = MenuCategoryRelation.builder()
                .menuId("MENU123")
                .categoryId("CAT456")
                .restaurantId("REST789")
                .isPrimary(true)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .createdBy("owner")
                .isDeleted(false)
                .build();

        // when
        MenuCategoryRelationEntity entity = MenuCategoryRelationEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getMenuId()).isEqualTo("MENU123");
        assertThat(entity.getCategoryId()).isEqualTo("CAT456");
        assertThat(entity.getRestaurantId()).isEqualTo("REST789");
        assertThat(entity.getIsPrimary()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(entity.getCreatedBy()).isEqualTo("owner");
        assertThat(entity.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("MenuCategoryRelationEntity를 도메인으로 변환")
    void toDomain_ShouldConvertEntityToRelation() {
        // given
        MenuCategoryRelationEntity entity = MenuCategoryRelationEntity.builder()
                .menuId("MENU456")
                .categoryId("CAT789")
                .restaurantId("REST012")
                .isPrimary(false)
                .createdAt(LocalDateTime.of(2024, 2, 1, 15, 30))
                .createdBy("admin")
                .isDeleted(false)
                .build();

        // when
        MenuCategoryRelation domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getMenuId()).isEqualTo("MENU456");
        assertThat(domain.getCategoryId()).isEqualTo("CAT789");
        assertThat(domain.getRestaurantId()).isEqualTo("REST012");
        assertThat(domain.isPrimary()).isFalse();
        assertThat(domain.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 2, 1, 15, 30));
        assertThat(domain.getCreatedBy()).isEqualTo("admin");
        assertThat(domain.isDeleted()).isFalse();
    }
}