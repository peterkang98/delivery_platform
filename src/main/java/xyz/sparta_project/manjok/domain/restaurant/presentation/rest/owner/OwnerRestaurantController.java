package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.restaurant.application.service.RestaurantCommandService;
import xyz.sparta_project.manjok.domain.restaurant.application.service.RestaurantQueryService;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.RestaurantCreateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.RestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.RestaurantUpdateRequest;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import jakarta.validation.Valid;

/**
 * Owner용 Restaurant 관리 컨트롤러
 * - 판매자가 자신의 식당을 등록/수정/삭제하는 API
 * - 권한: OWNER
 */
@RestController
@RequestMapping("/v1/owners/restaurants")
@RequiredArgsConstructor
public class OwnerRestaurantController {

    private final RestaurantCommandService restaurantCommandService;
    private final RestaurantQueryService restaurantQueryService;

    /**
     * 식당 등록
     * POST /v1/owners/restaurants
     *
     * Request Body: RestaurantCreateRequest
     * - restaurantName: 식당명 (필수)
     * - address: 주소 정보 (필수)
     * - contactNumber: 연락처 (필수)
     * - coordinate: 좌표 정보 (선택)
     * - tags: 태그 목록 (선택)
     * - categoryIds: 카테고리 ID 목록 (선택)
     *
     * TODO: @AuthenticationPrincipal 또는 SecurityContext에서 ownerId 추출
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> createRestaurant(
            @Valid @RequestBody RestaurantCreateRequest request
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 현재 로그인한 사용자 ID를 추출
        Long ownerId = 1L; // 임시 값
        String ownerName = "테스트 사장님"; // 임시 값

        RestaurantResponse restaurant = restaurantCommandService
                .createRestaurant(ownerId, ownerName, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(restaurant, "식당이 성공적으로 등록되었습니다."));
    }

    /**
     * 내 식당 목록 조회
     * GET /v1/owners/restaurants
     *
     * Query Parameters:
     * - page: 페이지 번호 (0부터 시작)
     * - size: 페이지 크기 (기본 20)
     * - sort: 정렬 기준 (기본: createdAt,desc)
     *
     * TODO: @AuthenticationPrincipal에서 ownerId 추출
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RestaurantResponse>>> getMyRestaurants(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 현재 로그인한 사용자 ID를 추출
        Long ownerId = 1L; // 임시 값

        PageResponse<RestaurantResponse> restaurants = restaurantQueryService
                .getRestaurantsByOwnerId(ownerId, pageable);

        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    /**
     * 내 식당 상세 조회
     * GET /v1/owners/restaurants/{restaurantId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     *
     * TODO: 소유자 검증 추가
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getMyRestaurant(
            @PathVariable String restaurantId
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증
        RestaurantResponse restaurant = restaurantQueryService
                .getRestaurantForOwner(restaurantId);

        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }

    /**
     * 식당 정보 전체 수정 (PUT)
     * PUT /v1/owners/restaurants/{restaurantId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     *
     * Request Body: RestaurantUpdateRequest
     *
     * TODO: 소유자 검증 추가
     */
    @PutMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
            @PathVariable String restaurantId,
            @Valid @RequestBody RestaurantUpdateRequest request
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증 및 현재 사용자 ID 추출
        String updatedBy = "OWNER_1"; // 임시 값

        RestaurantResponse restaurant = restaurantCommandService
                .updateRestaurant(restaurantId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(restaurant, "식당 정보가 수정되었습니다."));
    }

    /**
     * 식당 정보 부분 수정 (PATCH)
     * PATCH /v1/owners/restaurants/{restaurantId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     *
     * Request Body: RestaurantUpdateRequest (부분 업데이트)
     *
     * TODO: 소유자 검증 추가
     */
    @PatchMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> patchRestaurant(
            @PathVariable String restaurantId,
            @RequestBody RestaurantUpdateRequest request
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증 및 현재 사용자 ID 추출
        String updatedBy = "OWNER_1"; // 임시 값

        RestaurantResponse restaurant = restaurantCommandService
                .patchRestaurant(restaurantId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(restaurant, "식당 정보가 수정되었습니다."));
    }

    /**
     * 식당 삭제 (Soft Delete)
     * DELETE /v1/owners/restaurants/{restaurantId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     *
     * TODO: 소유자 검증 추가
     */
    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(
            @PathVariable String restaurantId
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증 및 현재 사용자 ID 추출
        String deletedBy = "OWNER_1"; // 임시 값

        restaurantCommandService.deleteRestaurant(restaurantId, deletedBy);

        return ResponseEntity.ok(ApiResponse.success(null, "식당이 삭제되었습니다."));
    }
}