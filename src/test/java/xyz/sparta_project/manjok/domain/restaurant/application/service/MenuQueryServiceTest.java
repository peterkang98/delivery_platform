package xyz.sparta_project.manjok.domain.restaurant.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminMenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.MenuDetailResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.MenuSummaryResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.MenuResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MenuQueryService 통합 테스트
 * - Menu 조회 전담 서비스 검증
 * - 권한별 조회 메서드 테스트 (Customer, Owner, Admin)
 * - 페이징, 정렬, 필터링 기능 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("MenuQueryService 통합 테스트")
class MenuQueryServiceTest {

    @Autowired
    private MenuQueryService menuQueryService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    // 테스트 데이터
    private Restaurant testRestaurant;
    private MenuCategory mainCategory;
    private MenuCategory sideCategory;
    private Menu menu1;
    private Menu menu2;
    private Menu menu3;
    private Menu hiddenMenu;
    private Menu deletedMenu;

    private final String OWNER_ID = "1";
    private final String CREATED_BY = "test-owner";

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
                .ownerName("테스트 사장님")
                .restaurantName("테스트 레스토랑")
                .status(RestaurantStatus.PREPARING)
                .address(address)
                .coordinate(coordinate)
                .contactNumber("02-1234-5678")
                .isActive(true)
                .createdBy(CREATED_BY)
                .menus(new ArrayList<>())
                .menuCategories(new ArrayList<>())
                .operatingDays(new HashSet<>())
                .categoryRelations(new HashSet<>())
                .build();

        // MenuCategory 추가
        mainCategory = testRestaurant.addMenuCategory(
                "메인메뉴",
                "대표 메인 메뉴",
                null,
                1,
                CREATED_BY
        );

        sideCategory = testRestaurant.addMenuCategory(
                "사이드메뉴",
                "반찬 및 사이드",
                null,
                2,
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

        // Menu 추가
        menu1 = testRestaurant.addMenu(
                "김치찌개",
                "얼큰한 김치찌개",
                new BigDecimal("8000"),
                CREATED_BY
        );
        menu1.setMain(true, CREATED_BY);
        menu1.setPopular(true, CREATED_BY);
        testRestaurant.addMenuToCategory(menu1.getId(), mainCategory.getId(), true, CREATED_BY);

        menu2 = testRestaurant.addMenu(
                "된장찌개",
                "구수한 된장찌개",
                new BigDecimal("7000"),
                CREATED_BY
        );
        testRestaurant.addMenuToCategory(menu2.getId(), mainCategory.getId(), false, CREATED_BY);

        menu3 = testRestaurant.addMenu(
                "계란말이",
                "부드러운 계란말이",
                new BigDecimal("5000"),
                CREATED_BY
        );
        testRestaurant.addMenuToCategory(menu3.getId(), sideCategory.getId(), true, CREATED_BY);

        // 숨김 메뉴
        hiddenMenu = testRestaurant.addMenu(
                "품절 메뉴",
                "일시적으로 품절된 메뉴",
                new BigDecimal("9000"),
                CREATED_BY
        );
        hiddenMenu.setAvailable(false, CREATED_BY);

        // 삭제된 메뉴
        deletedMenu = testRestaurant.addMenu(
                "삭제된 메뉴",
                "삭제 테스트용",
                new BigDecimal("10000"),
                CREATED_BY
        );
        deletedMenu.delete(CREATED_BY);

        // 옵션 그룹 추가 (menu1에만)
        MenuOptionGroup optionGroup = menu1.addOptionGroup(
                "맵기 선택",
                "맵기를 선택하세요",
                true,
                1,
                1,
                CREATED_BY
        );
        optionGroup.addOption("보통", 0, 1, CREATED_BY);
        optionGroup.addOption("매운맛", 500, 2, CREATED_BY);

        // Restaurant 저장
        testRestaurant = restaurantRepository.save(testRestaurant);
    }

    @Nested
    @DisplayName("Customer 조회 테스트")
    class CustomerQueryTest {

        @Test
        @DisplayName("성공: 레스토랑의 메뉴 목록 조회 (고객용)")
        void getMenus_customer_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

