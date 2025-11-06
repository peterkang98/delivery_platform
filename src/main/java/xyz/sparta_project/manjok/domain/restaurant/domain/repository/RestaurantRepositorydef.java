//package xyz.sparta_project.manjok.domain.restaurant.domain.repository;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
///**
// * Restaurant 도메인 Repository 인터페이스
// * - 조회(R) 중심 + Save 추가
// * - CUD는 Service 레벨에서 도메인 메서드 + 더티체킹으로 처리
// */
//public interface RestaurantRepositorydef {
//
//    // ==================== Restaurant CUD ====================
//
//    /**
//     * 레스토랑을 저장합니다.
//     *
//     * @param restaurant 저장할 레스토랑
//     * @return 저장된 레스토랑
//     */
//    Restaurant save(Restaurant restaurant);
//
//    /**
//     * 레스토랑 정보를 업데이트합니다. (더티 체킹)
//     *
//     * @param restaurant 업데이트할 레스토랑
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     */
//    void updateRestaurant(Restaurant restaurant);
//
//    // ==================== OperatingDay CUD ====================
//
//    /**
//     * 운영 시간을 저장합니다.
//     *
//     * @param operatingDay 저장할 운영 시간
//     * @return 저장된 운영 시간
//     */
//    OperatingDay saveOperatingDay(OperatingDay operatingDay);
//
//    /**
//     * 여러 운영 시간을 한번에 저장합니다.
//     *
//     * @param operatingDays 저장할 운영 시간 목록
//     * @return 저장된 운영 시간 목록
//     */
//    Set<OperatingDay> saveAllOperatingDays(Set<OperatingDay> operatingDays);
//
//    /**
//     * 운영 시간을 업데이트합니다.
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param operatingDay 업데이트할 운영 시간
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     */
//    void updateOperatingDay(String restaurantId, OperatingDay operatingDay);
//
//    /**
//     * 특정 레스토랑의 모든 운영 시간을 삭제하고 새로 저장합니다.
//     *
//     * @param restaurantId 레스토랑 ID
//     * @param operatingDays 새로운 운영 시간 목록
//     * @return 저장된 운영 시간 목록
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     */
//    Set<OperatingDay> replaceAllOperatingDays(String restaurantId, Set<OperatingDay> operatingDays);
//
//    /**
//     * 특정 레스토랑의 운영 시간을 삭제합니다.
//     *
//     * @param restaurantId 레스토랑 ID
//     * @param dayType 요일 타입
//     * @param timeType 시간 타입
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     */
//    void deleteOperatingDay(String restaurantId, DayType dayType, OperatingTimeType timeType);
//
//    /**
//     * 특정 레스토랑의 모든 운영 시간을 삭제합니다.
//     *
//     * @param restaurantId 레스토랑 ID
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     */
//    void deleteAllOperatingDays(String restaurantId);
//
//    // ==================== Menu CUD ====================
//
//    /**
//     * 메뉴를 저장합니다.
//     *
//     * @param menu 저장할 메뉴
//     * @return 저장된 메뉴
//     */
//    Menu saveMenu(Menu menu);
//
//    /**
//     * 메뉴 정보를 업데이트합니다. (더티 체킹)
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param menu 업데이트할 메뉴
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException MENU_NOT_FOUND, MENU_NOT_BELONG_TO_RESTAURANT
//     */
//    void updateMenu(String restaurantId, Menu menu);
//
//    /**
//     * 메뉴를 삭제합니다. (soft delete)
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param menuId 메뉴 ID
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException MENU_NOT_FOUND, MENU_NOT_BELONG_TO_RESTAURANT
//     */
//    void deleteMenu(String restaurantId, String menuId);
//
//    // ==================== MenuOptionGroup CUD ====================
//
//    /**
//     * 메뉴 옵션 그룹을 저장합니다.
//     *
//     * @param optionGroup 저장할 옵션 그룹
//     * @return 저장된 옵션 그룹
//     */
//    MenuOptionGroup saveMenuOptionGroup(MenuOptionGroup optionGroup);
//
//    /**
//     * 메뉴 옵션 그룹을 업데이트합니다. (더티 체킹)
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param optionGroup 업데이트할 옵션 그룹
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException OPTION_GROUP_NOT_FOUND, OPTION_GROUP_NOT_BELONG_TO_RESTAURANT
//     */
//    void updateMenuOptionGroup(String restaurantId, MenuOptionGroup optionGroup);
//
//    /**
//     * 메뉴 옵션 그룹을 삭제합니다. (soft delete)
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param optionGroupId 옵션 그룹 ID
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException OPTION_GROUP_NOT_FOUND, OPTION_GROUP_NOT_BELONG_TO_RESTAURANT
//     */
//    void deleteMenuOptionGroup(String restaurantId, String optionGroupId);
//
//    // ==================== MenuOption CUD ====================
//
//    /**
//     * 메뉴 옵션을 저장합니다.
//     *
//     * @param option 저장할 옵션
//     * @return 저장된 옵션
//     */
//    MenuOption saveMenuOption(MenuOption option);
//
//    /**
//     * 메뉴 옵션을 업데이트합니다. (더티 체킹)
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param option 업데이트할 옵션
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException OPTION_NOT_FOUND, OPTION_NOT_BELONG_TO_RESTAURANT
//     */
//    void updateMenuOption(String restaurantId, MenuOption option);
//
//    /**
//     * 메뉴 옵션을 삭제합니다. (soft delete)
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param optionId 옵션 ID
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException OPTION_NOT_FOUND, OPTION_NOT_BELONG_TO_RESTAURANT
//     */
//    void deleteMenuOption(String restaurantId, String optionId);
//
//    // ==================== MenuCategory CUD ====================
//
//    /**
//     * 메뉴 카테고리를 저장합니다.
//     *
//     * @param category 저장할 카테고리
//     * @return 저장된 카테고리
//     */
//    MenuCategory saveMenuCategory(MenuCategory category);
//
//    /**
//     * 메뉴 카테고리를 업데이트합니다. (더티 체킹)
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param category 업데이트할 카테고리
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException CATEGORY_NOT_FOUND, CATEGORY_NOT_BELONG_TO_RESTAURANT
//     */
//    void updateMenuCategory(String restaurantId, MenuCategory category);
//
//    /**
//     * 메뉴 카테고리를 삭제합니다. (soft delete)
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param categoryId 카테고리 ID
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException CATEGORY_NOT_FOUND, CATEGORY_NOT_BELONG_TO_RESTAURANT
//     */
//    void deleteMenuCategory(String restaurantId, String categoryId);
//
//    // ==================== MenuCategoryRelation CUD ====================
//
//    /**
//     * 메뉴-카테고리 관계를 저장합니다.
//     *
//     * @param relation 저장할 관계
//     * @return 저장된 관계
//     */
//    MenuCategoryRelation saveMenuCategoryRelation(MenuCategoryRelation relation);
//
//    /**
//     * 메뉴-카테고리 관계를 삭제합니다. (soft delete)
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param menuId 메뉴 ID
//     * @param categoryId 카테고리 ID
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND, RELATION_NOT_FOUND, RELATION_NOT_BELONG_TO_RESTAURANT
//     */
//    void deleteMenuCategoryRelation(String restaurantId, String menuId, String categoryId);
//
//    /**
//     * 특정 메뉴의 모든 카테고리 관계를 삭제합니다.
//     *
//     * @param restaurantId 레스토랑 ID (소유권 검증용)
//     * @param menuId 메뉴 ID
//     * @throws xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException RESTAURANT_NOT_FOUND
//     */
//    void deleteAllMenuCategoryRelationsByMenuId(String restaurantId, String menuId);
//    // ==================== 기본 조회 ====================
//
//    /**
//     * ID로 레스토랑 조회 (기본 정보만)
//     * @param id 레스토랑 ID
//     * @return 레스토랑 도메인
//     */
//    Optional<Restaurant> findById(String id);
//
//    /**
//     * ID로 레스토랑 조회 (모든 연관 관계 포함)
//     * - 메뉴, 메뉴 카테고리, 옵션 그룹, 옵션, 운영 시간, 카테고리 관계 모두 포함
//     * @param id 레스토랑 ID
//     * @return 완전한 Restaurant Aggregate
//     */
//    Optional<Restaurant> findByIdWithAll(String id);
//
//    /**
//     * ID로 레스토랑 조회 (메뉴만 포함)
//     * - 메뉴와 메뉴의 옵션까지 포함
//     * @param id 레스토랑 ID
//     * @return 레스토랑 + 메뉴 정보
//     */
//    Optional<Restaurant> findByIdWithMenus(String id);
//
//    /**
//     * ID로 레스토랑 조회 (메뉴 카테고리만 포함)
//     * @param id 레스토랑 ID
//     * @return 레스토랑 + 메뉴 카테고리 정보
//     */
//    Optional<Restaurant> findByIdWithMenuCategories(String id);
//
//    // ==================== 레스토랑 목록 조회 ====================
//
//    /**
//     * 주인 ID로 레스토랑 목록 조회
//     * @param ownerId 주인 ID
//     * @return 레스토랑 목록 (기본 정보만)
//     */
//    List<Restaurant> findByOwnerId(Long ownerId);
//
//    /**
//     * 활성화된 레스토랑 목록 조회
//     * @return 활성화된 레스토랑 목록
//     */
//    List<Restaurant> findAllActive();
//
//    /**
//     * 레스토랑 카테고리로 검색
//     * - RestaurantCategoryRelation을 조인하여 조회
//     * @param categoryId 레스토랑 카테고리 ID
//     * @return 해당 카테고리의 레스토랑 목록
//     */
//    List<Restaurant> findByCategoryId(String categoryId);
//
//    /**
//     * 레스토랑 이름으로 검색 (LIKE 검색)
//     * @param restaurantName 레스토랑 이름 (부분 검색)
//     * @return 검색된 레스토랑 목록
//     */
//    List<Restaurant> findByRestaurantNameContaining(String restaurantName);
//
//    /**
//     * 레스토랑 카테고리와 이름으로 검색
//     * @param categoryId 레스토랑 카테고리 ID
//     * @param restaurantName 레스토랑 이름 (부분 검색)
//     * @return 검색된 레스토랑 목록
//     */
//    List<Restaurant> findByCategoryIdAndRestaurantName(String categoryId, String restaurantName);
//
//    // ==================== 레스토랑 페이징 조회 ====================
//
//    /**
//     * 레스토랑 페이징 조회 (카테고리 조인 포함)
//     * - 레스토랑 기본 정보 + 메인 카테고리 정보
//     * - 검색 조건: 카테고리, 레스토랑 이름
//     * @param categoryId 레스토랑 카테고리 ID (nullable)
//     * @param restaurantName 레스토랑 이름 검색어 (nullable)
//     * @param pageable 페이징 정보
//     * @return 페이징된 레스토랑 목록
//     */
//    Page<Restaurant> findRestaurantsWithCategory(String categoryId, String restaurantName, Pageable pageable);
//
//    /**
//     * 활성화된 레스토랑 페이징 조회
//     * @param pageable 페이징 정보
//     * @return 페이징된 레스토랑 목록
//     */
//    Page<Restaurant> findAllActive(Pageable pageable);
//
//    // ==================== 메뉴 조회 ====================
//
//    /**
//     * 특정 레스토랑의 특정 메뉴 조회 (옵션 포함)
//     * - 메뉴와 옵션 그룹, 옵션까지 모두 조회
//     * @param restaurantId 레스토랑 ID
//     * @param menuId 메뉴 ID
//     * @return 메뉴 정보 (옵션 포함)
//     */
//    Optional<Menu> findMenuByRestaurantIdAndMenuId(String restaurantId, String menuId);
//
//    /**
//     * 특정 레스토랑의 모든 메뉴 조회 (옵션 포함)
//     * @param restaurantId 레스토랑 ID
//     * @return 메뉴 목록 (각 메뉴의 옵션 포함)
//     */
//    List<Menu> findMenusByRestaurantId(String restaurantId);
//
//    /**
//     * 특정 레스토랑의 메뉴 카테고리별 메뉴 조회
//     * @param restaurantId 레스토랑 ID
//     * @param menuCategoryId 메뉴 카테고리 ID
//     * @return 해당 카테고리의 메뉴 목록
//     */
//    List<Menu> findMenusByRestaurantIdAndMenuCategoryId(String restaurantId, String menuCategoryId);
//
//    // ==================== 메뉴 페이징 조회 ====================
//
//    /**
//     * 특정 레스토랑의 메뉴 페이징 조회 (옵션 포함)
//     * @param restaurantId 레스토랑 ID
//     * @param pageable 페이징 정보
//     * @return 페이징된 메뉴 목록
//     */
//    Page<Menu> findMenusByRestaurantId(String restaurantId, Pageable pageable);
//
//    /**
//     * 특정 레스토랑의 메뉴 카테고리별 메뉴 페이징 조회
//     * @param restaurantId 레스토랑 ID
//     * @param menuCategoryId 메뉴 카테고리 ID (nullable)
//     * @param pageable 페이징 정보
//     * @return 페이징된 메뉴 목록
//     */
//    Page<Menu> findMenusByRestaurantIdAndMenuCategoryId(String restaurantId, String menuCategoryId, Pageable pageable);
//
//    // ==================== 존재 여부 확인 ====================
//
//    /**
//     * 레스토랑 존재 여부 확인
//     * @param id 레스토랑 ID
//     * @return 존재 여부
//     */
//    boolean existsById(String id);
//
//    /**
//     * 주인이 소유한 레스토랑 존재 여부 확인
//     * @param ownerId 주인 ID
//     * @return 존재 여부
//     */
//    boolean existsByOwnerId(Long ownerId);
//}