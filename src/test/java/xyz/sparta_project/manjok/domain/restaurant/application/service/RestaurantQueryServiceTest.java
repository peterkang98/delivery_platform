package xyz.sparta_project.manjok.domain.restaurant.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantCategoryRepository;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminRestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantDetailResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.RestaurantSummaryResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.RestaurantResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RestaurantQueryService 통합 테스트")
class RestaurantQueryServiceTest {

    @Autowired
    private RestaurantQueryService restaurantQueryService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantCategoryRepository restaurantCategoryRepository;

    private Restaurant testRestaurant;
    private RestaurantCategory testCategory;
    private final String OWNER_ID = "1";
    private final String OWNER_NAME = "홍길동";

    @BeforeEach
    void setUp() {
        // 1. RestaurantCategory 생성 및 저장
        testCategory = RestaurantCategory.builder()
                .categoryCode("KOREAN")
                .categoryName("한식")
                .description("한국 음식")
                .depth(1)
                .displayOrder(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();
        testCategory = restaurantCategoryRepository.save(testCategory);

        // 2. Address 생성
        Address address = Address.builder()
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("테헤란로 123")
                .build();

        // 3. Coordinate 생성
        Coordinate coordinate = Coordinate.builder()
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .build();

        // 4. Restaurant 생성
        testRestaurant = Restaurant.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .ownerId(OWNER_ID)
                .ownerName(OWNER_NAME)
                .restaurantName("테스트식당")
                .status(RestaurantStatus.OPEN)
                .address(address)
                .coordinate(coordinate)
                .contactNumber("02-1234-5678")
                .tags(List.of("한식", "맛집"))
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

        // 5. RestaurantCategoryRelation 생성 및 추가
        RestaurantCategoryRelation categoryRelation = RestaurantCategoryRelation.builder()
                .categoryId(testCategory.getId())
                .isPrimary(true)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .build();
        testRestaurant.getCategoryRelations().add(categoryRelation);

        // 6. Restaurant 저장
        testRestaurant = restaurantRepository.save(testRestaurant);
    }

    // ==================== Customer 조회 테스트 ====================

    @Test
    @DisplayName("식당 목록 검색 성공")
    void searchRestaurants_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        PageResponse<RestaurantSummaryResponse> result = restaurantQueryService.searchRestaurants(
                "서울특별시", "강남구", null, null, null, pageable
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getRestaurantName()).isEqualTo("테스트식당");
        assertThat(result.getContent().get(0).getCategoryNames()).contains("한식");
    }

