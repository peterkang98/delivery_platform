package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.restaurant.application.service.MenuCommandService;
import xyz.sparta_project.manjok.domain.restaurant.application.service.MenuQueryService;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminMenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.request.AdminMenuUpdateRequest;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import jakarta.validation.Valid;

/**
 * Admin용 Menu 관리 컨트롤러
 * - 기본 경로: /v1/admin/restaurants/{restaurantId}/menus
 * - 권한: MANAGER, MASTER
 */
@RestController
@RequestMapping("/v1/admin/restaurants/{restaurantId}/menus")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
public class AdminMenuController {

    private final MenuCommandService menuCommandService;
    private final MenuQueryService menuQueryService;

    /**
     * 특정 식당의 전체 메뉴 조회 (삭제된 것 포함)
     * GET /v1/admin/restaurants/{restaurantId}/menus
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminMenuResponse>>> getAllMenus(
            @PathVariable String restaurantId,
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable) {

        String adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        PageResponse<AdminMenuResponse> menus = menuQueryService
                .getMenusForAdmin(restaurantId, pageable);

        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 특정 메뉴 상세 조회 (삭제된 것 포함)
     * GET /v1/admin/restaurants/{restaurantId}/menus/{menuId}
     */
    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<AdminMenuResponse>> getMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId) {

        String adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        AdminMenuResponse menu = menuQueryService.getMenuForAdmin(restaurantId, menuId);

        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    /**
     * 메뉴 정보 수정
     * PUT /v1/admin/restaurants/{restaurantId}/menus/{menuId}
     */
    @PutMapping("/{menuId}")
    public ResponseEntity<ApiResponse<AdminMenuResponse>> updateMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId,
            @Valid @RequestBody AdminMenuUpdateRequest request) {

        String updatedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        AdminMenuResponse menu = menuCommandService
                .updateMenuByAdmin(restaurantId, menuId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(menu, "메뉴가 수정되었습니다."));
    }

    /**
     * 메뉴 삭제 (Soft Delete)
     * DELETE /v1/admin/restaurants/{restaurantId}/menus/{menuId}
     */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId) {

        String deletedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        menuCommandService.deleteMenu(restaurantId, menuId, deletedBy);

        return ResponseEntity.ok(ApiResponse.success(null, "메뉴가 삭제되었습니다."));
    }

    /**
     * 삭제된 메뉴 복구
     * PATCH /v1/admin/restaurants/{restaurantId}/menus/{menuId}/restore
     */
    @PatchMapping("/{menuId}/restore")
    public ResponseEntity<ApiResponse<AdminMenuResponse>> restoreMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId) {

        String updatedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        AdminMenuResponse menu = menuCommandService.restoreMenu(restaurantId, menuId, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(menu, "메뉴가 복구되었습니다."));
    }
}