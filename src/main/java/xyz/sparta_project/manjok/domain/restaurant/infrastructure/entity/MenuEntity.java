package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Menu;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Menu JPA Entity
 * - BaseEntity 상속으로 ID와 createdAt 자동 관리
 * - Restaurant에 종속됨 (영속성 전이)
 * - MenuOptionGroup과 MenuCategoryRelation을 영속성 전이로 관리
 */
@Entity
@Table(name = "p_menus", indexes = {
        @Index(name = "idx_menu_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_menu_is_available", columnList = "is_available"),
        @Index(name = "idx_menu_is_deleted", columnList = "is_deleted")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MenuEntity extends BaseEntity {

    // 소속 정보 (Restaurant와의 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @Setter
    private RestaurantEntity restaurant;

    // 기본 정보
    @Column(name = "menu_name", length = 200, nullable = false)
    private String menuName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "ingredients", length = 500)
    private String ingredients;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    // 상태 정보
    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "is_main")
    @Builder.Default
    private Boolean isMain = false;

    @Column(name = "is_popular")
    @Builder.Default
    private Boolean isPopular = false;

    @Column(name = "is_new")
    @Builder.Default
    private Boolean isNew = false;

    // 영양 정보
    @Column(name = "calorie")
    private Integer calorie;

    // 통계 정보
    @Column(name = "purchase_count")
    @Builder.Default
    private Integer purchaseCount = 0;

    @Column(name = "wishlist_count")
    @Builder.Default
    private Integer wishlistCount = 0;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "review_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal reviewRating = BigDecimal.ZERO;

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
     * Menu → MenuOptionGroup (OneToMany, 영속성 전이)
     */
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MenuOptionGroupEntity> optionGroups = new HashSet<>();

    /**
     * Menu ↔ MenuCategory (ManyToMany via MenuCategoryRelation)
     * 양방향 전이: Menu와 Relation 모두에서 전이
     */
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MenuCategoryRelationEntity> categoryRelations = new HashSet<>();

    // ==================== 연관관계 편의 메서드 ====================

    /**
     * 옵션 그룹 추가 (양방향 관계 설정)
     */
    public void addOptionGroup(MenuOptionGroupEntity optionGroup) {
        optionGroups.add(optionGroup);
        optionGroup.setMenu(this);
    }

    /**
     * 옵션 그룹 제거
     */
    public void removeOptionGroup(MenuOptionGroupEntity optionGroup) {
        optionGroups.remove(optionGroup);
        optionGroup.setMenu(null);
    }

    /**
     * 카테고리 관계 추가 (양방향 관계 설정)
     */
    public void addCategoryRelation(MenuCategoryRelationEntity relation) {
        categoryRelations.add(relation);
        relation.setMenu(this);
    }

    /**
     * 카테고리 관계 제거
     */
    public void removeCategoryRelation(MenuCategoryRelationEntity relation) {
        categoryRelations.remove(relation);
        relation.setMenu(null);
    }

    // ==================== 도메인 ↔ 엔티티 변환 ====================

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static MenuEntity fromDomain(Menu domain) {
        if (domain == null) {
            return null;
        }

        MenuEntity entity = MenuEntity.builder()
                .menuName(domain.getMenuName())
                .description(domain.getDescription())
                .ingredients(domain.getIngredients())
                .price(domain.getPrice())
                .isAvailable(domain.getIsAvailable())
                .isMain(domain.getIsMain())
                .isPopular(domain.getIsPopular())
                .isNew(domain.getIsNew())
                .calorie(domain.getCalorie())
                .purchaseCount(domain.getPurchaseCount())
                .wishlistCount(domain.getWishlistCount())
                .reviewCount(domain.getReviewCount())
                .reviewRating(domain.getReviewRating())
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

        // 하위 엔티티 변환 (영속성 전이)
        domain.getOptionGroups().forEach(optionGroup ->
                entity.addOptionGroup(MenuOptionGroupEntity.fromDomain(optionGroup))
        );

        domain.getCategoryRelations().forEach(relation ->
                entity.addCategoryRelation(MenuCategoryRelationEntity.fromDomain(relation))
        );

        return entity;
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    public Menu toDomain() {
        return Menu.builder()
                .id(this.getId())
                .createdAt(this.getCreatedAt())
                .restaurantId(this.restaurant != null ? this.restaurant.getId() : null)
                .menuName(this.menuName)
                .description(this.description)
                .ingredients(this.ingredients)
                .price(this.price)
                .isAvailable(this.isAvailable)
                .isMain(this.isMain)
                .isPopular(this.isPopular)
                .isNew(this.isNew)
                .calorie(this.calorie)
                .purchaseCount(this.purchaseCount)
                .wishlistCount(this.wishlistCount)
                .reviewCount(this.reviewCount)
                .reviewRating(this.reviewRating)
                .createdBy(this.createdBy)
                .updatedAt(this.updatedAt)
                .updatedBy(this.updatedBy)
                .isDeleted(this.isDeleted)
                .deletedAt(this.deletedAt)
                .deletedBy(this.deletedBy)
                .optionGroups(this.optionGroups.stream()
                        .map(MenuOptionGroupEntity::toDomain)
                        .collect(Collectors.toList()))
                .categoryRelations(this.categoryRelations.stream()
                        .map(MenuCategoryRelationEntity::toDomain)
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