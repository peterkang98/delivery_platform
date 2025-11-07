package xyz.sparta_project.manjok.domain.restaurant.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminRestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantDetailResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantSummaryResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.RestaurantResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RestaurantMapper 단위 테스트")
class RestaurantMapperTest {

    private RestaurantMapper restaurantMapper;
    private Restaurant testRestaurant;
    private RestaurantCategory testCategory;
    private Map<String, RestaurantCategory> categoryMap;

    @BeforeEach
    void setUp() {
        restaurantMapper = new RestaurantMapper();

        // 1. Address 생성
        Address address = Address.builder()
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("테헤란로 123")
                .build();

        // 2. Coordinate 생성
        Coordinate coordinate = Coordinate.builder()
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .build();

        // 3. OperatingDay 생성
        OperatingDay operatingDay = OperatingDay.builder()
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .isHoliday(false)
                .build();

        // 4. Restaurant 생성
        testRestaurant = Restaurant.builder()
                .id("REST-12345678")
                .createdAt(LocalDateTime.now())
                .ownerId("1")
                .ownerName("홍길동")
                .restaurantName("테스트식당")
                .status(RestaurantStatus.OPEN)
                .address(address)
                .coordinate(coordinate)
                .contactNumber("02-1234-5678")
                .tags(List.of("한식", "맛집"))
                .isActive(true)
                .viewCount(10)
                .wishlistCount(5)
                .reviewCount(3)
                .reviewRating(new BigDecimal("4.5"))
                .purchaseCount(20)
                .menus(new ArrayList<>())
                .menuCategories(new ArrayList<>())
                .operatingDays(new HashSet<>(Set.of(operatingDay)))
                .categoryRelations(new HashSet<>())
                .createdBy("TEST")
                .isDeleted(false)
                .build();

        // 5. RestaurantCategory 생성
        testCategory = RestaurantCategory.builder()
                .id("CAT-001")
                .categoryCode("KOREAN")
                .categoryName("한식")
                .isActive(true)
                .build();

        // 6. RestaurantCategoryRelation 생성
        RestaurantCategoryRelation categoryRelation = RestaurantCategoryRelation.builder()
                .restaurantId("REL-001")
                .categoryId(testCategory.getId())
                .isPrimary(true)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .build();
        testRestaurant.getCategoryRelations().add(categoryRelation);

        // 7. CategoryMap 생성
        categoryMap = new HashMap<>();
        categoryMap.put(testCategory.getId(), testCategory);
    }

    // ==================== Customer DTO 변환 테스트 ====================

