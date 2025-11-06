package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer;

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
 * Customer용 Restaurant 조회 컨트롤러
 * - 고객이 식당 정보를 조회하는 API
 * - 인증 불필요 (Public API)
 */
@RestController
@RequestMapping("/v1/customers/restaurants")
@RequiredArgsConstructor
public class CustomerRestaurantController {

    private final RestaurantQueryService restaurantQueryService;

    /**
     * 식당 목록 조회 (필터링)
     * GET /v1/customers/restaurants
     *
     * Query Parameters:
     * - province: 시/도 (예: 서울특별시)
     * - city: 시/군/구 (예: 종로구)
     * - district: 동/읍/면 (예: 광화문동)
     * - categoryIds: 카테고리 ID 목록 (쉼표로 구분)
     * - keyword: 검색 키워드
     * - page: 페이지 번호 (0부터 시작)
     * - size: 페이지 크기 (기본 20)
     * - sort: 정렬 기준 (기본: createdAt,desc)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RestaurantSummaryResponse>>> getRestaurants(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Set<String> categoryIds,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<RestaurantSummaryResponse> restaurants = restaurantQueryService
                .searchRestaurants(province, city, district, categoryIds, keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    /**
     * 특정 식당 상세 조회
     * GET /v1/customers/restaurants/{restaurantId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantDetailResponse>> getRestaurant(
            @PathVariable String restaurantId
    ) {
        RestaurantDetailResponse restaurant = restaurantQueryService
                .getRestaurantDetail(restaurantId);

        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }
}