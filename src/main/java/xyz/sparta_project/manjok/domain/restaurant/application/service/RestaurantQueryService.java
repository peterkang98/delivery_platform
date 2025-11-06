package xyz.sparta_project.manjok.domain.restaurant.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Restaurant;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategory;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantCategoryRepository;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminRestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantDetailResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantSummaryResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.RestaurantResponse;
import xyz.sparta_project.manjok.global.common.utils.PageUtils;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Restaurant Query Service
 * - Restaurant 조회 전담 서비스
 * - Read-Only 트랜잭션
 * - 권한별 조회 메서드 분리 (Customer, Owner, Admin)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantQueryService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final RestaurantMapper restaurantMapper;

    // ==================== Customer 조회 API ====================

    /**
     * 식당 목록 조회 (Customer)
     * - 검색 및 필터링 지원
     * - 삭제되지 않고 활성화된 식당만 조회
     */
    public PageResponse<RestaurantSummaryResponse> searchRestaurants(
            String province,
            String city,
            String district,
            Set<String> categoryIds,
            String keyword,
            Pageable pageable
    ) {
        log.info("식당 검색 - province: {}, city: {}, district: {}, keyword: {}",
                province, city, district, keyword);

        Page<Restaurant> restaurantPage = restaurantRepository.searchRestaurants(
                province, city, district, categoryIds, keyword, pageable
        );

        // 모든 Restaurant의 카테고리 ID 수집
        Set<String> allCategoryIds = restaurantPage.getContent().stream()
                .flatMap(restaurant -> restaurant.getCategoryRelations().stream())
                .filter(rel -> !rel.isDeleted())
                .map(rel -> rel.getCategoryId())
                .collect(Collectors.toSet());

        // 카테고리 정보 일괄 조회
        Map<String, RestaurantCategory> categoryMap = loadCategoriesByIds(allCategoryIds);

        return PageUtils.toPageResponse(
                restaurantPage,
                restaurant -> restaurantMapper.toRestaurantSummaryResponse(restaurant, categoryMap)
        );
    }

    /**
     * 식당 상세 조회 (Customer)
     * - 삭제되지 않은 식당만 조회
     * - 조회수 증가
     */
    @Transactional
    public RestaurantDetailResponse getRestaurantDetail(String restaurantId) {
        log.info("식당 상세 조회 - restaurantId: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 조회수 증가
        restaurant.incrementViewCount();
        restaurantRepository.save(restaurant);

        // 카테고리 정보 조회
        Set<String> categoryIds = restaurant.getCategoryRelations().stream()
                .filter(rel -> !rel.isDeleted())
                .map(rel -> rel.getCategoryId())
                .collect(Collectors.toSet());

        Map<String, RestaurantCategory> categoryMap = loadCategoriesByIds(categoryIds);

        return restaurantMapper.toRestaurantDetailResponse(restaurant, categoryMap);
    }

    // ==================== Owner 조회 API ====================

    /**
     * Owner의 식당 목록 조회
     * - Owner가 소유한 식당 목록
     * - 삭제된 것도 조회 가능
     */
    public PageResponse<RestaurantResponse> getRestaurantsByOwnerId(Long ownerId, Pageable pageable) {
        log.info("Owner 식당 목록 조회 - ownerId: {}", ownerId);

        Page<Restaurant> restaurantPage = restaurantRepository.findByOwnerId(ownerId, pageable);

        // 모든 Restaurant의 카테고리 ID 수집
        Set<String> allCategoryIds = restaurantPage.getContent().stream()
                .flatMap(restaurant -> restaurant.getCategoryRelations().stream())
                .filter(rel -> !rel.isDeleted())
                .map(rel -> rel.getCategoryId())
                .collect(Collectors.toSet());

        // 카테고리 정보 일괄 조회
        Map<String, RestaurantCategory> categoryMap = loadCategoriesByIds(allCategoryIds);

        return PageUtils.toPageResponse(
                restaurantPage,
                restaurant -> restaurantMapper.toRestaurantResponse(restaurant, categoryMap)
        );
    }

    /**
     * Owner의 특정 식당 상세 조회
     * - Owner 본인의 식당만 조회 가능
     * - 삭제된 것도 조회 가능
     */
    public RestaurantResponse getRestaurantForOwner(String restaurantId) {
        log.info("Owner 식당 상세 조회 - restaurantId: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 카테고리 정보 조회
        Set<String> categoryIds = restaurant.getCategoryRelations().stream()
                .filter(rel -> !rel.isDeleted())
                .map(rel -> rel.getCategoryId())
                .collect(Collectors.toSet());

        Map<String, RestaurantCategory> categoryMap = loadCategoriesByIds(categoryIds);

        return restaurantMapper.toRestaurantResponse(restaurant, categoryMap);
    }

    // ==================== Admin 조회 API ====================

    /**
     * 전체 식당 목록 조회 (Admin)
     * - 삭제된 식당도 포함
     * - 모든 식당 조회 가능
     */
    public PageResponse<AdminRestaurantResponse> getAllRestaurantsForAdmin(Pageable pageable) {
        log.info("Admin 전체 식당 목록 조회");

        Page<Restaurant> restaurantPage = restaurantRepository.findAllIncludingDeleted(pageable);

        // 모든 Restaurant의 카테고리 ID 수집 (삭제된 것 포함)
        Set<String> allCategoryIds = restaurantPage.getContent().stream()
                .flatMap(restaurant -> restaurant.getCategoryRelations().stream())
                .map(rel -> rel.getCategoryId())
                .collect(Collectors.toSet());

        // 카테고리 정보 일괄 조회
        Map<String, RestaurantCategory> categoryMap = loadCategoriesByIds(allCategoryIds);

        return PageUtils.toPageResponse(
                restaurantPage,
                restaurant -> restaurantMapper.toAdminRestaurantResponse(restaurant, categoryMap)
        );
    }

    /**
     * 특정 식당 상세 조회 (Admin)
     * - 삭제된 식당도 조회 가능
     */
    public AdminRestaurantResponse getRestaurantForAdmin(String restaurantId) {
        log.info("Admin 식당 상세 조회 - restaurantId: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findByIdIncludingDeleted(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 카테고리 정보 조회 (삭제된 것 포함)
        Set<String> categoryIds = restaurant.getCategoryRelations().stream()
                .map(rel -> rel.getCategoryId())
                .collect(Collectors.toSet());

        Map<String, RestaurantCategory> categoryMap = loadCategoriesByIds(categoryIds);

        return restaurantMapper.toAdminRestaurantResponse(restaurant, categoryMap);
    }

    // ==================== 공통 메서드 ====================

    /**
     * Restaurant 존재 여부 확인
     */
    public boolean existsRestaurant(String restaurantId) {
        return restaurantRepository.existsById(restaurantId);
    }

    /**
     * Restaurant ID로 조회 (내부 사용)
     */
    public Restaurant getRestaurantById(String restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));
    }

    // ==================== Private Helper 메서드 ====================

    /**
     * 카테고리 ID 목록으로 카테고리 정보 조회
     * - 빈 리스트인 경우 빈 Map 반환
     * - 조회 성능 최적화를 위해 일괄 조회 (N+1 문제 방지)
     */
    private Map<String, RestaurantCategory> loadCategoriesByIds(Set<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }

        // 일괄 조회로 N+1 문제 방지
        List<RestaurantCategory> categories = restaurantCategoryRepository.findAllByIds(categoryIds);

        return categories.stream()
                .collect(Collectors.toMap(
                        RestaurantCategory::getId,
                        category -> category
                ));
    }
}