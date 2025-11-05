package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.restaurant.application.service.MenuCommandService;
import xyz.sparta_project.manjok.domain.restaurant.application.service.MenuQueryService;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.MenuCreateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.MenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.MenuUpdateRequest;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import jakarta.validation.Valid;

/**
 * Owner용 Menu 관리 컨트롤러
 * - 판매자가 자신의 식당 메뉴를 등록/수정/삭제하는 API
 * - 권한: OWNER
 */
@RestController
@RequestMapping("/v1/owners/restaurants/{restaurantId}/menus")
@RequiredArgsConstructor
public class OwnerMenuController {

    private final MenuCommandService menuCommandService;
    private final MenuQueryService menuQueryService;

    /**
     * 메뉴 등록
     * POST /v1/owners/restaurants/{restaurantId}/menus
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     *
     * Request Body: MenuCreateRequest
     *
     * TODO: 소유자 검증 추가
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(
            @PathVariable String restaurantId,
            @Valid @RequestBody MenuCreateRequest request
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증 및 현재 사용자 ID 추출
        String createdBy = "OWNER_1"; // 임시 값

        MenuResponse menu = menuCommandService.createMenu(restaurantId, request, createdBy);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(menu, "메뉴가 성공적으로 등록되었습니다."));
    }

    /**
     * 내 식당 메뉴 목록 조회 (삭제된 것 포함)
     * GET /v1/owners/restaurants/{restaurantId}/menus
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     *
     * Query Parameters:
     * - page: 페이지 번호 (0부터 시작)
     * - size: 페이지 크기 (기본 20)
     * - sort: 정렬 기준 (기본: displayOrder,asc)
     *
     * TODO: 소유자 검증 추가
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MenuResponse>>> getMyMenus(
            @PathVariable String restaurantId,
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증
        PageResponse<MenuResponse> menus = menuQueryService
                .getMenusForOwner(restaurantId, pageable);

        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 메뉴 상세 조회 (삭제된 것 포함)
     * GET /v1/owners/restaurants/{restaurantId}/menus/{menuId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     * - menuId: 메뉴 ID
     *
     * TODO: 소유자 검증 추가
     */
    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponse>> getMyMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증
        MenuResponse menu = menuQueryService.getMenuForOwner(restaurantId, menuId);

        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    /**
     * 메뉴 전체 수정 (PUT)
     * PUT /v1/owners/restaurants/{restaurantId}/menus/{menuId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     * - menuId: 메뉴 ID
     *
     * Request Body: MenuUpdateRequest
     *
     * TODO: 소유자 검증 추가
     */
    @PutMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId,
            @Valid @RequestBody MenuUpdateRequest request
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증 및 현재 사용자 ID 추출
        String updatedBy = "OWNER_1"; // 임시 값

        MenuResponse menu = menuCommandService.updateMenu(restaurantId, menuId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(menu, "메뉴가 수정되었습니다."));
    }

    /**
     * 메뉴 부분 수정 (PATCH)
     * PATCH /v1/owners/restaurants/{restaurantId}/menus/{menuId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     * - menuId: 메뉴 ID
     *
     * Request Body: MenuUpdateRequest (부분 업데이트)
     *
     * TODO: 소유자 검증 추가
     */
    @PatchMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponse>> patchMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId,
            @RequestBody MenuUpdateRequest request
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증 및 현재 사용자 ID 추출
        String updatedBy = "OWNER_1"; // 임시 값

        MenuResponse menu = menuCommandService.patchMenu(restaurantId, menuId, request, updatedBy);

        return ResponseEntity.ok(ApiResponse.success(menu, "메뉴가 수정되었습니다."));
    }

    /**
     * 메뉴 숨김/노출 처리
     * PATCH /v1/owners/restaurants/{restaurantId}/menus/{menuId}/hide
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     * - menuId: 메뉴 ID
     *
     * Query Parameter:
     * - hidden: true(숨김), false(노출)
     *
     * TODO: 소유자 검증 추가
     */
    @PatchMapping("/{menuId}/hide")
    public ResponseEntity<ApiResponse<MenuResponse>> toggleMenuVisibility(
            @PathVariable String restaurantId,
            @PathVariable String menuId,
            @RequestParam boolean hidden
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증 및 현재 사용자 ID 추출
        String updatedBy = "OWNER_1"; // 임시 값

        MenuResponse menu = menuCommandService.toggleMenuVisibility(restaurantId, menuId, hidden, updatedBy);

        String message = hidden ? "메뉴가 숨김 처리되었습니다." : "메뉴가 노출 처리되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(menu, message));
    }

    /**
     * 메뉴 삭제 (Soft Delete)
     * DELETE /v1/owners/restaurants/{restaurantId}/menus/{menuId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     * - menuId: 메뉴 ID
     *
     * TODO: 소유자 검증 추가
     */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 실제 구현 시 소유자 검증 및 현재 사용자 ID 추출
        String deletedBy = "OWNER_1"; // 임시 값

        menuCommandService.deleteMenu(restaurantId, menuId, deletedBy);

        return ResponseEntity.ok(ApiResponse.success(null, "메뉴가 삭제되었습니다."));
    }
}