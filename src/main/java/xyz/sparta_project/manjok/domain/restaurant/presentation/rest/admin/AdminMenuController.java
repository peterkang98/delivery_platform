package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.restaurant.application.service.MenuCommandService;
import xyz.sparta_project.manjok.domain.restaurant.application.service.MenuQueryService;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.AdminMenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.AdminMenuUpdateRequest;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import jakarta.validation.Valid;

/**
 * Admin용 Menu 관리 컨트롤러
 * - 관리자가 모든 메뉴를 조회/수정/삭제하는 API
 * - 권한: MANAGER, MASTER
 */
@RestController
@RequestMapping("/v1/admin/restaurants/{restaurantId}/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuCommandService menuCommandService;
    private final MenuQueryService menuQueryService;

    /**
     * 특정 식당의 전체 메뉴 조회 (삭제된 것 포함)
     * GET /v1/admin/restaurants/{restaurantId}/menus
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     *
     * Query Parameters:
     * - page: 페이지 번호 (0부터 시작)
     * - size: 페이지 크기 (기본 20)
     * - sort: 정렬 기준 (기본: displayOrder,asc)
     *
     * TODO: @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')") 추가
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminMenuResponse>>> getAllMenus(
            @PathVariable String restaurantId,
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PageResponse<AdminMenuResponse> menus = menuQueryService
                .getMenusForAdmin(restaurantId, pageable);

        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 특정 메뉴 상세 조회 (삭제된 것 포함)
     * GET /v1/admin/restaurants/{restaurantId}/menus/{menuId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     * - menuId: 메뉴 ID
     *
     * TODO: @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')") 추가
     */
    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<AdminMenuResponse>> getMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId
    ) {
        AdminMenuResponse menu = menuQueryService.getMenuForAdmin(restaurantId, menuId);

        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    /**
     * 메뉴 정보 수정
     * PUT /v1/admin/restaurants/{restaurantId}/menus/{menuId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     * - menuId: 메뉴 ID
     *
     * Request Body: AdminMenuUpdateRequest
     *
     * TODO: @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')") 추가
     * TODO: @AuthenticationPrincipal에서 관리자 ID 추출
     */
    @PutMapping("/{menuId}")
    public ResponseEntity<ApiResponse<AdminMenuResponse>> updateMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId,
            @Valid @RequestBody AdminMenuUpdateRequest request
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 관리자 ID 추출
        String updatedBy = "ADMIN_1"; // 임시 값

        AdminMenuResponse menu = menuCommandService
                .updateMenuByAdmin(restaurantId, menuId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(menu, "메뉴가 수정되었습니다."));
    }

    /**
     * 메뉴 삭제 (Soft Delete)
     * DELETE /v1/admin/restaurants/{restaurantId}/menus/{menuId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     * - menuId: 메뉴 ID
     *
     * TODO: @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')") 추가
     * TODO: @AuthenticationPrincipal에서 관리자 ID 추출
     */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 관리자 ID 추출
        String deletedBy = "ADMIN_1"; // 임시 값

        menuCommandService.deleteMenu(restaurantId, menuId, deletedBy);

        return ResponseEntity.ok(ApiResponse.success(null, "메뉴가 삭제되었습니다."));
    }

    /**
     * 삭제된 메뉴 복구
     * PATCH /v1/admin/restaurants/{restaurantId}/menus/{menuId}/restore
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     * - menuId: 메뉴 ID
     *
     * TODO: @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')") 추가
     * TODO: @AuthenticationPrincipal에서 관리자 ID 추출
     */
    @PatchMapping("/{menuId}/restore")
    public ResponseEntity<ApiResponse<AdminMenuResponse>> restoreMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 관리자 ID 추출
        String updatedBy = "ADMIN_1"; // 임시 값

        AdminMenuResponse menu = menuCommandService.restoreMenu(restaurantId, menuId, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(menu, "메뉴가 복구되었습니다."));
    }
}