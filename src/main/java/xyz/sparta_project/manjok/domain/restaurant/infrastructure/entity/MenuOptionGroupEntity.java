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
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuOptionGroup;

import java.time.LocalDateTime;

/**
 * MenuOptionGroup JPA Entity
 * - 메뉴의 옵션 그룹 (사이즈 선택, 맵기 선택 등)
 * - BaseEntity 상속으로 ID와 createdAt 자동 관리
 * - 메뉴 ID로만 연관 관계 관리
 */
@Entity
@Table(name = "p_menu_option_groups", indexes = {
        @Index(name = "idx_option_group_menu_id", columnList = "menu_id"),
        @Index(name = "idx_option_group_restaurant_id", columnList = "restaurant_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MenuOptionGroupEntity extends BaseEntity {

    // 소속 정보
    @Column(name = "menu_id", length = 36, nullable = false)
    private String menuId;

    @Column(name = "restaurant_id", length = 36, nullable = false)
    private String restaurantId;

    // 그룹 정보
    @Column(name = "group_name", length = 100, nullable = false)
    private String groupName;

    @Column(name = "description", length = 500)
    private String description;

    // 선택 규칙
    @Column(name = "min_selection", nullable = false)
    @Builder.Default
    private Integer minSelection = 0;

    @Column(name = "max_selection", nullable = false)
    @Builder.Default
    private Integer maxSelection = 1;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;

    // 표시 정보
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

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

    // ==================== 도메인 ↔ 엔티티 변환 ====================

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static MenuOptionGroupEntity fromDomain(MenuOptionGroup domain) {
        if (domain == null) {
            return null;
        }

        MenuOptionGroupEntity entity = MenuOptionGroupEntity.builder()
                .menuId(domain.getMenuId())
                .restaurantId(domain.getRestaurantId())
                .groupName(domain.getGroupName())
                .description(domain.getDescription())
                .minSelection(domain.getMinSelection())
                .maxSelection(domain.getMaxSelection())
                .isRequired(domain.getIsRequired())
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
     * 주의: 하위 옵션들은 포함되지 않음
     */
    public MenuOptionGroup toDomain() {
        return MenuOptionGroup.builder()
                .id(this.getId())
                .createdAt(this.getCreatedAt())
                .menuId(this.menuId)
                .restaurantId(this.restaurantId)
                .groupName(this.groupName)
                .description(this.description)
                .minSelection(this.minSelection)
                .maxSelection(this.maxSelection)
                .isRequired(this.isRequired)
                .displayOrder(this.displayOrder)
                .isActive(this.isActive)
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