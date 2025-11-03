package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenuCategoryRelation 엔티티 단위 테스트
 */

class MenuCategoryRelationTest {

    private MenuCategoryRelation relation;
    private final String menuId = "MENU001";
    private final String categoryId = "CAT001";
    private final String restaurantId = "REST001";
    private final String userId = "USER001";

    @BeforeEach
    void setUp() {
        relation = MenuCategoryRelation.create(
                menuId,
                categoryId,
                restaurantId,
                true,  // isPrimary
                userId
        );
    }

    @Test
    @DisplayName("관계 생성 - 팩토리 메서드")
    void createRelation_Success() {
        // then
        assertThat(relation).isNotNull();
        assertThat(relation.getMenuId()).isEqualTo(menuId);
        assertThat(relation.getCategoryId()).isEqualTo(categoryId);
        assertThat(relation.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(relation.isPrimary()).isTrue();
        assertThat(relation.getCreatedAt()).isNotNull();
        assertThat(relation.getCreatedBy()).isEqualTo(userId);
        assertThat(relation.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("비주요 카테고리 관계 생성")
    void createNonPrimaryRelation() {
        // given
        MenuCategoryRelation nonPrimaryRelation = MenuCategoryRelation.create(
                menuId,
                "CAT002",
                restaurantId,
                false,  // 비주요
                userId
        );

        // then
        assertThat(nonPrimaryRelation.isPrimary()).isFalse();
    }

    @Test
    @DisplayName("관계 활성 상태 확인 - 정상")
    void isActive_Normal() {
        // then
        assertThat(relation.isActive()).isTrue();
    }

    @Test
    @DisplayName("관계 소프트 삭제")
    void deleteRelation() {
        // when
        relation.delete(userId);

        // then
        assertThat(relation.isDeleted()).isTrue();
        assertThat(relation.isActive()).isFalse();
        assertThat(relation.getDeletedAt()).isNotNull();
        assertThat(relation.getDeletedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("주 카테고리 설정 변경")
    void setPrimary() {
        // given
        MenuCategoryRelation secondaryRelation = MenuCategoryRelation.create(
                menuId,
                categoryId,
                restaurantId,
                false,
                userId
        );

        // when
        secondaryRelation.setPrimary(true);

        // then
        assertThat(secondaryRelation.isPrimary()).isTrue();
    }

    @Test
    @DisplayName("equals 및 hashCode - 메뉴ID와 카테고리ID 기반")
    void equalsAndHashCode() {
        // given
        MenuCategoryRelation sameRelation = MenuCategoryRelation.builder()
                .menuId(menuId)
                .categoryId(categoryId)
                .restaurantId("different-restaurant")  // 다른 레스토랑
                .isPrimary(false)  // 다른 primary 상태
                .build();

        MenuCategoryRelation differentMenuRelation = MenuCategoryRelation.builder()
                .menuId("MENU002")  // 다른 메뉴
                .categoryId(categoryId)
                .restaurantId(restaurantId)
                .build();

        MenuCategoryRelation differentCategoryRelation = MenuCategoryRelation.builder()
                .menuId(menuId)
                .categoryId("CAT002")  // 다른 카테고리
                .restaurantId(restaurantId)
                .build();

        // then
        assertThat(relation).isEqualTo(sameRelation);  // menuId와 categoryId가 같으면 equal
        assertThat(relation.hashCode()).isEqualTo(sameRelation.hashCode());

        assertThat(relation).isNotEqualTo(differentMenuRelation);
        assertThat(relation).isNotEqualTo(differentCategoryRelation);
    }

    @Test
    @DisplayName("Builder 패턴 직접 사용")
    void builderPattern() {
        // given
        MenuCategoryRelation builtRelation = MenuCategoryRelation.builder()
                .menuId(menuId)
                .categoryId(categoryId)
                .restaurantId(restaurantId)
                .isPrimary(true)
                .createdAt(LocalDateTime.now())
                .createdBy(userId)
                .isDeleted(false)
                .build();

        // then
        assertThat(builtRelation.getMenuId()).isEqualTo(menuId);
        assertThat(builtRelation.getCategoryId()).isEqualTo(categoryId);
        assertThat(builtRelation.isPrimary()).isTrue();
        assertThat(builtRelation.isActive()).isTrue();
    }

    @Test
    @DisplayName("삭제 후 활성 상태 확인")
    void isActive_AfterDelete() {
        // given
        assertThat(relation.isActive()).isTrue();

        // when
        relation.delete(userId);

        // then
        assertThat(relation.isActive()).isFalse();
    }

    @Test
    @DisplayName("동일 메뉴의 여러 카테고리 관계")
    void multipleCategories_SameMenu() {
        // given
        MenuCategoryRelation primaryRelation = MenuCategoryRelation.create(
                menuId,
                "CAT001",
                restaurantId,
                true,  // 주 카테고리
                userId
        );

        MenuCategoryRelation secondaryRelation1 = MenuCategoryRelation.create(
                menuId,
                "CAT002",
                restaurantId,
                false,  // 보조 카테고리
                userId
        );

        MenuCategoryRelation secondaryRelation2 = MenuCategoryRelation.create(
                menuId,
                "CAT003",
                restaurantId,
                false,  // 보조 카테고리
                userId
        );

        // then
        assertThat(primaryRelation.isPrimary()).isTrue();
        assertThat(secondaryRelation1.isPrimary()).isFalse();
        assertThat(secondaryRelation2.isPrimary()).isFalse();

        // 모두 같은 메뉴를 참조
        assertThat(primaryRelation.getMenuId()).isEqualTo(menuId);
        assertThat(secondaryRelation1.getMenuId()).isEqualTo(menuId);
        assertThat(secondaryRelation2.getMenuId()).isEqualTo(menuId);

        // 서로 다른 카테고리를 참조
        assertThat(primaryRelation.getCategoryId()).isNotEqualTo(secondaryRelation1.getCategoryId());
        assertThat(primaryRelation.getCategoryId()).isNotEqualTo(secondaryRelation2.getCategoryId());
        assertThat(secondaryRelation1.getCategoryId()).isNotEqualTo(secondaryRelation2.getCategoryId());
    }

}