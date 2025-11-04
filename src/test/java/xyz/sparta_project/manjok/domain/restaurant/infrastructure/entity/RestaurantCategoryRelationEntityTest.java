package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategoryRelation;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RestaurantCategoryRelationEntity 변환 테스트 (연관관계 매핑 적용)
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
        // Restaurant, Category 연관관계는 null (부모에서 설정해야 함)
        assertThat(entity.getRestaurant()).isNull();
        assertThat(entity.getCategory()).isNull();
        assertThat(entity.getIsPrimary()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(entity.getCreatedBy()).isEqualTo("admin");
        assertThat(entity.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("RestaurantCategoryRelationEntity를 도메인으로 변환")
    void toDomain_ShouldConvertEntityToRelation() {
        // given
        // Restaurant, Category 엔티티 생성
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId(1L)
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        RestaurantCategoryEntity category = RestaurantCategoryEntity.builder()
                .categoryCode("KOREAN")
                .categoryName("한식")
                .depth(1)
                .isActive(true)
                .build();

        RestaurantCategoryRelationEntity entity = RestaurantCategoryRelationEntity.builder()
                .isPrimary(false)
                .createdAt(LocalDateTime.of(2024, 2, 1, 15, 30))
                .createdBy("owner")
                .isDeleted(false)
                .build();

        // 양방향 연관관계 설정
        restaurant.addCategoryRelation(entity);
        category.addRestaurantRelation(entity);

        // when
        RestaurantCategoryRelation domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getRestaurantId()).isEqualTo(restaurant.getId());
        assertThat(domain.getCategoryId()).isEqualTo(category.getId());
        assertThat(domain.isPrimary()).isFalse();
        assertThat(domain.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 2, 1, 15, 30));
        assertThat(domain.getCreatedBy()).isEqualTo("owner");
        assertThat(domain.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Restaurant와 RestaurantCategory 간 양방향 연관관계 설정")
    void addCategoryRelation_ShouldSetBidirectionalRelation() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId(1L)
                .restaurantName("맛있는 식당")
                .isActive(true)
                .build();

        RestaurantCategoryEntity category = RestaurantCategoryEntity.builder()
                .categoryCode("CHINESE")
                .categoryName("중식")
                .depth(1)
                .isActive(true)
                .build();

        RestaurantCategoryRelationEntity relation = RestaurantCategoryRelationEntity.builder()
                .isPrimary(true)
                .createdAt(LocalDateTime.now())
                .createdBy("admin")
                .isDeleted(false)
                .build();

        // when
        restaurant.addCategoryRelation(relation);
        category.addRestaurantRelation(relation);

        // then
        assertThat(restaurant.getCategoryRelations()).hasSize(1);
        assertThat(restaurant.getCategoryRelations()).contains(relation);
        assertThat(category.getRestaurantRelations()).hasSize(1);
        assertThat(category.getRestaurantRelations()).contains(relation);
        assertThat(relation.getRestaurant()).isEqualTo(restaurant);
        assertThat(relation.getCategory()).isEqualTo(category);
    }

    @Test
    @DisplayName("Restaurant에 여러 Category 추가")
    void multipleCategories_ShouldWork() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId(1L)
                .restaurantName("퓨전 레스토랑")
                .isActive(true)
                .build();

        RestaurantCategoryEntity koreanCategory = RestaurantCategoryEntity.builder()
                .categoryCode("KOREAN")
                .categoryName("한식")
                .depth(1)
                .isActive(true)
                .build();

        RestaurantCategoryEntity westernCategory = RestaurantCategoryEntity.builder()
                .categoryCode("WESTERN")
                .categoryName("양식")
                .depth(1)
                .isActive(true)
                .build();

        RestaurantCategoryRelationEntity relation1 = RestaurantCategoryRelationEntity.builder()
                .isPrimary(true)
                .createdAt(LocalDateTime.now())
                .createdBy("admin")
                .isDeleted(false)
                .build();

        RestaurantCategoryRelationEntity relation2 = RestaurantCategoryRelationEntity.builder()
                .isPrimary(false)
                .createdAt(LocalDateTime.now())
                .createdBy("admin")
                .isDeleted(false)
                .build();

        // when
        restaurant.addCategoryRelation(relation1);
        koreanCategory.addRestaurantRelation(relation1);

        restaurant.addCategoryRelation(relation2);
        westernCategory.addRestaurantRelation(relation2);

        // then
        assertThat(restaurant.getCategoryRelations()).hasSize(2);
        assertThat(koreanCategory.getRestaurantRelations()).hasSize(1);
        assertThat(westernCategory.getRestaurantRelations()).hasSize(1);
    }
}