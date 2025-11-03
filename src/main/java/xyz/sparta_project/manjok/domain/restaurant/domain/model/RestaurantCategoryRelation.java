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

    @Builder.Default
    private boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;


    public static RestaurantCategoryRelation create(String restaurantId, String categoryId,
                                                    boolean isPrimary, String createdBy) {
        return RestaurantCategoryRelation.builder()
                .restaurantId(restaurantId)
                .categoryId(categoryId)
                .isPrimary(isPrimary)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
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
    }

    /**
     * 관계가 활성 상태인지 확인
     */
    public boolean isActive() {
        return !isDeleted;
    }
}
