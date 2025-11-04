package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategoryRelation;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RestaurantCategoryRelation JPA Entity
 * - 레스토랑과 카테고리 간의 다대다 관계
 * - 복합키 사용 (restaurantId + categoryId)
 * - BaseEntity를 상속받지 않음 (관계 테이블)
 */
@Entity
@Table(name = "p_restaurant_category_relations")
@IdClass(RestaurantCategoryRelationEntity.RelationId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RestaurantCategoryRelationEntity {

    @Id
    @Column(name = "restaurant_id", length = 36, nullable = false)
    private String restaurantId;

    @Id
    @Column(name = "category_id", length = 36, nullable = false)
    private String categoryId;

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
                .restaurantId(domain.getRestaurantId())
                .categoryId(domain.getCategoryId())
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
                .restaurantId(this.restaurantId)
                .categoryId(this.categoryId)
                .isPrimary(this.isPrimary)
                .createdAt(this.createdAt)
                .createdBy(this.createdBy)
                .isDeleted(this.isDeleted)
                .deletedAt(this.deletedAt)
                .deletedBy(this.deletedBy)
                .build();
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
        private String restaurantId;
        private String categoryId;
    }
}