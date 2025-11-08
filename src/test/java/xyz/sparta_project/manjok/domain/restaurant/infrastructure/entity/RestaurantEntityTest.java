package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RestaurantEntity 변환 테스트 (연관관계 매핑 적용)
 */
class RestaurantEntityTest {

    @Test
    @DisplayName("도메인 Restaurant를 RestaurantEntity로 변환")
    void fromDomain_ShouldConvertRestaurantToEntity() {
        // given
        Address address = Address.builder()
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("테헤란로 123")
                .build();

        Coordinate coordinate = Coordinate.builder()
                .latitude(new BigDecimal("37.5665350"))
                .longitude(new BigDecimal("126.9779690"))
                .build();

        Restaurant domain = Restaurant.builder()
                .id("REST123")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .ownerId("1")
                .ownerName("홍길동")
                .restaurantName("맛있는 식당")
                .status(RestaurantStatus.OPEN)
                .address(address)
                .coordinate(coordinate)
                .contactNumber("02-1234-5678")
                .tags(List.of("한식", "맛집"))
                .isActive(true)
                .viewCount(100)
                .createdBy("admin")
                .build();

        // when
        RestaurantEntity entity = RestaurantEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("REST123");
        assertThat(entity.getOwnerId()).isEqualTo("1");
        assertThat(entity.getOwnerName()).isEqualTo("홍길동");
        assertThat(entity.getRestaurantName()).isEqualTo("맛있는 식당");
        assertThat(entity.getStatus()).isEqualTo(RestaurantStatus.OPEN);
        assertThat(entity.getContactNumber()).isEqualTo("02-1234-5678");
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getViewCount()).isEqualTo(100);
        assertThat(entity.getCreatedBy()).isEqualTo("admin");

        // Address VO 검증
        assertThat(entity.getAddress()).isNotNull();
        assertThat(entity.getAddress().getProvince()).isEqualTo("서울특별시");
        assertThat(entity.getAddress().getCity()).isEqualTo("강남구");

        // Coordinate VO 검증
        assertThat(entity.getCoordinate()).isNotNull();
        assertThat(entity.getCoordinate().getLatitude()).isEqualByComparingTo(new BigDecimal("37.5665350"));

