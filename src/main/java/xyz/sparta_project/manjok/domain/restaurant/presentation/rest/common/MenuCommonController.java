package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.restaurant.application.service.MenuQueryService;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.MenuDetailResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.MenuSummaryResponse;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

/**
 * Common용 Menu 조회 컨트롤러 (인증 불필요)
 * - 기본 경로: /v1/common/restaurants/{restaurantId}/menus
 * - 로그인 없이 메뉴 정보를 조회하는 API
 */
@RestController
@RequestMapping("/v1/common/restaurants/{restaurantId}/menus")
@RequiredArgsConstructor
public class MenuCommonController {

    private final MenuQueryService menuQueryService;

    /**
     * 특정 식당의 메뉴 목록 조회
     * GET /v1/common/restaurants/{restaurantId}/menus
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MenuSummaryResponse>>> getMenus(
            @PathVariable String restaurantId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable) {

        PageResponse<MenuSummaryResponse> menus;

        if (categoryId != null) {
            menus = menuQueryService.getMenusByCategory(restaurantId, categoryId, pageable);
        } else if (keyword != null) {
            menus = menuQueryService.searchMenus(restaurantId, keyword, pageable);
        } else {
            menus = menuQueryService.getMenus(restaurantId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 특정 메뉴 상세 조회
     * GET /v1/common/restaurants/{restaurantId}/menus/{menuId}
     */
    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> getMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId) {

        MenuDetailResponse menu = menuQueryService.getMenuDetail(restaurantId, menuId);

        return ResponseEntity.ok(ApiResponse.success(menu));
    }
}