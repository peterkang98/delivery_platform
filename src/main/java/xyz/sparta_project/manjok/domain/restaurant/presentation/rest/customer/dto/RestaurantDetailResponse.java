package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Customer용 Restaurant 상세 조회 응답 DTO
 * - 상세한 정보 포함 (운영시간, 주소, 좌표 등)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDetailResponse {

    private String restaurantId;
    private String restaurantName;
    private String ownerName;
    private String status;
    private String contactNumber;

    // 주소 정보
    private AddressDto address;

    // 좌표 정보
    private CoordinateDto coordinate;

    // 카테고리
    private List<String> categoryNames;

    // 통계 정보
    private Integer viewCount;
    private Integer wishlistCount;
    private Integer reviewCount;
    private BigDecimal reviewRating;
    private Integer purchaseCount;

    // 태그
    private List<String> tags;

    // 운영 정보
    private Boolean isActive;
    private Boolean isOpenNow;
    private String currentOperatingStatus;

    // 운영 시간 (요일별)
    private Map<String, OperatingHoursDto> operatingHours;

    // 메뉴 카테고리 목록
    private List<MenuCategorySummaryDto> menuCategories;

    // 생성 시간
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private String province;
        private String city;
        private String district;
        private String detailAddress;
        private String fullAddress;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinateDto {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String googleMapsUrl;
        private String naverMapUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHoursDto {
        private String dayType;
        private String timeType;
        private String startTime;
        private String endTime;
        private Boolean isHoliday;
        private String breakStartTime;
        private String breakEndTime;
        private String note;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuCategorySummaryDto {
        private String categoryId;
        private String categoryName;
        private String description;
        private Integer displayOrder;
        private Integer menuCount;
    }
}