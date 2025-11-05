package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * RestaurantCategoryRelation
 * - 레스토랑과 카테고리 간의 관계
 * */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"restaurantId", "categoryId"})
public class RestaurantCategoryRelation {

    private String restaurantId;
    private String categoryId;
    private boolean isPrimary;

    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    @Builder.Default
    private boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;


    public static RestaurantCategoryRelation create(String restaurantId, String categoryId,
                                                    boolean isPrimary, String createdBy) {
        LocalDateTime now = LocalDateTime.now();
        return RestaurantCategoryRelation.builder()
                .restaurantId(restaurantId)
                .categoryId(categoryId)
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
        LocalDateTime now = LocalDateTime.now();
        this.isDeleted = true;
        this.deletedAt = now;
        this.deletedBy = deletedBy;
        this.updatedAt = now;
        this.updatedBy = deletedBy;
    }

    /**
     * 관계 복구
     */
    public void restore(String updatedBy) {
        LocalDateTime now = LocalDateTime.now();
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
        this.updatedAt = now;
        this.updatedBy = updatedBy;
    }

    /**
     * Primary 여부 변경
     */
    public void updatePrimary(boolean isPrimary, String updatedBy) {
        LocalDateTime now = LocalDateTime.now();
        this.isPrimary = isPrimary;
        this.updatedAt = now;
        this.updatedBy = updatedBy;
    }

    /**
     * 관계가 활성 상태인지 확인
     */
    public boolean isActive() {
        return !isDeleted;
    }
}