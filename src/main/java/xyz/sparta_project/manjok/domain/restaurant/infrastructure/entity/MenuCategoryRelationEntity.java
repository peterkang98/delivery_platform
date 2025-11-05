package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuCategoryRelation;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MenuCategoryRelation JPA Entity
 * - Menu와 MenuCategory 간의 다대다 관계 중간 테이블
 * - 복합키 사용 (menuId + categoryId)
 * - 양방향 영속성 전이: Menu와 MenuCategory 모두에서 전이
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

    // Menu 측 연관관계
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    @Setter
    private MenuEntity menu;

    // MenuCategory 측 연관관계
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @Setter
    private MenuCategoryEntity category;

    @Column(name = "restaurant_id", length = 36, nullable = false)
    private String restaurantId;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

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
                .restaurantId(domain.getRestaurantId())
                .isPrimary(domain.isPrimary())
                .createdAt(domain.getCreatedAt())
                .createdBy(domain.getCreatedBy())
                .updatedAt(domain.getUpdatedAt())
                .updatedBy(domain.getUpdatedBy())
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
                .menuId(this.menu != null ? this.menu.getId() : null)
                .categoryId(this.category != null ? this.category.getId() : null)
                .restaurantId(this.restaurantId)
                .isPrimary(this.isPrimary)
                .createdAt(this.createdAt)
                .createdBy(this.createdBy)
                .updatedAt(this.updatedAt)
                .updatedBy(this.updatedBy)
                .isDeleted(this.isDeleted)
                .deletedAt(this.deletedAt)
                .deletedBy(this.deletedBy)
                .build();
    }

    // ==================== Helper Methods ====================

    /**
     * Menu ID 조회 (복합키용)
     */
    public String getMenuId() {
        return this.menu != null ? this.menu.getId() : null;
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
        private String menu;     // MenuEntity의 id 필드에 매핑
        private String category; // MenuCategoryEntity의 id 필드에 매핑
    }
}