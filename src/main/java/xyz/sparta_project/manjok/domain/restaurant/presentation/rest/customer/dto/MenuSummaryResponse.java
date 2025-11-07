package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Customer용 Menu 목록 조회 응답 DTO
 * - 간략한 정보만 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuSummaryResponse {

    private String menuId;
    private String menuName;
    private String description;
    private BigDecimal price;

    // 카테고리
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

    // 옵션 정보
    private Boolean hasOptions;
    private Boolean hasRequiredOptions;
}