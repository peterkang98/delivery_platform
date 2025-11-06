package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.restaurant.application.service.RestaurantQueryService;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantDetailResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantSummaryResponse;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

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
}