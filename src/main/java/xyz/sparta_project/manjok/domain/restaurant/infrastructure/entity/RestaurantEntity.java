package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Address;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Coordinate;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Restaurant;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantStatus;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.vo.AddressVO;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.vo.CoordinateVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Restaurant JPA Entity
 * - BaseEntity를 상속받아 ID와 createdAt 자동 관리
 * - 연관 관계는 ID로만 관리 (메시징 방식)
 * - @OneToMany 사용 안 함
 */
@Entity
@Table(name = "p_restaurants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RestaurantEntity extends BaseEntity {

    // 기본 정보
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "owner_name", length = 100)
    private String ownerName;

    @Column(name = "restaurant_name", nullable = false, length = 200)
    private String restaurantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.OPEN;

    // 주소 정보 (Embedded Value Object)
    @Embedded
    private AddressVO address;

    // 좌표 정보 (Embedded Value Object)
    @Embedded
    private CoordinateVO coordinate;

    // 연락처
    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    // 태그 (JSON 또는 별도 테이블로 관리)
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tagsJson;  // JSON 문자열로 저장

    // 상태
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // 통계 정보
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "wishlist_count")
    @Builder.Default
    private Integer wishlistCount = 0;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "review_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal reviewRating = BigDecimal.ZERO;

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

    // ==================== 도메인 ↔ 엔티티 변환 ====================

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static RestaurantEntity fromDomain(Restaurant domain) {
        if (domain == null) {
            return null;
        }

        RestaurantEntityBuilder builder = RestaurantEntity.builder()
                .ownerId(domain.getOwnerId())
                .ownerName(domain.getOwnerName())
                .restaurantName(domain.getRestaurantName())
                .status(domain.getStatus())
                .contactNumber(domain.getContactNumber())
                .isActive(domain.getIsActive())
                .viewCount(domain.getViewCount())
                .wishlistCount(domain.getWishlistCount())
                .reviewCount(domain.getReviewCount())
                .reviewRating(domain.getReviewRating())
                .purchaseCount(domain.getPurchaseCount())
                .createdBy(domain.getCreatedBy())
                .updatedAt(domain.getUpdatedAt())
                .updatedBy(domain.getUpdatedBy())
                .isDeleted(domain.isDeleted())
                .deletedAt(domain.getDeletedAt())
                .deletedBy(domain.getDeletedBy());

        // Address VO 변환
        if (domain.getAddress() != null) {
            builder.address(AddressVO.fromDomain(domain.getAddress()));
        }

        // Coordinate VO 변환
        if (domain.getCoordinate() != null) {
            builder.coordinate(CoordinateVO.fromDomain(domain.getCoordinate()));
        }

        // Tags 변환 (List<String> -> JSON)
        if (domain.getTags() != null && !domain.getTags().isEmpty()) {
            builder.tagsJson(convertTagsToJson(domain.getTags()));
        }

        RestaurantEntity entity = builder.build();

        // ID 설정 (업데이트의 경우)
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
    public Restaurant toDomain() {
        Restaurant.RestaurantBuilder builder = Restaurant.builder()
                .id(this.getId())
                .createdAt(this.getCreatedAt())
                .ownerId(this.ownerId)
                .ownerName(this.ownerName)
                .restaurantName(this.restaurantName)
                .status(this.status)
                .contactNumber(this.contactNumber)
                .isActive(this.isActive)
                .viewCount(this.viewCount)
                .wishlistCount(this.wishlistCount)
                .reviewCount(this.reviewCount)
                .reviewRating(this.reviewRating)
                .purchaseCount(this.purchaseCount)
                .createdBy(this.createdBy)
                .updatedAt(this.updatedAt)
                .updatedBy(this.updatedBy)
                .isDeleted(this.isDeleted)
                .deletedAt(this.deletedAt)
                .deletedBy(this.deletedBy);

        // Address VO 변환
        if (this.address != null) {
            builder.address(this.address.toDomain());
        }

        // Coordinate VO 변환
        if (this.coordinate != null) {
            builder.coordinate(this.coordinate.toDomain());
        }

        // Tags 변환 (JSON -> List<String>)
        if (this.tagsJson != null && !this.tagsJson.isEmpty()) {
            builder.tags(convertJsonToTags(this.tagsJson));
        }

        return builder.build();
    }

    // ==================== Helper Methods ====================

    /**
     * ID 설정 (도메인에서 가져올 때만 사용)
     */
    private void setIdFromDomain(String id) {
        try {
            java.lang.reflect.Field field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(this, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID from domain", e);
        }
    }

    /**
     * CreatedAt 설정 (도메인에서 가져올 때만 사용)
     */
    private void setCreatedAtFromDomain(LocalDateTime createdAt) {
        try {
            java.lang.reflect.Field field = BaseEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(this, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set createdAt from domain", e);
        }
    }

    /**
     * Tags를 JSON 문자열로 변환
     */
    private static String convertTagsToJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        // 간단한 JSON 배열 형태로 변환 (실제로는 Jackson 사용 권장)
        return "[\"" + String.join("\",\"", tags) + "\"]";
    }

    /**
     * JSON 문자열을 Tags 리스트로 변환
     */
    private static List<String> convertJsonToTags(String json) {
        if (json == null || json.isEmpty() || json.equals("[]")) {
            return new ArrayList<>();
        }
        // 간단한 파싱 (실제로는 Jackson 사용 권장)
        json = json.replaceAll("[\\[\\]\"]", "");
        return List.of(json.split(","));
    }
}