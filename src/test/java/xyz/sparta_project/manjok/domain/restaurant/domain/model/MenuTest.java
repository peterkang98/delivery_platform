package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Menu 도메인 테스트")
class MenuTest {

    private Menu menu;
    private final String restaurantId = "REST001";
    private final String userId = "USER001";

    @BeforeEach
    void setUp() {
        menu = Menu.builder()
                .id("MENU001")
                .createdAt(LocalDateTime.now())
                .restaurantId(restaurantId)
                .menuName("치즈버거")
                .description("맛있는 치즈버거")
                .price(new BigDecimal("8000"))
                .isAvailable(true)
                .createdBy(userId)
                .build();
    }

    @Test
    @DisplayName("메뉴 생성 - 정상")
    void createMenu_Success() {
        // then
        assertThat(menu).isNotNull();
        assertThat(menu.getMenuName()).isEqualTo("치즈버거");
        assertThat(menu.getPrice()).isEqualByComparingTo(new BigDecimal("8000"));
        assertThat(menu.getIsAvailable()).isTrue();
        assertThat(menu.getCategoryRelations()).isEmpty();
    }

    @Test
    @DisplayName("카테고리 관계 추가 - 단일 카테고리")
    void addCategoryRelation_Single() {
        // when
        MenuCategoryRelation relation = menu.addCategory("CAT001", true, userId);

        // then
        assertThat(menu.getCategoryRelations()).hasSize(1);
        assertThat(relation).isNotNull();
        assertThat(relation.getCategoryId()).isEqualTo("CAT001");
        assertThat(relation.isPrimary()).isTrue();
        assertThat(menu.getPrimaryCategoryId()).isEqualTo("CAT001");
    }

    @Test
    @DisplayName("카테고리 관계 추가 - 다중 카테고리")
    void addCategoryRelation_Multiple() {
        // when
        menu.addCategory("CAT001", true, userId);   // 주 카테고리
        menu.addCategory("CAT002", false, userId);  // 보조 카테고리
        menu.addCategory("CAT003", false, userId);  // 보조 카테고리

        // then
        assertThat(menu.getCategoryRelations()).hasSize(3);
        assertThat(menu.getActiveCategoryCount()).isEqualTo(3);
        assertThat(menu.getPrimaryCategoryId()).isEqualTo("CAT001");

        Set<String> categoryIds = menu.getActiveCategoryIds();
        assertThat(categoryIds).containsExactlyInAnyOrder("CAT001", "CAT002", "CAT003");
    }

    @Test
    @DisplayName("주 카테고리 변경")
    void changePrimaryCategory() {
        // given
        menu.addCategory("CAT001", true, userId);   // 첫 번째 주 카테고리
        menu.addCategory("CAT002", false, userId);  // 보조 카테고리

        // when
        menu.addCategory("CAT002", true, userId);   // cat-002를 주 카테고리로 변경

        // then
        assertThat(menu.getPrimaryCategoryId()).isEqualTo("CAT002");

        // cat-001은 더 이상 주 카테고리가 아님
        MenuCategoryRelation cat001Relation = menu.getCategoryRelations().stream()
                .filter(r -> r.getCategoryId().equals("CAT001"))
                .findFirst()
                .orElse(null);
        assertThat(cat001Relation).isNotNull();
        assertThat(cat001Relation.isPrimary()).isFalse();
    }

    @Test
    @DisplayName("중복 카테고리 추가 시 기존 관계 반환")
    void addDuplicateCategory() {
        // given
        MenuCategoryRelation firstRelation = menu.addCategory("CAT001", true, userId);

        // when
        MenuCategoryRelation duplicateRelation = menu.addCategory("CAT001", false, userId);

        // then
        assertThat(menu.getCategoryRelations()).hasSize(1);  // 중복 추가되지 않음
        assertThat(duplicateRelation).isEqualTo(firstRelation);  // 기존 관계 반환
    }

    @Test
    @DisplayName("카테고리 관계 제거")
    void removeCategoryRelation() {
        // given
        menu.addCategory("CAT001", true, userId);
        menu.addCategory("CAT002", false, userId);

        // when
        menu.removeCategory("CAT001", userId);

        // then
        assertThat(menu.getActiveCategoryCount()).isEqualTo(1);
        assertThat(menu.getPrimaryCategoryId()).isNull();  // 주 카테고리 제거됨
        assertThat(menu.belongsToCategory("CAT001")).isFalse();
        assertThat(menu.belongsToCategory("CAT002")).isTrue();
    }

