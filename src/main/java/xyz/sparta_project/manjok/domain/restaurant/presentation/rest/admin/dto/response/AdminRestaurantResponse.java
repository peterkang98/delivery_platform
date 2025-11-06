package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Admin용 Restaurant 응답 DTO
 * - 관리자가 모든 식당 정보를 조회할 때 사용
 * - 모든 정보 포함 (삭제된 정보도 포함)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRestaurantResponse {

    private String restaurantId;
    private Long ownerId;
    private String ownerName;
    private String restaurantName;
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

    // 운영 시간 (요일별)
    private Map<String, OperatingHoursDto> operatingHours;

    // 메뉴 통계
    private Integer totalMenuCount;
    private Integer activeMenuCount;
    private Integer deletedMenuCount;

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
}