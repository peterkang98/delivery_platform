package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuCategoryRelation;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenuCategoryRelationEntity 변환 테스트 (연관관계 매핑 적용)
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
        // Menu, Category 연관관계는 null (부모에서 설정해야 함)
        assertThat(entity.getMenu()).isNull();
        assertThat(entity.getCategory()).isNull();
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
        // Restaurant, Menu, Category 생성
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId(1L)
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        MenuEntity menu = MenuEntity.builder()
                .menuName("불고기")
                .price(new java.math.BigDecimal("15000"))
                .isAvailable(true)
                .build();

        MenuCategoryEntity category = MenuCategoryEntity.builder()
                .categoryName("메인")
                .depth(1)
                .isActive(true)
                .build();

        // 연관관계 설정
        restaurant.addMenu(menu);
        restaurant.addMenuCategory(category);

        MenuCategoryRelationEntity entity = MenuCategoryRelationEntity.builder()
                .restaurantId("REST012")
                .isPrimary(false)
                .createdAt(LocalDateTime.of(2024, 2, 1, 15, 30))
                .createdBy("admin")
                .isDeleted(false)
                .build();

        // 양방향 연관관계 설정
        menu.addCategoryRelation(entity);
        category.addMenuRelation(entity);

        // when
        MenuCategoryRelation domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getMenuId()).isEqualTo(menu.getId());
        assertThat(domain.getCategoryId()).isEqualTo(category.getId());
        assertThat(domain.getRestaurantId()).isEqualTo("REST012");
        assertThat(domain.isPrimary()).isFalse();
        assertThat(domain.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 2, 1, 15, 30));
        assertThat(domain.getCreatedBy()).isEqualTo("admin");
        assertThat(domain.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Menu와 MenuCategory 간 양방향 연관관계 설정")
    void addCategoryRelation_ShouldSetBidirectionalRelation() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId(1L)
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        MenuEntity menu = MenuEntity.builder()
                .menuName("김치찌개")
                .price(new java.math.BigDecimal("8000"))
                .isAvailable(true)
                .build();

        MenuCategoryEntity category = MenuCategoryEntity.builder()
                .categoryName("찌개류")
                .depth(2)
                .isActive(true)
                .build();

        MenuCategoryRelationEntity relation = MenuCategoryRelationEntity.builder()
                .restaurantId(restaurant.getId())
                .isPrimary(true)
                .createdAt(LocalDateTime.now())
                .createdBy("owner")
                .isDeleted(false)
                .build();

        // when
        menu.addCategoryRelation(relation);
        category.addMenuRelation(relation);

        // then
        assertThat(menu.getCategoryRelations()).hasSize(1);
        assertThat(menu.getCategoryRelations()).contains(relation);
        assertThat(category.getMenuRelations()).hasSize(1);
        assertThat(category.getMenuRelations()).contains(relation);
        assertThat(relation.getMenu()).isEqualTo(menu);
        assertThat(relation.getCategory()).isEqualTo(category);
    }
}