    @Test
    @DisplayName("특정 카테고리 소속 확인")
    void belongsToCategory() {
        // given
        menu.addCategory("CAT001", true, userId);
        menu.addCategory("CAT002", false, userId);

        // then
        assertThat(menu.belongsToCategory("CAT001")).isTrue();
        assertThat(menu.belongsToCategory("CAT002")).isTrue();
        assertThat(menu.belongsToCategory("CAT003")).isFalse();
    }

    @Test
    @DisplayName("메뉴 삭제 시 카테고리 관계도 삭제")
    void deleteMenu_WithCategoryRelations() {
        // given
        menu.addCategory("CAT001", true, userId);
        menu.addCategory("CAT002", false, userId);

        // when
        menu.delete(userId);

        // then
        assertThat(menu.getIsDeleted()).isTrue();
        assertThat(menu.getActiveCategoryCount()).isEqualTo(0);  // 활성 카테고리 없음
        assertThat(menu.getPrimaryCategoryId()).isNull();

        // 모든 카테고리 관계가 삭제됨
        menu.getCategoryRelations().forEach(relation -> {
            assertThat(relation.isActive()).isFalse();
            assertThat(relation.isDeleted()).isTrue();
        });
    }

    @Test
    @DisplayName("활성 카테고리 ID 목록 조회")
    void getActiveCategoryIds() {
        // given
        menu.addCategory("CAT001", true, userId);
        menu.addCategory("CAT002", false, userId);
        menu.addCategory("CAT003", false, userId);

        // when
        menu.removeCategory("CAT002", userId);  // 하나 제거

        // then
        Set<String> activeCategoryIds = menu.getActiveCategoryIds();
        assertThat(activeCategoryIds).hasSize(2);
        assertThat(activeCategoryIds).containsExactlyInAnyOrder("CAT001", "CAT003");
    }

    @Test
    @DisplayName("메뉴 가격 설정 - 음수 가격 예외")
    void setPrice_NegativePrice_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> menu.setPrice(new BigDecimal("-1000")))
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.INVALID_MENU_PRICE);
    }

    @Test
    @DisplayName("옵션 그룹과 카테고리 관계 모두 포함한 메뉴")
    void menuWithOptionGroupsAndCategories() {
        // given
        menu.addCategory("CAT001", true, userId);
        menu.addCategory("CAT002", false, userId);

        MenuOptionGroup sizeGroup = menu.addOptionGroup(
                "사이즈 선택",
                "사이즈를 선택하세요",
                true, 1, 1,
                userId
        );

        // then
        assertThat(menu.getCategoryRelations()).hasSize(2);
        assertThat(menu.getOptionGroups()).hasSize(1);
        assertThat(menu.hasRequiredOptions()).isTrue();
        assertThat(menu.getActiveCategoryCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("주문 가능 여부 확인")
    void isOrderable() {
        // given
        menu.addCategory("cat-001", true, userId);

        // then - 정상 상태
        assertThat(menu.isOrderable()).isTrue();

        // when - 판매 중단
        menu.setAvailable(false, userId);

        // then
        assertThat(menu.isOrderable()).isFalse();

        // when - 다시 판매 가능하지만 삭제
        menu.setAvailable(true, userId);
        menu.delete(userId);

        // then
        assertThat(menu.isOrderable()).isFalse();
    }

    @Test
    @DisplayName("메뉴 검증 - 필수 필드 누락")
    void validate_RequiredFields() {
        // given
        Menu invalidMenu = Menu.builder()
                .restaurantId(restaurantId)
                .build();

        // when & then - 메뉴명 누락
        assertThatThrownBy(() -> invalidMenu.validate())
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.MENU_NAME_REQUIRED);

        // given
        Menu noPriceMenu = Menu.builder()
                .menuName("테스트")
                .price(null)
                .build();

        // when & then - 가격 누락
        assertThatThrownBy(() -> noPriceMenu.validate())
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.MENU_PRICE_REQUIRED);
    }

    @Test
    @DisplayName("통계 업데이트")
    void updateStatistics() {
        // when
        menu.incrementPurchaseCount();
        menu.incrementWishlistCount();
        menu.updateReviewStats(10, new BigDecimal("4.5"));

        // then
        assertThat(menu.getPurchaseCount()).isEqualTo(1);
        assertThat(menu.getWishlistCount()).isEqualTo(1);
        assertThat(menu.getReviewCount()).isEqualTo(10);
        assertThat(menu.getReviewRating()).isEqualByComparingTo(new BigDecimal("4.5"));

        // when
        menu.decrementWishlistCount();

        // then
        assertThat(menu.getWishlistCount()).isEqualTo(0);
    }

}