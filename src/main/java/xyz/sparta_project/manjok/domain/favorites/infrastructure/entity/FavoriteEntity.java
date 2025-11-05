// FavoriteEntity.java
package xyz.sparta_project.manjok.domain.favorites.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;

import java.time.LocalDateTime;

/**
 * 찜하기 JPA Entity
 * - BaseEntity를 상속받아 ID와 createdAt 자동 관리
 */
@Entity
@Table(
        name = "p_favorite",
        indexes = {
                @Index(name = "idx_favorite_customer_id", columnList = "customer_id"),
                @Index(name = "idx_favorite_customer_type", columnList = "customer_id, type"),
                @Index(name = "idx_favorite_restaurant_id", columnList = "restaurant_id"),
                @Index(name = "idx_favorite_customer_restaurant", columnList = "customer_id, restaurant_id"),
                @Index(name = "idx_favorite_customer_restaurant_menu", columnList = "customer_id, restaurant_id, menu_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FavoriteEntity extends BaseEntity {

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private FavoriteType type;

    @Column(name = "restaurant_id", nullable = false, length = 36)
    private String restaurantId;

    @Column(name = "menu_id", length = 36)
    private String menuId;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    // ==================== 도메인 ↔ 엔티티 변환 ====================

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static FavoriteEntity fromDomain(Favorite domain) {
        if (domain == null) {
            return null;
        }

        FavoriteEntity entity = FavoriteEntity.builder()
                .customerId(domain.getCustomerId())
                .type(domain.getType())
                .restaurantId(domain.getRestaurantId())
                .menuId(domain.getMenuId())
                .createdBy(domain.getCreatedBy())
                .updatedAt(domain.getUpdatedAt())
                .updatedBy(domain.getUpdatedBy())
                .build();

        // ID와 createdAt 설정 (도메인에서 이미 있는 경우)
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
    public Favorite toDomain() {
        return Favorite.builder()
                .id(this.getId())
                .createdAt(this.getCreatedAt())
                .customerId(this.customerId)
                .type(this.type)
                .restaurantId(this.restaurantId)
                .menuId(this.menuId)
                .createdBy(this.createdBy)
                .updatedAt(this.updatedAt)
                .updatedBy(this.updatedBy)
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