        // 연관관계 컬렉션 초기화 확인
        assertThat(entity.getMenus()).isEmpty();
        assertThat(entity.getMenuCategories()).isEmpty();
        assertThat(entity.getOperatingDays()).isEmpty();
        assertThat(entity.getCategoryRelations()).isEmpty();
    }

    @Test
    @DisplayName("RestaurantEntity를 도메인 Restaurant로 변환")
    void toDomain_ShouldConvertEntityToRestaurant() {
        // given
        RestaurantEntity entity = RestaurantEntity.builder()
                .ownerId("1")
                .ownerName("김철수")
                .restaurantName("즐거운 식당")
                .status(RestaurantStatus.CLOSED)
                .contactNumber("02-9876-5432")
                .isActive(false)
                .viewCount(50)
                .wishlistCount(20)
                .reviewCount(10)
                .reviewRating(new BigDecimal("4.5"))
                .purchaseCount(30)
                .createdBy("owner")
                .build();

        // when
        Restaurant domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getOwnerId()).isEqualTo("1");
        assertThat(domain.getOwnerName()).isEqualTo("김철수");
        assertThat(domain.getRestaurantName()).isEqualTo("즐거운 식당");
        assertThat(domain.getStatus()).isEqualTo(RestaurantStatus.CLOSED);
        assertThat(domain.getContactNumber()).isEqualTo("02-9876-5432");
        assertThat(domain.getIsActive()).isFalse();
        assertThat(domain.getViewCount()).isEqualTo(50);
        assertThat(domain.getWishlistCount()).isEqualTo(20);
        assertThat(domain.getReviewCount()).isEqualTo(10);
        assertThat(domain.getReviewRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertThat(domain.getPurchaseCount()).isEqualTo(30);
        assertThat(domain.getCreatedBy()).isEqualTo("owner");
    }

    @Test
    @DisplayName("Restaurant Aggregate 전체 변환 (Menu, MenuCategory, OperatingDay 포함)")
    void fullAggregateConversion_ShouldWork() {
        // given - 완전한 Aggregate 생성
        Address address = Address.builder()
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("테헤란로 123")
                .build();

        Menu menu = Menu.builder()
                .id("MENU123")
                .restaurantId("REST456")
                .menuName("불고기")
                .price(new BigDecimal("15000"))
                .isAvailable(true)
                .build();

        MenuCategory category = MenuCategory.builder()
                .id("CAT123")
                .restaurantId("REST456")
                .categoryName("메인")
                .depth(1)
                .isActive(true)
                .build();

        OperatingDay operatingDay = OperatingDay.builder()
                .restaurantId("REST456")
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .isHoliday(false)
                .build();

        Restaurant domain = Restaurant.builder()
                .id("REST456")
                .ownerId("1")
                .restaurantName("맛있는 식당")
                .status(RestaurantStatus.OPEN)
                .address(address)
                .isActive(true)
                .menus(List.of(menu))
                .menuCategories(List.of(category))
                .operatingDays(java.util.Set.of(operatingDay))
                .build();

        // when
        RestaurantEntity entity = RestaurantEntity.fromDomain(domain);

        // then - 모든 하위 엔티티 변환 확인
        assertThat(entity).isNotNull();
        assertThat(entity.getMenus()).hasSize(1);
        assertThat(entity.getMenuCategories()).hasSize(1);
        assertThat(entity.getOperatingDays()).hasSize(1);

        // 양방향 관계 확인
        MenuEntity menuEntity = entity.getMenus().get(0);
        assertThat(menuEntity.getRestaurant()).isEqualTo(entity);

        MenuCategoryEntity categoryEntity = entity.getMenuCategories().get(0);
        assertThat(categoryEntity.getRestaurant()).isEqualTo(entity);

        OperatingDayEntity operatingDayEntity = entity.getOperatingDays().iterator().next();
        assertThat(operatingDayEntity.getRestaurant()).isEqualTo(entity);

        // 도메인으로 다시 변환
        Restaurant convertedDomain = entity.toDomain();
        assertThat(convertedDomain.getMenus()).hasSize(1);
        assertThat(convertedDomain.getMenuCategories()).hasSize(1);
        assertThat(convertedDomain.getOperatingDays()).hasSize(1);
    }

    @Test
    @DisplayName("Restaurant에 Menu 추가 시 양방향 연관관계 설정")
    void addMenu_ShouldSetBidirectionalRelation() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId("1")
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        MenuEntity menu1 = MenuEntity.builder()
                .menuName("메뉴1")
                .price(new BigDecimal("10000"))
                .isAvailable(true)
                .build();

        MenuEntity menu2 = MenuEntity.builder()
                .menuName("메뉴2")
                .price(new BigDecimal("12000"))
                .isAvailable(true)
                .build();

        // when
        restaurant.addMenu(menu1);
        restaurant.addMenu(menu2);

        // then
        assertThat(restaurant.getMenus()).hasSize(2);
        assertThat(restaurant.getMenus()).containsExactly(menu1, menu2);
        assertThat(menu1.getRestaurant()).isEqualTo(restaurant);
        assertThat(menu2.getRestaurant()).isEqualTo(restaurant);
    }

    @Test
    @DisplayName("Restaurant에 MenuCategory 추가 시 양방향 연관관계 설정")
    void addMenuCategory_ShouldSetBidirectionalRelation() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId("1")
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        MenuCategoryEntity category1 = MenuCategoryEntity.builder()
                .categoryName("메인")
                .depth(1)
                .isActive(true)
                .build();

        MenuCategoryEntity category2 = MenuCategoryEntity.builder()
                .categoryName("사이드")
                .depth(1)
                .isActive(true)
                .build();

        // when
        restaurant.addMenuCategory(category1);
        restaurant.addMenuCategory(category2);

        // then
        assertThat(restaurant.getMenuCategories()).hasSize(2);
        assertThat(restaurant.getMenuCategories()).containsExactly(category1, category2);
        assertThat(category1.getRestaurant()).isEqualTo(restaurant);
        assertThat(category2.getRestaurant()).isEqualTo(restaurant);
    }

    @Test
    @DisplayName("Restaurant에 OperatingDay 추가 시 양방향 연관관계 설정")
    void addOperatingDay_ShouldSetBidirectionalRelation() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId("1")
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        OperatingDayEntity monday = OperatingDayEntity.builder()
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .isHoliday(false)
                .build();

        OperatingDayEntity tuesday = OperatingDayEntity.builder()
                .dayType(DayType.TUE)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .isHoliday(false)
                .build();

        // when
        restaurant.addOperatingDay(monday);
        restaurant.addOperatingDay(tuesday);

        // then
        assertThat(restaurant.getOperatingDays()).hasSize(2);
        assertThat(restaurant.getOperatingDays()).contains(monday, tuesday);
        assertThat(monday.getRestaurant()).isEqualTo(restaurant);
        assertThat(tuesday.getRestaurant()).isEqualTo(restaurant);
    }

    @Test
    @DisplayName("Restaurant 제거 시 연관관계도 함께 제거")
    void removeRelations_ShouldWork() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId("1")
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        MenuEntity menu = MenuEntity.builder()
                .menuName("메뉴")
                .price(new BigDecimal("10000"))
                .isAvailable(true)
                .build();

        restaurant.addMenu(menu);

        // when
        restaurant.removeMenu(menu);

        // then
        assertThat(restaurant.getMenus()).isEmpty();
        assertThat(menu.getRestaurant()).isNull();
    }
}