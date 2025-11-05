package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Restaurant 도메인 테스트")
class RestaurantTest {

    private Restaurant restaurant;
    private Address validAddress;
    private Coordinate validCoordinate;
    private final Long ownerId = 1L;
    private final String ownerName = "김준형";
    private final String userId = "USER001";

    @BeforeEach
    void setUp() {
        validAddress = Address.builder()
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("123-45")
                .build();

        validCoordinate = Coordinate.of(37.5665, 126.9780);

        restaurant = Restaurant.builder()
                .id("REST001")
                .createdAt(LocalDateTime.now())
                .ownerId(ownerId)
                .ownerName(ownerName)
                .restaurantName("테스트 음식점")
                .contactNumber("02-1234-5678")
                .status(RestaurantStatus.OPEN)
                .address(validAddress)
                .coordinate(validCoordinate)
                .isActive(true)
                .createdBy(userId)
                .build();
    }

    @Test
    @DisplayName("레스토랑 생성 - 정상")
    void createRestaurant_Success() {
        // given & when & then
        assertThat(restaurant).isNotNull();
        assertThat(restaurant.getRestaurantName()).isEqualTo("테스트 음식점");
        assertThat(restaurant.getOwnerId()).isEqualTo(ownerId);
        assertThat(restaurant.getStatus()).isEqualTo(RestaurantStatus.OPEN);
        assertThat(restaurant.getAddress()).isEqualTo(validAddress);
        assertThat(restaurant.getCoordinate()).isEqualTo(validCoordinate);
    }