            // when
            PageResponse<MenuSummaryResponse> response = menuQueryService.getMenus(
                    testRestaurant.getId(),
                    pageable
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(3); // 숨김/삭제 메뉴 제외
            assertThat(response.getPageInfo().getTotalElements()).isEqualTo(3);

            // 숨김 메뉴와 삭제된 메뉴는 조회되지 않음
            assertThat(response.getContent())
                    .extracting(MenuSummaryResponse::getMenuName)
                    .doesNotContain("품절 메뉴", "삭제된 메뉴");
        }

        @Test
        @DisplayName("성공: 메뉴 요약 정보 확인")
        void getMenus_summaryInfo_correct() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            PageResponse<MenuSummaryResponse> response = menuQueryService.getMenus(
                    testRestaurant.getId(),
                    pageable
            );

            // then
            MenuSummaryResponse menu1Summary = response.getContent().stream()
                    .filter(m -> m.getMenuName().equals("김치찌개"))
                    .findFirst()
                    .orElseThrow();

            assertThat(menu1Summary.getMenuId()).isEqualTo(menu1.getId());
            assertThat(menu1Summary.getMenuName()).isEqualTo("김치찌개");
            assertThat(menu1Summary.getDescription()).isEqualTo("얼큰한 김치찌개");
            assertThat(menu1Summary.getPrice()).isEqualByComparingTo(new BigDecimal("8000"));
            assertThat(menu1Summary.getPrimaryCategoryName()).isEqualTo("메인메뉴");
            assertThat(menu1Summary.getIsAvailable()).isTrue();
            assertThat(menu1Summary.getIsMain()).isTrue();
            assertThat(menu1Summary.getIsPopular()).isTrue();
            assertThat(menu1Summary.getHasOptions()).isTrue();
            assertThat(menu1Summary.getHasRequiredOptions()).isTrue();
        }

        @Test
        @DisplayName("성공: 카테고리별 메뉴 조회")
        void getMenusByCategory_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            PageResponse<MenuSummaryResponse> response = menuQueryService.getMenusByCategory(
                    testRestaurant.getId(),
                    mainCategory.getId(),
                    pageable
            );

