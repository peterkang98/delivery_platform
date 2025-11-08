package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.restaurant.application.service.RestaurantCommandService;
import xyz.sparta_project.manjok.domain.restaurant.application.service.RestaurantQueryService;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminRestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.request.AdminRestaurantUpdateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.request.RestaurantStatusUpdateRequest;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import jakarta.validation.Valid;

/**
 * Admin용 Restaurant 관리 컨트롤러
 * - 기본 경로: /v1/admin/restaurants
 * - 권한: MANAGER, MASTER
 */
@RestController
@RequestMapping("/v1/admin/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
public class AdminRestaurantController {

    private final RestaurantCommandService restaurantCommandService;
    private final RestaurantQueryService restaurantQueryService;

    /**
     * 전체 식당 목록 조회 (삭제된 것 포함)
     * GET /v1/admin/restaurants
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminRestaurantResponse>>> getAllRestaurants(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        PageResponse<AdminRestaurantResponse> restaurants = restaurantQueryService
                .getAllRestaurantsForAdmin(pageable);

        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    /**
     * 특정 식당 상세 조회 (삭제된 것 포함)
     * GET /v1/admin/restaurants/{restaurantId}
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<AdminRestaurantResponse>> getRestaurant(
            @PathVariable String restaurantId) {

        String adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        AdminRestaurantResponse restaurant = restaurantQueryService
                .getRestaurantForAdmin(restaurantId);

        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }

    /**
     * 식당 정보 수정
     * PUT /v1/admin/restaurants/{restaurantId}
     */
    @PutMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<AdminRestaurantResponse>> updateRestaurant(
            @PathVariable String restaurantId,
            @Valid @RequestBody AdminRestaurantUpdateRequest request) {

        String updatedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        AdminRestaurantResponse restaurant = restaurantCommandService
                .updateRestaurantByAdmin(restaurantId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(restaurant, "식당 정보가 수정되었습니다."));
    }

    /**
     * 식당 상태 변경
     * PATCH /v1/admin/restaurants/{restaurantId}/status
     */
    @PatchMapping("/{restaurantId}/status")
    public ResponseEntity<ApiResponse<AdminRestaurantResponse>> updateRestaurantStatus(
            @PathVariable String restaurantId,
            @Valid @RequestBody RestaurantStatusUpdateRequest request) {

        String updatedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        AdminRestaurantResponse restaurant = restaurantCommandService
                .updateRestaurantStatus(restaurantId, request.getStatus(), updatedBy);

        return ResponseEntity.ok(ApiResponse.success(restaurant, "식당 상태가 변경되었습니다."));
    }

    /**
     * 식당 삭제 (Soft Delete)
     * DELETE /v1/admin/restaurants/{restaurantId}
     */
    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(
            @PathVariable String restaurantId) {

        String deletedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        restaurantCommandService.deleteRestaurant(restaurantId, deletedBy);

        return ResponseEntity.ok(ApiResponse.success(null, "식당이 삭제되었습니다."));
    }

    /**
     * 삭제된 식당 복구
     * PATCH /v1/admin/restaurants/{restaurantId}/restore
     */
    @PatchMapping("/{restaurantId}/restore")
    public ResponseEntity<ApiResponse<AdminRestaurantResponse>> restoreRestaurant(
            @PathVariable String restaurantId) {

        String updatedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        AdminRestaurantResponse restaurant = restaurantCommandService
                .restoreRestaurant(restaurantId, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(restaurant, "식당이 복구되었습니다."));
    }
}