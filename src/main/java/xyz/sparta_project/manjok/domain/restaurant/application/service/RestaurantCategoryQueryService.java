package xyz.sparta_project.manjok.domain.restaurant.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategory;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantCategoryRepository;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.common.dto.CategoryResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Restaurant Category Query Service
 * - 카테고리 조회 전담 서비스
 * - Read-Only 트랜잭션
 * - 계층 구조 처리 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantCategoryQueryService {

    private final RestaurantCategoryRepository categoryRepository;

    /**
     * 1차 카테고리 목록 조회 (최상위 카테고리)
     * - depth = 1인 활성 카테고리만 조회
     * - displayOrder 기준 정렬
     */
    public List<CategoryResponse> getRootCategories() {
        log.info("1차 카테고리 목록 조회");

        List<RestaurantCategory> categories = categoryRepository.findRootCategories();

        return categories.stream()
                .filter(RestaurantCategory::isAvailable)
                .sorted(Comparator.comparing(RestaurantCategory::getDisplayOrder))
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 부모의 하위 카테고리 조회
     * - 특정 depth의 카테고리만 필터링
     * - displayOrder 기준 정렬
     *
     * @param parentCategoryId 부모 카테고리 ID
     * @param expectedDepth 기대하는 depth (2 or 3)
     */
    public List<CategoryResponse> getSubCategoriesByParentId(String parentCategoryId, Integer expectedDepth) {
        log.info("하위 카테고리 조회 - parentId: {}, expectedDepth: {}", parentCategoryId, expectedDepth);

        // 부모 카테고리 존재 확인
        RestaurantCategory parentCategory = categoryRepository.findById(parentCategoryId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.CATEGORY_NOT_FOUND));

        if (!parentCategory.isAvailable()) {
            throw new RestaurantException(RestaurantErrorCode.CATEGORY_NOT_AVAILABLE);
        }

        // 하위 카테고리 조회
        List<RestaurantCategory> subCategories = categoryRepository
                .findByParentCategoryId(parentCategoryId);

        return subCategories.stream()
                .filter(RestaurantCategory::isAvailable)
                .filter(category -> category.getDepth().equals(expectedDepth))
                .sorted(Comparator.comparing(RestaurantCategory::getDisplayOrder))
                .map(category -> toCategoryResponseWithParent(category, parentCategory))
                .collect(Collectors.toList());
    }

    /**
     * 전체 카테고리 계층 구조 조회
     * - 1차 > 2차 > 3차 전체 구조를 트리 형태로 반환
     */
    public List<CategoryResponse> getCategoryHierarchy() {
        log.info("전체 카테고리 계층 구조 조회");

        // 모든 활성 카테고리 조회
        List<RestaurantCategory> allCategories = categoryRepository.findAllActive();

        // depth별로 그룹화
        Map<Integer, List<RestaurantCategory>> categoriesByDepth = allCategories.stream()
                .filter(RestaurantCategory::isAvailable)
                .collect(Collectors.groupingBy(RestaurantCategory::getDepth));

        // 1차 카테고리 (최상위)
        List<RestaurantCategory> rootCategories = categoriesByDepth.getOrDefault(1, List.of());

        // 2차, 3차 카테고리를 부모 ID로 그룹화
        Map<String, List<RestaurantCategory>> subCategoriesByParent = allCategories.stream()
                .filter(category -> category.getParentCategoryId() != null)
                .filter(RestaurantCategory::isAvailable)
                .collect(Collectors.groupingBy(RestaurantCategory::getParentCategoryId));

        // 계층 구조 빌드
        return rootCategories.stream()
                .sorted(Comparator.comparing(RestaurantCategory::getDisplayOrder))
                .map(root -> buildCategoryHierarchy(root, subCategoriesByParent))
                .collect(Collectors.toList());
    }

    /**
     * 인기 카테고리 목록 조회
     * - isPopular = true인 카테고리만 조회
     * - totalOrderCount 내림차순 정렬
     */
    public List<CategoryResponse> getPopularCategories() {
        log.info("인기 카테고리 목록 조회");

        List<RestaurantCategory> popularCategories = categoryRepository.findPopularCategories();

        return popularCategories.stream()
                .filter(RestaurantCategory::isAvailable)
                .sorted(Comparator.comparing(RestaurantCategory::getTotalOrderCount).reversed())
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 카테고리 상세 조회
     *
     * @param categoryId 카테고리 ID
     */
    public CategoryResponse getCategoryById(String categoryId) {
        log.info("카테고리 상세 조회 - categoryId: {}", categoryId);

        RestaurantCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.CATEGORY_NOT_FOUND));

        if (!category.isAvailable()) {
            throw new RestaurantException(RestaurantErrorCode.CATEGORY_NOT_AVAILABLE);
        }

        // 부모 카테고리 정보 조회 (있는 경우)
        RestaurantCategory parentCategory = null;
        if (category.getParentCategoryId() != null) {
            parentCategory = categoryRepository.findById(category.getParentCategoryId())
                    .orElse(null);
        }

        return parentCategory != null
                ? toCategoryResponseWithParent(category, parentCategory)
                : toCategoryResponse(category);
    }

    // ==================== Private Helper 메서드 ====================

    /**
     * 계층 구조를 재귀적으로 빌드
     */
    private CategoryResponse buildCategoryHierarchy(
            RestaurantCategory category,
            Map<String, List<RestaurantCategory>> subCategoriesByParent) {

        CategoryResponse response = toCategoryResponse(category);

        // 하위 카테고리 조회 및 재귀 빌드
        List<RestaurantCategory> children = subCategoriesByParent
                .getOrDefault(category.getId(), List.of());

        if (!children.isEmpty()) {
            List<CategoryResponse> subCategoryResponses = children.stream()
                    .sorted(Comparator.comparing(RestaurantCategory::getDisplayOrder))
                    .map(child -> buildCategoryHierarchy(child, subCategoriesByParent))
                    .collect(Collectors.toList());

            return CategoryResponse.builder()
                    .id(response.getId())
                    .categoryCode(response.getCategoryCode())
                    .categoryName(response.getCategoryName())
                    .description(response.getDescription())
                    .iconUrl(response.getIconUrl())
                    .colorCode(response.getColorCode())
                    .parentCategoryId(response.getParentCategoryId())
                    .parentCategoryName(response.getParentCategoryName())
                    .depth(response.getDepth())
                    .displayOrder(response.getDisplayOrder())
                    .isActive(response.getIsActive())
                    .isPopular(response.getIsPopular())
                    .isNew(response.getIsNew())
                    .activeRestaurantCount(response.getActiveRestaurantCount())
                    .totalOrderCount(response.getTotalOrderCount())
                    .defaultMinimumOrderAmount(response.getDefaultMinimumOrderAmount())
                    .averageDeliveryTime(response.getAverageDeliveryTime())
                    .subCategories(subCategoryResponses)
                    .build();
        }

        return response;
    }

    /**
     * RestaurantCategory -> CategoryResponse 변환
     */
    private CategoryResponse toCategoryResponse(RestaurantCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryCode(category.getCategoryCode())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .colorCode(category.getColorCode())
                .parentCategoryId(category.getParentCategoryId())
                .depth(category.getDepth())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .isPopular(category.getIsPopular())
                .isNew(category.getIsNew())
                .activeRestaurantCount(category.getActiveRestaurantCount())
                .totalOrderCount(category.getTotalOrderCount())
                .defaultMinimumOrderAmount(category.getDefaultMinimumOrderAmount())
                .averageDeliveryTime(category.getAverageDeliveryTime())
                .build();
    }

    /**
     * RestaurantCategory -> CategoryResponse 변환 (부모 정보 포함)
     */
    private CategoryResponse toCategoryResponseWithParent(
            RestaurantCategory category,
            RestaurantCategory parentCategory) {

        return CategoryResponse.builder()
                .id(category.getId())
                .categoryCode(category.getCategoryCode())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .colorCode(category.getColorCode())
                .parentCategoryId(category.getParentCategoryId())
                .parentCategoryName(parentCategory != null ? parentCategory.getCategoryName() : null)
                .depth(category.getDepth())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .isPopular(category.getIsPopular())
                .isNew(category.getIsNew())
                .activeRestaurantCount(category.getActiveRestaurantCount())
                .totalOrderCount(category.getTotalOrderCount())
                .defaultMinimumOrderAmount(category.getDefaultMinimumOrderAmount())
                .averageDeliveryTime(category.getAverageDeliveryTime())
                .build();
    }
}