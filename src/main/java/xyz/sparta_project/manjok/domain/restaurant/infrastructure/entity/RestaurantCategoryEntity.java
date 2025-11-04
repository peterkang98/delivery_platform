package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategory;

import java.time.LocalDateTime;

/**
 * RestaurantCategory JPA Entity
 * - 레스토랑 업종 분류 (한식, 중식, 일식 등)
 * - BaseEntity 상속으로 ID와 createdAt 자동 관리
 * - 연관 관계는 ID로만 관리
 */
@Entity
@Table(name = "p_restaurant_categories")
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

        return entity;
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