    @Test
    @DisplayName("식당 상세 조회 성공 - 조회수 증가 확인")
    void getRestaurantDetail_Success() {
        // given
        String restaurantId = testRestaurant.getId();
        int initialViewCount = testRestaurant.getViewCount();

        // when
        RestaurantDetailResponse result = restaurantQueryService.getRestaurantDetail(restaurantId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(result.getRestaurantName()).isEqualTo("테스트식당");
        assertThat(result.getCategoryNames()).contains("한식");
        assertThat(result.getViewCount()).isEqualTo(initialViewCount + 1);
    }

    @Test
    @DisplayName("식당 상세 조회 실패 - 존재하지 않는 식당")
    void getRestaurantDetail_NotFound() {
        // given
        String invalidId = "INVALID-ID";

        // when & then
        assertThatThrownBy(() -> restaurantQueryService.getRestaurantDetail(invalidId))
                .isInstanceOf(RestaurantException.class);
    }

    // ==================== Owner 조회 테스트 ====================

    @Test
    @DisplayName("Owner의 식당 목록 조회 성공")
    void getRestaurantsByOwnerId_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        PageResponse<RestaurantResponse> result = restaurantQueryService.getRestaurantsByOwnerId(
                OWNER_ID, pageable
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(result.getContent().get(0).getCategoryNames()).contains("한식");
    }

    @Test
    @DisplayName("Owner의 특정 식당 상세 조회 성공")
    void getRestaurantForOwner_Success() {
        // given
        String restaurantId = testRestaurant.getId();

        // when
        RestaurantResponse result = restaurantQueryService.getRestaurantForOwner(restaurantId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(result.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(result.getCategoryNames()).contains("한식");
    }

    // ==================== Admin 조회 테스트 ====================

    @Test
    @DisplayName("Admin 전체 식당 목록 조회 성공")
    void getAllRestaurantsForAdmin_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        PageResponse<AdminRestaurantResponse> result = restaurantQueryService.getAllRestaurantsForAdmin(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Admin 특정 식당 상세 조회 성공")
    void getRestaurantForAdmin_Success() {
        // given
        String restaurantId = testRestaurant.getId();

        // when
        AdminRestaurantResponse result = restaurantQueryService.getRestaurantForAdmin(restaurantId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(result.getCategoryNames()).contains("한식");
    }

    @Test
    @DisplayName("Admin 삭제된 식당도 조회 가능")
    void getRestaurantForAdmin_IncludingDeleted() {
        // given
        String restaurantId = testRestaurant.getId();
        restaurantRepository.delete(restaurantId, "ADMIN");

        // when
        AdminRestaurantResponse result = restaurantQueryService.getRestaurantForAdmin(restaurantId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(result.getIsDeleted()).isTrue();
    }

    // ==================== 공통 메서드 테스트 ====================

    @Test
    @DisplayName("식당 존재 여부 확인 - 존재함")
    void existsRestaurant_True() {
        // given
        String restaurantId = testRestaurant.getId();

        // when
        boolean result = restaurantQueryService.existsRestaurant(restaurantId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("식당 존재 여부 확인 - 존재하지 않음")
    void existsRestaurant_False() {
        // given
        String invalidId = "INVALID-ID";

        // when
        boolean result = restaurantQueryService.existsRestaurant(invalidId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("식당 ID로 조회 성공")
    void getRestaurantById_Success() {
        // given
        String restaurantId = testRestaurant.getId();

        // when
        Restaurant result = restaurantQueryService.getRestaurantById(restaurantId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(restaurantId);
    }

    @Test
    @DisplayName("식당 ID로 조회 실패 - 존재하지 않는 식당")
    void getRestaurantById_NotFound() {
        // given
        String invalidId = "INVALID-ID";

        // when & then
        assertThatThrownBy(() -> restaurantQueryService.getRestaurantById(invalidId))
                .isInstanceOf(RestaurantException.class);
    }

    // ==================== 카테고리 관련 테스트 ====================

    @Test
    @DisplayName("카테고리가 없는 식당 조회")
    void searchRestaurants_WithoutCategory() {
        // given
        Restaurant noCategoryRestaurant = Restaurant.builder()
                .id(UUID.randomUUID().toString())
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
        restaurantRepository.save(noCategoryRestaurant);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        PageResponse<RestaurantSummaryResponse> result = restaurantQueryService.searchRestaurants(
                null, null, null, null, "카테고리없는", pageable
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getCategoryNames()).isEmpty();
    }

    @Test
    @DisplayName("비활성화된 카테고리는 응답에서 제외")
    void searchRestaurants_ExcludeInactiveCategory() {
        // given
        RestaurantCategory inactiveCategory = RestaurantCategory.builder()
                .categoryCode("INACTIVE")
                .categoryName("비활성카테고리")
                .description("비활성화된 카테고리")
                .depth(1)
                .displayOrder(2)
                .isActive(false)  // 비활성화
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();
        inactiveCategory = restaurantCategoryRepository.save(inactiveCategory);

        RestaurantCategoryRelation inactiveRelation = RestaurantCategoryRelation.builder()
                .categoryId(inactiveCategory.getId())
                .isPrimary(false)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .build();
        testRestaurant.getCategoryRelations().add(inactiveRelation);
        restaurantRepository.save(testRestaurant);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        PageResponse<RestaurantSummaryResponse> result = restaurantQueryService.searchRestaurants(
                null, null, null, null, "테스트식당", pageable
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getCategoryNames())
                .contains("한식")
                .doesNotContain("비활성카테고리");
    }
}