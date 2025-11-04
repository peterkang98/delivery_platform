package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategoryRelation;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RestaurantCategoryRelationEntity 변환 테스트
 */
class RestaurantCategoryRelationEntityTest {

    @Test
    @DisplayName("도메인 RestaurantCategoryRelation을 Entity로 변환")
    void fromDomain_ShouldConvertRelationToEntity() {
        // given
        RestaurantCategoryRelation domain = RestaurantCategoryRelation.builder()
                .restaurantId("REST123")
                .categoryId("CAT456")
                .isPrimary(true)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .createdBy("admin")
                .isDeleted(false)
                .build();

        // when
        RestaurantCategoryRelationEntity entity = RestaurantCategoryRelationEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getRestaurantId()).isEqualTo("REST123");
        assertThat(entity.getCategoryId()).isEqualTo("CAT456");
        assertThat(entity.getIsPrimary()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(entity.getCreatedBy()).isEqualTo("admin");
        assertThat(entity.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("RestaurantCategoryRelationEntity를 도메인으로 변환")
    void toDomain_ShouldConvertEntityToRelation() {
        // given
        RestaurantCategoryRelationEntity entity = RestaurantCategoryRelationEntity.builder()
                .restaurantId("REST789")
                .categoryId("CAT012")
                .isPrimary(false)
                .createdAt(LocalDateTime.of(2024, 2, 1, 15, 30))
                .createdBy("owner")
                .isDeleted(false)
                .build();

        // when
        RestaurantCategoryRelation domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getRestaurantId()).isEqualTo("REST789");
        assertThat(domain.getCategoryId()).isEqualTo("CAT012");
        assertThat(domain.isPrimary()).isFalse();
        assertThat(domain.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 2, 1, 15, 30));
        assertThat(domain.getCreatedBy()).isEqualTo("owner");
        assertThat(domain.isDeleted()).isFalse();
    }
}