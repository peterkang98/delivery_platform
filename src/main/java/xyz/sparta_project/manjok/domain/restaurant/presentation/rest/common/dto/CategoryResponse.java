package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Customer용 카테고리 응답 DTO
 * - 계층 구조를 포함한 카테고리 정보 제공
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    // 기본 정보
    private String id;
    private String categoryCode;
    private String categoryName;
    private String description;
    private String iconUrl;
    private String colorCode;

    // 계층 정보
    private String parentCategoryId;
    private String parentCategoryName;  // 부모 카테고리 이름
    private Integer depth;              // 1: 대분류, 2: 중분류, 3: 소분류

    // 표시 정보
    private Integer displayOrder;
    private Boolean isActive;
    private Boolean isPopular;
    private Boolean isNew;

    // 통계 정보
    private Integer activeRestaurantCount;  // 활성 레스토랑 수
    private Integer totalOrderCount;        // 총 주문 수

    // 정책 정보 (선택적)
    private Integer defaultMinimumOrderAmount;  // 기본 최소 주문 금액
    private Integer averageDeliveryTime;        // 평균 배달 시간

    // 하위 카테고리 (계층 구조 조회 시)
    private List<CategoryResponse> subCategories;

    /**
     * 하위 카테고리가 있는지 확인
     */
    public boolean hasSubCategories() {
        return subCategories != null && !subCategories.isEmpty();
    }
}