package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuCategory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenuCategoryEntity 변환 테스트
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
        assertThat(entity.getRestaurantId()).isEqualTo("REST456");
        assertThat(entity.getCategoryName()).isEqualTo("메인 메뉴");
        assertThat(entity.getDescription()).isEqualTo("주력 메인 메뉴");
        assertThat(entity.getParentCategoryId()).isNull();
        assertThat(entity.getDepth()).isEqualTo(1);
        assertThat(entity.getDisplayOrder()).isEqualTo(1);
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo("owner");
    }

    @Test
    @DisplayName("MenuCategoryEntity를 도메인 MenuCategory로 변환")
    void toDomain_ShouldConvertEntityToMenuCategory() {
        // given
        MenuCategoryEntity entity = MenuCategoryEntity.builder()
                .restaurantId("REST789")
                .categoryName("사이드 메뉴")
                .description("사이드 디쉬")
                .parentCategoryId("CAT123")
                .depth(2)
                .displayOrder(2)
                .isActive(true)
                .createdBy("admin")
                .build();

        // when
        MenuCategory domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getRestaurantId()).isEqualTo("REST789");
        assertThat(domain.getCategoryName()).isEqualTo("사이드 메뉴");
        assertThat(domain.getDescription()).isEqualTo("사이드 디쉬");
        assertThat(domain.getParentCategoryId()).isEqualTo("CAT123");
        assertThat(domain.getDepth()).isEqualTo(2);
        assertThat(domain.getDisplayOrder()).isEqualTo(2);
        assertThat(domain.getIsActive()).isTrue();
        assertThat(domain.getCreatedBy()).isEqualTo("admin");
    }
}