package xyz.sparta_project.manjok.domain.restaurant.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Restaurant 도메인 Repository 인터페이스
 *
 * DDD 설계 원칙:
 * - Restaurant는 Aggregate Root
 * - Repository는 Aggregate Root 단위로만 존재
 * - 조회(R) 중심 + CUD는 save()를 통해 처리
 * - Service 레벨에서 도메인 메서드 호출 + 더티체킹으로 CUD 처리
 * - 영속성 전이(Cascade)로 하위 엔티티 자동 관리
 *
 * API 매핑:
 * - 고객용: GET /v1/customers/restaurants
 * - 판매자용: POST/PUT/DELETE /v1/owners/restaurants
 * - 관리자용: GET/PUT/DELETE /v1/admin/restaurants
 */
public interface RestaurantRepository {

    // ==================== CREATE & UPDATE ====================

    /**
     * Restaurant 저장 (생성 및 수정)
     * - 영속성 전이로 모든 하위 엔티티(Menu, MenuCategory, OperatingDay 등) 자동 저장
     * - 신규: ID가 없으면 생성
     * - 수정: ID가 있으면 기존 데이터 수정
     *
     * @param restaurant 저장할 Restaurant Aggregate
     * @return 저장된 Restaurant
     */
    Restaurant save(Restaurant restaurant);

    // ==================== DELETE ====================

    /**
     * Restaurant Soft Delete
     * - isDeleted = true로 설정
     * - 모든 하위 엔티티도 Soft Delete
     *
     * @param restaurantId Restaurant ID
     * @param deletedBy 삭제자
     */
    void delete(String restaurantId, String deletedBy);

    // ==================== READ - Restaurant 단건 조회 ====================

    /**
     * ID로 Restaurant 조회
     * - Aggregate 전체 조회 (Menu, MenuCategory, OperatingDay 포함)
     * - 삭제되지 않은 것만 조회
     *
     * API: GET /v1/customers/restaurants/{restaurantId}
     *
     * @param restaurantId Restaurant ID
     * @return Restaurant (없으면 Optional.empty())
     */
    Optional<Restaurant> findById(String restaurantId);

    /**
     * ID로 Restaurant 조회 (삭제된 것도 포함)
     * - 관리자용
     *
     * API: GET /v1/admin/restaurants/{restaurantId}
     *
     * @param restaurantId Restaurant ID
     * @return Restaurant (없으면 Optional.empty())
     */
    Optional<Restaurant> findByIdIncludingDeleted(String restaurantId);

    // ==================== READ - Restaurant 목록 조회 ====================

    /**
     * 전체 Restaurant 조회 (페이징)
     * - 고객용: 삭제되지 않고 활성화된 Restaurant만
     *
     * API: GET /v1/customers/restaurants
     *
     * @param pageable 페이징 및 정렬 정보
     * @return Restaurant 페이지
     */
    Page<Restaurant> findAll(Pageable pageable);

    /**
     * Owner ID로 Restaurant 목록 조회 (페이징)
     * - 판매자가 자신의 식당 목록 조회
     *
     * API: GET /v1/owners/restaurants
     *
     * @param ownerId Owner ID
     * @param pageable 페이징 정보
     * @return Restaurant 페이지
     */
    Page<Restaurant> findByOwnerId(String ownerId, Pageable pageable);

    /**
     * 전체 Restaurant 조회 (페이징, 관리자용)
     * - 삭제된 Restaurant도 포함
     *
     * API: GET /v1/admin/restaurants
     *
     * @param pageable 페이징 및 정렬 정보
     * @return Restaurant 페이지
     */
    Page<Restaurant> findAllIncludingDeleted(Pageable pageable);

    /**
     * Restaurant와 Menu ID로 단일 Menu 조회 (관리자용)
     * - 삭제된 메뉴도 포함
     *
     * API: GET /v1/admin/restaurants/{restaurantId}/menus/{menuId}
     *
     * @param restaurantId Restaurant ID (필수)
     * @param menuId Menu ID
     * @return Menu (없으면 Optional.empty())
     */
    Optional<Menu> findMenuByRestaurantIdAndMenuIdIncludingDeleted(String restaurantId, String menuId);