    @Test
    @DisplayName("Restaurant -> RestaurantSummaryResponse 변환 성공")
    void toRestaurantSummaryResponse_Success() {
        // when
        RestaurantSummaryResponse result = restaurantMapper.toRestaurantSummaryResponse(
                testRestaurant, categoryMap
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo("REST-12345678");
        assertThat(result.getRestaurantName()).isEqualTo("테스트식당");
        assertThat(result.getProvince()).isEqualTo("서울특별시");
        assertThat(result.getCity()).isEqualTo("강남구");
        assertThat(result.getDistrict()).isEqualTo("역삼동");
        assertThat(result.getCategoryNames()).contains("한식");
        assertThat(result.getViewCount()).isEqualTo(10);
        assertThat(result.getTags()).containsExactlyInAnyOrder("한식", "맛집");
    }

    @Test
    @DisplayName("Restaurant -> RestaurantDetailResponse 변환 성공")
    void toRestaurantDetailResponse_Success() {
        // when
        RestaurantDetailResponse result = restaurantMapper.toRestaurantDetailResponse(
                testRestaurant, categoryMap
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo("REST-12345678");
        assertThat(result.getRestaurantName()).isEqualTo("테스트식당");
        assertThat(result.getOwnerName()).isEqualTo("홍길동");
        assertThat(result.getContactNumber()).isEqualTo("02-1234-5678");

        // 주소 검증
        assertThat(result.getAddress()).isNotNull();
        assertThat(result.getAddress().getProvince()).isEqualTo("서울특별시");
        assertThat(result.getAddress().getFullAddress()).contains("서울특별시");

        // 좌표 검증
        assertThat(result.getCoordinate()).isNotNull();
        assertThat(result.getCoordinate().getLatitude()).isEqualByComparingTo(new BigDecimal("37.5665"));
        assertThat(result.getCoordinate().getLongitude()).isEqualByComparingTo(new BigDecimal("126.9780"));

        // 카테고리 검증
        assertThat(result.getCategoryNames()).contains("한식");

        // 통계 정보 검증
        assertThat(result.getViewCount()).isEqualTo(10);
        assertThat(result.getReviewCount()).isEqualTo(3);
        assertThat(result.getReviewRating()).isEqualByComparingTo(new BigDecimal("4.5"));
    }

    // ==================== Owner DTO 변환 테스트 ====================

    @Test
    @DisplayName("Restaurant -> RestaurantResponse 변환 성공")
    void toRestaurantResponse_Success() {
        // when
        RestaurantResponse result = restaurantMapper.toRestaurantResponse(
                testRestaurant, categoryMap
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo("REST-12345678");
        assertThat(result.getOwnerId()).isEqualTo("1");
        assertThat(result.getOwnerName()).isEqualTo("홍길동");
        assertThat(result.getStatus()).isEqualTo(RestaurantStatus.OPEN.name());

        // 카테고리 검증
        assertThat(result.getCategoryNames()).contains("한식");

        // 감사 정보 검증
        assertThat(result.getCreatedBy()).isEqualTo("TEST");
        assertThat(result.getIsDeleted()).isFalse();
    }

    // ==================== Admin DTO 변환 테스트 ====================

    @Test
    @DisplayName("Restaurant -> AdminRestaurantResponse 변환 성공")
    void toAdminRestaurantResponse_Success() {
        // when
        AdminRestaurantResponse result = restaurantMapper.toAdminRestaurantResponse(
                testRestaurant, categoryMap
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo("REST-12345678");
        assertThat(result.getOwnerId()).isEqualTo("1");
        assertThat(result.getStatus()).isEqualTo(RestaurantStatus.OPEN.name());

        // 카테고리 검증
        assertThat(result.getCategoryNames()).contains("한식");

        // 감사 정보 검증
        assertThat(result.getCreatedBy()).isEqualTo("TEST");
        assertThat(result.getIsDeleted()).isFalse();
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getDeletedBy()).isNull();
    }

    // ==================== 카테고리 관련 테스트 ====================

    @Test
    @DisplayName("카테고리 없는 Restaurant 변환 성공")
    void toRestaurantSummaryResponse_WithoutCategories() {
        // given
        Restaurant noCategoryRestaurant = Restaurant.builder()
                .id("REST-99999999")
                .createdAt(LocalDateTime.now())
                .ownerId("2")
                .ownerName("김철수")
                .restaurantName("카테고리없는식당")
                .status(RestaurantStatus.OPEN)
                .address(testRestaurant.getAddress())
                .contactNumber("02-9999-9999")
                .tags(List.of())
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
                .createdBy("TEST")
                .isDeleted(false)
                .build();

        // when
        RestaurantSummaryResponse result = restaurantMapper.toRestaurantSummaryResponse(
                noCategoryRestaurant, new HashMap<>()
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryNames()).isEmpty();
    }

    @Test
    @DisplayName("비활성화된 카테고리는 제외하고 변환")
    void toRestaurantSummaryResponse_ExcludeInactiveCategory() {
        // given
        RestaurantCategory inactiveCategory = RestaurantCategory.builder()
                .id("CAT-002")
                .categoryCode("INACTIVE")
                .categoryName("비활성카테고리")
                .isActive(false)  // 비활성화
                .build();

        RestaurantCategoryRelation inactiveRelation = RestaurantCategoryRelation.builder()
                .restaurantId("REL-002")
                .categoryId(inactiveCategory.getId())
                .isPrimary(false)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .build();
        testRestaurant.getCategoryRelations().add(inactiveRelation);

        Map<String, RestaurantCategory> mapWithInactive = new HashMap<>(categoryMap);
        mapWithInactive.put(inactiveCategory.getId(), inactiveCategory);

        // when
        RestaurantSummaryResponse result = restaurantMapper.toRestaurantSummaryResponse(
                testRestaurant, mapWithInactive
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryNames())
                .contains("한식")
                .doesNotContain("비활성카테고리");
    }

    @Test
    @DisplayName("삭제된 카테고리 관계는 제외하고 변환")
    void toRestaurantSummaryResponse_ExcludeDeletedRelation() {
        // given
        RestaurantCategoryRelation deletedRelation = RestaurantCategoryRelation.builder()
                .restaurantId("REL-003")
                .categoryId("CAT-003")
                .isPrimary(false)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(true)  // 삭제됨
                .build();
        testRestaurant.getCategoryRelations().add(deletedRelation);

        // when
        RestaurantSummaryResponse result = restaurantMapper.toRestaurantSummaryResponse(
                testRestaurant, categoryMap
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryNames()).contains("한식");
        assertThat(result.getCategoryNames()).hasSize(1);
    }

    // ==================== Edge Case 테스트 ====================

    @Test
    @DisplayName("좌표가 없는 Restaurant 변환 성공")
    void toRestaurantDetailResponse_WithoutCoordinate() {
        // given
        Restaurant noCoordinateRestaurant = Restaurant.builder()
                .id("REST-88888888")
                .createdAt(LocalDateTime.now())
                .ownerId("3")
                .ownerName("이영희")
                .restaurantName("좌표없는식당")
                .status(RestaurantStatus.OPEN)
                .address(testRestaurant.getAddress())
                .coordinate(null)  // 좌표 없음
                .contactNumber("02-8888-8888")
                .tags(List.of())
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
                .createdBy("TEST")
                .isDeleted(false)
                .build();

        // when
        RestaurantDetailResponse result = restaurantMapper.toRestaurantDetailResponse(
                noCoordinateRestaurant, new HashMap<>()
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCoordinate()).isNull();
    }

    @Test
    @DisplayName("운영 시간이 있는 Restaurant 변환 성공")
    void toRestaurantDetailResponse_WithOperatingHours() {
        // when
        RestaurantDetailResponse result = restaurantMapper.toRestaurantDetailResponse(
                testRestaurant, categoryMap
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOperatingHours()).isNotEmpty();

        // 운영 시간 상세 검증
        String key = "MON_REGULAR";
        assertThat(result.getOperatingHours()).containsKey(key);

        RestaurantDetailResponse.OperatingHoursDto operatingHours = result.getOperatingHours().get(key);
        assertThat(operatingHours).isNotNull();
        assertThat(operatingHours.getStartTime()).isEqualTo("10:00");
        assertThat(operatingHours.getEndTime()).isEqualTo("22:00");
        assertThat(operatingHours.getIsHoliday()).isFalse();
    }

    @Test
    @DisplayName("현재 운영 상태 - 활성화된 경우")
    void getCurrentOperatingStatus_Active() {
        // given
        testRestaurant.setActive(true, "TEST");

        // when
        RestaurantSummaryResponse result = restaurantMapper.toRestaurantSummaryResponse(
                testRestaurant, categoryMap
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentOperatingStatus()).isIn("영업 중", "영업 종료");
    }

    @Test
    @DisplayName("현재 운영 상태 - 비활성화된 경우")
    void getCurrentOperatingStatus_Inactive() {
        // given
        testRestaurant.setActive(false, "TEST");

        // when
        RestaurantSummaryResponse result = restaurantMapper.toRestaurantSummaryResponse(
                testRestaurant, categoryMap
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentOperatingStatus()).isEqualTo("운영 중지");
    }

    @Test
    @DisplayName("카테고리 맵에 없는 ID는 필터링됨")
    void toRestaurantSummaryResponse_FilterMissingCategories() {
        // given
        RestaurantCategoryRelation unknownRelation = RestaurantCategoryRelation.builder()
                .restaurantId("REL-999")
                .categoryId("CAT-999")  // categoryMap에 없는 ID
                .isPrimary(false)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .build();
        testRestaurant.getCategoryRelations().add(unknownRelation);

        // when
        RestaurantSummaryResponse result = restaurantMapper.toRestaurantSummaryResponse(
                testRestaurant, categoryMap
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryNames()).hasSize(1);
        assertThat(result.getCategoryNames()).contains("한식");
    }
}