            // then
            assertThat(response.getContent()).hasSize(2); // 김치찌개, 된장찌개
            assertThat(response.getContent())
                    .extracting(MenuSummaryResponse::getMenuName)
                    .containsExactlyInAnyOrder("김치찌개", "된장찌개");
        }

        @Test
        @DisplayName("성공: 메뉴명 검색")
        void searchMenus_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            PageResponse<MenuSummaryResponse> response = menuQueryService.searchMenus(
                    testRestaurant.getId(),
                    "찌개",
                    pageable
            );

            // then
            assertThat(response.getContent()).hasSize(2); // 김치찌개, 된장찌개
            assertThat(response.getContent())
                    .allMatch(menu -> menu.getMenuName().contains("찌개"));
        }

        @Test
        @DisplayName("성공: 메뉴 상세 조회")
        void getMenuDetail_success() {
            // when
            MenuDetailResponse response = menuQueryService.getMenuDetail(
                    testRestaurant.getId(),
                    menu1.getId()
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.getMenuId()).isEqualTo(menu1.getId());
            assertThat(response.getRestaurantId()).isEqualTo(testRestaurant.getId());
            assertThat(response.getMenuName()).isEqualTo("김치찌개");
            assertThat(response.getDescription()).isEqualTo("얼큰한 김치찌개");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("8000"));
            assertThat(response.getCategoryNames()).contains("메인메뉴");
            assertThat(response.getPrimaryCategoryName()).isEqualTo("메인메뉴");
            assertThat(response.getIsMain()).isTrue();
            assertThat(response.getIsPopular()).isTrue();

            // 옵션 그룹 확인
            assertThat(response.getOptionGroups()).hasSize(1);
            MenuDetailResponse.MenuOptionGroupDto optionGroup = response.getOptionGroups().get(0);
            assertThat(optionGroup.getGroupName()).isEqualTo("맵기 선택");
            assertThat(optionGroup.getIsRequired()).isTrue();
            assertThat(optionGroup.getOptions()).hasSize(2);

            // 옵션 확인
            assertThat(optionGroup.getOptions())
                    .extracting(MenuDetailResponse.MenuOptionDto::getOptionName)
                    .containsExactlyInAnyOrder("보통", "매운맛");
        }

        @Test
        @DisplayName("실패: 숨김 처리된 메뉴 상세 조회 시 예외")
        void getMenuDetail_hiddenMenu_throwsException() {
            // when & then
            assertThatThrownBy(() ->
                    menuQueryService.getMenuDetail(
                            testRestaurant.getId(),
                            hiddenMenu.getId()
                    )
            )
                    .isInstanceOf(RestaurantException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.MENU_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 삭제된 메뉴 상세 조회 시 예외")
        void getMenuDetail_deletedMenu_throwsException() {
            // when & then
            assertThatThrownBy(() ->
                    menuQueryService.getMenuDetail(
                            testRestaurant.getId(),
                            deletedMenu.getId()
                    )
            )
                    .isInstanceOf(RestaurantException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.MENU_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 레스토랑")
        void getMenus_restaurantNotFound_throwsException() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when & then
            assertThatThrownBy(() ->
                    menuQueryService.getMenus("INVALID_ID", pageable)
            )
                    .isInstanceOf(RestaurantException.class)
                    .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.RESTAURANT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Owner 조회 테스트")
    class OwnerQueryTest {

        @Test
        @DisplayName("성공: Owner 메뉴 목록 조회 (숨김 메뉴 포함)")
        void getMenusForOwner_includesHidden_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            PageResponse<MenuResponse> response = menuQueryService.getMenusForOwner(
                    testRestaurant.getId(),
                    pageable
            );

            // then
            assertThat(response.getContent()).hasSize(4); // 삭제된 메뉴 제외, 숨김 메뉴 포함
            assertThat(response.getContent())
                    .extracting(MenuResponse::getMenuName)
                    .contains("김치찌개", "된장찌개", "계란말이", "품절 메뉴");
        }

        @Test
        @DisplayName("성공: Owner 메뉴 상세 조회 (숨김 메뉴)")
        void getMenuForOwner_hiddenMenu_success() {
            // when
            MenuResponse response = menuQueryService.getMenuForOwner(
                    testRestaurant.getId(),
                    hiddenMenu.getId()
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.getMenuId()).isEqualTo(hiddenMenu.getId());
            assertThat(response.getMenuName()).isEqualTo("품절 메뉴");
            assertThat(response.getIsAvailable()).isFalse();

            // Owner는 감사 정보를 볼 수 있음
            assertThat(response.getCreatedBy()).isNotNull();
            assertThat(response.getUpdatedBy()).isNotNull();
            assertThat(response.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공: Owner 메뉴 응답에 옵션 구매 통계 포함")
        void getMenuForOwner_includesOptionStats_success() {
            // when
            MenuResponse response = menuQueryService.getMenuForOwner(
                    testRestaurant.getId(),
                    menu1.getId()
            );

            // then
            assertThat(response.getOptionGroups()).hasSize(1);
            MenuResponse.MenuOptionGroupDto optionGroup = response.getOptionGroups().get(0);

            // Owner용 옵션 정보는 구매 통계 포함
            assertThat(optionGroup.getOptions())
                    .allMatch(option -> option.getPurchaseCount() != null);
        }

        @Test
        @DisplayName("실패: Owner도 삭제된 메뉴는 조회 불가")
        void getMenuForOwner_deletedMenu_throwsException() {
            // when & then
            assertThatThrownBy(() ->
                    menuQueryService.getMenuForOwner(
                            testRestaurant.getId(),
                            deletedMenu.getId()
                    )
            )
                    .isInstanceOf(RestaurantException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.MENU_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Admin 조회 테스트")
    class AdminQueryTest {

        @Test
        @DisplayName("성공: Admin 메뉴 목록 조회 (삭제된 메뉴 포함)")
        void getMenusForAdmin_includesDeleted_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            PageResponse<AdminMenuResponse> response = menuQueryService.getMenusForAdmin(
                    testRestaurant.getId(),
                    pageable
            );

            // then
            assertThat(response.getContent()).hasSize(5); // 삭제된 메뉴 포함 5개
            assertThat(response.getContent())
                    .extracting(AdminMenuResponse::getMenuName)
                    .contains("김치찌개", "된장찌개", "계란말이", "품절 메뉴", "삭제된 메뉴");
        }

        @Test
        @DisplayName("성공: Admin 메뉴 상세 조회 (삭제된 메뉴)")
        void getMenuForAdmin_deletedMenu_success() {
            // when
            AdminMenuResponse response = menuQueryService.getMenuForAdmin(
                    testRestaurant.getId(),
                    deletedMenu.getId()
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.getMenuId()).isEqualTo(deletedMenu.getId());
            assertThat(response.getMenuName()).isEqualTo("삭제된 메뉴");
            assertThat(response.getIsDeleted()).isTrue();
            assertThat(response.getDeletedAt()).isNotNull();
            assertThat(response.getDeletedBy()).isEqualTo(CREATED_BY);
        }

        @Test
        @DisplayName("성공: Admin 응답에 레스토랑 이름 포함")
        void getMenuForAdmin_includesRestaurantName_success() {
            // when
            AdminMenuResponse response = menuQueryService.getMenuForAdmin(
                    testRestaurant.getId(),
                    menu1.getId()
            );

            // then
            assertThat(response.getRestaurantName()).isEqualTo("테스트 레스토랑");
        }

        @Test
        @DisplayName("성공: Admin은 삭제된 레스토랑의 메뉴도 조회 가능")
        void getMenusForAdmin_deletedRestaurant_success() {
            // given
            // Restaurant의 상태만 변경 (메뉴는 삭제하지 않음)
            try {
                java.lang.reflect.Field isDeletedField = Restaurant.class.getDeclaredField("isDeleted");
                isDeletedField.setAccessible(true);
                isDeletedField.set(testRestaurant, true);

                java.lang.reflect.Field deletedAtField = Restaurant.class.getDeclaredField("deletedAt");
                deletedAtField.setAccessible(true);
                deletedAtField.set(testRestaurant, LocalDateTime.now());

                java.lang.reflect.Field deletedByField = Restaurant.class.getDeclaredField("deletedBy");
                deletedByField.setAccessible(true);
                deletedByField.set(testRestaurant, CREATED_BY);
            } catch (Exception e) {
                throw new RuntimeException("Failed to mark restaurant as deleted", e);
            }

            restaurantRepository.save(testRestaurant);

            Pageable pageable = PageRequest.of(0, 10);

            // when
            PageResponse<AdminMenuResponse> response = menuQueryService.getMenusForAdmin(
                    testRestaurant.getId(),
                    pageable
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(5);
        }

        @Test
        @DisplayName("성공: Admin 옵션 그룹 정보에 감사 정보 포함")
        void getMenuForAdmin_optionGroupIncludesAuditInfo_success() {
            // when
            AdminMenuResponse response = menuQueryService.getMenuForAdmin(
                    testRestaurant.getId(),
                    menu1.getId()
            );

            // then
            assertThat(response.getOptionGroups()).hasSize(1);
            AdminMenuResponse.MenuOptionGroupDto optionGroup = response.getOptionGroups().get(0);

            assertThat(optionGroup.getCreatedAt()).isNotNull();
            assertThat(optionGroup.getCreatedBy()).isNotNull();
            assertThat(optionGroup.getIsDeleted()).isNotNull();

            // 옵션도 감사 정보 포함
            assertThat(optionGroup.getOptions())
                    .allMatch(option -> option.getCreatedAt() != null)
                    .allMatch(option -> option.getCreatedBy() != null)
                    .allMatch(option -> option.getIsDeleted() != null);
        }
    }

    @Nested
    @DisplayName("페이징 및 정렬 테스트")
    class PagingAndSortingTest {

        @Test
        @DisplayName("성공: 페이지 크기에 맞춰 조회")
        void getMenus_withPageSize_success() {
            // given
            Pageable pageable = PageRequest.of(0, 2); // 페이지당 2개

            // when
            PageResponse<MenuSummaryResponse> response = menuQueryService.getMenus(
                    testRestaurant.getId(),
                    pageable
            );

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getPageInfo().getTotalElements()).isEqualTo(3);
            assertThat(response.getPageInfo().getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공: 가격순 정렬")
        void getMenus_sortByPrice_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("price").ascending());

            // when
            PageResponse<MenuSummaryResponse> response = menuQueryService.getMenus(
                    testRestaurant.getId(),
                    pageable
            );

            // then
            assertThat(response.getContent())
                    .extracting(MenuSummaryResponse::getPrice)
                    .isSortedAccordingTo(BigDecimal::compareTo);
        }

    }

    @Nested
    @DisplayName("메뉴 존재 여부 확인 테스트")
    class ExistsMenuTest {

        @Test
        @DisplayName("성공: 메뉴 존재 확인 - true")
        void existsMenu_exists_returnsTrue() {
            // when
            boolean exists = menuQueryService.existsMenu(
                    testRestaurant.getId(),
                    menu1.getId()
            );

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("성공: 메뉴 존재 확인 - false")
        void existsMenu_notExists_returnsFalse() {
            // when
            boolean exists = menuQueryService.existsMenu(
                    testRestaurant.getId(),
                    "INVALID_MENU_ID"
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("성공: 삭제된 메뉴도 존재하는 것으로 카운트")
        void existsMenu_deletedMenu_returnsTrue() {
            // when
            boolean exists = menuQueryService.existsMenu(
                    testRestaurant.getId(),
                    deletedMenu.getId()
            );

            // then
            assertThat(exists).isTrue();
        }
    }

    @Nested
    @DisplayName("복잡한 시나리오 테스트")
    class ComplexScenarioTest {

        @Test
        @DisplayName("성공: 여러 카테고리에 속한 메뉴 조회")
        void getMenuDetail_multipleCategories_success() {
            // given - menu1에 추가 카테고리 연결
            testRestaurant.addMenuToCategory(menu1.getId(), sideCategory.getId(), false, CREATED_BY);
            restaurantRepository.save(testRestaurant);

            // when
            MenuDetailResponse response = menuQueryService.getMenuDetail(
                    testRestaurant.getId(),
                    menu1.getId()
            );

            // then
            assertThat(response.getCategoryNames()).hasSize(2);
            assertThat(response.getCategoryNames()).containsExactlyInAnyOrder("메인메뉴", "사이드메뉴");
            assertThat(response.getPrimaryCategoryName()).isEqualTo("메인메뉴"); // 주 카테고리는 메인메뉴
        }

        @Test
        @DisplayName("성공: 옵션이 없는 메뉴 조회")
        void getMenuDetail_noOptions_success() {
            // when
            MenuDetailResponse response = menuQueryService.getMenuDetail(
                    testRestaurant.getId(),
                    menu2.getId()
            );

            // then
            assertThat(response.getOptionGroups()).isEmpty();
        }

        @Test
        @DisplayName("성공: 빈 검색 결과")
        void searchMenus_noResults_returnsEmpty() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            PageResponse<MenuSummaryResponse> response = menuQueryService.searchMenus(
                    testRestaurant.getId(),
                    "존재하지않는메뉴",
                    pageable
            );

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getPageInfo().getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("성공: 다양한 권한 레벨 비교")
        void compareResponsesByRole_success() {
            // when - 특정 메뉴 ID로 필터링하여 비교
            String targetMenuId = menu1.getId(); // 모든 권한에서 조회 가능한 일반 메뉴

            MenuSummaryResponse customerResponse = menuQueryService.getMenus(
                            testRestaurant.getId(),
                            PageRequest.of(0, 10)
                    ).getContent().stream()
                    .filter(menu -> menu.getMenuId().equals(targetMenuId))
                    .findFirst()
                    .orElseThrow();

            MenuResponse ownerResponse = menuQueryService.getMenusForOwner(
                            testRestaurant.getId(),
                            PageRequest.of(0, 10)
                    ).getContent().stream()
                    .filter(menu -> menu.getMenuId().equals(targetMenuId))
                    .findFirst()
                    .orElseThrow();

            AdminMenuResponse adminResponse = menuQueryService.getMenusForAdmin(
                            testRestaurant.getId(),
                            PageRequest.of(0, 10)
                    ).getContent().stream()
                    .filter(menu -> menu.getMenuId().equals(targetMenuId))
                    .findFirst()
                    .orElseThrow();

            // then - 기본 정보는 동일
            assertThat(customerResponse.getMenuId()).isEqualTo(targetMenuId);
            assertThat(ownerResponse.getMenuId()).isEqualTo(targetMenuId);
            assertThat(adminResponse.getMenuId()).isEqualTo(targetMenuId);

            // Owner와 Admin은 감사 정보 포함
            assertThat(ownerResponse.getCreatedBy()).isNotNull();
            assertThat(ownerResponse.getCreatedAt()).isNotNull();
            assertThat(adminResponse.getCreatedBy()).isNotNull();
            assertThat(adminResponse.getCreatedAt()).isNotNull();

            // Admin만 레스토랑 이름 포함
            assertThat(adminResponse.getRestaurantName()).isNotNull();
        }
    }
}