package xyz.sparta_project.manjok.domain.restaurant.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantCategoryRepository categoryRepository;

    // 테스트 데이터
    private Restaurant testRestaurant;
    private Menu testMenu;
    private MenuCategory testMenuCategory;
    private MenuOptionGroup testOptionGroup;
    private MenuOption testOption;
    private OperatingDay testOperatingDay;
    private RestaurantCategoryRelation testCategoryRelation;
    private RestaurantCategory savedCategory;

    // 기본 정보
    private final String OWNER_ID = "1";
    private final String OWNER_NAME = "홍길동";
    private final String RESTAURANT_NAME = "테스트 레스토랑";
    private final String CREATED_BY = "test-user";

    @BeforeEach
    void setUp() {
        // 1. RestaurantCategory 먼저 생성 및 저장
        RestaurantCategory category = RestaurantCategory.builder()
                .categoryCode("KOREAN")
                .categoryName("한식")
                .description("한국 음식")
                .depth(1)
                .displayOrder(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy(CREATED_BY)
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();

        savedCategory = categoryRepository.save(category);

        // 2. Address 생성
        Address address = Address.builder()
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("123-45 테스트빌딩 1층")
                .build();

        // 3. Coordinate 생성
        Coordinate coordinate = Coordinate.builder()
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .build();

        // 4. OperatingDay 생성
        testOperatingDay = OperatingDay.builder()
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .isHoliday(false)
                .breakStartTime(LocalTime.of(15, 0))
                .breakEndTime(LocalTime.of(17, 0))
                .note("월요일 정규 운영")
                .build();

        // 5. RestaurantCategoryRelation 생성
        testCategoryRelation = RestaurantCategoryRelation.builder()
                .categoryId(savedCategory.getId())
                .isPrimary(true)
                .createdAt(LocalDateTime.now())
                .createdBy(CREATED_BY)
                .isDeleted(false)
                .build();

        // 6. Restaurant 먼저 생성 (하위 요소들 없이)
        testRestaurant = Restaurant.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .ownerId(OWNER_ID)
                .ownerName(OWNER_NAME)
                .restaurantName(RESTAURANT_NAME)
                .status(RestaurantStatus.OPEN)
                .address(address)
                .coordinate(coordinate)
                .contactNumber("02-1234-5678")
                .tags(List.of("한식", "찌개", "맛집"))
                .isActive(true)
                .viewCount(0)
                .wishlistCount(0)
                .reviewCount(0)
                .reviewRating(BigDecimal.ZERO)
                .purchaseCount(0)
                .menus(new ArrayList<>())
                .menuCategories(new ArrayList<>())
                .operatingDays(new HashSet<>(Set.of(testOperatingDay)))
                .categoryRelations(new HashSet<>(Set.of(testCategoryRelation)))
                .createdBy(CREATED_BY)
                .isDeleted(false)
                .build();

        // 7. 이제 Restaurant ID가 있으니 MenuOption 생성
        testOption = MenuOption.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .optionName("사이즈 업")
                .description("라지 사이즈로 변경")
                .additionalPrice(new BigDecimal("2000"))
                .isAvailable(true)
                .isDefault(false)
                .displayOrder(1)
                .purchaseCount(0)
                .createdBy(CREATED_BY)
                .isDeleted(false)
                .build();

        // 8. MenuOptionGroup 생성
        testOptionGroup = MenuOptionGroup.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .groupName("사이즈 선택")
                .description("메뉴 사이즈를 선택하세요")
                .minSelection(1)
                .maxSelection(1)
                .isRequired(true)
                .displayOrder(1)
                .isActive(true)
                .options(new ArrayList<>(List.of(testOption)))
                .createdBy(CREATED_BY)
                .isDeleted(false)
                .build();

        // 9. MenuCategory 생성
        testMenuCategory = MenuCategory.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .categoryName("메인 메뉴")
                .description("대표 메뉴")
                .parentCategoryId(null)
                .depth(1)
                .displayOrder(1)
                .isActive(true)
                .menuIds(new HashSet<>())
                .createdBy(CREATED_BY)
                .isDeleted(false)
                .build();

        // 10. MenuCategoryRelation 생성 (이제 restaurantId 사용 가능)
        MenuCategoryRelation menuCategoryRelation = MenuCategoryRelation.builder()
                .categoryId(testMenuCategory.getId())
                .restaurantId(testRestaurant.getId())  // Restaurant ID 사용
                .isPrimary(true)
                .createdAt(LocalDateTime.now())
                .createdBy(CREATED_BY)
                .isDeleted(false)
                .build();

        // 11. Menu 생성
        testMenu = Menu.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .menuName("김치찌개")
                .description("맛있는 김치찌개")
                .ingredients("김치, 돼지고기, 두부")
                .price(new BigDecimal("8000"))
                .isAvailable(true)
                .isMain(true)
                .isPopular(false)
                .isNew(false)
                .calorie(500)
                .purchaseCount(0)
                .wishlistCount(0)
                .reviewCount(0)
                .reviewRating(BigDecimal.ZERO)
                .optionGroups(new ArrayList<>(List.of(testOptionGroup)))
                .categoryRelations(new HashSet<>(Set.of(menuCategoryRelation)))
                .createdBy(CREATED_BY)
                .isDeleted(false)
                .build();

        // 12. Restaurant에 Menu와 MenuCategory 추가
        testRestaurant.getMenus().add(testMenu);
        testRestaurant.getMenuCategories().add(testMenuCategory);
    }

    // ==================== CREATE & UPDATE 테스트 ====================

    @Test
    @DisplayName("Restaurant 저장 - 성공")
    void save_Success() {
        // when
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRestaurantName()).isEqualTo(RESTAURANT_NAME);
        assertThat(saved.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(saved.getMenus()).hasSize(1);
        assertThat(saved.getMenuCategories()).hasSize(1);
        assertThat(saved.getOperatingDays()).hasSize(1);
    }

    @Test
    @DisplayName("Restaurant 저장 - 하위 엔티티 모두 저장 확인")
    void save_WithAllChildEntities() {
        // when
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // then
        assertThat(saved.getMenus()).hasSize(1);
        Menu savedMenu = saved.getMenus().get(0);
        assertThat(savedMenu.getMenuName()).isEqualTo("김치찌개");
        assertThat(savedMenu.getOptionGroups()).hasSize(1);

        MenuOptionGroup savedOptionGroup = savedMenu.getOptionGroups().get(0);
        assertThat(savedOptionGroup.getGroupName()).isEqualTo("사이즈 선택");
        assertThat(savedOptionGroup.getOptions()).hasSize(1);

        MenuOption savedOption = savedOptionGroup.getOptions().get(0);
        assertThat(savedOption.getOptionName()).isEqualTo("사이즈 업");
    }

    @Test
    @DisplayName("Restaurant 수정 - 성공")
    void update_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        Restaurant updated = Restaurant.builder()
                .id(saved.getId())
                .createdAt(saved.getCreatedAt())
                .ownerId(saved.getOwnerId())
                .ownerName(saved.getOwnerName())
                .restaurantName("수정된 레스토랑")
                .status(RestaurantStatus.OPEN)
                .address(saved.getAddress())
                .coordinate(saved.getCoordinate())
                .contactNumber("02-9999-9999")
                .tags(saved.getTags())
                .isActive(true)
                .viewCount(100)
                .wishlistCount(50)
                .reviewCount(30)
                .reviewRating(new BigDecimal("4.5"))
                .purchaseCount(200)
                .menus(saved.getMenus())
                .menuCategories(saved.getMenuCategories())
                .operatingDays(saved.getOperatingDays())
                .categoryRelations(saved.getCategoryRelations())
                .createdBy(saved.getCreatedBy())
                .updatedBy("updater")
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        Restaurant result = restaurantRepository.save(updated);

        // then
        assertThat(result.getRestaurantName()).isEqualTo("수정된 레스토랑");
        assertThat(result.getContactNumber()).isEqualTo("02-9999-9999");
        assertThat(result.getViewCount()).isEqualTo(100);
    }

    // ==================== DELETE 테스트 ====================

    @Test
    @DisplayName("Restaurant 삭제 - 성공")
    void delete_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        restaurantRepository.delete(saved.getId(), "deleter");

        // then
        Optional<Restaurant> found = restaurantRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Restaurant 삭제 - 존재하지 않는 ID")
    void delete_NotFound() {
        // when & then
        assertThatThrownBy(() -> restaurantRepository.delete("non-existent-id", "deleter"))
                .isInstanceOf(RestaurantException.class)
                .hasMessageContaining("레스토랑을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("Restaurant 삭제 - 이미 삭제된 레스토랑")
    void delete_AlreadyDeleted() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);
        restaurantRepository.delete(saved.getId(), "deleter");

        // when & then
        assertThatThrownBy(() -> restaurantRepository.delete(saved.getId(), "deleter"))
                .isInstanceOf(RestaurantException.class)
                .hasMessageContaining("이미 삭제된 레스토랑입니다");
    }

    // ==================== READ - Restaurant 단건 조회 테스트 ====================

    @Test
    @DisplayName("Restaurant 조회 by ID - 성공")
    void findById_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        Optional<Restaurant> found = restaurantRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getRestaurantName()).isEqualTo(RESTAURANT_NAME);
        assertThat(found.get().getMenus()).hasSize(1);
        assertThat(found.get().getMenuCategories()).hasSize(1);
    }

    @Test
    @DisplayName("Restaurant 조회 by ID - 존재하지 않음")
    void findById_NotFound() {
        // when
        Optional<Restaurant> found = restaurantRepository.findById("non-existent-id");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Restaurant 조회 by ID - 삭제된 레스토랑은 조회 안됨")
    void findById_DeletedNotFound() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);
        restaurantRepository.delete(saved.getId(), "deleter");

        // when
        Optional<Restaurant> found = restaurantRepository.findById(saved.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Restaurant 조회 by ID - 삭제된 것 포함")
    void findByIdIncludingDeleted_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);
        restaurantRepository.delete(saved.getId(), "deleter");

        // when
        Optional<Restaurant> found = restaurantRepository.findByIdIncludingDeleted(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().isDeleted()).isTrue();
    }

    // ==================== READ - Restaurant 목록 조회 테스트 ====================

    @Test
    @DisplayName("Restaurant 전체 조회 - 페이징")
    void findAll_WithPaging() {
        // given
        restaurantRepository.save(testRestaurant);

        Restaurant another = Restaurant.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .ownerId(OWNER_ID)
                .ownerName(OWNER_NAME)
                .restaurantName("두 번째 레스토랑")
                .status(RestaurantStatus.OPEN)
                .address(testRestaurant.getAddress())
                .coordinate(testRestaurant.getCoordinate())
                .contactNumber("02-1111-1111")
                .tags(List.of("중식"))
                .isActive(true)
                .viewCount(0)
                .wishlistCount(0)
                .reviewCount(0)
                .reviewRating(BigDecimal.ZERO)
                .purchaseCount(0)
                .menus(new ArrayList<>())
                .menuCategories(new ArrayList<>())
                .operatingDays(new HashSet<>())
                .categoryRelations(new HashSet<>())
                .createdBy(CREATED_BY)
                .isDeleted(false)
                .build();
        restaurantRepository.save(another);

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> page = restaurantRepository.findAll(pageable);

        // then
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Restaurant 조회 by OwnerId - 성공")
    void findByOwnerId_Success() {
        // given
        restaurantRepository.save(testRestaurant);

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> page = restaurantRepository.findByOwnerId(OWNER_ID, pageable);

        // then
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(page.getContent().get(0).getOwnerId()).isEqualTo(OWNER_ID);
    }

    @Test
    @DisplayName("Restaurant 조회 by OwnerId - null 입력 시 예외")
    void findByOwnerId_NullOwnerId() {
        // when & then
        Pageable pageable = PageRequest.of(0, 10);
        assertThatThrownBy(() -> restaurantRepository.findByOwnerId(null, pageable))
                .isInstanceOf(RestaurantException.class);
    }

    @Test
    @DisplayName("Restaurant 전체 조회 - 삭제된 것 포함")
    void findAllIncludingDeleted_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);
        restaurantRepository.delete(saved.getId(), "deleter");

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> page = restaurantRepository.findAllIncludingDeleted(pageable);

        // then
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(1);
    }

    // ==================== READ - Restaurant 검색 테스트 ====================

    @Test
    @DisplayName("Restaurant 검색 - 지역별")
    void searchRestaurants_ByLocation() {
        // given
        restaurantRepository.save(testRestaurant);

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> page = restaurantRepository.searchRestaurants(
                "서울특별시", "강남구", null, null, null, pageable
        );

        // then
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(page.getContent().get(0).getAddress().getProvince()).isEqualTo("서울특별시");
    }

    @Test
    @DisplayName("Restaurant 검색 - 키워드")
    void searchRestaurants_ByKeyword() {
        // given
        restaurantRepository.save(testRestaurant);

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> page = restaurantRepository.searchRestaurants(
                null, null, null, null, "테스트", pageable
        );

        // then
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(page.getContent().get(0).getRestaurantName()).contains("테스트");
    }

    @Test
    @DisplayName("Restaurant 검색 - 복합 조건")
    void searchRestaurants_MultipleConditions() {
        // given
        restaurantRepository.save(testRestaurant);

        // when
        Pageable pageable = PageRequest.of(0, 10, Sort.by("reviewRating").descending());
        Page<Restaurant> page = restaurantRepository.searchRestaurants(
                "서울특별시", "강남구", "역삼동", null, "레스토랑", pageable
        );

        // then
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(1);
    }

    // ==================== READ - Menu 조회 테스트 ====================

    @Test
    @DisplayName("Menu 단건 조회 - 성공")
    void findMenuByRestaurantIdAndMenuId_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);
        String menuId = saved.getMenus().get(0).getId();

        // when
        Optional<Menu> found = restaurantRepository.findMenuByRestaurantIdAndMenuId(
                saved.getId(), menuId
        );

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getMenuName()).isEqualTo("김치찌개");
        assertThat(found.get().getOptionGroups()).hasSize(1);
    }

    @Test
    @DisplayName("Menu 단건 조회 - 존재하지 않음")
    void findMenuByRestaurantIdAndMenuId_NotFound() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        Optional<Menu> found = restaurantRepository.findMenuByRestaurantIdAndMenuId(
                saved.getId(), "non-existent-menu-id"
        );

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Menu 목록 조회 by RestaurantId")
    void findMenusByRestaurantId_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Menu> page = restaurantRepository.findMenusByRestaurantId(
                saved.getId(), pageable
        );

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getMenuName()).isEqualTo("김치찌개");
    }

    @Test
    @DisplayName("Menu 검색 by 이름")
    void searchMenusByRestaurantIdAndName_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Menu> page = restaurantRepository.searchMenusByRestaurantIdAndName(
                saved.getId(), "김치", pageable
        );

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getMenuName()).contains("김치");
    }

    // ==================== READ - MenuCategory 조회 테스트 ====================

    @Test
    @DisplayName("MenuCategory 조회 by RestaurantId")
    void findMenuCategoriesByRestaurantId_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        List<MenuCategory> categories = restaurantRepository.findMenuCategoriesByRestaurantId(
                saved.getId()
        );

        // then
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getCategoryName()).isEqualTo("메인 메뉴");
    }

    @Test
    @DisplayName("MenuCategory 최상위 조회")
    void findRootMenuCategoriesByRestaurantId_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        List<MenuCategory> rootCategories = restaurantRepository.findRootMenuCategoriesByRestaurantId(
                saved.getId()
        );

        // then
        assertThat(rootCategories).hasSize(1);
        assertThat(rootCategories.get(0).getDepth()).isEqualTo(1);
    }

    // ==================== READ - OperatingDay 조회 테스트 ====================

    @Test
    @DisplayName("OperatingDay 전체 조회")
    void findOperatingDaysByRestaurantId_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        Set<OperatingDay> operatingDays = restaurantRepository.findOperatingDaysByRestaurantId(
                saved.getId()
        );

        // then
        assertThat(operatingDays).hasSize(1);
        assertThat(operatingDays.iterator().next().getDayType()).isEqualTo(DayType.MON);
    }

    @Test
    @DisplayName("OperatingDay 조회 by DayType")
    void findOperatingDayByRestaurantIdAndDayType_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        Optional<OperatingDay> found = restaurantRepository.findOperatingDayByRestaurantIdAndDayType(
                saved.getId(), DayType.MON
        );

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(found.get().getEndTime()).isEqualTo(LocalTime.of(22, 0));
    }

    // ==================== 존재 확인 테스트 ====================

    @Test
    @DisplayName("Restaurant 존재 확인 - 존재함")
    void existsById_True() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        boolean exists = restaurantRepository.existsById(saved.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Restaurant 존재 확인 - 존재하지 않음")
    void existsById_False() {
        // when
        boolean exists = restaurantRepository.existsById("non-existent-id");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Restaurant 중복 확인 - ownerId와 이름")
    void existsByOwnerIdAndName_True() {
        // given
        restaurantRepository.save(testRestaurant);

        // when
        boolean exists = restaurantRepository.existsByOwnerIdAndName(OWNER_ID, RESTAURANT_NAME);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Restaurant 중복 확인 - 존재하지 않음")
    void existsByOwnerIdAndName_False() {
        // when
        boolean exists = restaurantRepository.existsByOwnerIdAndName("999L", "존재하지 않는 레스토랑");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Menu 존재 확인 - 존재함")
    void existsMenuByRestaurantIdAndMenuId_True() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);
        String menuId = saved.getMenus().get(0).getId();

        // when
        boolean exists = restaurantRepository.existsMenuByRestaurantIdAndMenuId(
                saved.getId(), menuId
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Menu 존재 확인 - 존재하지 않음")
    void existsMenuByRestaurantIdAndMenuId_False() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        boolean exists = restaurantRepository.existsMenuByRestaurantIdAndMenuId(
                saved.getId(), "non-existent-menu-id"
        );

        // then
        assertThat(exists).isFalse();
    }

    // ==================== 이벤트 처리용 테스트 ====================

    @Test
    @DisplayName("Restaurant with Menus 조회 - 성공")
    void findByIdWithMenus_Success() {
        // given
        Restaurant saved = restaurantRepository.save(testRestaurant);

        // when
        Optional<Restaurant> found = restaurantRepository.findByIdWithMenus(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getMenus()).hasSize(1);
        assertThat(found.get().getMenus().get(0).getMenuName()).isEqualTo("김치찌개");
    }

    @Test
    @DisplayName("Restaurant with Menus 조회 - 존재하지 않음")
    void findByIdWithMenus_NotFound() {
        // when
        Optional<Restaurant> found = restaurantRepository.findByIdWithMenus("non-existent-id");

        // then
        assertThat(found).isEmpty();
    }
}