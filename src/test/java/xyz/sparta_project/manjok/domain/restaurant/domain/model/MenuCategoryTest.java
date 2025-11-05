package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MenuCategory 도메인 모델 테스트")
class MenuCategoryTest {

    private MenuCategory category;
    private final String restaurantId = "REST001";
    private final String userId = "USER001";

    @BeforeEach
    void setUp() {
        category = MenuCategory.builder()
                .id("CAT001")
                .createdAt(LocalDateTime.now())
                .restaurantId(restaurantId)
                .categoryName("메인 메뉴")
                .description("주요 메뉴 카테고리")
                .parentCategoryId(null)
                .depth(1)
                .displayOrder(1)
                .isActive(true)
                .createdBy(userId)
                .build();
    }

    @Test
    @DisplayName("메뉴 카테고리 생성 - 정상")
    void createCategory_Success() {
        // then
        assertThat(category).isNotNull();
        assertThat(category.getCategoryName()).isEqualTo("메인 메뉴");
        assertThat(category.getDepth()).isEqualTo(1);
        assertThat(category.getIsActive()).isTrue();
        assertThat(category.isRootCategory()).isTrue();
    }

    @Test
    @DisplayName("하위 카테고리 생성")
    void createSubCategory() {
        // given
        MenuCategory subCategory = MenuCategory.builder()
                .id("CAT002")
                .createdAt(LocalDateTime.now())
                .restaurantId(restaurantId)
                .categoryName("버거")
                .description("버거 메뉴")
                .parentCategoryId(category.getId())
                .depth(2)
                .displayOrder(1)
                .isActive(true)
                .createdBy(userId)
                .build();

        // then
        assertThat(subCategory.isRootCategory()).isFalse();
        assertThat(subCategory.getParentCategoryId()).isEqualTo(category.getId());
        assertThat(subCategory.getDepth()).isEqualTo(2);
    }

    @Test
    @DisplayName("메뉴 추가/제거")
    void addAndRemoveMenu() {
        // given
        String menuId1 = "MENU001";
        String menuId2 = "MENU002";

        // when - 메뉴 추가
        category.addMenu(menuId1);
        category.addMenu(menuId2);

        // then
        assertThat(category.getMenuIds()).hasSize(2);
        assertThat(category.getMenuIds()).contains(menuId1, menuId2);

        // when - 메뉴 제거
        category.removeMenu(menuId1);

        // then
        assertThat(category.getMenuIds()).hasSize(1);
        assertThat(category.getMenuIds()).doesNotContain(menuId1);
        assertThat(category.getMenuIds()).contains(menuId2);
    }

    @Test
    @DisplayName("중복 메뉴 추가 시 중복 제거")
    void addDuplicateMenu() {
        // given
        String menuId = "MENU001";

        // when
        category.addMenu(menuId);
        category.addMenu(menuId);  // 중복 추가

        // then
        assertThat(category.getMenuIds()).hasSize(1);  // Set이므로 중복 제거
    }

    @Test
    @DisplayName("카테고리 정보 업데이트")
    void updateCategory() {
        // when
        category.update(
                "사이드 메뉴",
                "사이드 메뉴 카테고리",
                2,
                userId
        );

        // then
        assertThat(category.getCategoryName()).isEqualTo("사이드 메뉴");
        assertThat(category.getDescription()).isEqualTo("사이드 메뉴 카테고리");
        assertThat(category.getDisplayOrder()).isEqualTo(2);
        assertThat(category.getUpdatedAt()).isNotNull();
        assertThat(category.getUpdatedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("카테고리 활성/비활성 설정")
    void setActive() {
        // when
        category.setActive(false, userId);

        // then
        assertThat(category.getIsActive()).isFalse();
        assertThat(category.isAvailable()).isFalse();
        assertThat(category.getUpdatedAt()).isNotNull();

        // when
        category.setActive(true, userId);

        // then
        assertThat(category.getIsActive()).isTrue();
        assertThat(category.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("카테고리 소프트 삭제")
    void deleteCategory() {
        // when
        category.delete(userId);

        // then
        assertThat(category.getIsDeleted()).isTrue();
        assertThat(category.getIsActive()).isFalse();
        assertThat(category.isAvailable()).isFalse();
        assertThat(category.getDeletedAt()).isNotNull();
        assertThat(category.getDeletedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("최상위 카테고리 확인 - parentId가 null")
    void isRootCategory_NullParent() {
        // given
        MenuCategory rootCategory = MenuCategory.builder()
                .parentCategoryId(null)
                .depth(1)
                .build();

        // then
        assertThat(rootCategory.isRootCategory()).isTrue();
    }

    @Test
    @DisplayName("최상위 카테고리 확인 - depth가 1")
    void isRootCategory_DepthOne() {
        // given
        MenuCategory rootCategory = MenuCategory.builder()
                .parentCategoryId("some-parent")  // parent가 있지만
                .depth(1)  // depth가 1
                .build();

        // then
        assertThat(rootCategory.isRootCategory()).isTrue();
    }

    @Test
    @DisplayName("사용 가능 여부 확인 - 활성 상태")
    void isAvailable_Active() {
        // then
        assertThat(category.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("사용 가능 여부 확인 - 비활성 상태")
    void isAvailable_Inactive() {
        // when
        category.setActive(false, userId);

        // then
        assertThat(category.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("사용 가능 여부 확인 - 삭제된 상태")
    void isAvailable_Deleted() {
        // when
        category.delete(userId);

        // then
        assertThat(category.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("null 메뉴 ID 추가 시 무시")
    void addNullMenu() {
        // given
        int initialSize = category.getMenuIds().size();

        // when
        category.addMenu(null);

        // then
        assertThat(category.getMenuIds()).hasSize(initialSize);
    }

}