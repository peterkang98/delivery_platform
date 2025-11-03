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

    public static RestaurantCategoryRelation create(String restaurantId, String categoryId,
                                                    boolean isPrimary, String createdBy) {
        return RestaurantCategoryRelation.builder()
                .restaurantId(restaurantId)
                .categoryId(categoryId)
                .isPrimary(isPrimary)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
    }
}
