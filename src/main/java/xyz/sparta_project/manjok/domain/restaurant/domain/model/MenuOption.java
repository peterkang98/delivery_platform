package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MenuOption Entity
 * - 메뉴의 개별 옵션
 * - 예: "사이즈 L", "매운맛", "치즈 추가" 등
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class MenuOption {

    // 식별자
    private String id;
    private LocalDateTime createdAt;

    // 소속 정보
    private String optionGroupId;       // 소속 옵션 그룹 ID
    private String menuId;              // 소속 메뉴 ID
    private String restaurantId;        // 레스토랑 ID

    // 옵션 정보
    private String optionName;          // 옵션명 (예: "Large", "매운맛")
    private String description;         // 옵션 설명

    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;; // 추가 금액

    // 상태 정보
    @Builder.Default
    private Boolean isAvailable = true;  // 판매 가능 여부

    @Builder.Default
    private Boolean isDefault = false;   // 기본 선택 여부

    @Builder.Default
    private Integer displayOrder = 0;    // 표시 순서

    // 통계 정보
    @Builder.Default
    private Integer purchaseCount = 0;   // 구매 횟수

    // 감사 필드
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    @Builder.Default
    private Boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;

    /**
     * 가격 검증 및 설정
     */
    public void setAdditionalPrice(BigDecimal price) {
        if (price.longValue() < 0) {
            throw new RestaurantException(MenuErrorCode.INVALID_OPTION_PRICE);
        }

        this.additionalPrice = price;
    }

    /**
     * 옵션 정보 업데이트
     */
    public void update(String optionName, String description,
                       BigDecimal additionalPrice, Integer displayOrder,
                       String updatedBy) {
        this.optionName = optionName;
        this.description = description;
        setAdditionalPrice(additionalPrice);
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 기본 옵션으로 설정
     */
    public void setDefault(boolean isDefault, String updatedBy) {
        this.isDefault = isDefault;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 판매 가능 여부 설정
     */
    public void setAvailable(boolean available, String updatedBy) {
        this.isAvailable = available;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 구매 카운트 증가
     */
    public void incrementPurchaseCount() {
        this.purchaseCount++;
    }

    /**
     * Soft Delete
     */
    public void delete(String deletedBy) {
        this.isDeleted = true;
        this.isAvailable = false;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 사용 가능한지 확인
     */
    public boolean isSelectable() {
        return isAvailable && !isDeleted;
    }
    /**
     * 판매상태 여부
     * */
    public boolean isAvailable() {
        return this.isAvailable != null && this.isAvailable && !this.isDeleted;
    }
}
