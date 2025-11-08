package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer;

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
 * Customer용 Menu 조회 컨트롤러
 * - 고객이 메뉴 정보를 조회하는 API
 * - 인증 불필요 (Public API)
 */
@RestController
@RequestMapping("/v1/customers/restaurants/{restaurantId}/menus")
@RequiredArgsConstructor
public class CustomerMenuController {

    private final MenuQueryService menuQueryService;

    /**
     * 특정 식당의 메뉴 목록 조회
     * GET /v1/customers/restaurants/{restaurantId}/menus
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     *
     * Query Parameters:
     * - categoryId: 메뉴 카테고리 ID (옵션)
     * - keyword: 메뉴명 검색 키워드 (옵션)
     * - page: 페이지 번호 (0부터 시작)
     * - size: 페이지 크기 (기본 20)
     * - sort: 정렬 기준 (기본: displayOrder,asc)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MenuSummaryResponse>>> getMenus(
            @PathVariable String restaurantId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable
    ) {
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
     * GET /v1/customers/restaurants/{restaurantId}/menus/{menuId}
     *
     * Path Variable:
     * - restaurantId: 식당 ID
     * - menuId: 메뉴 ID
     */
    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> getMenu(
            @PathVariable String restaurantId,
            @PathVariable String menuId
    ) {
        MenuDetailResponse menu = menuQueryService.getMenuDetail(restaurantId, menuId);

        return ResponseEntity.ok(ApiResponse.success(menu));
    }
}