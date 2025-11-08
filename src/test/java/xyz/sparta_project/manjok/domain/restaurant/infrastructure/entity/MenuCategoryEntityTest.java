package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuCategory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenuCategoryEntity 변환 테스트 (연관관계 매핑 적용)
 */
class MenuCategoryEntityTest {

    @Test
    @DisplayName("도메인 MenuCategory를 MenuCategoryEntity로 변환")
    void fromDomain_ShouldConvertMenuCategoryToEntity() {
        // given
        MenuCategory domain = MenuCategory.builder()
                .id("CAT123")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .restaurantId("REST456")
                .categoryName("메인 메뉴")
                .description("주력 메인 메뉴")
                .parentCategoryId(null)
                .depth(1)
                .displayOrder(1)
                .isActive(true)
                .createdBy("owner")
                .build();

        // when
        MenuCategoryEntity entity = MenuCategoryEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("CAT123");
        // Restaurant 연관관계는 null (Restaurant에서 설정해야 함)
        assertThat(entity.getRestaurant()).isNull();
        assertThat(entity.getCategoryName()).isEqualTo("메인 메뉴");
        assertThat(entity.getDescription()).isEqualTo("주력 메인 메뉴");
        assertThat(entity.getParentCategoryId()).isNull();
        assertThat(entity.getDepth()).isEqualTo(1);
        assertThat(entity.getDisplayOrder()).isEqualTo(1);
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo("owner");

        // 연관관계 컬렉션 초기화 확인
        assertThat(entity.getMenuRelations()).isEmpty();
    }

    @Test
    @DisplayName("MenuCategoryEntity를 도메인 MenuCategory로 변환")
    void toDomain_ShouldConvertEntityToMenuCategory() {
        // given
        // Restaurant 엔티티 생성 (연관관계 설정을 위해)
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId("1")
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        MenuCategoryEntity entity = MenuCategoryEntity.builder()
                .categoryName("사이드 메뉴")
                .description("사이드 디쉬")
                .parentCategoryId("CAT123")
                .depth(2)
                .displayOrder(2)
                .isActive(true)
                .createdBy("admin")
                .build();

        // 양방향 연관관계 설정
        restaurant.addMenuCategory(entity);

        // when
        MenuCategory domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getRestaurantId()).isEqualTo(restaurant.getId());
        assertThat(domain.getCategoryName()).isEqualTo("사이드 메뉴");
        assertThat(domain.getDescription()).isEqualTo("사이드 디쉬");
        assertThat(domain.getParentCategoryId()).isEqualTo("CAT123");
        assertThat(domain.getDepth()).isEqualTo(2);
        assertThat(domain.getDisplayOrder()).isEqualTo(2);
        assertThat(domain.getIsActive()).isTrue();
        assertThat(domain.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("MenuCategory와 Restaurant 양방향 연관관계 설정")
    void addMenuCategory_ShouldSetBidirectionalRelation() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId("1")
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        MenuCategoryEntity category = MenuCategoryEntity.builder()
                .categoryName("음료")
                .description("음료수")
                .depth(1)
                .displayOrder(3)
                .isActive(true)
                .createdBy("owner")
                .build();

        // when
        restaurant.addMenuCategory(category);

        // then
        assertThat(restaurant.getMenuCategories()).hasSize(1);
        assertThat(restaurant.getMenuCategories()).contains(category);
        assertThat(category.getRestaurant()).isEqualTo(restaurant);
    }
}