    /**
     * Restaurant의 전체 Menu 목록 조회 (관리자용, 페이징)
     * - 삭제된 메뉴도 포함
     *
     * API: GET /v1/admin/restaurants/{restaurantId}/menus
     *
     * @param restaurantId Restaurant ID
     * @param pageable 페이징 및 정렬 정보
     * @return Menu 페이지
     */
    Page<Menu> findMenusByRestaurantIdIncludingDeleted(String restaurantId, Pageable pageable);

    // ==================== READ - Restaurant 검색 ====================

    /**
     * Restaurant 검색 (복합 조건)
     * - 지역별, 카테고리별, 키워드 검색
     * - 모든 파라미터는 Optional (null이면 해당 조건 제외)
     *
     * API: GET /v1/customers/restaurants?province=서울특별시&city=종로구&category=한식&keyword=김치찌개
     *
     * @param province 시/도 (예: 서울특별시)
     * @param city 시/군/구 (예: 종로구)
     * @param district 동/읍/면 (예: 광화문동)
     * @param categoryIds 카테고리 ID 목록
     * @param keyword 키워드 (레스토랑명, 태그 검색)
     * @param pageable 페이징 및 정렬 정보
     * @return Restaurant 페이지
     */
    Page<Restaurant> searchRestaurants(
            String province,
            String city,
            String district,
            Set<String> categoryIds,
            String keyword,
            Pageable pageable
    );

    // ==================== READ - Menu 단건 조회 ====================

    /**
     * Restaurant와 Menu ID로 단일 Menu 조회 (고객용)
     * - 해당 Restaurant의 Menu인지 검증 필수
     * - MenuOptionGroup, MenuOption 포함
     * - 삭제되지 않고 숨김처리 안되고 판매 가능한 것만
     *
     * API: GET /v1/customers/restaurants/{restaurantId}/menus/{menuId}
     *
     * @param restaurantId Restaurant ID (필수)
     * @param menuId Menu ID
     * @return Menu (없으면 Optional.empty())
     */
    Optional<Menu> findMenuByRestaurantIdAndMenuId(String restaurantId, String menuId);

    /**
     * Restaurant와 Menu ID로 단일 Menu 조회 (판매자/관리자용)
     * - 삭제된 것, 숨김처리된 것도 포함
     *
     * API: GET /v1/owners/restaurants/{restaurantId}/menus/{menuId}
     * API: GET /v1/admin/restaurants/{restaurantId}/menus/{menuId}
     *
     * @param restaurantId Restaurant ID (필수)
     * @param menuId Menu ID
     * @return Menu (없으면 Optional.empty())
     */
    Optional<Menu> findMenuByRestaurantIdAndMenuIdIncludingHidden(
            String restaurantId,
            String menuId
    );

    // ==================== READ - Menu 목록 조회 ====================

    /**
     * Restaurant의 Menu 목록 조회 (페이징 + 정렬, 고객용)
     * - 삭제되지 않고 판매 가능하고 숨김처리 안된 Menu만
     * - 정렬: 가격순, 인기순, 최신순 등
     *
     * API: GET /v1/customers/restaurants/{restaurantId}/menus
     *
     * @param restaurantId Restaurant ID
     * @param pageable 페이징 및 정렬 정보
     * @return Menu 페이지
     */
    Page<Menu> findMenusByRestaurantId(String restaurantId, Pageable pageable);

    /**
     * Restaurant의 Menu 목록 조회 (판매자/관리자용, 페이징)
     * - 삭제된 것, 숨김처리된 것도 포함
     *
     * API: GET /v1/owners/restaurants/{restaurantId}/menus
     * API: GET /v1/admin/restaurants/{restaurantId}/menus
     *
     * @param restaurantId Restaurant ID
     * @param pageable 페이징 및 정렬 정보
     * @return Menu 페이지
     */
    Page<Menu> findMenusByRestaurantIdIncludingHidden(
            String restaurantId,
            Pageable pageable
    );

