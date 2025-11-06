package xyz.sparta_project.manjok.domain.restaurant.application.service;

import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminRestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantDetailResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantSummaryResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.RestaurantResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Restaurant Mapper
 * - Restaurant 도메인 모델과 DTO 간 변환
 * - 권한별 DTO 분리 (Customer, Owner, Admin)
 * - 순수 변환 로직만 담당 (데이터 조회 X)
 */
@Component
public class RestaurantMapper {

    // ==================== Customer DTO 변환 ====================

    /**
     * Restaurant -> RestaurantSummaryResponse (목록 조회용)
     */
    public RestaurantSummaryResponse toRestaurantSummaryResponse(
            Restaurant restaurant,
            Map<String, RestaurantCategory> categoryMap) {
        return RestaurantSummaryResponse.builder()
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getRestaurantName())
                .status(restaurant.getStatus().getDisplayName())
                .province(restaurant.getAddress().getProvince())
                .city(restaurant.getAddress().getCity())
                .district(restaurant.getAddress().getDistrict())
                .fullAddress(restaurant.getAddress().getFullAddress())
                .categoryNames(extractCategoryNames(restaurant, categoryMap))
                .viewCount(restaurant.getViewCount())
                .wishlistCount(restaurant.getWishlistCount())
                .reviewCount(restaurant.getReviewCount())
                .reviewRating(restaurant.getReviewRating())
                .tags(restaurant.getTags())
                .isOpenNow(restaurant.isOpenNow())
                .currentOperatingStatus(getCurrentOperatingStatus(restaurant))
                .build();
    }

    /**
     * Restaurant -> RestaurantDetailResponse (상세 조회용)
     */
    public RestaurantDetailResponse toRestaurantDetailResponse(
            Restaurant restaurant,
            Map<String, RestaurantCategory> categoryMap) {
        return RestaurantDetailResponse.builder()
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getRestaurantName())
                .ownerName(restaurant.getOwnerName())
                .status(restaurant.getStatus().getDisplayName())
                .contactNumber(restaurant.getContactNumber())
                .address(toAddressDto(restaurant.getAddress()))
                .coordinate(toCoordinateDto(restaurant.getCoordinate()))
                .categoryNames(extractCategoryNames(restaurant, categoryMap))
                .viewCount(restaurant.getViewCount())
                .wishlistCount(restaurant.getWishlistCount())
                .reviewCount(restaurant.getReviewCount())
                .reviewRating(restaurant.getReviewRating())
                .purchaseCount(restaurant.getPurchaseCount())
                .tags(restaurant.getTags())
                .isActive(restaurant.getIsActive())
                .isOpenNow(restaurant.isOpenNow())
                .currentOperatingStatus(getCurrentOperatingStatus(restaurant))
                .operatingHours(toOperatingHoursMap(restaurant.getOperatingDays()))
                .menuCategories(toMenuCategorySummaryList(restaurant.getActiveMenuCategories()))
                .createdAt(restaurant.getCreatedAt())
                .build();
    }

    // ==================== Owner DTO 변환 ====================

    /**
     * Restaurant -> RestaurantResponse (Owner용)
     */
    public RestaurantResponse toRestaurantResponse(
            Restaurant restaurant,
            Map<String, RestaurantCategory> categoryMap) {
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getId())
                .ownerId(restaurant.getOwnerId())
                .ownerName(restaurant.getOwnerName())
                .restaurantName(restaurant.getRestaurantName())
                .status(restaurant.getStatus().name())
                .contactNumber(restaurant.getContactNumber())
                .address(toOwnerAddressDto(restaurant.getAddress()))
                .coordinate(toOwnerCoordinateDto(restaurant.getCoordinate()))
                .categoryNames(extractCategoryNames(restaurant, categoryMap))
                .viewCount(restaurant.getViewCount())
                .wishlistCount(restaurant.getWishlistCount())
                .reviewCount(restaurant.getReviewCount())
                .reviewRating(restaurant.getReviewRating())
                .purchaseCount(restaurant.getPurchaseCount())
                .tags(restaurant.getTags())
                .isActive(restaurant.getIsActive())
                .isOpenNow(restaurant.isOpenNow())
                .operatingHours(toOwnerOperatingHoursMap(restaurant.getOperatingDays()))
                .totalMenuCount(restaurant.getMenus().size())
                .activeMenuCount((int) restaurant.getMenus().stream()
                        .filter(Menu::isOrderable)
                        .count())
                .createdAt(restaurant.getCreatedAt())
                .createdBy(restaurant.getCreatedBy())
                .updatedAt(restaurant.getUpdatedAt())
                .updatedBy(restaurant.getUpdatedBy())
                .isDeleted(restaurant.isDeleted())
                .deletedAt(restaurant.getDeletedAt())
                .deletedBy(restaurant.getDeletedBy())
                .build();
    }

    // ==================== Admin DTO 변환 ====================

    /**
     * Restaurant -> AdminRestaurantResponse (Admin용)
     */
    public AdminRestaurantResponse toAdminRestaurantResponse(
            Restaurant restaurant,
            Map<String, RestaurantCategory> categoryMap) {
        return AdminRestaurantResponse.builder()
                .restaurantId(restaurant.getId())
                .ownerId(restaurant.getOwnerId())
                .ownerName(restaurant.getOwnerName())
                .restaurantName(restaurant.getRestaurantName())
                .status(restaurant.getStatus().name())
                .contactNumber(restaurant.getContactNumber())
                .address(toAdminAddressDto(restaurant.getAddress()))
                .coordinate(toAdminCoordinateDto(restaurant.getCoordinate()))
                .categoryNames(extractCategoryNames(restaurant, categoryMap))
                .viewCount(restaurant.getViewCount())
                .wishlistCount(restaurant.getWishlistCount())
                .reviewCount(restaurant.getReviewCount())
                .reviewRating(restaurant.getReviewRating())
                .purchaseCount(restaurant.getPurchaseCount())
                .tags(restaurant.getTags())
                .isActive(restaurant.getIsActive())
                .isOpenNow(restaurant.isOpenNow())
                .operatingHours(toAdminOperatingHoursMap(restaurant.getOperatingDays()))
                .totalMenuCount(restaurant.getMenus().size())
                .activeMenuCount((int) restaurant.getMenus().stream()
                        .filter(menu -> !menu.getIsDeleted() && menu.getIsAvailable())
                        .count())
                .deletedMenuCount((int) restaurant.getMenus().stream()
                        .filter(Menu::getIsDeleted)
                        .count())
                .createdAt(restaurant.getCreatedAt())
                .createdBy(restaurant.getCreatedBy())
                .updatedAt(restaurant.getUpdatedAt())
                .updatedBy(restaurant.getUpdatedBy())
                .isDeleted(restaurant.isDeleted())
                .deletedAt(restaurant.getDeletedAt())
                .deletedBy(restaurant.getDeletedBy())
                .build();
    }

    // ==================== 공통 Helper 메서드 ====================

    /**
     * 카테고리명 추출
     * - Service에서 조회한 카테고리 정보를 기반으로 카테고리명 추출
     * - 활성화된 카테고리만 필터링
     */
    private List<String> extractCategoryNames(
            Restaurant restaurant,
            Map<String, RestaurantCategory> categoryMap) {
        return restaurant.getCategoryRelations().stream()
                .filter(RestaurantCategoryRelation::isActive)
                .map(RestaurantCategoryRelation::getCategoryId)
                .map(categoryMap::get)
                .filter(category -> category != null && category.getIsActive())
                .map(RestaurantCategory::getCategoryName)
                .collect(Collectors.toList());
    }

    /**
     * 현재 운영 상태 문자열 생성
     */
    private String getCurrentOperatingStatus(Restaurant restaurant) {
        if (!restaurant.getIsActive()) {
            return "운영 중지";
        }
        if (restaurant.isOpenNow()) {
            return "영업 중";
        }
        return "영업 종료";
    }

    /**
     * Address -> AddressDto (Customer용)
     */
    private RestaurantDetailResponse.AddressDto toAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        return RestaurantDetailResponse.AddressDto.builder()
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .fullAddress(address.getFullAddress())
                .build();
    }

    /**
     * Coordinate -> CoordinateDto (Customer용)
     */
    private RestaurantDetailResponse.CoordinateDto toCoordinateDto(Coordinate coordinate) {
        if (coordinate == null || !coordinate.hasCoordinate()) {
            return null;
        }
        return RestaurantDetailResponse.CoordinateDto.builder()
                .latitude(coordinate.getLatitude())
                .longitude(coordinate.getLongitude())
                .googleMapsUrl(coordinate.toGoogleMapsUrl())
                .naverMapUrl(coordinate.toNaverMapUrl())
                .build();
    }

    /**
     * Address -> AddressDto (Owner용)
     */
    private RestaurantResponse.AddressDto toOwnerAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        return RestaurantResponse.AddressDto.builder()
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .fullAddress(address.getFullAddress())
                .build();
    }

    /**
     * Coordinate -> CoordinateDto (Owner용)
     */
    private RestaurantResponse.CoordinateDto toOwnerCoordinateDto(Coordinate coordinate) {
        if (coordinate == null || !coordinate.hasCoordinate()) {
            return null;
        }
        return RestaurantResponse.CoordinateDto.builder()
                .latitude(coordinate.getLatitude())
                .longitude(coordinate.getLongitude())
                .build();
    }

    /**
     * Address -> AddressDto (Admin용)
     */
    private AdminRestaurantResponse.AddressDto toAdminAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        return AdminRestaurantResponse.AddressDto.builder()
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .fullAddress(address.getFullAddress())
                .build();
    }

    /**
     * Coordinate -> CoordinateDto (Admin용)
     */
    private AdminRestaurantResponse.CoordinateDto toAdminCoordinateDto(Coordinate coordinate) {
        if (coordinate == null || !coordinate.hasCoordinate()) {
            return null;
        }
        return AdminRestaurantResponse.CoordinateDto.builder()
                .latitude(coordinate.getLatitude())
                .longitude(coordinate.getLongitude())
                .googleMapsUrl(coordinate.toGoogleMapsUrl())
                .naverMapUrl(coordinate.toNaverMapUrl())
                .build();
    }

    /**
     * OperatingDay Set -> Map<요일, OperatingHoursDto> (Customer용)
     */
    private Map<String, RestaurantDetailResponse.OperatingHoursDto> toOperatingHoursMap(
            java.util.Set<OperatingDay> operatingDays) {
        Map<String, RestaurantDetailResponse.OperatingHoursDto> map = new HashMap<>();
        for (OperatingDay day : operatingDays) {
            String key = day.getDayType().name() + "_" + day.getTimeType().name();
            map.put(key, RestaurantDetailResponse.OperatingHoursDto.builder()
                    .dayType(day.getDayType().getKoreanName())
                    .timeType(day.getTimeType().getDescription())
                    .startTime(day.getStartTime() != null ? day.getStartTime().toString() : null)
                    .endTime(day.getEndTime() != null ? day.getEndTime().toString() : null)
                    .isHoliday(day.getIsHoliday())
                    .breakStartTime(day.getBreakStartTime() != null ?
                            day.getBreakStartTime().toString() : null)
                    .breakEndTime(day.getBreakEndTime() != null ?
                            day.getBreakEndTime().toString() : null)
                    .note(day.getNote())
                    .build());
        }
        return map;
    }

    /**
     * OperatingDay Set -> Map (Owner용)
     */
    private Map<String, RestaurantResponse.OperatingHoursDto> toOwnerOperatingHoursMap(
            java.util.Set<OperatingDay> operatingDays) {
        Map<String, RestaurantResponse.OperatingHoursDto> map = new HashMap<>();
        for (OperatingDay day : operatingDays) {
            String key = day.getDayType().name() + "_" + day.getTimeType().name();
            map.put(key, RestaurantResponse.OperatingHoursDto.builder()
                    .dayType(day.getDayType().name())
                    .timeType(day.getTimeType().name())
                    .startTime(day.getStartTime() != null ? day.getStartTime().toString() : null)
                    .endTime(day.getEndTime() != null ? day.getEndTime().toString() : null)
                    .isHoliday(day.getIsHoliday())
                    .breakStartTime(day.getBreakStartTime() != null ?
                            day.getBreakStartTime().toString() : null)
                    .breakEndTime(day.getBreakEndTime() != null ?
                            day.getBreakEndTime().toString() : null)
                    .note(day.getNote())
                    .build());
        }
        return map;
    }

    /**
     * OperatingDay Set -> Map (Admin용)
     */
    private Map<String, AdminRestaurantResponse.OperatingHoursDto> toAdminOperatingHoursMap(
            java.util.Set<OperatingDay> operatingDays) {
        Map<String, AdminRestaurantResponse.OperatingHoursDto> map = new HashMap<>();
        for (OperatingDay day : operatingDays) {
            String key = day.getDayType().name() + "_" + day.getTimeType().name();
            map.put(key, AdminRestaurantResponse.OperatingHoursDto.builder()
                    .dayType(day.getDayType().name())
                    .timeType(day.getTimeType().name())
                    .startTime(day.getStartTime() != null ? day.getStartTime().toString() : null)
                    .endTime(day.getEndTime() != null ? day.getEndTime().toString() : null)
                    .isHoliday(day.getIsHoliday())
                    .breakStartTime(day.getBreakStartTime() != null ?
                            day.getBreakStartTime().toString() : null)
                    .breakEndTime(day.getBreakEndTime() != null ?
                            day.getBreakEndTime().toString() : null)
                    .note(day.getNote())
                    .build());
        }
        return map;
    }

    /**
     * MenuCategory List -> MenuCategorySummaryDto List
     */
    private List<RestaurantDetailResponse.MenuCategorySummaryDto> toMenuCategorySummaryList(
            List<MenuCategory> categories) {
        return categories.stream()
                .map(category -> RestaurantDetailResponse.MenuCategorySummaryDto.builder()
                        .categoryId(category.getId())
                        .categoryName(category.getCategoryName())
                        .description(category.getDescription())
                        .displayOrder(category.getDisplayOrder())
                        .menuCount(category.getMenuIds().size())
                        .build())
                .collect(Collectors.toList());
    }
}