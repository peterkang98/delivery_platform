package xyz.sparta_project.manjok.domain.restaurant.application.service;

import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminMenuResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.MenuDetailResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.customer.dto.response.MenuSummaryResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.MenuResponse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Menu Mapper
 * - Menu 도메인 모델과 DTO 간 변환
 * - 권한별 DTO 분리 (Customer, Owner, Admin)
 */
@Component
public class MenuMapper {

    // ==================== Customer DTO 변환 ====================

    /**
     * Menu -> MenuSummaryResponse (목록 조회용)
     *
     * @param menu Menu 엔티티
     * @param restaurant Menu가 속한 Restaurant (카테고리명 조회용)
     */
    public MenuSummaryResponse toMenuSummaryResponse(Menu menu, Restaurant restaurant) {
        return MenuSummaryResponse.builder()
                .menuId(menu.getId())
                .menuName(menu.getMenuName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .primaryCategoryName(getPrimaryCategoryName(menu, restaurant))
                .isAvailable(menu.getIsAvailable())
                .isMain(menu.getIsMain())
                .isPopular(menu.getIsPopular())
                .isNew(menu.getIsNew())
                .calorie(menu.getCalorie())
                .purchaseCount(menu.getPurchaseCount())
                .wishlistCount(menu.getWishlistCount())
                .reviewCount(menu.getReviewCount())
                .reviewRating(menu.getReviewRating())
                .hasOptions(!menu.getOptionGroups().isEmpty())
                .hasRequiredOptions(menu.hasRequiredOptions())
                .build();
    }

    /**
     * Menu -> MenuDetailResponse (상세 조회용)
     *
     * @param menu Menu 엔티티
     * @param restaurant Menu가 속한 Restaurant (카테고리명 조회용)
     */
    public MenuDetailResponse toMenuDetailResponse(Menu menu, Restaurant restaurant) {
        return MenuDetailResponse.builder()
                .menuId(menu.getId())
                .restaurantId(menu.getRestaurantId())
                .menuName(menu.getMenuName())
                .description(menu.getDescription())
                .ingredients(menu.getIngredients())
                .price(menu.getPrice())
                .categoryNames(getCategoryNames(menu, restaurant))
                .primaryCategoryName(getPrimaryCategoryName(menu, restaurant))
                .isAvailable(menu.getIsAvailable())
                .isMain(menu.getIsMain())
                .isPopular(menu.getIsPopular())
                .isNew(menu.getIsNew())
                .calorie(menu.getCalorie())
                .purchaseCount(menu.getPurchaseCount())
                .wishlistCount(menu.getWishlistCount())
                .reviewCount(menu.getReviewCount())
                .reviewRating(menu.getReviewRating())
                .optionGroups(toMenuOptionGroupDtoList(menu.getOptionGroups()))
                .createdAt(menu.getCreatedAt())
                .build();
    }

    /**
     * MenuOptionGroup List -> MenuOptionGroupDto List (Customer용)
     */
    private List<MenuDetailResponse.MenuOptionGroupDto> toMenuOptionGroupDtoList(
            List<MenuOptionGroup> optionGroups) {
        return optionGroups.stream()
                .filter(MenuOptionGroup::isAvailable)
                .map(group -> MenuDetailResponse.MenuOptionGroupDto.builder()
                        .optionGroupId(group.getId())
                        .groupName(group.getGroupName())
                        .description(group.getDescription())
                        .isRequired(group.getIsRequired())
                        .minSelection(group.getMinSelection())
                        .maxSelection(group.getMaxSelection())
                        .displayOrder(group.getDisplayOrder())
                        .options(toMenuOptionDtoList(group.getOptions()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * MenuOption List -> MenuOptionDto List (Customer용)
     */
    private List<MenuDetailResponse.MenuOptionDto> toMenuOptionDtoList(List<MenuOption> options) {
        return options.stream()
                .filter(MenuOption::isSelectable)
                .map(option -> MenuDetailResponse.MenuOptionDto.builder()
                        .optionId(option.getId())
                        .optionName(option.getOptionName())
                        .description(option.getDescription())
                        .additionalPrice(option.getAdditionalPrice())
                        .isAvailable(option.getIsAvailable())
                        .isDefault(option.getIsDefault())
                        .displayOrder(option.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== Owner DTO 변환 ====================

    /**
     * Menu -> MenuResponse (Owner용)
     *
     * @param menu Menu 엔티티
     * @param restaurant Menu가 속한 Restaurant (카테고리명 조회용)
     */
    public MenuResponse toMenuResponse(Menu menu, Restaurant restaurant) {
        return MenuResponse.builder()
                .menuId(menu.getId())
                .restaurantId(menu.getRestaurantId())
                .menuName(menu.getMenuName())
                .description(menu.getDescription())
                .ingredients(menu.getIngredients())
                .price(menu.getPrice())
                .categoryNames(getCategoryNames(menu, restaurant))
                .primaryCategoryName(getPrimaryCategoryName(menu, restaurant))
                .isAvailable(menu.getIsAvailable())
                .isMain(menu.getIsMain())
                .isPopular(menu.getIsPopular())
                .isNew(menu.getIsNew())
                .calorie(menu.getCalorie())
                .purchaseCount(menu.getPurchaseCount())
                .wishlistCount(menu.getWishlistCount())
                .reviewCount(menu.getReviewCount())
                .reviewRating(menu.getReviewRating())
                .optionGroups(toOwnerMenuOptionGroupDtoList(menu.getOptionGroups()))
                .createdAt(menu.getCreatedAt())
                .createdBy(menu.getCreatedBy())
                .updatedAt(menu.getUpdatedAt())
                .updatedBy(menu.getUpdatedBy())
                .isDeleted(menu.getIsDeleted())
                .deletedAt(menu.getDeletedAt())
                .deletedBy(menu.getDeletedBy())
                .build();
    }

    /**
     * MenuOptionGroup List -> MenuOptionGroupDto List (Owner용)
     */
    private List<MenuResponse.MenuOptionGroupDto> toOwnerMenuOptionGroupDtoList(
            List<MenuOptionGroup> optionGroups) {
        return optionGroups.stream()
                .map(group -> MenuResponse.MenuOptionGroupDto.builder()
                        .optionGroupId(group.getId())
                        .groupName(group.getGroupName())
                        .description(group.getDescription())
                        .isRequired(group.getIsRequired())
                        .minSelection(group.getMinSelection())
                        .maxSelection(group.getMaxSelection())
                        .displayOrder(group.getDisplayOrder())
                        .isActive(group.getIsActive())
                        .options(toOwnerMenuOptionDtoList(group.getOptions()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * MenuOption List -> MenuOptionDto List (Owner용)
     */
    private List<MenuResponse.MenuOptionDto> toOwnerMenuOptionDtoList(List<MenuOption> options) {
        return options.stream()
                .map(option -> MenuResponse.MenuOptionDto.builder()
                        .optionId(option.getId())
                        .optionName(option.getOptionName())
                        .description(option.getDescription())
                        .additionalPrice(option.getAdditionalPrice())
                        .isAvailable(option.getIsAvailable())
                        .isDefault(option.getIsDefault())
                        .displayOrder(option.getDisplayOrder())
                        .purchaseCount(option.getPurchaseCount())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== Admin DTO 변환 ====================

    /**
     * Menu -> AdminMenuResponse (Admin용)
     *
     * @param menu Menu 엔티티
     * @param restaurant Menu가 속한 Restaurant (카테고리명 조회용)
     */
    public AdminMenuResponse toAdminMenuResponse(Menu menu, Restaurant restaurant) {
        return AdminMenuResponse.builder()
                .menuId(menu.getId())
                .restaurantId(menu.getRestaurantId())
                .restaurantName(restaurant.getRestaurantName())
                .menuName(menu.getMenuName())
                .description(menu.getDescription())
                .ingredients(menu.getIngredients())
                .price(menu.getPrice())
                .categoryNames(getCategoryNames(menu, restaurant))
                .primaryCategoryName(getPrimaryCategoryName(menu, restaurant))
                .isAvailable(menu.getIsAvailable())
                .isMain(menu.getIsMain())
                .isPopular(menu.getIsPopular())
                .isNew(menu.getIsNew())
                .calorie(menu.getCalorie())
                .purchaseCount(menu.getPurchaseCount())
                .wishlistCount(menu.getWishlistCount())
                .reviewCount(menu.getReviewCount())
                .reviewRating(menu.getReviewRating())
                .optionGroups(toAdminMenuOptionGroupDtoList(menu.getOptionGroups()))
                .createdAt(menu.getCreatedAt())
                .createdBy(menu.getCreatedBy())
                .updatedAt(menu.getUpdatedAt())
                .updatedBy(menu.getUpdatedBy())
                .isDeleted(menu.getIsDeleted())
                .deletedAt(menu.getDeletedAt())
                .deletedBy(menu.getDeletedBy())
                .build();
    }

    /**
     * MenuOptionGroup List -> MenuOptionGroupDto List (Admin용)
     */
    private List<AdminMenuResponse.MenuOptionGroupDto> toAdminMenuOptionGroupDtoList(
            List<MenuOptionGroup> optionGroups) {
        return optionGroups.stream()
                .map(group -> AdminMenuResponse.MenuOptionGroupDto.builder()
                        .optionGroupId(group.getId())
                        .groupName(group.getGroupName())
                        .description(group.getDescription())
                        .isRequired(group.getIsRequired())
                        .minSelection(group.getMinSelection())
                        .maxSelection(group.getMaxSelection())
                        .displayOrder(group.getDisplayOrder())
                        .isActive(group.getIsActive())
                        .options(toAdminMenuOptionDtoList(group.getOptions()))
                        .createdAt(group.getCreatedAt())
                        .createdBy(group.getCreatedBy())
                        .isDeleted(group.getIsDeleted())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * MenuOption List -> MenuOptionDto List (Admin용)
     */
    private List<AdminMenuResponse.MenuOptionDto> toAdminMenuOptionDtoList(List<MenuOption> options) {
        return options.stream()
                .map(option -> AdminMenuResponse.MenuOptionDto.builder()
                        .optionId(option.getId())
                        .optionName(option.getOptionName())
                        .description(option.getDescription())
                        .additionalPrice(option.getAdditionalPrice())
                        .isAvailable(option.getIsAvailable())
                        .isDefault(option.getIsDefault())
                        .displayOrder(option.getDisplayOrder())
                        .purchaseCount(option.getPurchaseCount())
                        .createdAt(option.getCreatedAt())
                        .createdBy(option.getCreatedBy())
                        .isDeleted(option.getIsDeleted())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== 공통 Helper 메서드 ====================

    /**
     * 주 카테고리명 추출
     * Restaurant의 MenuCategory 목록에서 카테고리명 조회
     *
     * @param menu Menu 엔티티
     * @param restaurant Menu가 속한 Restaurant
     * @return 주 카테고리명
     */
    private String getPrimaryCategoryName(Menu menu, Restaurant restaurant) {
        String primaryCategoryId = menu.getPrimaryCategoryId();
        if (primaryCategoryId == null) {
            return null;
        }

        return restaurant.getMenuCategories().stream()
                .filter(category -> category.getId().equals(primaryCategoryId))
                .findFirst()
                .map(MenuCategory::getCategoryName)
                .orElse(null);
    }

    /**
     * 전체 카테고리명 추출
     * Restaurant의 MenuCategory 목록에서 카테고리명 조회
     *
     * @param menu Menu 엔티티
     * @param restaurant Menu가 속한 Restaurant
     * @return 카테고리명 목록
     */
    private List<String> getCategoryNames(Menu menu, Restaurant restaurant) {
        Set<String> categoryIds = menu.getActiveCategoryIds();

        return restaurant.getMenuCategories().stream()
                .filter(category -> categoryIds.contains(category.getId()))
                .map(MenuCategory::getCategoryName)
                .collect(Collectors.toList());
    }
}