    // ==================== READ - Menu 검색/필터링 ====================

    /**
     * Restaurant의 특정 카테고리 Menu 조회 (페이징)
     * - 카테고리별 필터링 + 페이징 + 정렬
     * - 고객용: 판매 가능한 Menu만
     *
     * API: GET /v1/customers/restaurants/{restaurantId}/menus?categoryId={categoryId}
     *
     * @param restaurantId Restaurant ID
     * @param categoryId Menu 카테고리 ID
     * @param pageable 페이징 및 정렬 정보
     * @return Menu 페이지
     */
    Page<Menu> findMenusByRestaurantIdAndCategory(
            String restaurantId,
            String categoryId,
            Pageable pageable
    );

    /**
     * Restaurant의 Menu 검색 (페이징)
     * - 메뉴명으로 검색 (LIKE)
     * - 고객용: 판매 가능한 Menu만
     *
     * API: GET /v1/customers/restaurants/{restaurantId}/menus?keyword={menuName}
     *
     * @param restaurantId Restaurant ID
     * @param menuName 검색할 메뉴명
     * @param pageable 페이징 및 정렬 정보
     * @return Menu 페이지
     */
    Page<Menu> searchMenusByRestaurantIdAndName(
            String restaurantId,
            String menuName,
            Pageable pageable
    );

    // ==================== READ - MenuCategory ====================

    /**
     * Restaurant의 MenuCategory 목록 조회
     * - 활성화된 카테고리만
     *
     * @param restaurantId Restaurant ID
     * @return MenuCategory 목록
     */
    List<MenuCategory> findMenuCategoriesByRestaurantId(String restaurantId);

    /**
     * Restaurant의 최상위 MenuCategory 조회
     * - depth = 1인 카테고리
     *
     * @param restaurantId Restaurant ID
     * @return MenuCategory 목록
     */
    List<MenuCategory> findRootMenuCategoriesByRestaurantId(String restaurantId);

    // ==================== READ - OperatingDay ====================

    /**
     * Restaurant의 OperatingDay 전체 조회
     *
     * @param restaurantId Restaurant ID
     * @return OperatingDay Set
     */
    Set<OperatingDay> findOperatingDaysByRestaurantId(String restaurantId);

    /**
     * Restaurant의 특정 요일 OperatingDay 조회
     *
     * @param restaurantId Restaurant ID
     * @param dayType 요일
     * @return OperatingDay
     */
    Optional<OperatingDay> findOperatingDayByRestaurantIdAndDayType(
            String restaurantId,
            DayType dayType
    );

    // ==================== 존재 확인 ====================

    /**
     * Restaurant 존재 여부 확인
     *
     * @param restaurantId Restaurant ID
     * @return 존재하면 true
     */
    boolean existsById(String restaurantId);

    /**
     * Restaurant 이름 중복 확인 (Owner 내에서)
     * - 동일 Owner가 같은 이름의 Restaurant를 가지고 있는지 확인
     *
     * @param ownerId Owner ID
     * @param restaurantName Restaurant 이름
     * @return 중복이면 true
     */
    boolean existsByOwnerIdAndName(String ownerId, String restaurantName);

    /**
     * Menu 존재 여부 확인 (Restaurant ID 포함)
     *
     * @param restaurantId Restaurant ID
     * @param menuId Menu ID
     * @return 존재하면 true
     */
    boolean existsMenuByRestaurantIdAndMenuId(String restaurantId, String menuId);

    // ==================== 이벤트 처리용 ====================

    /**
     * Restaurant 조회 (Menu 포함)
     * - 이벤트 핸들러에서 통계 업데이트용
     * - Menu의 구매수, 찜 수 등을 업데이트할 때 사용
     *
     * 사용처:
     * - OrderEventHandler: 주문 완료 시 구매수 증가
     * - WishlistEventHandler: 찜 추가/제거 시 찜 수 증가/감소
     *
     * @param restaurantId Restaurant ID
     * @return Restaurant with Menus
     */
    Optional<Restaurant> findByIdWithMenus(String restaurantId);
}