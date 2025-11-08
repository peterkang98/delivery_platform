package xyz.sparta_project.manjok.domain.restaurant.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Menu;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Restaurant;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminMenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.request.AdminMenuUpdateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.MenuCreateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.MenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.MenuUpdateRequest;

/**
 * Menu Command Service
 * - Menu 생성, 수정, 삭제 등 CUD 작업 담당
 * - Restaurant Aggregate를 통해서만 Menu 조작
 * - 트랜잭션 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuCommandService {

    private final RestaurantRepository restaurantRepository;
    private final MenuMapper menuMapper;

    /**
     * 메뉴 등록 (Owner)
     * - Restaurant를 통해 Menu 생성
     * - 카테고리 연결
     */
    public MenuResponse createMenu(String restaurantId, MenuCreateRequest request, String createdBy) {
        log.info("메뉴 등록 시작 - restaurantId: {}, menuName: {}", restaurantId, request.getMenuName());

        // 1. Restaurant 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. Restaurant를 통해 Menu 생성
        Menu menu = restaurant.addMenu(
                request.getMenuName(),
                request.getDescription(),
                request.getPrice(),
                createdBy
        );

        // 3. 추가 속성 설정
        if (request.getIngredients() != null) {
            menu.update(
                    menu.getMenuName(),
                    menu.getDescription(),
                    request.getIngredients(),
                    menu.getPrice(),
                    request.getCalorie(),
                    createdBy
            );
        }

        // 4. 상태 설정
        if (request.getIsMain() != null && request.getIsMain()) {
            menu.setMain(true, createdBy);
        }
        if (request.getIsPopular() != null && request.getIsPopular()) {
            menu.setPopular(true, createdBy);
        }

        // 5. 카테고리 연결
        if (request.getCategoryIds() != null) {
            for (String categoryId : request.getCategoryIds()) {
                boolean isPrimary = categoryId.equals(request.getPrimaryCategoryId());
                restaurant.addMenuToCategory(menu.getId(), categoryId, isPrimary, createdBy);
            }
        }

        // 6. 저장 (영속성 전이로 Menu도 함께 저장)
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // 7. 저장된 Menu 찾기
        Menu savedMenu = savedRestaurant.getMenus().stream()
                .filter(m -> m.getId().equals(menu.getId()))
                .findFirst()
                .orElseThrow(() -> new RestaurantException(MenuErrorCode.MENU_NOT_FOUND));

        log.info("메뉴 등록 완료 - menuId: {}", savedMenu.getId());

        return menuMapper.toMenuResponse(savedMenu, savedRestaurant);
    }

    /**
     * 메뉴 전체 수정 (PUT)
     * - Owner 전용
     */
    public MenuResponse updateMenu(String restaurantId, String menuId,
                                   MenuUpdateRequest request, String updatedBy) {
        log.info("메뉴 수정 시작 - restaurantId: {}, menuId: {}", restaurantId, menuId);

        // 1. Restaurant 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. Menu 조회
        Menu menu = restaurant.findMenuById(menuId);

        // 3. 기본 정보 업데이트
        menu.update(
                request.getMenuName() != null ? request.getMenuName() : menu.getMenuName(),
                request.getDescription() != null ? request.getDescription() : menu.getDescription(),
                request.getIngredients() != null ? request.getIngredients() : menu.getIngredients(),
                request.getPrice() != null ? request.getPrice() : menu.getPrice(),
                request.getCalorie() != null ? request.getCalorie() : menu.getCalorie(),
                updatedBy
        );

        // 4. 상태 업데이트
        if (request.getIsAvailable() != null) {
            menu.setAvailable(request.getIsAvailable(), updatedBy);
        }
        if (request.getIsMain() != null) {
            menu.setMain(request.getIsMain(), updatedBy);
        }
        if (request.getIsPopular() != null) {
            menu.setPopular(request.getIsPopular(), updatedBy);
        }

        // 5. 카테고리 업데이트
        if (request.getCategoryIds() != null) {
            // 기존 카테고리 제거
            menu.getCategoryRelations().forEach(rel -> menu.removeCategory(rel.getCategoryId(), updatedBy));

            // 새 카테고리 추가
            for (String categoryId : request.getCategoryIds()) {
                boolean isPrimary = categoryId.equals(request.getPrimaryCategoryId());
                restaurant.addMenuToCategory(menuId, categoryId, isPrimary, updatedBy);
            }
        }

        // 6. 저장
        restaurantRepository.save(restaurant);

        log.info("메뉴 수정 완료 - menuId: {}", menuId);

        return menuMapper.toMenuResponse(menu, restaurant);
    }

    /**
     * 메뉴 부분 수정 (PATCH)
     * - Owner 전용
     * - null이 아닌 필드만 업데이트
     */
    public MenuResponse patchMenu(String restaurantId, String menuId,
                                  MenuUpdateRequest request, String updatedBy) {
        log.info("메뉴 부분 수정 시작 - restaurantId: {}, menuId: {}", restaurantId, menuId);

        // 1. Restaurant 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. Menu 조회
        Menu menu = restaurant.findMenuById(menuId);

        // 3. null이 아닌 필드만 업데이트
        if (request.getMenuName() != null || request.getDescription() != null ||
                request.getIngredients() != null || request.getPrice() != null ||
                request.getCalorie() != null) {
            menu.update(
                    request.getMenuName() != null ? request.getMenuName() : menu.getMenuName(),
                    request.getDescription() != null ? request.getDescription() : menu.getDescription(),
                    request.getIngredients() != null ? request.getIngredients() : menu.getIngredients(),
                    request.getPrice() != null ? request.getPrice() : menu.getPrice(),
                    request.getCalorie() != null ? request.getCalorie() : menu.getCalorie(),
                    updatedBy
            );
        }

        // 4. 상태 업데이트
        if (request.getIsAvailable() != null) {
            menu.setAvailable(request.getIsAvailable(), updatedBy);
        }
        if (request.getIsMain() != null) {
            menu.setMain(request.getIsMain(), updatedBy);
        }
        if (request.getIsPopular() != null) {
            menu.setPopular(request.getIsPopular(), updatedBy);
        }

        // 5. 카테고리 업데이트 (null이 아닌 경우만)
        if (request.getCategoryIds() != null) {
            menu.getCategoryRelations().forEach(rel -> menu.removeCategory(rel.getCategoryId(), updatedBy));

            for (String categoryId : request.getCategoryIds()) {
                boolean isPrimary = categoryId.equals(request.getPrimaryCategoryId());
                restaurant.addMenuToCategory(menuId, categoryId, isPrimary, updatedBy);
            }
        }

        log.info("메뉴 부분 수정 완료 - menuId: {}", menuId);

        return menuMapper.toMenuResponse(menu, restaurant);
    }

    /**
     * 메뉴 숨김/노출 처리
     * - Owner 전용
     * - isHidden 플래그 토글
     */
    public MenuResponse toggleMenuVisibility(String restaurantId, String menuId,
                                             boolean hidden, String updatedBy) {
        log.info("메뉴 숨김/노출 처리 - restaurantId: {}, menuId: {}, hidden: {}",
                restaurantId, menuId, hidden);

        // 1. Restaurant 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. Menu 조회
        Menu menu = restaurant.findMenuById(menuId);

        // 3. 가시성 변경 (isAvailable 사용)
        menu.setAvailable(!hidden, updatedBy);

        // 4. 저장
        restaurantRepository.save(restaurant);

        log.info("메뉴 숨김/노출 처리 완료 - menuId: {}", menuId);

        return menuMapper.toMenuResponse(menu, restaurant);
    }

    /**
     * 메뉴 삭제 (Soft Delete)
     * - Owner/Admin 공통
     * - 하위 MenuOptionGroup, MenuOption도 cascade soft delete
     */
    public void deleteMenu(String restaurantId, String menuId, String deletedBy) {
        log.info("메뉴 삭제 시작 - restaurantId: {}, menuId: {}", restaurantId, menuId);

        // 1. Restaurant 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. Menu 조회
        Menu menu = restaurant.findMenuById(menuId);

        // 3. Soft Delete
        menu.delete(deletedBy);

        // 4. 저장
        restaurantRepository.save(restaurant);

        log.info("메뉴 삭제 완료 - menuId: {}", menuId);
    }

    /**
     * 메뉴 복구 (Admin 전용)
     */
    public AdminMenuResponse restoreMenu(String restaurantId, String menuId, String updatedBy) {
        log.info("메뉴 복구 시작 - restaurantId: {}, menuId: {}", restaurantId, menuId);

        // 1. Restaurant 조회 (삭제된 것 포함)
        Restaurant restaurant = restaurantRepository.findByIdIncludingDeleted(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. Menu 조회 (삭제된 것 포함)
        Menu menu = restaurant.getMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new RestaurantException(MenuErrorCode.MENU_NOT_FOUND));

        // 3. 복구
        menu.restore(updatedBy);

        // 4. 저장
        restaurantRepository.save(restaurant);

        log.info("메뉴 복구 완료 - menuId: {}", menuId);

        return menuMapper.toAdminMenuResponse(menu, restaurant);
    }

    /**
     * 메뉴 수정 (Admin 전용)
     */
    public AdminMenuResponse updateMenuByAdmin(String restaurantId, String menuId,
                                               AdminMenuUpdateRequest request, String updatedBy) {
        log.info("메뉴 수정 시작 (Admin) - restaurantId: {}, menuId: {}", restaurantId, menuId);

        // 1. Restaurant 조회 (삭제된 것 포함)
        Restaurant restaurant = restaurantRepository.findByIdIncludingDeleted(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. Menu 조회
        Menu menu = restaurant.getMenus().stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new RestaurantException(MenuErrorCode.MENU_NOT_FOUND));

        // 3. 기본 정보 업데이트
        if (request.getMenuName() != null || request.getDescription() != null ||
                request.getIngredients() != null || request.getPrice() != null ||
                request.getCalorie() != null) {
            menu.update(
                    request.getMenuName() != null ? request.getMenuName() : menu.getMenuName(),
                    request.getDescription() != null ? request.getDescription() : menu.getDescription(),
                    request.getIngredients() != null ? request.getIngredients() : menu.getIngredients(),
                    request.getPrice() != null ? request.getPrice() : menu.getPrice(),
                    request.getCalorie() != null ? request.getCalorie() : menu.getCalorie(),
                    updatedBy
            );
        }

        // 4. 상태 업데이트
        if (request.getIsAvailable() != null) {
            menu.setAvailable(request.getIsAvailable(), updatedBy);
        }
        if (request.getIsMain() != null) {
            menu.setMain(request.getIsMain(), updatedBy);
        }
        if (request.getIsPopular() != null) {
            menu.setPopular(request.getIsPopular(), updatedBy);
        }

        // 5. 저장
        restaurantRepository.save(restaurant);

        log.info("메뉴 수정 완료 (Admin) - menuId: {}", menuId);

        return menuMapper.toAdminMenuResponse(menu, restaurant);
    }
}