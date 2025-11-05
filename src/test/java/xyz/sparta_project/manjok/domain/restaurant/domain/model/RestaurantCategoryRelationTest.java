package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RestaurantCategoryRelation 테스트")
class RestaurantCategoryRelationTest {

    @Test
    @DisplayName("레스토랑과 카테고리 관계를 생성할 수 있다")
    void should_create_relation() {
        // when
        RestaurantCategoryRelation relation = RestaurantCategoryRelation.create(
                "REST001",
                "CAT001",
                true,
                "admin"
        );

        // then
        assertThat(relation.getRestaurantId()).isEqualTo("REST001");
        assertThat(relation.getCategoryId()).isEqualTo("CAT001");
        assertThat(relation.isPrimary()).isTrue();
        assertThat(relation.getCreatedBy()).isEqualTo("admin");
        assertThat(relation.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("부 카테고리로 관계를 생성할 수 있다")
    void should_create_secondary_relation() {
        // when
        RestaurantCategoryRelation relation = RestaurantCategoryRelation.create(
                "REST001",
                "CAT002",
                false,
                "admin"
        );

        // then
        assertThat(relation.isPrimary()).isFalse();
    }

    @Test
    @DisplayName("동등성은 restaurantId와 categoryId로 판단한다")
    void should_check_equality_by_ids() {
        // given
        RestaurantCategoryRelation relation1 = RestaurantCategoryRelation.create(
                "REST001",
                "CAT001",
                true,
                "admin"
        );

        RestaurantCategoryRelation relation2 = RestaurantCategoryRelation.create(
                "REST001",
                "CAT001",
                false,
                "user"
        );

        RestaurantCategoryRelation relation3 = RestaurantCategoryRelation.create(
                "REST001",
                "CAT002",
                true,
                "admin"
        );

        // when & then
        assertThat(relation1).isEqualTo(relation2);  // 같은 restaurant, category
        assertThat(relation1).isNotEqualTo(relation3);  // 다른 category
        assertThat(relation1.hashCode()).isEqualTo(relation2.hashCode());
    }

    @Test
    @DisplayName("관계 삭제 시 isDeleted가 true로 설정되고 비활성 상태가 된다")
    void should_mark_relation_as_deleted_and_inactive() {
        // given
        RestaurantCategoryRelation relation = RestaurantCategoryRelation.create(
                "REST001",
                "CAT001",
                true,
                "admin1"
        );

        // when
        relation.delete("admin2");

        // then
        assertThat(relation.isDeleted()).isTrue();
        assertThat(relation.getDeletedBy()).isEqualTo("admin2");
        assertThat(relation.getDeletedAt()).isNotNull();
        assertThat(relation.isActive()).isFalse();
    }
}