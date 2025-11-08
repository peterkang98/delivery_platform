package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.restaurant.application.service.RestaurantCommandService;
import xyz.sparta_project.manjok.domain.restaurant.application.service.RestaurantQueryService;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.RestaurantCreateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.RestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.RestaurantUpdateRequest;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import jakarta.validation.Valid;

/**
 * Owner용 Restaurant 관리 컨트롤러
 * - 기본 경로: /v1/owners/restaurants
 * - 권한: OWNER
 */
@RestController
@RequestMapping("/v1/owners/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerRestaurantController {

    private final RestaurantCommandService restaurantCommandService;
    private final RestaurantQueryService restaurantQueryService;

    /**
     * 식당 등록
     * POST /v1/owners/restaurants
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> createRestaurant(
            @Valid @RequestBody RestaurantCreateRequest request) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 사용자 정보에서 이름 가져오기
        String ownerName = "Owner"; // 임시 값

        RestaurantResponse restaurant = restaurantCommandService
                .createRestaurant(ownerId, ownerName, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(restaurant, "식당이 성공적으로 등록되었습니다."));
    }

    /**
     * 내 식당 목록 조회
     * GET /v1/owners/restaurants
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RestaurantResponse>>> getMyRestaurants(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        PageResponse<RestaurantResponse> restaurants = restaurantQueryService
                .getRestaurantsByOwnerId(ownerId, pageable);

        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    /**
     * 내 식당 상세 조회
     * GET /v1/owners/restaurants/{restaurantId}
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getMyRestaurant(
            @PathVariable String restaurantId) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 소유자 검증 로직 추가
        RestaurantResponse restaurant = restaurantQueryService
                .getRestaurantForOwner(restaurantId);

        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }

    /**
     * 식당 정보 전체 수정 (PUT)
     * PUT /v1/owners/restaurants/{restaurantId}
     */
    @PutMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
            @PathVariable String restaurantId,
            @Valid @RequestBody RestaurantUpdateRequest request) {

        String updatedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 소유자 검증 로직 추가
        RestaurantResponse restaurant = restaurantCommandService
                .updateRestaurant(restaurantId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(restaurant, "식당 정보가 수정되었습니다."));
    }

    /**
     * 식당 정보 부분 수정 (PATCH)
     * PATCH /v1/owners/restaurants/{restaurantId}
     */
    @PatchMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> patchRestaurant(
            @PathVariable String restaurantId,
            @RequestBody RestaurantUpdateRequest request) {

        String updatedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 소유자 검증 로직 추가
        RestaurantResponse restaurant = restaurantCommandService
                .patchRestaurant(restaurantId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(restaurant, "식당 정보가 수정되었습니다."));
    }

    /**
     * 식당 삭제 (Soft Delete)
     * DELETE /v1/owners/restaurants/{restaurantId}
     */
    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(
            @PathVariable String restaurantId) {

        String deletedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 소유자 검증 로직 추가
        restaurantCommandService.deleteRestaurant(restaurantId, deletedBy);

        return ResponseEntity.ok(ApiResponse.success(null, "식당이 삭제되었습니다."));
    }
}