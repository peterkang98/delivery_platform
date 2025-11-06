package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.restaurant.application.service.MenuCommandService;
import xyz.sparta_project.manjok.domain.restaurant.application.service.MenuQueryService;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.MenuCreateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.MenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.MenuUpdateRequest;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import jakarta.validation.Valid;

/**
 * Owner용 Menu 관리 컨트롤러
 * - 기본 경로: /v1/owners/restaurants/{restaurantId}/menus
 * - 권한: OWNER
 */
@RestController
@RequestMapping("/v1/owners/restaurants/{restaurantId}/menus")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerMenuController {

    private final MenuCommandService menuCommandService;
    private final MenuQueryService menuQueryService;

    /**
     * 메뉴 등록
     * POST /v1/owners/restaurants/{restaurantId}/menus
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(
            @PathVariable String restaurantId,
            @Valid @RequestBody MenuCreateRequest request) {

        String createdBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 레스토랑 소유자 검증 로직 추가
        MenuResponse menu = menuCommandService.createMenu(restaurantId, request, createdBy);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(menu, "메뉴가 성공적으로 등록되었습니다."));
    }

    /**
     * 내 식당 메뉴 목록 조회 (삭제된 것 포함)
     * GET /v1/owners/restaurants/{restaurantId}/menus
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MenuResponse>>> getMyMenus(
            @PathVariable String restaurantId,
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 레스토랑 소유자 검증 로직 추가
        PageResponse<MenuResponse> menus = menuQueryService
                .getMenusForOwner(restaurantId, pageable);

        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 메뉴 상세 조회 (삭제된 것 포함)
     * GET /v1/owners/restaurants/{restaurantId}/menus/{menuId}
     */
    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponse>> getMyMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 레스토랑 소유자 검증 로직 추가
        MenuResponse menu = menuQueryService.getMenuForOwner(restaurantId, menuId);

        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    /**
     * 메뉴 전체 수정 (PUT)
     * PUT /v1/owners/restaurants/{restaurantId}/menus/{menuId}
     */
    @PutMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId,
            @Valid @RequestBody MenuUpdateRequest request) {

        String updatedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 레스토랑 소유자 검증 로직 추가
        MenuResponse menu = menuCommandService.updateMenu(restaurantId, menuId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(menu, "메뉴가 수정되었습니다."));
    }

    /**
     * 메뉴 부분 수정 (PATCH)
     * PATCH /v1/owners/restaurants/{restaurantId}/menus/{menuId}
     */
    @PatchMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponse>> patchMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId,
            @RequestBody MenuUpdateRequest request) {

        String updatedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 레스토랑 소유자 검증 로직 추가
        MenuResponse menu = menuCommandService.patchMenu(restaurantId, menuId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(menu, "메뉴가 수정되었습니다."));
    }

    /**
     * 메뉴 숨김/노출 처리
     * PATCH /v1/owners/restaurants/{restaurantId}/menus/{menuId}/hide
     */
    @PatchMapping("/{menuId}/hide")
    public ResponseEntity<ApiResponse<MenuResponse>> toggleMenuVisibility(
            @PathVariable String restaurantId,
            @PathVariable String menuId,
            @RequestParam boolean hidden) {

        String updatedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 레스토랑 소유자 검증 로직 추가
        MenuResponse menu = menuCommandService.toggleMenuVisibility(restaurantId, menuId, hidden, updatedBy);

        String message = hidden ? "메뉴가 숨김 처리되었습니다." : "메뉴가 노출 처리되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(menu, message));
    }

    /**
     * 메뉴 삭제 (Soft Delete)
     * DELETE /v1/owners/restaurants/{restaurantId}/menus/{menuId}
     */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId) {

        String deletedBy = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        // TODO: 레스토랑 소유자 검증 로직 추가
        menuCommandService.deleteMenu(restaurantId, menuId, deletedBy);

        return ResponseEntity.ok(ApiResponse.success(null, "메뉴가 삭제되었습니다."));
    }
}