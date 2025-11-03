package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * RestaurantCategory Entity
 * - 레스토랑 업종 분류 (한식, 중식, 일식 등)
 * - 관리자가 동적으로 관리 가능
 * - 순수 도메인 모델
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class RestaurantCategory {

    // 식별자
    private String id;
    private LocalDateTime createdAt;

    // 기본 정보
    private String categoryCode;        // KOREAN, CHINESE, JAPANESE 등
    private String categoryName;        // 한식, 중식, 일식 등
    private String description;         // 카테고리 설명
    private String iconUrl;            // 카테고리 아이콘 URL
    private String colorCode;          // 카테고리 색상 코드 (#FF5733 등)

    // 계층 구조
    private String parentCategoryId;    // 상위 카테고리 ID (null이면 최상위)
    private Integer depth;              // 계층 깊이 (1: 대분류, 2: 중분류, 3: 소분류)

    // 운영 정보
    private Integer displayOrder;       // 표시 순서

    @Builder.Default
    private Boolean isActive = true;   // 활성 상태

    @Builder.Default
    private Boolean isPopular = false; // 인기 카테고리 여부

    @Builder.Default
    private Boolean isNew = false;     // 신규 카테고리 여부

    // 정책 정보
    private Integer defaultMinimumOrderAmount;  // 기본 최소 주문 금액
    private Integer averageDeliveryTime;        // 평균 배달 시간 (분)
    private Double platformCommissionRate;      // 플랫폼 수수료율 (%)

    // 통계 정보
    @Builder.Default
    private Integer activeRestaurantCount = 0;  // 활성 레스토랑 수

    @Builder.Default
    private Integer totalOrderCount = 0;        // 총 주문 수

    // 연관 관계
    @Builder.Default
    private Set<RestaurantCategoryRelation> restaurantRelations = new HashSet<>();

    // 감사 필드
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    @Builder.Default
    private Boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;

    // ==================== 비즈니스 메서드 ====================

    /**
     * 레스토랑 연결 추가
     */
    public RestaurantCategoryRelation addRestaurant(String restaurantId, boolean isPrimary, String createdBy) {
        RestaurantCategoryRelation relation = RestaurantCategoryRelation.create(
                restaurantId, this.id, isPrimary, createdBy
        );

        this.restaurantRelations.add(relation);
        this.activeRestaurantCount++;

        return relation;
    }

    /**
     * 레스토랑 연결 제거
     */
    public void removeRestaurant(String restaurantId) {
        this.restaurantRelations.removeIf(r -> r.getRestaurantId().equals(restaurantId));
        if (this.activeRestaurantCount > 0) {
            this.activeRestaurantCount--;
        }
    }

    /**
     * 카테고리 정보 업데이트
     */
    public void update(String categoryName, String description, String iconUrl,
                       String colorCode, Integer displayOrder, String updatedBy) {
        this.categoryName = categoryName;
        this.description = description;
        this.iconUrl = iconUrl;
        this.colorCode = colorCode;
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 정책 정보 설정
     */
    public void setPolicyInfo(Integer minimumOrderAmount, Integer deliveryTime,
                              Double commissionRate, String updatedBy) {
        this.defaultMinimumOrderAmount = minimumOrderAmount;
        this.averageDeliveryTime = deliveryTime;
        this.platformCommissionRate = commissionRate;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 활성/비활성 설정
     */
    public void setActive(boolean active, String updatedBy) {
        this.isActive = active;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 인기 카테고리 설정
     */
    public void setPopular(boolean popular, String updatedBy) {
        this.isPopular = popular;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * Soft Delete
     */
    public void delete(String deletedBy) {
        this.isDeleted = true;
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 복구
     */
    public void restore(String updatedBy) {
        this.isDeleted = false;
        this.isActive = true;
        this.deletedAt = null;
        this.deletedBy = null;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 최상위 카테고리인지 확인
     */
    public boolean isRootCategory() {
        return parentCategoryId == null || depth == 1;
    }

    /**
     * 사용 가능한지 확인
     */
    public boolean isAvailable() {
        return isActive && !isDeleted;
    }

    /**
     * 통계 업데이트
     */
    public void updateStatistics(int orderCount) {
        this.totalOrderCount += orderCount;
    }
}
