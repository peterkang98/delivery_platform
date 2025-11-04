package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Restaurant;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantStatus;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.vo.AddressVO;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.vo.CoordinateVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Restaurant JPA Entity (Aggregate Root)
 * - BaseEntity를 상속받아 ID와 createdAt 자동 관리
 * - 모든 하위 엔티티를 영속성 전이로 관리
 * - RestaurantCategory와는 ManyToMany 관계 (Relation 테이블)
 */
@Entity
@Table(name = "p_restaurants", indexes = {
        @Index(name = "idx_restaurant_owner_id", columnList = "owner_id"),
        @Index(name = "idx_restaurant_is_active", columnList = "is_active"),
        @Index(name = "idx_restaurant_status", columnList = "status")
})
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

    // 태그 (JSON)
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tagsJson;

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

    // ==================== 연관 관계 (영속성 전이) ====================

    /**
     * Restaurant → Menu (OneToMany, 영속성 전이)
     * orphanRemoval = true: 부모와의 관계가 끊어진 자식은 자동 삭제
     */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuEntity> menus = new ArrayList<>();

    /**
     * Restaurant → MenuCategory (OneToMany, 영속성 전이)
     */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuCategoryEntity> menuCategories = new ArrayList<>();

    /**
     * Restaurant → OperatingDay (OneToMany, 영속성 전이)
     */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<OperatingDayEntity> operatingDays = new HashSet<>();

    /**
     * Restaurant ↔ RestaurantCategory (ManyToMany via RestaurantCategoryRelation)
     * 양방향 전이: Restaurant와 Relation 모두에서 전이
     */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RestaurantCategoryRelationEntity> categoryRelations = new HashSet<>();

    // ==================== 연관관계 편의 메서드 ====================

    /**
     * 메뉴 추가 (양방향 관계 설정)
     */
    public void addMenu(MenuEntity menu) {
        menus.add(menu);
        menu.setRestaurant(this);
    }

    /**
     * 메뉴 제거
     */
    public void removeMenu(MenuEntity menu) {
        menus.remove(menu);
        menu.setRestaurant(null);
    }

    /**
     * 메뉴 카테고리 추가 (양방향 관계 설정)
     */
    public void addMenuCategory(MenuCategoryEntity category) {
        menuCategories.add(category);
        category.setRestaurant(this);
    }

    /**
     * 메뉴 카테고리 제거
     */
    public void removeMenuCategory(MenuCategoryEntity category) {
        menuCategories.remove(category);
        category.setRestaurant(null);
    }

    /**
     * 운영 시간 추가 (양방향 관계 설정)
     */
    public void addOperatingDay(OperatingDayEntity operatingDay) {
        operatingDays.add(operatingDay);
        operatingDay.setRestaurant(this);
    }

    /**
     * 운영 시간 제거
     */
    public void removeOperatingDay(OperatingDayEntity operatingDay) {
        operatingDays.remove(operatingDay);
        operatingDay.setRestaurant(null);
    }

    /**
     * 레스토랑 카테고리 관계 추가 (양방향 관계 설정)
     */
    public void addCategoryRelation(RestaurantCategoryRelationEntity relation) {
        categoryRelations.add(relation);
        relation.setRestaurant(this);
    }

    /**
     * 레스토랑 카테고리 관계 제거
     */
    public void removeCategoryRelation(RestaurantCategoryRelationEntity relation) {
        categoryRelations.remove(relation);
        relation.setRestaurant(null);
    }

    // ==================== 도메인 ↔ 엔티티 변환 ====================

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static RestaurantEntity fromDomain(Restaurant domain) {
        if (domain == null) {
            return null;
        }

        RestaurantEntity entity = RestaurantEntity.builder()
                .ownerId(domain.getOwnerId())
                .ownerName(domain.getOwnerName())
                .restaurantName(domain.getRestaurantName())
                .status(domain.getStatus())
                .address(AddressVO.fromDomain(domain.getAddress()))
                .coordinate(CoordinateVO.fromDomain(domain.getCoordinate()))
                .contactNumber(domain.getContactNumber())
                .tagsJson(convertTagsToJson(domain.getTags()))
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
        domain.getMenus().forEach(menu ->
                entity.addMenu(MenuEntity.fromDomain(menu))
        );

        domain.getMenuCategories().forEach(category ->
                entity.addMenuCategory(MenuCategoryEntity.fromDomain(category))
        );

        domain.getOperatingDays().forEach(operatingDay ->
                entity.addOperatingDay(OperatingDayEntity.fromDomain(operatingDay))
        );

        domain.getCategoryRelations().forEach(relation ->
                entity.addCategoryRelation(RestaurantCategoryRelationEntity.fromDomain(relation))
        );

        return entity;
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    public Restaurant toDomain() {
        return Restaurant.builder()
                .id(this.getId())
                .createdAt(this.getCreatedAt())
                .ownerId(this.ownerId)
                .ownerName(this.ownerName)
                .restaurantName(this.restaurantName)
                .status(this.status)
                .address(this.address != null ? this.address.toDomain() : null)
                .coordinate(this.coordinate != null ? this.coordinate.toDomain() : null)
                .contactNumber(this.contactNumber)
                .tags(convertJsonToTags(this.tagsJson))
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
                .deletedBy(this.deletedBy)
                .menus(this.menus.stream()
                        .map(MenuEntity::toDomain)
                        .collect(Collectors.toList()))
                .menuCategories(this.menuCategories.stream()
                        .map(MenuCategoryEntity::toDomain)
                        .collect(Collectors.toList()))
                .operatingDays(this.operatingDays.stream()
                        .map(OperatingDayEntity::toDomain)
                        .collect(Collectors.toSet()))
                .categoryRelations(this.categoryRelations.stream()
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

    private static String convertTagsToJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return "[\"" + String.join("\",\"", tags) + "\"]";
    }

    private static List<String> convertJsonToTags(String json) {
        if (json == null || json.isEmpty() || json.equals("[]")) {
            return new ArrayList<>();
        }
        json = json.replaceAll("[\\[\\]\"]", "");
        return List.of(json.split(","));
    }
}