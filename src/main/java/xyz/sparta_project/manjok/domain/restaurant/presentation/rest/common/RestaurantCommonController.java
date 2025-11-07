package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.restaurant.application.service.RestaurantCategoryQueryService;
import xyz.sparta_project.manjok.domain.restaurant.application.service.RestaurantQueryService;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.common.dto.CategoryResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantDetailResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantSummaryResponse;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import java.util.List;
import java.util.Set;

/**
 * Common용 Restaurant 조회 컨트롤러 (인증 불필요)
 * - 기본 경로: /v1/common/restaurants
 * - 로그인 없이 식당 정보를 조회하는 API
 */
@RestController
@RequestMapping("/v1/common/restaurants")
@RequiredArgsConstructor
public class RestaurantCommonController {

    private final RestaurantQueryService restaurantQueryService;
    private final RestaurantCategoryQueryService categoryQueryService;

    // ==================== 식당 조회 API ====================

    /**
     * 식당 목록 조회 (필터링)
     * GET /v1/common/restaurants
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RestaurantSummaryResponse>>> getRestaurants(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Set<String> categoryIds,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<RestaurantSummaryResponse> restaurants = restaurantQueryService
                .searchRestaurants(province, city, district, categoryIds, keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    /**
     * 특정 식당 상세 조회
     * GET /v1/common/restaurants/{restaurantId}
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantDetailResponse>> getRestaurant(
            @PathVariable String restaurantId) {

        RestaurantDetailResponse restaurant = restaurantQueryService
                .getRestaurantDetail(restaurantId);

        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }

    // ==================== 카테고리 조회 API ====================

    /**
     * 1차 카테고리 목록 조회 (최상위 카테고리)
     * GET /v1/common/restaurants/categories
     *
     * @return 최상위 카테고리 목록 (depth = 1)
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        List<CategoryResponse> categories = categoryQueryService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 2차 카테고리 목록 조회 (특정 1차 카테고리의 하위)
     * GET /v1/common/restaurants/categories/{parentCategoryId}/sub-categories
     *
     * @param parentCategoryId 부모 카테고리 ID (1차 카테고리)
     * @return 2차 카테고리 목록 (depth = 2)
     */
    @GetMapping("/categories/{parentCategoryId}/sub-categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSubCategories(
            @PathVariable String parentCategoryId) {
        List<CategoryResponse> categories = categoryQueryService
                .getSubCategoriesByParentId(parentCategoryId, 2);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 3차 카테고리 목록 조회 (특정 2차 카테고리의 하위)
     * GET /v1/common/restaurants/categories/{parentCategoryId}/sub-categories/detail
     *
     * @param parentCategoryId 부모 카테고리 ID (2차 카테고리)
     * @return 3차 카테고리 목록 (depth = 3)
     */
    @GetMapping("/categories/{parentCategoryId}/sub-categories/detail")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getDetailCategories(
            @PathVariable String parentCategoryId) {
        List<CategoryResponse> categories = categoryQueryService
                .getSubCategoriesByParentId(parentCategoryId, 3);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 전체 카테고리 계층 구조 조회
     * GET /v1/common/restaurants/categories/hierarchy
     *
     * @return 전체 카테고리 계층 구조 (1차 > 2차 > 3차)
     */
    @GetMapping("/categories/hierarchy")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryHierarchy() {
        List<CategoryResponse> categories = categoryQueryService.getCategoryHierarchy();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 인기 카테고리 목록 조회
     * GET /v1/common/restaurants/categories/popular
     *
     * @return 인기 카테고리 목록
     */
    @GetMapping("/categories/popular")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getPopularCategories() {
        List<CategoryResponse> categories = categoryQueryService.getPopularCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 특정 카테고리 상세 조회
     * GET /v1/common/restaurants/categories/{categoryId}
     *
     * @param categoryId 카테고리 ID
     * @return 카테고리 상세 정보
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryDetail(
            @PathVariable String categoryId) {
        CategoryResponse category = categoryQueryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
}