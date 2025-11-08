package xyz.sparta_project.manjok.domain.restaurant.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RestaurantCategoryRepository 통합 테스트")
class RestaurantCategoryRepositoryTest {

    @Autowired
    private RestaurantCategoryRepository restaurantCategoryRepository;

    private RestaurantCategory koreanCategory;
    private RestaurantCategory japaneseCategory;
    private RestaurantCategory chineseCategory;

    @BeforeEach
    void setUp() {
        // 1. 한식 카테고리 (최상위, 활성화, 인기)
        koreanCategory = RestaurantCategory.builder()
                .categoryCode("KOREAN")
                .categoryName("한식")
                .description("한국 전통 음식")
                .parentCategoryId(null)
                .depth(1)
                .displayOrder(1)
                .isActive(true)
                .isPopular(true)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();
        koreanCategory = restaurantCategoryRepository.save(koreanCategory);

        // 2. 일식 카테고리 (최상위, 활성화)
        japaneseCategory = RestaurantCategory.builder()
                .categoryCode("JAPANESE")
                .categoryName("일식")
                .description("일본 음식")
                .parentCategoryId(null)
                .depth(1)
                .displayOrder(2)
                .isActive(true)
                .isPopular(false)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();
        japaneseCategory = restaurantCategoryRepository.save(japaneseCategory);

        // 3. 중식 카테고리 (최상위, 비활성화)
        chineseCategory = RestaurantCategory.builder()
                .categoryCode("CHINESE")
                .categoryName("중식")
                .description("중국 음식")
                .parentCategoryId(null)
                .depth(1)
                .displayOrder(3)
                .isActive(false)
                .isPopular(false)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();
        chineseCategory = restaurantCategoryRepository.save(chineseCategory);
    }

    // ==================== CREATE & UPDATE 테스트 ====================

