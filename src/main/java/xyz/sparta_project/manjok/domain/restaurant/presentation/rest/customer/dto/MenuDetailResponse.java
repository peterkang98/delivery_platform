package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer용 Menu 상세 조회 응답 DTO
 * - 상세한 정보 포함 (옵션 그룹, 옵션 등)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDetailResponse {

    private String menuId;
    private String restaurantId;
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

    // 생성 시간
    private LocalDateTime createdAt;

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
        private List<MenuOptionDto> options;
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
    }
}