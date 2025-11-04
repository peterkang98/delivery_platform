package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MenuOption JPA Entity
 * - 메뉴의 개별 옵션 (Large, 매운맛, 치즈 추가 등)
 * - BaseEntity 상속으로 ID와 createdAt 자동 관리
 * - 옵션 그룹 ID로만 연관 관계 관리
 */
@Entity
@Table(name = "p_menu_options", indexes = {
        @Index(name = "idx_menu_option_group_id", columnList = "option_group_id"),
        @Index(name = "idx_menu_option_menu_id", columnList = "menu_id"),
        @Index(name = "idx_menu_option_restaurant_id", columnList = "restaurant_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MenuOptionEntity extends BaseEntity {

    // 소속 정보
    @Column(name = "option_group_id", length = 36, nullable = false)
    private String optionGroupId;

    @Column(name = "menu_id", length = 36, nullable = false)
    private String menuId;

    @Column(name = "restaurant_id", length = 36, nullable = false)
    private String restaurantId;

    // 옵션 정보
    @Column(name = "option_name", length = 100, nullable = false)
    private String optionName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "additional_price", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    // 상태 정보
    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    // 통계 정보
    @Column(name = "purchase_count")
    @Builder.Default
    private Integer purchaseCount = 0;

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

    // 도메인 ↔ 엔티티 변환

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static MenuOptionEntity fromDomain(MenuOption domain) {
        if (domain == null) {
            return null;
        }

        MenuOptionEntity entity = MenuOptionEntity.builder()
                .optionGroupId(domain.getOptionGroupId())
                .menuId(domain.getMenuId())
                .restaurantId(domain.getRestaurantId())
                .optionName(domain.getOptionName())
                .description(domain.getDescription())
                .additionalPrice(domain.getAdditionalPrice())
                .isAvailable(domain.getIsAvailable())
                .isDefault(domain.getIsDefault())
                .displayOrder(domain.getDisplayOrder())
                .purchaseCount(domain.getPurchaseCount())
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
    public MenuOption toDomain() {
        return MenuOption.builder()
                .id(this.getId())
                .createdAt(this.getCreatedAt())
                .optionGroupId(this.optionGroupId)
                .menuId(this.menuId)
                .restaurantId(this.restaurantId)
                .optionName(this.optionName)
                .description(this.description)
                .additionalPrice(this.additionalPrice)
                .isAvailable(this.isAvailable)
                .isDefault(this.isDefault)
                .displayOrder(this.displayOrder)
                .purchaseCount(this.purchaseCount)
                .createdBy(this.createdBy)
                .updatedAt(this.updatedAt)
                .updatedBy(this.updatedBy)
                .isDeleted(this.isDeleted)
                .deletedAt(this.deletedAt)
                .deletedBy(this.deletedBy)
                .build();
    }

    // Helper Methods

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