    @Test
    @DisplayName("카테고리 저장 성공")
    void save_Success() {
        // given
        RestaurantCategory newCategory = RestaurantCategory.builder()
                .categoryCode("WESTERN")
                .categoryName("양식")
                .description("서양 음식")
                .parentCategoryId(null)
                .depth(1)
                .displayOrder(4)
                .isActive(true)
                .isPopular(false)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();

        // when
        RestaurantCategory saved = restaurantCategoryRepository.save(newCategory);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCategoryName()).isEqualTo("양식");
        assertThat(saved.getCategoryCode()).isEqualTo("WESTERN");
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void update_Success() {
        // given
        String categoryId = koreanCategory.getId();

        // 기존 카테고리 조회
        RestaurantCategory existing = restaurantCategoryRepository.findById(categoryId)
                .orElseThrow();

        // 수정된 카테고리 생성
        RestaurantCategory updated = RestaurantCategory.builder()
                .id(existing.getId())
                .categoryCode(existing.getCategoryCode())
                .categoryName("한식 (수정됨)")  // 이름 변경
                .description("한국 전통 음식 (수정)")  // 설명 변경
                .parentCategoryId(existing.getParentCategoryId())
                .depth(existing.getDepth())
                .displayOrder(10)  // 순서 변경
                .isActive(false)  // 비활성화
                .isPopular(existing.getIsPopular())
                .createdAt(existing.getCreatedAt())
                .createdBy(existing.getCreatedBy())
                .updatedAt(LocalDateTime.now())
                .updatedBy("UPDATER")
                .isDeleted(false)
                .restaurantRelations(existing.getRestaurantRelations())
                .build();

        // when
        RestaurantCategory result = restaurantCategoryRepository.save(updated);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getCategoryName()).isEqualTo("한식 (수정됨)");
        assertThat(result.getDescription()).isEqualTo("한국 전통 음식 (수정)");
        assertThat(result.getDisplayOrder()).isEqualTo(10);
        assertThat(result.getIsActive()).isFalse();
    }

    // ==================== READ 테스트 ====================

    @Test
    @DisplayName("ID로 카테고리 조회 성공")
    void findById_Success() {
        // given
        String categoryId = koreanCategory.getId();

        // when
        Optional<RestaurantCategory> result = restaurantCategoryRepository.findById(categoryId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCategoryName()).isEqualTo("한식");
        assertThat(result.get().getCategoryCode()).isEqualTo("KOREAN");
    }

    @Test
    @DisplayName("ID로 카테고리 조회 실패 - 존재하지 않음")
    void findById_NotFound() {
        // given
        String invalidId = "INVALID-ID";

        // when
        Optional<RestaurantCategory> result = restaurantCategoryRepository.findById(invalidId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 ID로 카테고리 일괄 조회 성공")
    void findAllByIds_Success() {
        // given
        List<String> categoryIds = List.of(
                koreanCategory.getId(),
                japaneseCategory.getId()
        );

        // when
        List<RestaurantCategory> result = restaurantCategoryRepository.findAllByIds(categoryIds);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RestaurantCategory::getCategoryName)
                .containsExactlyInAnyOrder("한식", "일식");
    }

    @Test
    @DisplayName("여러 ID로 카테고리 일괄 조회 - 빈 리스트 입력")
    void findAllByIds_EmptyList() {
        // when
        List<RestaurantCategory> result = restaurantCategoryRepository.findAllByIds(List.of());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 ID로 카테고리 일괄 조회 - 존재하지 않는 ID 포함")
    void findAllByIds_WithInvalidId() {
        // given
        List<String> categoryIds = List.of(
                koreanCategory.getId(),
                "INVALID-ID",
                japaneseCategory.getId()
        );

        // when
        List<RestaurantCategory> result = restaurantCategoryRepository.findAllByIds(categoryIds);

        // then
        assertThat(result).hasSize(2);  // 유효한 ID만 조회됨
        assertThat(result).extracting(RestaurantCategory::getCategoryName)
                .containsExactlyInAnyOrder("한식", "일식");
    }

    @Test
    @DisplayName("카테고리 코드로 조회 성공")
    void findByCategoryCode_Success() {
        // when
        Optional<RestaurantCategory> result = restaurantCategoryRepository.findByCategoryCode("KOREAN");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCategoryName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("카테고리 코드로 조회 실패 - 존재하지 않음")
    void findByCategoryCode_NotFound() {
        // when
        Optional<RestaurantCategory> result = restaurantCategoryRepository.findByCategoryCode("INVALID");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("활성화된 카테고리 전체 조회")
    void findAllActive_Success() {
        // when
        List<RestaurantCategory> result = restaurantCategoryRepository.findAllActive();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(RestaurantCategory::getIsActive)
                .containsOnly(true);
        assertThat(result).extracting(RestaurantCategory::getCategoryName)
                .containsExactlyInAnyOrder("한식", "일식");
        assertThat(result).extracting(RestaurantCategory::getCategoryName)
                .doesNotContain("중식");  // 비활성화된 카테고리는 제외
    }

    @Test
    @DisplayName("최상위 카테고리 조회")
    void findRootCategories_Success() {
        // when
        List<RestaurantCategory> result = restaurantCategoryRepository.findRootCategories();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(category -> category.getDepth() == 1);
        assertThat(result).allMatch(RestaurantCategory::getIsActive);
    }

    @Test
    @DisplayName("하위 카테고리 조회")
    void findByParentCategoryId_Success() {
        // given
        String parentId = koreanCategory.getId();

        // 하위 카테고리 생성 (한식 > 찌개류)
        RestaurantCategory subCategory = RestaurantCategory.builder()
                .categoryCode("KOREAN_STEW")
                .categoryName("찌개류")
                .description("한식 찌개 종류")
                .parentCategoryId(parentId)
                .depth(2)
                .displayOrder(1)
                .isActive(true)
                .isPopular(false)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();
        restaurantCategoryRepository.save(subCategory);

        // when
        List<RestaurantCategory> result = restaurantCategoryRepository.findByParentCategoryId(parentId);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(category -> category.getParentCategoryId().equals(parentId));
        assertThat(result.get(0).getCategoryName()).isEqualTo("찌개류");
    }

    @Test
    @DisplayName("인기 카테고리 조회")
    void findPopularCategories_Success() {
        // when
        List<RestaurantCategory> result = restaurantCategoryRepository.findPopularCategories();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(RestaurantCategory::getIsPopular);
        assertThat(result).extracting(RestaurantCategory::getCategoryName)
                .contains("한식");
    }

    // ==================== DELETE 테스트 ====================

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteById_Success() {
        // given
        String categoryId = japaneseCategory.getId();

        // when
        restaurantCategoryRepository.deleteById(categoryId);

        // then
        Optional<RestaurantCategory> result = restaurantCategoryRepository.findById(categoryId);
        assertThat(result).isEmpty();
    }

    // ==================== EXISTS 테스트 ====================

    @Test
    @DisplayName("카테고리 존재 여부 확인 - 존재함")
    void existsById_True() {
        // given
        String categoryId = koreanCategory.getId();

        // when
        boolean result = restaurantCategoryRepository.existsById(categoryId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("카테고리 존재 여부 확인 - 존재하지 않음")
    void existsById_False() {
        // given
        String invalidId = "INVALID-ID";

        // when
        boolean result = restaurantCategoryRepository.existsById(invalidId);

        // then
        assertThat(result).isFalse();
    }

    // ==================== Edge Case 테스트 ====================

    @Test
    @DisplayName("비활성화된 카테고리는 findById로 조회 가능")
    void findById_InactiveCategory() {
        // given
        String categoryId = chineseCategory.getId();

        // when
        Optional<RestaurantCategory> result = restaurantCategoryRepository.findById(categoryId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getIsActive()).isFalse();
    }

    @Test
    @DisplayName("displayOrder 순서대로 정렬되어 조회")
    void findAllActive_OrderByDisplayOrder() {
        // when
        List<RestaurantCategory> result = restaurantCategoryRepository.findAllActive();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getDisplayOrder()).isLessThan(result.get(1).getDisplayOrder());
    }

    @Test
    @DisplayName("계층 구조 테스트 - 부모-자식 관계")
    void hierarchyTest() {
        // given
        String parentId = koreanCategory.getId();

        // 자식 카테고리들 생성
        RestaurantCategory child1 = RestaurantCategory.builder()
                .categoryCode("KOREAN_SOUP")
                .categoryName("국/탕류")
                .parentCategoryId(parentId)
                .depth(2)
                .displayOrder(1)
                .isActive(true)
                .isPopular(false)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();

        RestaurantCategory child2 = RestaurantCategory.builder()
                .categoryCode("KOREAN_BBQ")
                .categoryName("구이류")
                .parentCategoryId(parentId)
                .depth(2)
                .displayOrder(2)
                .isActive(true)
                .isPopular(false)
                .createdAt(LocalDateTime.now())
                .createdBy("TEST")
                .isDeleted(false)
                .restaurantRelations(new HashSet<>())
                .build();

        restaurantCategoryRepository.save(child1);
        restaurantCategoryRepository.save(child2);

        // when
        List<RestaurantCategory> children = restaurantCategoryRepository.findByParentCategoryId(parentId);

        // then
        assertThat(children).hasSize(2);
        assertThat(children).extracting(RestaurantCategory::getCategoryName)
                .containsExactly("국/탕류", "구이류");  // displayOrder 순서
        assertThat(children).allMatch(cat -> cat.getDepth() == 2);
        assertThat(children).allMatch(cat -> cat.getParentCategoryId().equals(parentId));
    }
}