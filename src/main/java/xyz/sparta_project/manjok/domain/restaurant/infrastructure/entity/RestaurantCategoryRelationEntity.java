package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategoryRelation;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RestaurantCategoryRelation JPA Entity
 * - Restaurant와 RestaurantCategory 간의 다대다 관계 중간 테이블
 * - 복합키 사용 (restaurantId + categoryId)
 * - 양방향 영속성 전이: Restaurant와 RestaurantCategory 모두에서 전이
 */
@Entity
@Table(name = "p_restaurant_category_relations", indexes = {
        @Index(name = "idx_restaurant_category_rel_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_restaurant_category_rel_category", columnList = "category_id")
})
@IdClass(RestaurantCategoryRelationEntity.RelationId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RestaurantCategoryRelationEntity {

    // Restaurant 측 연관관계
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @Setter
    private RestaurantEntity restaurant;

    // RestaurantCategory 측 연관관계
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @Setter
    private RestaurantCategoryEntity category;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    // ==================== 도메인 ↔ 엔티티 변환 ====================

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static RestaurantCategoryRelationEntity fromDomain(RestaurantCategoryRelation domain) {
        if (domain == null) {
            return null;
        }

        return RestaurantCategoryRelationEntity.builder()
                .isPrimary(domain.isPrimary())
                .createdAt(domain.getCreatedAt())
                .createdBy(domain.getCreatedBy())
                .isDeleted(domain.isDeleted())
                .deletedAt(domain.getDeletedAt())
                .deletedBy(domain.getDeletedBy())
                .build();
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    public RestaurantCategoryRelation toDomain() {
        return RestaurantCategoryRelation.builder()
                .restaurantId(this.restaurant != null ? this.restaurant.getId() : null)
                .categoryId(this.category != null ? this.category.getId() : null)
                .isPrimary(this.isPrimary)
                .createdAt(this.createdAt)
                .createdBy(this.createdBy)
                .isDeleted(this.isDeleted)
                .deletedAt(this.deletedAt)
                .deletedBy(this.deletedBy)
                .build();
    }

    // ==================== Helper Methods ====================

    /**
     * Restaurant ID 조회 (복합키용)
     */
    public String getRestaurantId() {
        return this.restaurant != null ? this.restaurant.getId() : null;
    }

    /**
     * Category ID 조회 (복합키용)
     */
    public String getCategoryId() {
        return this.category != null ? this.category.getId() : null;
    }

    // ==================== 복합키 클래스 ====================

    /**
     * 복합키 클래스
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class RelationId implements Serializable {
        private String restaurant; // RestaurantEntity의 id 필드에 매핑
        private String category;   // RestaurantCategoryEntity의 id 필드에 매핑
    }
}