package xyz.sparta_project.manjok.domain.restaurant.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminMenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.request.AdminMenuUpdateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.MenuCreateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.MenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.MenuUpdateRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MenuCommandService 통합 테스트
 * - Restaurant Aggregate를 통한 Menu CUD 작업 검증
 * - 트랜잭션 범위 내에서 도메인 모델과 서비스 레이어 통합 테스트
 * - 실제 DB를 사용하여 영속성 전이와 변경 감지 확인
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("MenuCommandService 통합 테스트")
class MenuCommandServiceTest {

    @Autowired
    private MenuCommandService menuCommandService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    // 테스트 데이터
    private Restaurant testRestaurant;
    private MenuCategory testCategory;
    private final Long OWNER_ID = 1L;
    private final String OWNER_NAME = "테스트 사장님";
    private final String CREATED_BY = "test-owner";
    private final String UPDATED_BY = "test-updater";

    @BeforeEach
    void setUp() {
        // Restaurant 생성
        Address address = Address.builder()
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("123-45")
                .build();

        Coordinate coordinate = Coordinate.builder()
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .build();

        testRestaurant = Restaurant.builder()
                .id("REST" + UUID.randomUUID().toString().substring(0, 8))
                .createdAt(LocalDateTime.now())
                .ownerId(OWNER_ID)
                .ownerName(OWNER_NAME)
                .restaurantName("테스트 레스토랑")
                .status(RestaurantStatus.PREPARING) // 메뉴 수정 가능한 상태
                .address(address)
                .coordinate(coordinate)
                .contactNumber("02-1234-5678")
                .tags(new ArrayList<>(Arrays.asList("맛집", "깨끗함")))
                .isActive(true)
                .createdBy(CREATED_BY)
                .menus(new ArrayList<>())
                .menuCategories(new ArrayList<>())
                .operatingDays(new HashSet<>())
                .categoryRelations(new HashSet<>())
                .build();

        // MenuCategory 추가
        testCategory = testRestaurant.addMenuCategory(
                "메인메뉴",
                "대표 메인 메뉴",
                null,
                1,
                CREATED_BY
        );

        // 운영시간 설정
        testRestaurant.setOperatingDay(
                DayType.MON,
                OperatingTimeType.REGULAR,
                LocalTime.of(10, 0),
                LocalTime.of(22, 0),
                false,
                "월요일 정규 운영"
        );

        // Restaurant 저장
        testRestaurant = restaurantRepository.save(testRestaurant);
    }

    @Nested
    @DisplayName("메뉴 생성 테스트")
    class CreateMenuTest {

        @Test
        @DisplayName("성공: 기본 정보만으로 메뉴 생성")
        void createMenu_withBasicInfo_success() {
            // given
            MenuCreateRequest request = MenuCreateRequest.builder()
                    .menuName("김치찌개")
                    .description("얼큰한 김치찌개")
                    .price(new BigDecimal("8000"))
                    .build();

            // when
            MenuResponse response = menuCommandService.createMenu(
                    testRestaurant.getId(),
                    request,
                    CREATED_BY
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.getMenuId()).isNotNull();
            assertThat(response.getMenuName()).isEqualTo("김치찌개");
            assertThat(response.getDescription()).isEqualTo("얼큰한 김치찌개");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("8000"));
            assertThat(response.getIsAvailable()).isTrue();
            assertThat(response.getIsDeleted()).isFalse();

            // 영속성 확인
            Restaurant savedRestaurant = restaurantRepository.findById(testRestaurant.getId()).orElseThrow();
            assertThat(savedRestaurant.getMenus()).hasSize(1);
            assertThat(savedRestaurant.getMenus().get(0).getMenuName()).isEqualTo("김치찌개");
        }

