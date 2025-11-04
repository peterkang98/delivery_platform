package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Restaurant Aggregate Root
 * - 음식점의 모든 정보와 비즈니스 규칙을 관리
 * - Menu, MenuCategory, OperatingDay의 생명주기를 관리
 * - 순수 도메인 모델
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Restaurant {

    // 식별자
    private String id;
    private LocalDateTime createdAt;

    // 기본정보
    private Long ownerId;
    private String ownerName;
    private String restaurantName;

    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.OPEN;

    // 주소 (value Object)
    private Address address;

    // 좌표 정보 (value Object)
    private Coordinate coordinate;

    // 연락처 및 카테고리
    private String contactNumber;

    //통계 데이터
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    private Integer wishlistCount = 0;

    @Builder.Default
    private Integer reviewCount = 0;

    @Builder.Default
    private BigDecimal reviewRating = BigDecimal.ZERO;

    @Builder.Default
    private Integer purchaseCount = 0;

    // 감사 필드
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    @Builder.Default
    private boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;

    // 연관 관계
    @Builder.Default
    List<Menu> menus = new ArrayList<>();

    @Builder.Default
    private List<MenuCategory> menuCategories = new ArrayList<>();

    @Builder.Default
    private Set<OperatingDay> operatingDays = new HashSet<>();

    @Builder.Default
    private Set<RestaurantCategoryRelation> categoryRelations = new HashSet<>();

    // ==================== 레스토랑 기본 정보 관리 ====================

    /**
     * 레스토랑 기본 정보 업데이트
     */
    public void updateBasicInfo(String restaurantName, String contactNumber,
                                String updatedBy) {
        this.restaurantName = restaurantName;
        this.contactNumber = contactNumber;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 주소 업데이트
     */
    public void updateAddress(Address address, String updatedBy) {
        if (address != null && !address.isValid()) {
            throw new RestaurantException(RestaurantErrorCode.INVALID_ADDRESS);
        }
        this.address = address;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 좌표 업데이트
     */
    public void updateCoordinate(Coordinate coordinate, String updatedBy) {
        this.coordinate = coordinate;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 운영 상태 변경
     */
    public void changeStatus(RestaurantStatus newStatus, String updatedBy) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 태그 추가
     */
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    /**
     * 태그 제거
     */
    public void removeTag(String tag) {
        this.tags.remove(tag);
    }

    // ==================== 메뉴 카테고리 관리 ====================

    /**
     * 메뉴 카테고리 추가
     */
    public MenuCategory addMenuCategory(String categoryName, String description,
                                        String parentCategoryId, Integer displayOrder,
                                        String createdBy) {
        // 계층 깊이 계산
        Integer depth = 1;
        if (parentCategoryId != null) {
            MenuCategory parentCategory = findMenuCategoryById(parentCategoryId);
            if (parentCategory != null) {
                depth = parentCategory.getDepth() + 1;
                if (depth > 3) {
                    throw new RestaurantException(MenuErrorCode.INVALID_CATEGORY_DEPTH);
                }
            }
        }

        MenuCategory category = MenuCategory.builder()
                .id(generateCategoryId())
                .createdAt(LocalDateTime.now())
                .restaurantId(this.id)
                .categoryName(categoryName)
                .description(description)
                .parentCategoryId(parentCategoryId)
                .depth(depth)
                .displayOrder(displayOrder != null ? displayOrder : menuCategories.size())
                .isActive(true)
                .createdBy(createdBy)
                .build();

        this.menuCategories.add(category);
        return category;
    }

    /**
     * 메뉴 카테고리 찾기
     */
    public MenuCategory findMenuCategoryById(String categoryId) {
        return menuCategories.stream()
                .filter(cat -> cat.getId() != null && cat.getId().equals(categoryId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 활성 메뉴 카테고리 목록
     */
    public List<MenuCategory> getActiveMenuCategories() {
        return menuCategories.stream()
                .filter(MenuCategory::isAvailable)
                .collect(Collectors.toList());
    }

    // ==================== 메뉴 관리 ====================

    /**
     * 메뉴 추가
     */
    public Menu addMenu(String menuName, String description, BigDecimal price,
                        String createdBy) {
        if (!status.canModifyMenu()) {
            throw new RestaurantException(RestaurantErrorCode.CANNOT_MODIFY_MENU_WHILE_OPEN);
        }

        Menu menu = Menu.builder()
                .id(generateMenuId())
                .createdAt(LocalDateTime.now())
                .restaurantId(this.id)
                .menuName(menuName)
                .description(description)
                .price(price)
                .isAvailable(true)
                .createdBy(createdBy)
                .build();

        menu.validate();
        this.menus.add(menu);
        return menu;
    }

    /**
     * 메뉴에 카테고리 연결
     */
    public MenuCategoryRelation addMenuToCategory(String menuId, String categoryId,
                                                  boolean isPrimary, String createdBy) {
        Menu menu = findMenuById(menuId);
        MenuCategory category = findMenuCategoryById(categoryId);

        if (category == null) {
            throw new RestaurantException(MenuErrorCode.CATEGORY_NOT_FOUND);
        }

        // 메뉴에 카테고리 추가
        MenuCategoryRelation relation = menu.addCategory(categoryId, isPrimary, createdBy);

        // 카테고리에도 메뉴 ID 추가
        category.addMenu(menuId);

        return relation;
    }

    /**
     * 메뉴 찾기
     */
    public Menu findMenuById(String menuId) {
        return menus.stream()
                .filter(menu -> menu.getId() != null && menu.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new RestaurantException(MenuErrorCode.MENU_NOT_FOUND));
    }

    /**
     * 메뉴 제거
     */
    public void removeMenu(String menuId, String deletedBy) {
        Menu menu = findMenuById(menuId);
        menu.delete(deletedBy);

        // 카테고리에서도 메뉴 ID 제거
        menuCategories.forEach(cat -> cat.removeMenu(menuId));
    }

    /**
     * 활성 메뉴 개수
     */
    public int getActiveMenuCount() {
        return (int) menus.stream()
                .filter(Menu::isOrderable)
                .count();
    }

    /**
     * 대표 메뉴 목록
     */
    public List<Menu> getMainMenus() {
        return menus.stream()
                .filter(menu -> menu.getIsMain() && menu.isOrderable())
                .collect(Collectors.toList());
    }

    /**
     * 인기 메뉴 목록
     */
    public List<Menu> getPopularMenus() {
        return menus.stream()
                .filter(menu -> menu.getIsPopular() && menu.isOrderable())
                .collect(Collectors.toList());
    }

    /**
     * 신메뉴 목록
     */
    public List<Menu> getNewMenus() {
        return menus.stream()
                .filter(menu -> menu.getIsNew() && menu.isOrderable())
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 메뉴 목록
     */
    public List<Menu> getMenusByCategory(String categoryId) {
        return menus.stream()
                .filter(menu -> menu.belongsToCategory(categoryId) && menu.isOrderable())
                .collect(Collectors.toList());
    }

    // ==================== 메뉴 옵션 관리 ====================

    /**
     * 메뉴에 옵션 그룹 추가
     */
    public MenuOptionGroup addOptionGroupToMenu(String menuId, String groupName,
                                                String description, Boolean isRequired,
                                                Integer minSelection, Integer maxSelection,
                                                String createdBy) {
        Menu menu = findMenuById(menuId);
        return menu.addOptionGroup(groupName, description, isRequired,
                minSelection, maxSelection, createdBy);
    }

    /**
     * 옵션 그룹에 옵션 추가
     */
    public MenuOption addOptionToGroup(String menuId, String optionGroupId,
                                       String optionName, Integer additionalPrice,
                                       Integer displayOrder, String createdBy) {
        Menu menu = findMenuById(menuId);
        MenuOptionGroup group = menu.getOptionGroups().stream()
                .filter(g -> g.getId() != null && g.getId().equals(optionGroupId))
                .findFirst()
                .orElseThrow(() -> new RestaurantException(MenuErrorCode.OPTION_GROUP_NOT_FOUND));

        return group.addOption(optionName, additionalPrice, displayOrder, createdBy);
    }

    // ==================== 운영 시간 관리 ====================

    /**
     * 운영 시간 설정
     */
    public OperatingDay setOperatingDay(DayType dayType, OperatingTimeType timeType,
                                        LocalTime startTime, LocalTime endTime,
                                        Boolean isHoliday, String note) {
        // 기존 운영시간 제거
        operatingDays.removeIf(day ->
                day.getDayType() == dayType && day.getTimeType() == timeType);

        OperatingDay operatingDay = OperatingDay.builder()
                .restaurantId(this.id)
                .dayType(dayType)
                .timeType(timeType)
                .startTime(startTime)
                .endTime(endTime)
                .isHoliday(isHoliday)
                .note(note)
                .build();

        operatingDays.add(operatingDay);
        return operatingDay;
    }

    /**
     * 브레이크 타임 설정
     */
    public void setBreakTime(DayType dayType, LocalTime breakStartTime,
                             LocalTime breakEndTime) {
        OperatingDay operatingDay = operatingDays.stream()
                .filter(day -> day.getDayType() == dayType
                        && day.getTimeType() == OperatingTimeType.REGULAR)
                .findFirst()
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.OPERATING_DAY_NOT_FOUND));

        OperatingDay updated = OperatingDay.builder()
                .restaurantId(operatingDay.getRestaurantId())
                .dayType(operatingDay.getDayType())
                .timeType(operatingDay.getTimeType())
                .startTime(operatingDay.getStartTime())
                .endTime(operatingDay.getEndTime())
                .isHoliday(operatingDay.getIsHoliday())
                .breakStartTime(breakStartTime)
                .breakEndTime(breakEndTime)
                .note(operatingDay.getNote())
                .build();

        operatingDays.remove(operatingDay);
        operatingDays.add(updated);
    }

    /**
     * 현재 영업 중인지 확인
     */
    public boolean isOpenNow() {
        if (status != RestaurantStatus.OPEN) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return operatingDays.stream()
                .anyMatch(day -> day.isOpenAt(now));
    }

    /**
     * 특정 요일 운영시간 조회
     */
    public OperatingDay getOperatingDay(DayType dayType, OperatingTimeType timeType) {
        return operatingDays.stream()
                .filter(day -> day.getDayType() == dayType && day.getTimeType() == timeType)
                .findFirst()
                .orElse(null);
    }

    // ==================== 레스토랑 카테고리 관리 ====================

    /**
     * 레스토랑 카테고리 추가
     */
    public RestaurantCategoryRelation addRestaurantCategory(String categoryId,
                                                            boolean isPrimary,
                                                            String createdBy) {
        RestaurantCategoryRelation relation = RestaurantCategoryRelation.create(
                this.id, categoryId, isPrimary, createdBy);

        this.categoryRelations.add(relation);
        return relation;
    }

    /**
     * 레스토랑 카테고리 제거
     */
    public void removeRestaurantCategory(String categoryId, String deletedBy) {
        categoryRelations.stream()
                .filter(rel -> rel.getCategoryId().equals(categoryId))
                .forEach(rel -> rel.delete(deletedBy));
    }

    // ==================== 통계 관리 ====================

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 찜 추가/제거
     */
    public void incrementWishlistCount() {
        this.wishlistCount++;
    }

    public void decrementWishlistCount() {
        if (this.wishlistCount > 0) {
            this.wishlistCount--;
        }
    }

    /**
     * 리뷰 통계 업데이트
     */
    public void updateReviewStats(int reviewCount, BigDecimal rating) {
        this.reviewCount = reviewCount;
        this.reviewRating = rating;
    }

    /**
     * 구매수 증가
     */
    public void incrementPurchaseCount() {
        this.purchaseCount++;
    }

    // ==================== 상태 관리 ====================

    /**
     * 활성화/비활성화
     */
    public void setActive(boolean active, String updatedBy) {
        this.isActive = active;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * Soft Delete
     */
    public void delete(String deletedBy) {
        if (this.isDeleted) {
            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_ALREADY_DELETED);
        }

        this.isDeleted = true;
        this.isActive = false;
        this.status = RestaurantStatus.CLOSED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;

        // 하위 엔티티들도 삭제
        this.menus.forEach(menu -> menu.delete(deletedBy));
        this.menuCategories.forEach(cat -> cat.delete(deletedBy));
        this.categoryRelations.forEach(rel -> rel.delete(deletedBy));
    }

    /**
     * 복구
     */
    public void restore(String updatedBy) {
        this.isDeleted = false;
        this.isActive = true;
        this.deletedAt = null;
        this.deletedBy = null;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 주문 가능한지 확인
     */
    public boolean canAcceptOrder() {
        return isActive && !isDeleted && status.canAcceptOrder() && isOpenNow();
    }

    // ==================== 유틸리티 메서드 ====================

    /**
     * 메뉴 ID 생성
     */
    private String generateMenuId() {
        return "MENU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 카테고리 ID 생성
     */
    private String generateCategoryId() {
        return "CAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 검증
     */
    public void validate() {
        if (restaurantName == null || restaurantName.trim().isEmpty()) {
            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NAME_REQUIRED);
        }
        if (ownerId == null) {
            throw new RestaurantException(RestaurantErrorCode.OWNER_REQUIRED);
        }
        if (address != null && !address.isValid()) {
            throw new RestaurantException(RestaurantErrorCode.INVALID_ADDRESS);
        }
    }
}
