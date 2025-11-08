package xyz.sparta_project.manjok.domain.restaurant.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategory;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantStatus;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantCategoryRepository;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminRestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.request.AdminRestaurantUpdateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.RestaurantCreateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.RestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.RestaurantUpdateRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RestaurantCommandService 통합 테스트")
class RestaurantCommandServiceTest {

    @Autowired
    private RestaurantCommandService restaurantCommandService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantCategoryRepository restaurantCategoryRepository;

    private RestaurantCategory testCategory;
    private RestaurantCategory japaneseCategory;  // 일식 카테고리 추가
    private RestaurantCreateRequest createRequest;
    private final String OWNER_ID = "1";
    private final String OWNER_NAME = "홍길동";

    @BeforeEach
    void setUp() {
        // 1. RestaurantCategory 생성 및 저장 (한식)
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

        // 2. RestaurantCategory 생성 및 저장 (일식)
        japaneseCategory = RestaurantCategory.builder()
                .categoryCode("JAPANESE")
                .categoryName("일식")
                .description("일본 음식")
                .depth(1)
                .displayOrder(2)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();
        japaneseCategory = restaurantCategoryRepository.save(japaneseCategory);

        // 3. RestaurantCreateRequest 생성
        createRequest = RestaurantCreateRequest.builder()
                .restaurantName("새로운식당")
                .address(RestaurantCreateRequest.AddressDto.builder()
                        .province("서울특별시")
                        .city("강남구")
                        .district("역삼동")
                        .detailAddress("테헤란로 123")
                        .build())
                .contactNumber("02-1234-5678")
                .coordinate(RestaurantCreateRequest.CoordinateDto.builder()
                        .latitude(new BigDecimal("37.5665"))
                        .longitude(new BigDecimal("126.9780"))
                        .build())
                .tags(List.of("한식", "맛집"))
                .categoryIds(Set.of(testCategory.getId()))
                .build();
    }

    // ==================== 생성 테스트 ====================

    @Test
    @DisplayName("식당 등록 성공")
    void createRestaurant_Success() {
        // when
        RestaurantResponse result = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isNotNull();
        assertThat(result.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(result.getRestaurantName()).isEqualTo("새로운식당");
        assertThat(result.getCategoryNames()).contains("한식");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getStatus()).isEqualTo(RestaurantStatus.OPEN.name());
    }

    @Test
    @DisplayName("식당 등록 실패 - 중복된 식당명")
    void createRestaurant_DuplicateName() {
        // given
        restaurantCommandService.createRestaurant(OWNER_ID, OWNER_NAME, createRequest);

        // when & then
        assertThatThrownBy(() ->
                restaurantCommandService.createRestaurant(OWNER_ID, OWNER_NAME, createRequest)
        ).isInstanceOf(RestaurantException.class);
    }