        @Test
        @DisplayName("성공: 카테고리와 함께 메뉴 생성")
        void createMenu_withCategory_success() {
            // given
            MenuCreateRequest request = MenuCreateRequest.builder()
                    .menuName("된장찌개")
                    .description("구수한 된장찌개")
                    .price(new BigDecimal("7000"))
                    .categoryIds(Set.of(testCategory.getId()))
                    .primaryCategoryId(testCategory.getId())
                    .build();

            // when
            MenuResponse response = menuCommandService.createMenu(
                    testRestaurant.getId(),
                    request,
                    CREATED_BY
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCategoryNames()).contains("메인메뉴");
            assertThat(response.getPrimaryCategoryName()).isEqualTo("메인메뉴");

            // 카테고리 연결 확인
            Restaurant savedRestaurant = restaurantRepository.findById(testRestaurant.getId()).orElseThrow();
            Menu savedMenu = savedRestaurant.findMenuById(response.getMenuId());
            assertThat(savedMenu.belongsToCategory(testCategory.getId())).isTrue();
            assertThat(savedMenu.getPrimaryCategoryId()).isEqualTo(testCategory.getId());
        }

        @Test
        @DisplayName("성공: 대표/인기 메뉴로 생성")
        void createMenu_asMainAndPopular_success() {
            // given
            MenuCreateRequest request = MenuCreateRequest.builder()
                    .menuName("특제 불고기")
                    .description("사장님 추천 메뉴")
                    .price(new BigDecimal("12000"))
                    .ingredients("소고기, 양파, 대파")
                    .calorie(450)
                    .isMain(true)
                    .isPopular(true)
                    .build();

            // when
            MenuResponse response = menuCommandService.createMenu(
                    testRestaurant.getId(),
                    request,
                    CREATED_BY
            );

            // then
            assertThat(response.getIsMain()).isTrue();
            assertThat(response.getIsPopular()).isTrue();
            assertThat(response.getIngredients()).isEqualTo("소고기, 양파, 대파");
            assertThat(response.getCalorie()).isEqualTo(450);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 레스토랑")
        void createMenu_restaurantNotFound_throwsException() {
            // given
            MenuCreateRequest request = MenuCreateRequest.builder()
                    .menuName("테스트 메뉴")
                    .price(new BigDecimal("5000"))
                    .build();

            // when & then
            assertThatThrownBy(() ->
                    menuCommandService.createMenu("INVALID_ID", request, CREATED_BY)
            )
                    .isInstanceOf(RestaurantException.class)
                    .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.RESTAURANT_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 영업 중인 레스토랑에서 메뉴 생성 시도")
        void createMenu_whileOpen_throwsException() {
            // given
            testRestaurant.changeStatus(RestaurantStatus.OPEN, CREATED_BY);
            restaurantRepository.save(testRestaurant);

            MenuCreateRequest request = MenuCreateRequest.builder()
                    .menuName("테스트 메뉴")
                    .price(new BigDecimal("5000"))
                    .build();

            // when & then
            assertThatThrownBy(() ->
                    menuCommandService.createMenu(testRestaurant.getId(), request, CREATED_BY)
            )
                    .isInstanceOf(RestaurantException.class)
                    .hasFieldOrPropertyWithValue("errorCode",
                            RestaurantErrorCode.CANNOT_MODIFY_MENU_WHILE_OPEN);
        }
    }

    @Nested
    @DisplayName("메뉴 수정 테스트")
    class UpdateMenuTest {

        private Menu existingMenu;

        @BeforeEach
        void setUpMenu() {
            // 테스트용 메뉴 생성
            existingMenu = testRestaurant.addMenu(
                    "김치찌개",
                    "얼큰한 김치찌개",
                    new BigDecimal("8000"),
                    CREATED_BY
            );
            testRestaurant.addMenuToCategory(
                    existingMenu.getId(),
                    testCategory.getId(),
                    true,
                    CREATED_BY
            );
            testRestaurant = restaurantRepository.save(testRestaurant);

            // 저장된 메뉴 다시 조회
            existingMenu = testRestaurant.findMenuById(existingMenu.getId());
        }

        @Test
        @DisplayName("성공: 메뉴 전체 정보 수정 (PUT)")
        void updateMenu_fullUpdate_success() {
            // given
            MenuUpdateRequest request = MenuUpdateRequest.builder()
                    .menuName("특제 김치찌개")
                    .description("더욱 맵고 얼큰한 김치찌개")
                    .ingredients("김치, 돼지고기, 두부, 대파")
                    .price(new BigDecimal("9000"))
                    .calorie(350)
                    .isAvailable(true)
                    .isMain(true)
                    .isPopular(true)
                    .build();

            // when
            MenuResponse response = menuCommandService.updateMenu(
                    testRestaurant.getId(),
                    existingMenu.getId(),
                    request,
                    UPDATED_BY
            );

            // then
            assertThat(response.getMenuName()).isEqualTo("특제 김치찌개");
            assertThat(response.getDescription()).isEqualTo("더욱 맵고 얼큰한 김치찌개");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("9000"));
            assertThat(response.getIngredients()).isEqualTo("김치, 돼지고기, 두부, 대파");
            assertThat(response.getCalorie()).isEqualTo(350);
            assertThat(response.getIsMain()).isTrue();
            assertThat(response.getIsPopular()).isTrue();
            assertThat(response.getUpdatedBy()).isEqualTo(UPDATED_BY);
            assertThat(response.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공: 메뉴 부분 수정 (PATCH) - 가격만 변경")
        void patchMenu_priceOnly_success() {
            // given
            String originalName = existingMenu.getMenuName();
            String originalDescription = existingMenu.getDescription();

            MenuUpdateRequest request = MenuUpdateRequest.builder()
                    .price(new BigDecimal("7500"))
                    .build();

            // when
            MenuResponse response = menuCommandService.patchMenu(
                    testRestaurant.getId(),
                    existingMenu.getId(),
                    request,
                    UPDATED_BY
            );

            // then
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("7500"));
            assertThat(response.getMenuName()).isEqualTo(originalName); // 변경되지 않음
            assertThat(response.getDescription()).isEqualTo(originalDescription); // 변경되지 않음
        }

        @Test
        @DisplayName("성공: 판매 가능 상태만 변경")
        void patchMenu_availabilityOnly_success() {
            // given
            MenuUpdateRequest request = MenuUpdateRequest.builder()
                    .isAvailable(false)
                    .build();

            // when
            MenuResponse response = menuCommandService.patchMenu(
                    testRestaurant.getId(),
                    existingMenu.getId(),
                    request,
                    UPDATED_BY
            );

            // then
            assertThat(response.getIsAvailable()).isFalse();
        }

        @Test
        @DisplayName("성공: 카테고리 변경")
        void updateMenu_changeCategory_success() {
            // given
            MenuCategory newCategory = testRestaurant.addMenuCategory(
                    "사이드메뉴",
                    "반찬 및 사이드",
                    null,
                    2,
                    CREATED_BY
            );
            testRestaurant = restaurantRepository.save(testRestaurant);

            MenuUpdateRequest request = MenuUpdateRequest.builder()
                    .menuName(existingMenu.getMenuName())
                    .description(existingMenu.getDescription())
                    .price(existingMenu.getPrice())
                    .categoryIds(Set.of(newCategory.getId()))
                    .primaryCategoryId(newCategory.getId())
                    .build();

            // when
            MenuResponse response = menuCommandService.updateMenu(
                    testRestaurant.getId(),
                    existingMenu.getId(),
                    request,
                    UPDATED_BY
            );

            // then
            assertThat(response.getPrimaryCategoryName()).isEqualTo("사이드메뉴");

            // 기존 카테고리 연결이 제거되었는지 확인
            Restaurant updatedRestaurant = restaurantRepository.findById(testRestaurant.getId()).orElseThrow();
            Menu updatedMenu = updatedRestaurant.findMenuById(existingMenu.getId());
            assertThat(updatedMenu.belongsToCategory(testCategory.getId())).isFalse();
            assertThat(updatedMenu.belongsToCategory(newCategory.getId())).isTrue();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 메뉴 수정")
        void updateMenu_menuNotFound_throwsException() {
            // given
            MenuUpdateRequest request = MenuUpdateRequest.builder()
                    .menuName("수정된 메뉴")
                    .price(new BigDecimal("10000"))
                    .build();

            // when & then
            assertThatThrownBy(() ->
                    menuCommandService.updateMenu(
                            testRestaurant.getId(),
                            "INVALID_MENU_ID",
                            request,
                            UPDATED_BY
                    )
            )
                    .isInstanceOf(RestaurantException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.MENU_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("메뉴 가시성 제어 테스트")
    class MenuVisibilityTest {

        private Menu existingMenu;

        @BeforeEach
        void setUpMenu() {
            existingMenu = testRestaurant.addMenu(
                    "테스트 메뉴",
                    "테스트 설명",
                    new BigDecimal("5000"),
                    CREATED_BY
            );
            testRestaurant = restaurantRepository.save(testRestaurant);
            existingMenu = testRestaurant.findMenuById(existingMenu.getId());
        }

        @Test
        @DisplayName("성공: 메뉴 숨김 처리")
        void toggleMenuVisibility_hide_success() {
            // given
            assertThat(existingMenu.getIsAvailable()).isTrue();

            // when
            MenuResponse response = menuCommandService.toggleMenuVisibility(
                    testRestaurant.getId(),
                    existingMenu.getId(),
                    true, // hidden = true
                    UPDATED_BY
            );

            // then
            assertThat(response.getIsAvailable()).isFalse();

            // 영속성 확인
            Restaurant updatedRestaurant = restaurantRepository.findById(testRestaurant.getId()).orElseThrow();
            Menu updatedMenu = updatedRestaurant.findMenuById(existingMenu.getId());
            assertThat(updatedMenu.getIsAvailable()).isFalse();
        }

        @Test
        @DisplayName("성공: 메뉴 노출 처리")
        void toggleMenuVisibility_show_success() {
            // given
            existingMenu.setAvailable(false, CREATED_BY);
            restaurantRepository.save(testRestaurant);

            // when
            MenuResponse response = menuCommandService.toggleMenuVisibility(
                    testRestaurant.getId(),
                    existingMenu.getId(),
                    false, // hidden = false
                    UPDATED_BY
            );

            // then
            assertThat(response.getIsAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("메뉴 삭제 테스트")
    class DeleteMenuTest {

        private Menu existingMenu;
        private MenuOptionGroup optionGroup;

        @BeforeEach
        void setUpMenu() {
            // 메뉴 생성
            existingMenu = testRestaurant.addMenu(
                    "삭제할 메뉴",
                    "삭제 테스트용 메뉴",
                    new BigDecimal("6000"),
                    CREATED_BY
            );

            // 옵션 그룹 추가
            optionGroup = existingMenu.addOptionGroup(
                    "사이즈",
                    "사이즈를 선택하세요",
                    true,
                    1,
                    1,
                    CREATED_BY
            );

            // 옵션 추가
            optionGroup.addOption("보통", 0, 1, CREATED_BY);
            optionGroup.addOption("곱배기", 1000, 2, CREATED_BY);

            testRestaurant = restaurantRepository.save(testRestaurant);
            existingMenu = testRestaurant.findMenuById(existingMenu.getId());
        }

        @Test
        @DisplayName("성공: 메뉴 soft delete")
        void deleteMenu_success() {
            // given
            String menuId = existingMenu.getId();
            assertThat(existingMenu.getIsDeleted()).isFalse();
            assertThat(existingMenu.getOptionGroups()).isNotEmpty();

            // when
            menuCommandService.deleteMenu(
                    testRestaurant.getId(),
                    menuId,
                    "admin"
            );

            // then
            Restaurant updatedRestaurant = restaurantRepository.findById(testRestaurant.getId()).orElseThrow();
            Menu deletedMenu = updatedRestaurant.getMenus().stream()
                    .filter(m -> m.getId().equals(menuId))
                    .findFirst()
                    .orElseThrow();

            assertThat(deletedMenu.getIsDeleted()).isTrue();
            assertThat(deletedMenu.getDeletedAt()).isNotNull();
            assertThat(deletedMenu.getDeletedBy()).isEqualTo("admin");
            assertThat(deletedMenu.getIsAvailable()).isFalse();

            // 하위 옵션 그룹도 삭제되었는지 확인
            assertThat(deletedMenu.getOptionGroups()).allMatch(MenuOptionGroup::getIsDeleted);
            assertThat(deletedMenu.getOptionGroups()).allMatch(group ->
                    group.getOptions().stream().allMatch(MenuOption::getIsDeleted)
            );

            // 카테고리 관계도 삭제되었는지 확인
            assertThat(deletedMenu.getCategoryRelations()).allMatch(rel -> rel.isDeleted());
        }

        @Test
        @DisplayName("실패: 이미 삭제된 메뉴 재삭제 시도")
        void deleteMenu_alreadyDeleted_throwsException() {
            // given
            existingMenu.delete(CREATED_BY);
            restaurantRepository.save(testRestaurant);

            // when & then
            assertThatThrownBy(() ->
                    menuCommandService.deleteMenu(
                            testRestaurant.getId(),
                            existingMenu.getId(),
                            "admin"
                    )
            )
                    .isInstanceOf(RestaurantException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.MENU_ALREADY_DELETED);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 메뉴 삭제")
        void deleteMenu_notFound_throwsException() {
            // when & then
            assertThatThrownBy(() ->
                    menuCommandService.deleteMenu(
                            testRestaurant.getId(),
                            "INVALID_MENU_ID",
                            "admin"
                    )
            )
                    .isInstanceOf(RestaurantException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.MENU_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("메뉴 복구 테스트 (Admin)")
    class RestoreMenuTest {

        private Menu deletedMenu;

        @BeforeEach
        void setUpDeletedMenu() {
            // 메뉴 생성 후 삭제
            deletedMenu = testRestaurant.addMenu(
                    "복구할 메뉴",
                    "복구 테스트용 메뉴",
                    new BigDecimal("7000"),
                    CREATED_BY
            );
            deletedMenu.delete(CREATED_BY);
            testRestaurant = restaurantRepository.save(testRestaurant);
        }

        @Test
        @DisplayName("성공: 삭제된 메뉴 복구")
        void restoreMenu_success() {
            // given
            String menuId = deletedMenu.getId();
            assertThat(deletedMenu.getIsDeleted()).isTrue();

            // when
            AdminMenuResponse response = menuCommandService.restoreMenu(
                    testRestaurant.getId(),
                    menuId,
                    "admin"
            );

            // then
            assertThat(response.getIsDeleted()).isFalse();
            assertThat(response.getDeletedAt()).isNull();
            assertThat(response.getDeletedBy()).isNull();
            assertThat(response.getUpdatedBy()).isEqualTo("admin");

            // 영속성 확인
            Restaurant updatedRestaurant = restaurantRepository
                    .findByIdIncludingDeleted(testRestaurant.getId())
                    .orElseThrow();
            Menu restoredMenu = updatedRestaurant.getMenus().stream()
                    .filter(m -> m.getId().equals(menuId))
                    .findFirst()
                    .orElseThrow();

            assertThat(restoredMenu.getIsDeleted()).isFalse();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 메뉴 복구")
        void restoreMenu_notFound_throwsException() {
            // when & then
            assertThatThrownBy(() ->
                    menuCommandService.restoreMenu(
                            testRestaurant.getId(),
                            "INVALID_MENU_ID",
                            "admin"
                    )
            )
                    .isInstanceOf(RestaurantException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.MENU_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Admin 메뉴 수정 테스트")
    class AdminUpdateMenuTest {

        private Menu existingMenu;

        @BeforeEach
        void setUpMenu() {
            existingMenu = testRestaurant.addMenu(
                    "관리자 수정 대상 메뉴",
                    "테스트 설명",
                    new BigDecimal("5000"),
                    CREATED_BY
            );
            testRestaurant = restaurantRepository.save(testRestaurant);
            existingMenu = testRestaurant.findMenuById(existingMenu.getId());
        }

        @Test
        @DisplayName("성공: Admin이 메뉴 정보 수정")
        void updateMenuByAdmin_success() {
            // given
            AdminMenuUpdateRequest request = AdminMenuUpdateRequest.builder()
                    .menuName("관리자가 수정한 메뉴")
                    .description("관리자가 수정한 설명")
                    .price(new BigDecimal("6000"))
                    .calorie(300)
                    .isAvailable(true)
                    .isMain(true)
                    .build();

            // when
            AdminMenuResponse response = menuCommandService.updateMenuByAdmin(
                    testRestaurant.getId(),
                    existingMenu.getId(),
                    request,
                    "admin"
            );

            // then
            assertThat(response.getMenuName()).isEqualTo("관리자가 수정한 메뉴");
            assertThat(response.getDescription()).isEqualTo("관리자가 수정한 설명");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("6000"));
            assertThat(response.getCalorie()).isEqualTo(300);
            assertThat(response.getIsMain()).isTrue();
            assertThat(response.getUpdatedBy()).isEqualTo("admin");
        }

        @Test
        @DisplayName("성공: Admin이 삭제된 레스토랑의 메뉴 수정")
        void updateMenuByAdmin_deletedRestaurant_success() {
            // given
            testRestaurant.delete(CREATED_BY);
            restaurantRepository.save(testRestaurant);

            AdminMenuUpdateRequest request = AdminMenuUpdateRequest.builder()
                    .menuName("수정된 메뉴")
                    .price(new BigDecimal("7000"))
                    .build();

            // when
            AdminMenuResponse response = menuCommandService.updateMenuByAdmin(
                    testRestaurant.getId(),
                    existingMenu.getId(),
                    request,
                    "admin"
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.getMenuName()).isEqualTo("수정된 메뉴");
        }
    }

    @Nested
    @DisplayName("트랜잭션 및 영속성 전이 테스트")
    class TransactionAndCascadeTest {

        @Test
        @DisplayName("성공: 메뉴 생성 시 옵션 그룹과 옵션이 함께 저장됨")
        void createMenu_withOptions_cascadePersist() {
            // given
            MenuCreateRequest request = MenuCreateRequest.builder()
                    .menuName("옵션 테스트 메뉴")
                    .price(new BigDecimal("10000"))
                    .build();

            // when
            MenuResponse menuResponse = menuCommandService.createMenu(
                    testRestaurant.getId(),
                    request,
                    CREATED_BY
            );

            // 옵션 그룹 추가
            Restaurant restaurant = restaurantRepository.findById(testRestaurant.getId()).orElseThrow();
            Menu menu = restaurant.findMenuById(menuResponse.getMenuId());
            MenuOptionGroup optionGroup = menu.addOptionGroup(
                    "맵기 선택",
                    "맵기를 선택하세요",
                    true,
                    1,
                    1,
                    CREATED_BY
            );
            optionGroup.addOption("보통", 0, 1, CREATED_BY);
            optionGroup.addOption("매운맛", 500, 2, CREATED_BY);

            restaurantRepository.save(restaurant);

            // then
            Restaurant savedRestaurant = restaurantRepository.findById(testRestaurant.getId()).orElseThrow();
            Menu savedMenu = savedRestaurant.findMenuById(menuResponse.getMenuId());

            assertThat(savedMenu.getOptionGroups()).hasSize(1);
            assertThat(savedMenu.getOptionGroups().get(0).getOptions()).hasSize(2);
            assertThat(savedMenu.getOptionGroups().get(0).getGroupName()).isEqualTo("맵기 선택");
        }

        @Test
        @DisplayName("성공: 메뉴 삭제 시 하위 엔티티들도 soft delete됨")
        void deleteMenu_cascadeSoftDelete() {
            // given
            Menu menu = testRestaurant.addMenu("삭제 테스트", "설명", new BigDecimal("5000"), CREATED_BY);
            MenuOptionGroup group = menu.addOptionGroup("그룹", "설명", false, 0, 1, CREATED_BY);
            group.addOption("옵션1", 0, 1, CREATED_BY);
            testRestaurant.addMenuToCategory(menu.getId(), testCategory.getId(), true, CREATED_BY);
            testRestaurant = restaurantRepository.save(testRestaurant);

            String menuId = menu.getId();

            // when
            menuCommandService.deleteMenu(testRestaurant.getId(), menuId, "admin");

            // then
            Restaurant updatedRestaurant = restaurantRepository.findById(testRestaurant.getId()).orElseThrow();
            Menu deletedMenu = updatedRestaurant.getMenus().stream()
                    .filter(m -> m.getId().equals(menuId))
                    .findFirst()
                    .orElseThrow();

            // 메뉴, 옵션 그룹, 옵션, 카테고리 관계 모두 삭제됨
            assertThat(deletedMenu.getIsDeleted()).isTrue();
            assertThat(deletedMenu.getOptionGroups()).allMatch(MenuOptionGroup::getIsDeleted);
            assertThat(deletedMenu.getCategoryRelations()).allMatch(rel -> rel.isDeleted());
        }
    }
}