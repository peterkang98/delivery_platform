package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * MenuCategoryRelation
 * - 메뉴와 카테고리 간의 다대다 관계를 관리
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"menuId", "categoryId"})
public class MenuCategoryRelation {

    private String menuId;
    private String categoryId;
    private String restaurantId;     // 레스토랑 확인용

    @Builder.Default
    private boolean isPrimary = true; // 주 카테고리 여부

    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    @Builder.Default
    private boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;

    /**
     * 관계 생성 팩토리 메서드
     */
    public static MenuCategoryRelation create(String menuId, String categoryId,
                                              String restaurantId, boolean isPrimary,
                                              String createdBy) {
        LocalDateTime now = LocalDateTime.now();
        return MenuCategoryRelation.builder()
                .menuId(menuId)
                .categoryId(categoryId)
                .restaurantId(restaurantId)
                .isPrimary(isPrimary)
                .createdAt(now)
                .createdBy(createdBy)
                .updatedAt(now)
                .updatedBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * 관계 삭제 (soft delete)
     */
    public void delete(String deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = deletedBy;
    }

    /**
     * 관계 복구
     * - Soft delete된 관계를 재활성화
     * - 카테고리 변경 시 기존 관계 재활용
     */
    public void restore(boolean isPrimary, String updatedBy) {
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
        this.isPrimary = isPrimary;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 관계가 활성 상태인지 확인
     */
    public boolean isActive() {
        return !isDeleted;
    }

    /**
     * 주 카테고리로 설정
     */
    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 주 카테고리로 설정 (updatedBy 포함)
     */
    public void setPrimary(boolean primary, String updatedBy) {
        this.isPrimary = primary;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
}
