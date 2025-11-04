package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Menu Aggregate Root
 * - 메뉴의 모든 정보와 비즈니스 규칙을 관리
 * - MenuOptionGroup의 생명주기를 관리
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class Menu {

    // 식별자
    private String id;
    private LocalDateTime createdAt;

    // 소속 정보
    private String restaurantId;        // 레스토랑 ID

    // 기본 정보
    private String menuName;            // 메뉴명
    private String description;         // 메뉴 설명
    private String ingredients;         // 식재료 정보

    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO; // 기본 가격

    // 카테고리 관계 관리 (MenuCategoryRelation을 통한 연관)
    @Builder.Default
    private Set<MenuCategoryRelation> categoryRelations = new HashSet<>();

    // 상태 정보
    @Builder.Default
    private Boolean isAvailable = true; // 판매 가능 여부

    @Builder.Default
    private Boolean isMain = false;     // 대표 메뉴 여부

    @Builder.Default
    private Boolean isPopular = false;  // 인기 메뉴 여부

    @Builder.Default
    private Boolean isNew = false;      // 신메뉴 여부

    // 영양 정보
    private Integer calorie;            // 칼로리 (kcal)

    // 통계 정보
    @Builder.Default
    private Integer purchaseCount = 0;  // 구매 횟수

    @Builder.Default
    private Integer wishlistCount = 0;  // 찜 횟수

    @Builder.Default
    private Integer reviewCount = 0;    // 리뷰 개수

    @Builder.Default
    private BigDecimal reviewRating = BigDecimal.ZERO; // 평균 평점

    // 옵션 그룹 관리 (하위 엔티티 직접 관리)
    @Builder.Default
    private List<MenuOptionGroup> optionGroups = new ArrayList<>();

    // 감사 필드
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    @Builder.Default
    private Boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;

    /**
     * 가격 검증 및 설정
     */
    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new RestaurantException(MenuErrorCode.INVALID_MENU_PRICE);
        }
        this.price = price;
    }

    /**
     * 카테고리 관계 추가
     */
    public MenuCategoryRelation addCategory(String categoryId, boolean isPrimary, String createdBy) {
        // 이미 연결된 카테고리 찾기
        MenuCategoryRelation existingRelation = categoryRelations.stream()
                .filter(relation -> relation.getCategoryId().equals(categoryId)
                        && relation.isActive())
                .findFirst()
                .orElse(null);

        // 주 카테고리로 설정하는 경우 기존 주 카테고리 해제
        if (isPrimary) {
            categoryRelations.stream()
                    .filter(MenuCategoryRelation::isActive)
                    .filter(MenuCategoryRelation::isPrimary)
                    .forEach(relation -> relation.setPrimary(false));
        }

        // 이미 존재하는 관계가 있으면 primary 상태만 업데이트
        if (existingRelation != null) {
            existingRelation.setPrimary(isPrimary);
            return existingRelation;
        }

        // 새로운 관계 생성
        MenuCategoryRelation relation = MenuCategoryRelation.create(
                this.id,
                categoryId,
                this.restaurantId,
                isPrimary,
                createdBy
        );

        this.categoryRelations.add(relation);
        return relation;
    }

    /**
     * 카테고리 관계 제거
     */
    public void removeCategory(String categoryId, String deletedBy) {
        categoryRelations.stream()
                .filter(relation -> relation.getCategoryId().equals(categoryId))
                .forEach(relation -> relation.delete(deletedBy));
    }

    /**
     * 주 카테고리 ID 조회
     */
    public String getPrimaryCategoryId() {
        return categoryRelations.stream()
                .filter(relation -> relation.isPrimary() && relation.isActive())
                .map(MenuCategoryRelation::getCategoryId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 활성 카테고리 ID 목록 조회
     */
    public Set<String> getActiveCategoryIds() {
        return categoryRelations.stream()
                .filter(MenuCategoryRelation::isActive)
                .map(MenuCategoryRelation::getCategoryId)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * 특정 카테고리에 속하는지 확인
     */
    public boolean belongsToCategory(String categoryId) {
        return categoryRelations.stream()
                .anyMatch(relation -> relation.getCategoryId().equals(categoryId)
                        && relation.isActive());
    }

    /**
     * 옵션 그룹 추가
     */
    public MenuOptionGroup addOptionGroup(String groupName, String description,
                                          Boolean isRequired, Integer minSelection,
                                          Integer maxSelection, String createdBy) {
        MenuOptionGroup optionGroup = MenuOptionGroup.builder()
                .id(null)  // ID는 나중에 생성
                .createdAt(LocalDateTime.now())
                .menuId(this.id)
                .restaurantId(this.restaurantId)
                .groupName(groupName)
                .description(description)
                .isRequired(isRequired)
                .minSelection(minSelection)
                .maxSelection(maxSelection)
                .displayOrder(this.optionGroups.size())
                .isActive(true)
                .createdBy(createdBy)
                .build();

        optionGroup.validateSelectionRule();
        this.optionGroups.add(optionGroup);
        return optionGroup;
    }

    /**
     * 옵션 그룹 제거
     */
    public void removeOptionGroup(String optionGroupId) {
        this.optionGroups.removeIf(group -> group.getId().equals(optionGroupId));
    }

    /**
     * 메뉴 정보 업데이트
     */
    public void update(String menuName, String description, String ingredients,
                       BigDecimal price, Integer calorie, String updatedBy) {
        this.menuName = menuName;
        this.description = description;
        this.ingredients = ingredients;
        setPrice(price);
        this.calorie = calorie;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 판매 가능 여부 설정
     */
    public void setAvailable(boolean available, String updatedBy) {
        this.isAvailable = available;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 대표 메뉴 설정
     */
    public void setMain(boolean isMain, String updatedBy) {
        this.isMain = isMain;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 인기 메뉴 설정
     */
    public void setPopular(boolean isPopular, String updatedBy) {
        this.isPopular = isPopular;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 통계 업데이트
     */
    public void incrementPurchaseCount() {
        this.purchaseCount++;
    }

    public void incrementWishlistCount() {
        this.wishlistCount++;
    }

    public void decrementWishlistCount() {
        if (this.wishlistCount > 0) {
            this.wishlistCount--;
        }
    }

    public void updateReviewStats(int newReviewCount, BigDecimal newRating) {
        this.reviewCount = newReviewCount;
        this.reviewRating = newRating;
    }

    /**
     * Soft Delete
     */
    public void delete(String deletedBy) {
        if (this.isDeleted) {
            throw new RestaurantException(MenuErrorCode.MENU_ALREADY_DELETED);
        }

        this.isDeleted = true;
        this.isAvailable = false;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;

        // 하위 옵션 그룹들도 삭제 처리
        this.optionGroups.forEach(group -> group.delete(deletedBy));

        // 카테고리 관계도 삭제 처리
        this.categoryRelations.forEach(relation -> relation.delete(deletedBy));
    }

    /**
     * 복구
     */
    public void restore(String updatedBy) {
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 사용 가능한지 확인
     */
    public boolean isOrderable() {
        return isAvailable && !isDeleted;
    }

    /**
     * 필수 옵션 그룹이 있는지 확인
     */
    public boolean hasRequiredOptions() {
        return optionGroups.stream()
                .anyMatch(group -> group.getIsRequired() && group.isAvailable());
    }

    /**
     * 전체 옵션 그룹 중 활성화된 그룹 수
     */
    public int getActiveOptionGroupCount() {
        return (int) optionGroups.stream()
                .filter(MenuOptionGroup::isAvailable)
                .count();
    }

    /**
     * 활성 카테고리 관계 수
     */
    public int getActiveCategoryCount() {
        return (int) categoryRelations.stream()
                .filter(MenuCategoryRelation::isActive)
                .count();
    }

    /**
     * 메뉴 검증
     */
    public void validate() {
        if (menuName == null || menuName.trim().isEmpty()) {
            throw new RestaurantException(MenuErrorCode.MENU_NAME_REQUIRED);
        }
        if (price == null) {
            throw new RestaurantException(MenuErrorCode.MENU_PRICE_REQUIRED);
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new RestaurantException(MenuErrorCode.INVALID_MENU_PRICE);
        }
    }
}