    @Test
    @DisplayName("카테고리 없이 식당 등록 성공")
    void createRestaurant_WithoutCategory() {
        // given
        RestaurantCreateRequest requestWithoutCategory = RestaurantCreateRequest.builder()
                .restaurantName("카테고리없는식당")
                .address(createRequest.getAddress())
                .contactNumber("02-9999-9999")
                .build();

        // when
        RestaurantResponse result = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, requestWithoutCategory
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantName()).isEqualTo("카테고리없는식당");
        assertThat(result.getCategoryNames()).isEmpty();
    }

    // ==================== 수정 테스트 ====================

    @Test
    @DisplayName("식당 정보 전체 수정 성공 (PUT)")
    void updateRestaurant_Success() {
        // given
        RestaurantResponse created = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );

        RestaurantUpdateRequest updateRequest = RestaurantUpdateRequest.builder()
                .restaurantName("수정된식당명")
                .contactNumber("02-8888-8888")
                .status(RestaurantStatus.TEMPORARILY_CLOSED.name())
                .build();

        // when
        RestaurantResponse result = restaurantCommandService.updateRestaurant(
                created.getRestaurantId(), updateRequest, "OWNER_1"
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantName()).isEqualTo("수정된식당명");
        assertThat(result.getContactNumber()).isEqualTo("02-8888-8888");
        assertThat(result.getStatus()).isEqualTo(RestaurantStatus.TEMPORARILY_CLOSED.name());
    }

    @Test
    @DisplayName("식당 정보 부분 수정 성공 (PATCH)")
    void patchRestaurant_Success() {
        // given
        RestaurantResponse created = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );

        RestaurantUpdateRequest patchRequest = RestaurantUpdateRequest.builder()
                .restaurantName("부분수정된식당명")
                .build();

        // when
        RestaurantResponse result = restaurantCommandService.patchRestaurant(
                created.getRestaurantId(), patchRequest, "OWNER_1"
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantName()).isEqualTo("부분수정된식당명");
        assertThat(result.getContactNumber()).isEqualTo(createRequest.getContactNumber());
    }

    @Test
    @DisplayName("식당 정보 수정 실패 - 존재하지 않는 식당")
    void updateRestaurant_NotFound() {
        // given
        String invalidId = "INVALID-ID";
        RestaurantUpdateRequest updateRequest = RestaurantUpdateRequest.builder()
                .restaurantName("수정된식당명")
                .build();

        // when & then
        assertThatThrownBy(() ->
                restaurantCommandService.updateRestaurant(invalidId, updateRequest, "OWNER_1")
        ).isInstanceOf(RestaurantException.class);
    }

    // ==================== 삭제 테스트 ====================

    @Test
    @DisplayName("식당 삭제 성공 (Soft Delete)")
    void deleteRestaurant_Success() {
        // given
        RestaurantResponse created = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );

        // when
        restaurantCommandService.deleteRestaurant(created.getRestaurantId(), "OWNER_1");

        // then
        assertThat(restaurantRepository.findById(created.getRestaurantId())).isEmpty();
        assertThat(restaurantRepository.findByIdIncludingDeleted(created.getRestaurantId()))
                .isPresent();
    }

    @Test
    @DisplayName("식당 삭제 실패 - 존재하지 않는 식당")
    void deleteRestaurant_NotFound() {
        // given
        String invalidId = "INVALID-ID";

        // when & then
        assertThatThrownBy(() ->
                restaurantCommandService.deleteRestaurant(invalidId, "OWNER_1")
        ).isInstanceOf(RestaurantException.class);
    }

    // ==================== Admin 전용 테스트 ====================

    @Test
    @DisplayName("식당 복구 성공 (Admin)")
    void restoreRestaurant_Success() {
        // given
        RestaurantResponse created = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );
        restaurantCommandService.deleteRestaurant(created.getRestaurantId(), "OWNER_1");

        // when
        AdminRestaurantResponse result = restaurantCommandService.restoreRestaurant(
                created.getRestaurantId(), "ADMIN_1"
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIsDeleted()).isFalse();
        assertThat(restaurantRepository.findById(created.getRestaurantId())).isPresent();
    }

    @Test
    @DisplayName("식당 상태 변경 성공 (Admin)")
    void updateRestaurantStatus_Success() {
        // given
        RestaurantResponse created = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );

        // when
        AdminRestaurantResponse result = restaurantCommandService.updateRestaurantStatus(
                created.getRestaurantId(), RestaurantStatus.CLOSED.name(), "ADMIN_1"
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RestaurantStatus.CLOSED.name());
    }

    @Test
    @DisplayName("식당 정보 수정 성공 (Admin)")
    void updateRestaurantByAdmin_Success() {
        // given
        RestaurantResponse created = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );

        AdminRestaurantUpdateRequest adminUpdateRequest = AdminRestaurantUpdateRequest.builder()
                .restaurantName("관리자수정식당")
                .isActive(false)
                .status(RestaurantStatus.PREPARING.name())
                .build();

        // when
        AdminRestaurantResponse result = restaurantCommandService.updateRestaurantByAdmin(
                created.getRestaurantId(), adminUpdateRequest, "ADMIN_1"
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantName()).isEqualTo("관리자수정식당");
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getStatus()).isEqualTo(RestaurantStatus.PREPARING.name());
    }

    // ==================== 카테고리 관련 테스트 ====================

    @Test
    @DisplayName("식당 카테고리 변경 성공 (한식 -> 일식)")
    void updateRestaurant_ChangeCategory() {
        // given - 한식 카테고리로 식당 생성
        RestaurantResponse created = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );

        // when - 일식으로 변경
        RestaurantUpdateRequest updateRequest = RestaurantUpdateRequest.builder()
                .categoryIds(Set.of(japaneseCategory.getId()))  // 한식 제거, 일식만
                .build();

        RestaurantResponse result = restaurantCommandService.updateRestaurant(
                created.getRestaurantId(), updateRequest, "OWNER_1"
        );

        // then - 일식만 존재해야 함
        assertThat(result.getCategoryNames()).containsExactly("일식");
    }

    @Test
    @DisplayName("식당 카테고리 추가 성공 (기존 카테고리 유지)")
    void updateRestaurant_AddCategory() {
        // given - 한식 카테고리로만 식당 생성
        RestaurantResponse created = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );

        // when - 기존 한식 + 일식 추가
        RestaurantUpdateRequest updateRequest = RestaurantUpdateRequest.builder()
                .categoryIds(Set.of(testCategory.getId(), japaneseCategory.getId()))
                .build();

        RestaurantResponse result = restaurantCommandService.updateRestaurant(
                created.getRestaurantId(), updateRequest, "OWNER_1"
        );

        // then - 한식과 일식 모두 존재
        assertThat(result).isNotNull();
        assertThat(result.getCategoryNames()).hasSize(2);
        assertThat(result.getCategoryNames()).containsExactlyInAnyOrder("한식", "일식");
    }

    @Test
    @DisplayName("식당 카테고리 제거 성공")
    void updateRestaurant_RemoveCategories() {
        // given
        RestaurantResponse created = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );

        RestaurantUpdateRequest updateRequest = RestaurantUpdateRequest.builder()
                .categoryIds(Set.of())
                .build();

        // when
        RestaurantResponse result = restaurantCommandService.updateRestaurant(
                created.getRestaurantId(), updateRequest, "OWNER_1"
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryNames()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 ID로 변경 시 실패")
    void updateRestaurant_InvalidCategoryId() {
        // given
        RestaurantResponse created = restaurantCommandService.createRestaurant(
                OWNER_ID, OWNER_NAME, createRequest
        );

        RestaurantUpdateRequest updateRequest = RestaurantUpdateRequest.builder()
                .categoryIds(Set.of("INVALID-CATEGORY-ID"))
                .build();

        // when & then - 예외 발생 기대
        assertThatThrownBy(() -> restaurantCommandService.updateRestaurant(
                created.getRestaurantId(), updateRequest, "OWNER_1"
        ))
                .isInstanceOf(RestaurantException.class)
                .hasMessageContaining("카테고리를 찾을 수 없습니다");
    }
}