    @Test
    @DisplayName("레스토랑 검증 - 이름 필수")
    void validate_RestaurantNameRequired() {
        // given
        Restaurant invalidRestaurant = Restaurant.builder()
                .ownerId(ownerId)
                .address(validAddress)
                .build();

        // when & then
        assertThatThrownBy(() -> invalidRestaurant.validate())
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.RESTAURANT_NAME_REQUIRED);
    }

    @Test
    @DisplayName("레스토랑 검증 - 소유자 필수")
    void validate_OwnerRequired() {
        // given
        Restaurant invalidRestaurant = Restaurant.builder()
                .restaurantName("테스트")
                .address(validAddress)
                .build();

        // when & then
        assertThatThrownBy(() -> invalidRestaurant.validate())
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.OWNER_REQUIRED);
    }


    @Test
    @DisplayName("레스토랑 검증 - 유효하지 않은 주소")
    void validate_invalidAddress() {
        // given
        Address invalidAddress = Address.builder()
                .province("서울특별시")
                .build();

        Restaurant invalidRestaurant = Restaurant.builder()
                .restaurantName("테스트")
                .ownerId(ownerId)
                .address(invalidAddress)
                .build();

        // when & then
        assertThatThrownBy(() -> invalidRestaurant.validate())
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.INVALID_ADDRESS);
    }

    @Test
    @DisplayName("기본 정보 업데이트")
    void updateBasicInfo() {
        // given
        restaurant.updateBasicInfo("새로운 음식점", "02-9999-9999", userId);

        // when & then
        assertThat(restaurant.getRestaurantName()).isEqualTo("새로운 음식점");
        assertThat(restaurant.getContactNumber()).isEqualTo("02-9999-9999");
        assertThat(restaurant.getUpdatedBy()).isEqualTo(userId);
        assertThat(restaurant.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("주소 업데이트 - 정상")
    void updateAddress_Success() {
        // given
        Address newAddress = Address.builder()
                .province("경기도")
                .city("고양시")
                .district("일산동구")
                .build();

        // when
        restaurant.updateAddress(newAddress, userId);

        // then
        assertThat(restaurant.getAddress()).isEqualTo(newAddress);
        assertThat(restaurant.getUpdatedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("주소 업데이트 - 유효하지 않은 주소")
    void updateAddress_Invalid() {
        // given
        Address invalidAddress = Address.builder()
                .province("서울특별시")
                .build();

        // when & then
        assertThatThrownBy(() -> restaurant.updateAddress(invalidAddress, userId))
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode",RestaurantErrorCode.INVALID_ADDRESS);
    }

    @Test
    @DisplayName("좌표 업데이트")
    void updateCoordinate() {
        // given
        Coordinate newCoordinate = Coordinate.of(37.5000, 127.0000);

        // when
        restaurant.updateCoordinate(newCoordinate, userId);

        // then
        assertThat(restaurant.getCoordinate()).isEqualTo(newCoordinate);
        assertThat(restaurant.getUpdatedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("운영 상태 변경")
    void changeStatus() {
        // when
        restaurant.changeStatus(RestaurantStatus.CLOSED, userId);

        // then
        assertThat(restaurant.getStatus()).isEqualTo(RestaurantStatus.CLOSED);
        assertThat(restaurant.getUpdatedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("태그 추가")
    void addTag() {
        // when
        restaurant.addTag("맛집");
        restaurant.addTag("가성비");

        // then
        assertThat(restaurant.getTags()).containsExactly("맛집", "가성비");
    }

    @Test
    @DisplayName("태그 제거")
    void removeTag() {
        // given
        restaurant.addTag("맛집");
        restaurant.addTag("가성비");

        // when
        restaurant.removeTag("맛집");

        // then
        assertThat(restaurant.getTags()).containsExactly("가성비");
    }

    @Test
    @DisplayName("메뉴 추가 - 영업 중에는 불가")
    void addMenu_whileOpen_ThrowsException() {
        // given
        restaurant.changeStatus(RestaurantStatus.OPEN, userId);

        // when & then
        assertThatThrownBy(() -> restaurant.addMenu(
                "치즈버거",
                "치이즈 버거거",
                new BigDecimal("8000"),
                userId
        ))
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode",RestaurantErrorCode.CANNOT_MODIFY_MENU_WHILE_OPEN);
    }

    @Test
    @DisplayName("메뉴 추가 - 영업 종료 시 가능")
    void addMenu_whenClosed_success() {
        // given
        restaurant.changeStatus(RestaurantStatus.CLOSED, userId);

        // when
        Menu menu = restaurant.addMenu(
                "치즈버거",
                "치이즈 버거거",
                new BigDecimal("8000"),
                userId
        );

        // then
        assertThat(menu).isNotNull();
        assertThat(menu.getMenuName()).isEqualTo("치즈버거");
        assertThat(restaurant.getMenus()).hasSize(1);
    }

    @Test
    @DisplayName("메뉴 찾기 - 성공")
    void findMenuById_success() {
        // given
        restaurant.changeStatus(RestaurantStatus.CLOSED, userId);
        Menu addedMenu = restaurant.addMenu("치즈버거", "설명", new BigDecimal("8000"), userId);

        // when
        Menu foundMenu = restaurant.findMenuById(addedMenu.getId());

        // then
        assertThat(foundMenu).isNotNull();
        assertThat(foundMenu.getId()).isEqualTo(addedMenu.getId());
    }

    @Test
    @DisplayName("메뉴 찾기 - 존재하지 않는 메뉴")
    void findMenuById_NotFound() {
        // when & then
        assertThatThrownBy(() -> restaurant.findMenuById("INVALID_ID"))
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.MENU_NOT_FOUND);
    }

    @Test
    @DisplayName("메뉴 카테고리 추가 - 최상위")
    void addMenuCategory_root() {
        // when
        MenuCategory category = restaurant.addMenuCategory(
                "버거류",
                "버거 메뉴",
                null,
                1,
                userId
        );

        // then
        assertThat(category).isNotNull();
        assertThat(category.getCategoryName()).isEqualTo("버거류");
        assertThat(category.getDepth()).isEqualTo(1);
        assertThat(restaurant.getMenuCategories()).hasSize(1);
    }

    @Test
    @DisplayName("메뉴 카테고리 추가 - 계층 구조")
    void addMenuCategory_hierarchy() {
        // given
        MenuCategory parent = restaurant.addMenuCategory("버거류", "설명", null, 1, userId);

        // when
        MenuCategory child = restaurant.addMenuCategory(
                "치즈버거",
                "치즈버거 종류",
                parent.getId(),
                1,
                userId
        );

        // then
        assertThat(child.getDepth()).isEqualTo(2);
        assertThat(child.getParentCategoryId()).isEqualTo(parent.getId());
    }

    @Test
    @DisplayName("메뉴 카테고리 찾기")
    void findMenuCategoryById() {
        // given
        MenuCategory category = restaurant.addMenuCategory("버거류", "설명", null, 1, userId);

        // when
        MenuCategory found = restaurant.findMenuCategoryById(category.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("메뉴에 카테고리 연결")
    void addMenuToCategory() {
        // given
        restaurant.changeStatus(RestaurantStatus.CLOSED, userId);
        Menu menu = restaurant.addMenu("치즈버거", "설명", new BigDecimal("8000"), userId);
        MenuCategory category = restaurant.addMenuCategory("버거류", "설명", null, 1, userId);

        // when
        MenuCategoryRelation relation = restaurant.addMenuToCategory(
                menu.getId(),
                category.getId(),
                true,
                userId
        );

        // then
        assertThat(relation).isNotNull();
        assertThat(relation.getCategoryId()).isEqualTo(category.getId());
        assertThat(menu.belongsToCategory(category.getId())).isTrue();
        assertThat(category.getMenuIds()).contains(menu.getId());
    }

    @Test
    @DisplayName("메뉴 제거")
    void removeMenu() {
        // given
        restaurant.changeStatus(RestaurantStatus.CLOSED, userId);
        Menu menu = restaurant.addMenu("치즈버거", "설명", new BigDecimal("8000"), userId);
        MenuCategory category = restaurant.addMenuCategory("버거류", "설명", null, 1, userId);
        restaurant.addMenuToCategory(menu.getId(), category.getId(), true, userId);

        // when
        restaurant.removeMenu(menu.getId(), userId);

        // then
        assertThat(menu.getIsDeleted()).isTrue();
        assertThat(category.getMenuIds()).doesNotContain(menu.getId());
    }

    @Test
    @DisplayName("운영 시간 설정")
    void setOperatingDay() {
        OperatingDay operatingDay = restaurant.setOperatingDay(
                DayType.MON,
                OperatingTimeType.REGULAR,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                false,
                null
        );

        assertThat(operatingDay).isNotNull();
        assertThat(operatingDay.getDayType()).isEqualTo(DayType.MON);
        assertThat(restaurant.getOperatingDays()).hasSize(1);
    }

    @Test
    @DisplayName("브레이크 타임 설정")
    void setBreakTime() {
        // given
        restaurant.setOperatingDay(
                DayType.MON,
                OperatingTimeType.REGULAR,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                false,
                null
        );

        restaurant.setBreakTime(
                DayType.MON,
                LocalTime.of(15, 0),
                LocalTime.of(17, 0)
        );

        // when
        OperatingDay operatingDay = restaurant.getOperatingDay(DayType.MON, OperatingTimeType.REGULAR);

        // then
        assertThat(operatingDay.getBreakStartTime()).isEqualTo(LocalTime.of(15, 0));
        assertThat(operatingDay.getBreakEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    @DisplayName("레스토랑 카테고리 추가")
    void addRestaurantCategory() {
        RestaurantCategoryRelation relation = restaurant.addRestaurantCategory(
                "CAT001",
                true,
                userId
        );

        assertThat(relation).isNotNull();
        assertThat(relation.getCategoryId()).isEqualTo("CAT001");
        assertThat(relation.isPrimary()).isTrue();
        assertThat(restaurant.getCategoryRelations()).hasSize(1);
    }

    @Test
    @DisplayName("레스토랑 카테고리 제거")
    void removeRestaurantCategory() {
        restaurant.addRestaurantCategory("CAT001", true, userId);

        restaurant.removeRestaurantCategory("CAT001", userId);

        restaurant.getCategoryRelations().forEach(relation -> {
            assertThat(relation.isDeleted()).isTrue();
        });
    }

    @Test
    @DisplayName("레스토랑 삭제")
    void deleteRestaurant() {
        restaurant.changeStatus(RestaurantStatus.CLOSED, userId);

        restaurant.addMenuCategory("카테고리", "설명", null, 1, userId);
        restaurant.addRestaurantCategory("CAT001", true, userId);

        restaurant.delete(userId);

        assertThat(restaurant.isDeleted()).isTrue();
        assertThat(restaurant.getIsActive()).isFalse();
        assertThat(restaurant.getStatus()).isEqualTo(RestaurantStatus.CLOSED);
        assertThat(restaurant.getDeletedBy()).isEqualTo(userId);

        restaurant.getMenuCategories().forEach(cat ->
                assertThat(cat.getIsDeleted()).isTrue()
        );
        restaurant.getCategoryRelations().forEach(rel ->
                assertThat(rel.isDeleted()).isTrue()
        );
    }

    @Test
    @DisplayName("이미 삭제된 레스토랑 삭제 시도")
    void deleteRestaurant_AlreadyDeleted() {
        restaurant.delete(userId);

        assertThatThrownBy(() -> restaurant.delete(userId))
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.RESTAURANT_ALREADY_DELETED);
    }

    @Test
    @DisplayName("레스토랑 복구")
    void restoreRestaurant() {
        restaurant.delete(userId);
        restaurant.restore(userId);

        assertThat(restaurant.isDeleted()).isFalse();
        assertThat(restaurant.getIsActive()).isTrue();
        assertThat(restaurant.getDeletedAt()).isNull();
        assertThat(restaurant.getDeletedBy()).isNull();
    }

    @Test
    @DisplayName("통계 업데이트")
    void updateStatistics() {
        restaurant.incrementViewCount();
        restaurant.incrementWishlistCount();
        restaurant.incrementPurchaseCount();
        restaurant.updateReviewStats(10, new BigDecimal("4.5"));

        assertThat(restaurant.getViewCount()).isEqualTo(1);
        assertThat(restaurant.getWishlistCount()).isEqualTo(1);
        assertThat(restaurant.getPurchaseCount()).isEqualTo(1);
        assertThat(restaurant.getReviewCount()).isEqualTo(10);
        assertThat(restaurant.getReviewRating()).isEqualByComparingTo(new BigDecimal("4.5"));
    }

    @Test
    @DisplayName("찜 감소 - 0보다 작아지지 않음")
    void decrementWishlistCount_NotBelowZero() {
        restaurant.decrementWishlistCount();

        assertThat(restaurant.getWishlistCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("활성 메뉴 개수 조회")
    void getActiveMenuCount() {
        restaurant.changeStatus(RestaurantStatus.CLOSED, userId);
        restaurant.addMenu("메뉴1", "설명", new BigDecimal("5000"), userId);
        restaurant.addMenu("메뉴2", "설명", new BigDecimal("6000"), userId);

        assertThat(restaurant.getActiveMenuCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("주문 가능 여부 확인")
    void canAcceptOrder() {
        LocalDateTime now = LocalDateTime.now();
        DayType currentDay = getDayTypeFromLocalDateTime(now);

        restaurant.setOperatingDay(
                currentDay,
                OperatingTimeType.REGULAR,
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                false,
                null
        );

        assertThat(restaurant.canAcceptOrder()).isTrue();

        restaurant.changeStatus(RestaurantStatus.CLOSED, userId);
        assertThat(restaurant.canAcceptOrder()).isFalse();
    }

    @Test
    @DisplayName("활성/비활성 설정")
    void setActive() {
        restaurant.setActive(false, userId);

        assertThat(restaurant.getIsActive()).isFalse();
        assertThat(restaurant.getUpdatedBy()).isEqualTo(userId);
    }

    private DayType getDayTypeFromLocalDateTime(LocalDateTime dateTime) {
        return switch (dateTime.getDayOfWeek()) {
            case MONDAY -> DayType.MON;
            case TUESDAY -> DayType.TUE;
            case WEDNESDAY -> DayType.WED;
            case THURSDAY -> DayType.THU;
            case FRIDAY -> DayType.FRI;
            case SATURDAY -> DayType.SAT;
            case SUNDAY -> DayType.SUN;
        };
    }
}