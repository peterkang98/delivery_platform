package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RestaurantCategory 도메인 모델 테스트")
class RestaurantCategoryTest {

    @Test
    @DisplayName("카테고리를 생성할 수 있다.")
    void should_create_category() {
        // when
        RestaurantCategory category = RestaurantCategory.builder()
                .id("CAT001")
                .categoryCode("KOREAN")
                .categoryName("한식")
                .description("한국 전통 요리")
                .depth(1)
                .build();

        // then
        assertThat(category.getCategoryCode()).isEqualTo("KOREAN");
        assertThat(category.getCategoryName()).isEqualTo("한식");
        assertThat(category.getIsActive()).isTrue();
        assertThat(category.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("레스토랑의 카테고리에 추가할 수 있다.")
    void should_add_restaurant_to_category() {
        // given
        RestaurantCategory category = RestaurantCategory.builder()
                .id("CAT001")
                .categoryCode("KOREAN")
                .categoryName("한식")
                .build();

        // when
        RestaurantCategoryRelation relation = category.addRestaurant("REST001",true,"admin");

        // then
        assertThat(category.getActiveRestaurantCount()).isEqualTo(1);
        assertThat(category.getRestaurantRelations()).hasSize(1);
        assertThat(relation.isPrimary()).isTrue();
    }

    @Test
    @DisplayName("레스토랑을 카테고리에서 제거할 수 있다.")
    void should_remove_restaurant_from_category() {
        // given
        RestaurantCategory category = RestaurantCategory.builder()
                .id("CAT001")
                .categoryCode("KOREAN")
                .categoryName("한식")
                .build();
        category.addRestaurant("REST001", true, "admin");
        category.addRestaurant("REST002", false, "admin");

        // when
        category.removeRestaurant("REST001");

        // then
        assertThat(category.getActiveRestaurantCount()).isEqualTo(1);
        assertThat(category.getRestaurantRelations()).hasSize(1);
    }

    @Test
    @DisplayName("카테고리 정보를 업데이트할 수 있다.")
    void should_update_category_info() {
        // given
        RestaurantCategory category = RestaurantCategory.builder()
                .id("CAT001")
                .categoryCode("KOREAN")
                .categoryName("한식")
                .build();

        // when
        category.update("한국 요리", "맛있는 한국 음식", "icon.png", "#FF0000", 1, "admin");

        // then
        assertThat(category.getCategoryName()).isEqualTo("한국 요리");
        assertThat(category.getDescription()).isEqualTo("맛있는 한국 음식");
        assertThat(category.getIconUrl()).isEqualTo("icon.png");
        assertThat(category.getColorCode()).isEqualTo("#FF0000");
        assertThat(category.getUpdatedBy()).isEqualTo("admin");
        assertThat(category.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("정책 정보를 설정할 수 있다.")
    void should_set_policy_info() {
        // given
        RestaurantCategory category = RestaurantCategory.builder()
                .id("CAT001")
                .categoryCode("KOREAN")
                .categoryName("한식")
                .build();

        // when
        category.setPolicyInfo(15000, 30, 5.5, "admin");

        // then
        assertThat(category.getDefaultMinimumOrderAmount()).isEqualTo(15000);
        assertThat(category.getAverageDeliveryTime()).isEqualTo(30);
        assertThat(category.getPlatformCommissionRate()).isEqualTo(5.5);
    }

    @Test
    @DisplayName("카테고리를 삭제하고 복구 할 수 있다.")
    void should_delete_and_restore_category() {
        // given
        RestaurantCategory category = RestaurantCategory.builder()
                .id("CAT001")
                .categoryCode("KOREAN")
                .categoryName("한식")
                .build();

        // when - 삭제
        category.delete("admin");

        // then
        assertThat(category.getIsDeleted()).isTrue();
        assertThat(category.getIsActive()).isFalse();
        assertThat(category.getDeletedBy()).isEqualTo("admin");
        assertThat(category.getDeletedAt()).isNotNull();
        assertThat(category.isAvailable()).isFalse();

        // when - 복구
        category.restore("admin");

        // then
        assertThat(category.getIsDeleted()).isFalse();
        assertThat(category.getIsActive()).isTrue();
        assertThat(category.getUpdatedBy()).isEqualTo("admin");
        assertThat(category.getDeletedAt()).isNull();
        assertThat(category.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("최상위 카테고리를 판별할 수 있다.")
    void should_identify_root_category() {
        // given
        RestaurantCategory rootCategory = RestaurantCategory.builder()
                .id("CAT001")
                .depth(1)
                .build();

        RestaurantCategory subCategory = RestaurantCategory.builder()
                .id("CAT002")
                .parentCategoryId("CAT001")
                .depth(2)
                .build();

        // when & then
        assertThat(rootCategory.isRootCategory()).isTrue();
        assertThat(subCategory.isRootCategory()).isFalse();
    }

    @Test
    @DisplayName("통계를 업데이트할 수 있다.")
    void should_update_statistics() {
        // given
        RestaurantCategory category = RestaurantCategory.builder()
                .id("CAT001")
                .categoryCode("KOREAN")
                .categoryName("한식")
                .build();
    }

    @Test
    @DisplayName("인기 카테고리를 설정할 수 있다.")
    void should_set_popular_status() {
        // given
        RestaurantCategory category = RestaurantCategory.builder()
                .id("CAT001")
                .categoryCode("KOREAN")
                .categoryName("한식")
                .build();

        // when
        category.setPopular(true, "admin");

        // then
        assertThat(category.getIsPopular()).isTrue();
        assertThat(category.getUpdatedBy()).isEqualTo("admin");
    }
}