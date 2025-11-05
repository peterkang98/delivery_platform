package xyz.sparta_project.manjok.domain.restaurant.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Menu;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Restaurant;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.AdminMenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.MenuDetailResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.MenuSummaryResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.MenuResponse;
import xyz.sparta_project.manjok.global.common.utils.PageUtils;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

/**
 * Menu Query Service
 * - Menu 조회 전담 서비스
 * - Read-Only 트랜잭션
 * - 권한별 조회 메서드 분리 (Customer, Owner, Admin)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuQueryService {

    private final RestaurantRepository restaurantRepository;
    private final MenuMapper menuMapper;

    // ==================== Customer 조회 API ====================

    /**
     * 특정 식당의 메뉴 목록 조회 (Customer)
     * - 삭제되지 않고 판매 가능한 메뉴만 조회
     */
    public PageResponse<MenuSummaryResponse> getMenus(String restaurantId, Pageable pageable) {
        log.info("메뉴 목록 조회 - restaurantId: {}", restaurantId);

        // Restaurant 조회 (MenuCategory 정보 필요)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        Page<Menu> menuPage = restaurantRepository.findMenusByRestaurantId(restaurantId, pageable);

        return PageUtils.toPageResponse(
                menuPage,
                menu -> menuMapper.toMenuSummaryResponse(menu, restaurant)
        );
    }

    /**
     * 카테고리별 메뉴 조회 (Customer)
     */
    public PageResponse<MenuSummaryResponse> getMenusByCategory(
            String restaurantId,
            String categoryId,
            Pageable pageable
    ) {
        log.info("카테고리별 메뉴 조회 - restaurantId: {}, categoryId: {}", restaurantId, categoryId);

        // Restaurant 조회 (MenuCategory 정보 필요)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        Page<Menu> menuPage = restaurantRepository.findMenusByRestaurantIdAndCategory(
                restaurantId, categoryId, pageable
        );

        return PageUtils.toPageResponse(
                menuPage,
                menu -> menuMapper.toMenuSummaryResponse(menu, restaurant)
        );
    }

    /**
     * 메뉴명 검색 (Customer)
     */
    public PageResponse<MenuSummaryResponse> searchMenus(
            String restaurantId,
            String keyword,
            Pageable pageable
    ) {
        log.info("메뉴 검색 - restaurantId: {}, keyword: {}", restaurantId, keyword);

        // Restaurant 조회 (MenuCategory 정보 필요)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        Page<Menu> menuPage = restaurantRepository.searchMenusByRestaurantIdAndName(
                restaurantId, keyword, pageable
        );

        return PageUtils.toPageResponse(
                menuPage,
                menu -> menuMapper.toMenuSummaryResponse(menu, restaurant)
        );
    }

    /**
     * 메뉴 상세 조회 (Customer)
     * - 삭제되지 않고 판매 가능한 메뉴만 조회
     */
    public MenuDetailResponse getMenuDetail(String restaurantId, String menuId) {
        log.info("메뉴 상세 조회 - restaurantId: {}, menuId: {}", restaurantId, menuId);

        // Restaurant 조회 (MenuCategory 정보 필요)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        Menu menu = restaurantRepository.findMenuByRestaurantIdAndMenuId(restaurantId, menuId)
                .orElseThrow(() -> new RestaurantException(MenuErrorCode.MENU_NOT_FOUND));

        return menuMapper.toMenuDetailResponse(menu, restaurant);
    }

    // ==================== Owner 조회 API ====================

    /**
     * Owner의 식당 메뉴 목록 조회
     * - 삭제된 메뉴도 조회 가능
     */
    public PageResponse<MenuResponse> getMenusForOwner(String restaurantId, Pageable pageable) {
        log.info("Owner 메뉴 목록 조회 - restaurantId: {}", restaurantId);

        // Restaurant 조회 (MenuCategory 정보 필요)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        Page<Menu> menuPage = restaurantRepository.findMenusByRestaurantIdIncludingHidden(
                restaurantId, pageable
        );

        return PageUtils.toPageResponse(
                menuPage,
                menu -> menuMapper.toMenuResponse(menu, restaurant)
        );
    }

    /**
     * Owner의 특정 메뉴 상세 조회
     * - 삭제된 메뉴도 조회 가능
     */
    public MenuResponse getMenuForOwner(String restaurantId, String menuId) {
        log.info("Owner 메뉴 상세 조회 - restaurantId: {}, menuId: {}", restaurantId, menuId);

        // Restaurant 조회 (MenuCategory 정보 필요)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        Menu menu = restaurantRepository.findMenuByRestaurantIdAndMenuIdIncludingHidden(
                        restaurantId, menuId)
                .orElseThrow(() -> new RestaurantException(MenuErrorCode.MENU_NOT_FOUND));

        return menuMapper.toMenuResponse(menu, restaurant);
    }

    // ==================== Admin 조회 API ====================

    /**
     * 특정 식당의 전체 메뉴 조회 (Admin)
     * - 삭제된 메뉴도 포함
     */
    public PageResponse<AdminMenuResponse> getMenusForAdmin(String restaurantId, Pageable pageable) {
        log.info("Admin 메뉴 목록 조회 - restaurantId: {}", restaurantId);

        // Restaurant 조회 (삭제된 것 포함, MenuCategory 정보 필요)
        Restaurant restaurant = restaurantRepository.findByIdIncludingDeleted(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        Page<Menu> menuPage = restaurantRepository.findMenusByRestaurantIdIncludingDeleted(
                restaurantId, pageable
        );

        return PageUtils.toPageResponse(
                menuPage,
                menu -> menuMapper.toAdminMenuResponse(menu, restaurant)
        );
    }

    /**
     * 특정 메뉴 상세 조회 (Admin)
     * - 삭제된 메뉴도 조회 가능
     */
    public AdminMenuResponse getMenuForAdmin(String restaurantId, String menuId) {
        log.info("Admin 메뉴 상세 조회 - restaurantId: {}, menuId: {}", restaurantId, menuId);

        // Restaurant 조회 (삭제된 것 포함, MenuCategory 정보 필요)
        Restaurant restaurant = restaurantRepository.findByIdIncludingDeleted(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        Menu menu = restaurantRepository.findMenuByRestaurantIdAndMenuIdIncludingDeleted(
                        restaurantId, menuId)
                .orElseThrow(() -> new RestaurantException(MenuErrorCode.MENU_NOT_FOUND));

        return menuMapper.toAdminMenuResponse(menu, restaurant);
    }

    // ==================== 공통 메서드 ====================

    /**
     * Menu 존재 여부 확인
     */
    public boolean existsMenu(String restaurantId, String menuId) {
        return restaurantRepository.existsMenuByRestaurantIdAndMenuId(restaurantId, menuId);
    }
}