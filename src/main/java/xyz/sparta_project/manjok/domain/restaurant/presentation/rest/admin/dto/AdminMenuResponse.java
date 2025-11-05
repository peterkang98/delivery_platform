package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin용 Menu 응답 DTO
 * - 관리자가 모든 메뉴 정보를 조회할 때 사용
 * - 모든 정보 포함 (삭제된 정보도 포함)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMenuResponse {

    private String menuId;
    private String restaurantId;
    private String restaurantName;
    private String menuName;
    private String description;
    private String ingredients;
    private BigDecimal price;

    // 카테고리
    private List<String> categoryNames;
    private String primaryCategoryName;

    // 상태 정보
    private Boolean isAvailable;
    private Boolean isMain;
    private Boolean isPopular;
    private Boolean isNew;

    // 영양 정보
    private Integer calorie;

    // 통계 정보
    private Integer purchaseCount;
    private Integer wishlistCount;
    private Integer reviewCount;
    private BigDecimal reviewRating;

    // 옵션 그룹 목록
    private List<MenuOptionGroupDto> optionGroups;

    // 감사 정보 (전체)
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuOptionGroupDto {
        private String optionGroupId;
        private String groupName;
        private String description;
        private Boolean isRequired;
        private Integer minSelection;
        private Integer maxSelection;
        private Integer displayOrder;
        private Boolean isActive;
        private List<MenuOptionDto> options;

        // 감사 정보
        private LocalDateTime createdAt;
        private String createdBy;
        private Boolean isDeleted;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuOptionDto {
        private String optionId;
        private String optionName;
        private String description;
        private BigDecimal additionalPrice;
        private Boolean isAvailable;
        private Boolean isDefault;
        private Integer displayOrder;
        private Integer purchaseCount;

        // 감사 정보
        private LocalDateTime createdAt;
        private String createdBy;
        private Boolean isDeleted;
    }
}