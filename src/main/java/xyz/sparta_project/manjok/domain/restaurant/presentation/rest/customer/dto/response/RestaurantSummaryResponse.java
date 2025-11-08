package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Customer용 Restaurant 목록 조회 응답 DTO
 * - 간략한 정보만 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSummaryResponse {

    private String restaurantId;
    private String restaurantName;
    private String status;

    // 주소 정보
    private String province;
    private String city;
    private String district;
    private String fullAddress;

    // 카테고리
    private List<String> categoryNames;

    // 통계 정보
    private Integer viewCount;
    private Integer wishlistCount;
    private Integer reviewCount;
    private BigDecimal reviewRating;

    // 태그
    private List<String> tags;

    // 운영 정보
    private Boolean isOpenNow;
    private String currentOperatingStatus;
}