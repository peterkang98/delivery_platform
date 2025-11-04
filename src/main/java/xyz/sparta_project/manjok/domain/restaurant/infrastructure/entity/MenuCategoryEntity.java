package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuCategory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MenuCategory JPA Entity
 * - BaseEntity 상속으로 ID와 createdAt 자동 관리
 * - Restaurant에 종속됨 (영속성 전이)
 * - MenuCategoryRelation을 영속성 전이로 관리
 */
@Entity
@Table(name = "p_menu_categories", indexes = {
        @Index(name = "idx_menu_category_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_menu_category_parent_id", columnList = "parent_category_id"),
        @Index(name = "idx_menu_category_is_active", columnList = "is_active")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MenuCategoryEntity extends BaseEntity {

    // 소속 정보 (Restaurant와의 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @Setter
    private RestaurantEntity restaurant;

    // 기본 정보
    @Column(name = "category_name", length = 100, nullable = false)
    private String categoryName;

    @Column(name = "description", length = 500)
    private String description;

    // 계층 구조 (ID 참조)
    @Column(name = "parent_category_id", length = 36)
    private String parentCategoryId;

    @Column(name = "depth")
    private Integer depth;

    @Column(name = "display_order")
    private Integer displayOrder;

    // 상태 정보
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // 감사 필드
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

    // ==================== 연관 관계 (영속성 전이) ====================

    /**
     * MenuCategory ↔ Menu (ManyToMany via MenuCategoryRelation)
     * 양방향 전이: MenuCategory와 Relation 모두에서 전이
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MenuCategoryRelationEntity> menuRelations = new HashSet<>();

    // ==================== 연관관계 편의 메서드 ====================

    /**
     * 메뉴 관계 추가 (양방향 관계 설정)
     */
    public void addMenuRelation(MenuCategoryRelationEntity relation) {
        menuRelations.add(relation);
        relation.setCategory(this);
    }

    /**
     * 메뉴 관계 제거
     */
    public void removeMenuRelation(MenuCategoryRelationEntity relation) {
        menuRelations.remove(relation);
        relation.setCategory(null);
    }

    // ==================== 도메인 ↔ 엔티티 변환 ====================

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static MenuCategoryEntity fromDomain(MenuCategory domain) {
        if (domain == null) {
            return null;
        }

        MenuCategoryEntity entity = MenuCategoryEntity.builder()
                .categoryName(domain.getCategoryName())
                .description(domain.getDescription())
                .parentCategoryId(domain.getParentCategoryId())
                .depth(domain.getDepth())
                .displayOrder(domain.getDisplayOrder())
                .isActive(domain.getIsActive())
                .createdBy(domain.getCreatedBy())
                .updatedAt(domain.getUpdatedAt())
                .updatedBy(domain.getUpdatedBy())
                .isDeleted(domain.getIsDeleted())
                .deletedAt(domain.getDeletedAt())
                .deletedBy(domain.getDeletedBy())
                .build();

        // ID 설정
        if (domain.getId() != null) {
            entity.setIdFromDomain(domain.getId());
        }
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAtFromDomain(domain.getCreatedAt());
        }

        return entity;
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    public MenuCategory toDomain() {
        return MenuCategory.builder()
                .id(this.getId())
                .createdAt(this.getCreatedAt())
                .restaurantId(this.restaurant != null ? this.restaurant.getId() : null)
                .categoryName(this.categoryName)
                .description(this.description)
                .parentCategoryId(this.parentCategoryId)
                .depth(this.depth)
                .displayOrder(this.displayOrder)
                .isActive(this.isActive)
                .createdBy(this.createdBy)
                .updatedAt(this.updatedAt)
                .updatedBy(this.updatedBy)
                .isDeleted(this.isDeleted)
                .deletedAt(this.deletedAt)
                .deletedBy(this.deletedBy)
                .menuIds(this.menuRelations.stream()
                        .filter(rel -> !rel.getIsDeleted())
                        .map(MenuCategoryRelationEntity::getMenuId)
                        .collect(Collectors.toSet()))
                .build();
    }

    // ==================== Helper Methods ====================

    private void setIdFromDomain(String id) {
        try {
            java.lang.reflect.Field field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(this, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID from domain", e);
        }
    }

    private void setCreatedAtFromDomain(LocalDateTime createdAt) {
        try {
            java.lang.reflect.Field field = BaseEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(this, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set createdAt from domain", e);
        }
    }
}