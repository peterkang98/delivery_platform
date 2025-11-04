package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuCategoryRelation;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MenuCategoryRelation JPA Entity
 * - 메뉴와 카테고리 간의 다대다 관계
 * - 복합키 사용 (menuId + categoryId)
 */
@Entity
@Table(name = "p_menu_category_relations", indexes = {
        @Index(name = "idx_menu_category_rel_menu_id", columnList = "menu_id"),
        @Index(name = "idx_menu_category_rel_category_id", columnList = "category_id"),
        @Index(name = "idx_menu_category_rel_restaurant_id", columnList = "restaurant_id")
})
@IdClass(MenuCategoryRelationEntity.RelationId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MenuCategoryRelationEntity {

    @Id
    @Column(name = "menu_id", length = 36, nullable = false)
    private String menuId;

    @Id
    @Column(name = "category_id", length = 36, nullable = false)
    private String categoryId;

    @Column(name = "restaurant_id", length = 36, nullable = false)
    private String restaurantId;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = true;

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
    public static MenuCategoryRelationEntity fromDomain(MenuCategoryRelation domain) {
        if (domain == null) {
            return null;
        }

        return MenuCategoryRelationEntity.builder()
                .menuId(domain.getMenuId())
                .categoryId(domain.getCategoryId())
                .restaurantId(domain.getRestaurantId())
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
    public MenuCategoryRelation toDomain() {
        return MenuCategoryRelation.builder()
                .menuId(this.menuId)
                .categoryId(this.categoryId)
                .restaurantId(this.restaurantId)
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
        private String menuId;
        private String categoryId;
    }
}
