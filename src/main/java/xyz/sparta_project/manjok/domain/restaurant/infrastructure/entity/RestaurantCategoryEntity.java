package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RestaurantCategory JPA Entity
 * - BaseEntity 상속으로 ID와 createdAt 자동 관리
 * - Restaurant와 독립적으로 존재 (ManyToMany 관계)
 * - RestaurantCategoryRelation을 영속성 전이로 관리
 */
@Entity
@Table(name = "p_restaurant_categories", indexes = {
        @Index(name = "idx_restaurant_category_code", columnList = "category_code"),
        @Index(name = "idx_restaurant_category_is_active", columnList = "is_active")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RestaurantCategoryEntity extends BaseEntity {

    // 기본 정보
    @Column(name = "category_code", length = 50, nullable = false, unique = true)
    private String categoryCode;

    @Column(name = "category_name", length = 100, nullable = false)
    private String categoryName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "color_code", length = 20)
    private String colorCode;

    // 계층 구조 (ID 참조만)
    @Column(name = "parent_category_id", length = 36)
    private String parentCategoryId;

    @Column(name = "depth")
    private Integer depth;

    // 운영 정보
    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_popular")
    @Builder.Default
    private Boolean isPopular = false;

    @Column(name = "is_new")
    @Builder.Default
    private Boolean isNew = false;

    // 정책 정보
    @Column(name = "default_minimum_order_amount")
    private Integer defaultMinimumOrderAmount;

    @Column(name = "average_delivery_time")
    private Integer averageDeliveryTime;

    @Column(name = "platform_commission_rate")
    private Double platformCommissionRate;

    // 통계 정보
    @Column(name = "active_restaurant_count")
    @Builder.Default
    private Integer activeRestaurantCount = 0;

    @Column(name = "total_order_count")
    @Builder.Default
    private Integer totalOrderCount = 0;

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
     * RestaurantCategory ↔ Restaurant (ManyToMany via RestaurantCategoryRelation)
     * 양방향 전이: RestaurantCategory와 Relation 모두에서 전이
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RestaurantCategoryRelationEntity> restaurantRelations = new HashSet<>();

    // ==================== 연관관계 편의 메서드 ====================

    /**
     * 레스토랑 관계 추가 (양방향 관계 설정)
     */
    public void addRestaurantRelation(RestaurantCategoryRelationEntity relation) {
        restaurantRelations.add(relation);
        relation.setCategory(this);
    }

    /**
     * 레스토랑 관계 제거
     */
    public void removeRestaurantRelation(RestaurantCategoryRelationEntity relation) {
        restaurantRelations.remove(relation);
        relation.setCategory(null);
    }

    // ==================== 도메인 ↔ 엔티티 변환 ====================

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static RestaurantCategoryEntity fromDomain(RestaurantCategory domain) {
        if (domain == null) {
            return null;
        }

        RestaurantCategoryEntity entity = RestaurantCategoryEntity.builder()
                .categoryCode(domain.getCategoryCode())
                .categoryName(domain.getCategoryName())
                .description(domain.getDescription())
                .iconUrl(domain.getIconUrl())
                .colorCode(domain.getColorCode())
                .parentCategoryId(domain.getParentCategoryId())
                .depth(domain.getDepth())
                .displayOrder(domain.getDisplayOrder())
                .isActive(domain.getIsActive())
                .isPopular(domain.getIsPopular())
                .isNew(domain.getIsNew())
                .defaultMinimumOrderAmount(domain.getDefaultMinimumOrderAmount())
                .averageDeliveryTime(domain.getAverageDeliveryTime())
                .platformCommissionRate(domain.getPlatformCommissionRate())
                .activeRestaurantCount(domain.getActiveRestaurantCount())
                .totalOrderCount(domain.getTotalOrderCount())
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

        // 관계 설정은 별도로 처리 (양방향 참조 방지)
        domain.getRestaurantRelations().forEach(relation ->
                entity.addRestaurantRelation(RestaurantCategoryRelationEntity.fromDomain(relation))
        );

        return entity;
    }

    /**
     * 기존 엔티티를 도메인 정보로 업데이트
     * - JPA 더티체킹을 위한 메서드
     * - ID, createdAt, createdBy는 변경하지 않음
     */
    public void updateFromDomain(RestaurantCategory domain) {
        if (domain == null) {
            return;
        }

        // 기본 정보
        this.categoryCode = domain.getCategoryCode();
        this.categoryName = domain.getCategoryName();
        this.description = domain.getDescription();
        this.iconUrl = domain.getIconUrl();
        this.colorCode = domain.getColorCode();

        // 계층 구조
        this.parentCategoryId = domain.getParentCategoryId();
        this.depth = domain.getDepth();

        // 운영 정보
        this.displayOrder = domain.getDisplayOrder();
        this.isActive = domain.getIsActive();
        this.isPopular = domain.getIsPopular();
        this.isNew = domain.getIsNew();

        // 정책 정보
        this.defaultMinimumOrderAmount = domain.getDefaultMinimumOrderAmount();
        this.averageDeliveryTime = domain.getAverageDeliveryTime();
        this.platformCommissionRate = domain.getPlatformCommissionRate();

        // 통계 정보
        this.activeRestaurantCount = domain.getActiveRestaurantCount();
        this.totalOrderCount = domain.getTotalOrderCount();

        // 감사 정보 (수정 관련만)
        this.updatedAt = domain.getUpdatedAt();
        this.updatedBy = domain.getUpdatedBy();

        // 삭제 정보
        this.isDeleted = domain.getIsDeleted();
        this.deletedAt = domain.getDeletedAt();
        this.deletedBy = domain.getDeletedBy();

        // 관계는 별도로 관리 (필요시 추가 메서드 구현)
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    public RestaurantCategory toDomain() {
        return RestaurantCategory.builder()
                .id(this.getId())
                .createdAt(this.getCreatedAt())
                .categoryCode(this.categoryCode)
                .categoryName(this.categoryName)
                .description(this.description)
                .iconUrl(this.iconUrl)
                .colorCode(this.colorCode)
                .parentCategoryId(this.parentCategoryId)
                .depth(this.depth)
                .displayOrder(this.displayOrder)
                .isActive(this.isActive)
                .isPopular(this.isPopular)
                .isNew(this.isNew)
                .defaultMinimumOrderAmount(this.defaultMinimumOrderAmount)
                .averageDeliveryTime(this.averageDeliveryTime)
                .platformCommissionRate(this.platformCommissionRate)
                .activeRestaurantCount(this.activeRestaurantCount)
                .totalOrderCount(this.totalOrderCount)
                .createdBy(this.createdBy)
                .updatedAt(this.updatedAt)
                .updatedBy(this.updatedBy)
                .isDeleted(this.isDeleted)
                .deletedAt(this.deletedAt)
                .deletedBy(this.deletedBy)
                .restaurantRelations(this.restaurantRelations.stream()
                        .map(RestaurantCategoryRelationEntity::toDomain)
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