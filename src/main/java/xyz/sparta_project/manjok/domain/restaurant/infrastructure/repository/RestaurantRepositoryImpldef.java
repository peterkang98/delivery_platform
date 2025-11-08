//package xyz.sparta_project.manjok.domain.restaurant.infrastructure.repository;
//
//import com.querydsl.core.BooleanBuilder;
//import com.querydsl.core.Tuple;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
//import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
//import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
//import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
//import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
//import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.*;
//import xyz.sparta_project.manjok.domain.restaurant.infrastructure.jpa.*;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QMenuCategoryEntity.menuCategoryEntity;
//import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QMenuCategoryRelationEntity.menuCategoryRelationEntity;
//import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QMenuEntity.menuEntity;
//import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QMenuOptionEntity.menuOptionEntity;
//import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QMenuOptionGroupEntity.menuOptionGroupEntity;
//import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QOperatingDayEntity.operatingDayEntity;
//import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QRestaurantCategoryRelationEntity.restaurantCategoryRelationEntity;
//import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QRestaurantEntity.restaurantEntity;
//
///**
// * Restaurant Repository 구현체
// * - QueryDSL Tuple + Join 방식으로 N+1 문제 완전 해결
// * - 한 번의 쿼리로 모든 연관 데이터 조회 후 메모리에서 Aggregate 재구성
// * - 모든 CUD 로직 포함
// */
//@Repository
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class RestaurantRepositoryImpldef implements RestaurantRepository {
//
//    private final RestaurantJpaRepository restaurantJpaRepository;
//    private final MenuJpaRepository menuJpaRepository;
//    private final MenuCategoryJpaRepository menuCategoryJpaRepository;
//    private final MenuCategoryRelationJpaRepository menuCategoryRelationJpaRepository;
//    private final MenuOptionGroupJpaRepository menuOptionGroupJpaRepository;
//    private final MenuOptionJpaRepository menuOptionJpaRepository;
//    private final OperatingDayJpaRepository operatingDayJpaRepository;
//    private final RestaurantCategoryRelationJpaRepository restaurantCategoryRelationJpaRepository;
//    private final JPAQueryFactory queryFactory;
//
//    // ==================== Restaurant CUD ====================
//
//    @Override
//    @Transactional
//    public Restaurant save(Restaurant restaurant) {
//        // 1. Restaurant 저장
//        RestaurantEntity restaurantEntity = RestaurantEntity.fromDomain(restaurant);
//        RestaurantEntity savedRestaurant = restaurantJpaRepository.save(restaurantEntity);
//
//        String restaurantId = savedRestaurant.getId();
//
//        // 2. OperatingDays 저장 (운영 시간)
//        if (restaurant.getOperatingDays() != null && !restaurant.getOperatingDays().isEmpty()) {
//            List<OperatingDayEntity> operatingDayEntities = restaurant.getOperatingDays().stream()
//                    .map(OperatingDayEntity::fromDomain)
//                    .collect(Collectors.toList());
//            operatingDayJpaRepository.saveAll(operatingDayEntities);
//        }
//
//        // 3. RestaurantCategoryRelations 저장 (레스토랑-카테고리 관계)
//        if (restaurant.getCategoryRelations() != null && !restaurant.getCategoryRelations().isEmpty()) {
//            List<RestaurantCategoryRelationEntity> categoryRelationEntities =
//                    restaurant.getCategoryRelations().stream()
//                            .map(RestaurantCategoryRelationEntity::fromDomain)
//                            .collect(Collectors.toList());
//            restaurantCategoryRelationJpaRepository.saveAll(categoryRelationEntities);
//        }
//
//        // 4. MenuCategories 저장 (메뉴 카테고리)
//        if (restaurant.getMenuCategories() != null && !restaurant.getMenuCategories().isEmpty()) {
//            List<MenuCategoryEntity> menuCategoryEntities = restaurant.getMenuCategories().stream()
//                    .map(MenuCategoryEntity::fromDomain)
//                    .collect(Collectors.toList());
//            menuCategoryJpaRepository.saveAll(menuCategoryEntities);
//        }
//
//        // 5. Menus 저장 (메뉴 + 옵션 그룹 + 옵션 + 카테고리 관계)
//        if (restaurant.getMenus() != null && !restaurant.getMenus().isEmpty()) {
//            for (Menu menu : restaurant.getMenus()) {
//                saveMenuWithRelations(menu, restaurantId);
//            }
//        }
//
//        // 6. 전체 데이터 다시 조회하여 반환
//        return findByIdWithAll(restaurantId).orElse(savedRestaurant.toDomain());
//    }
//
//    @Override
//    @Transactional
//    public void updateRestaurant(Restaurant restaurant) {
//        // 1. Restaurant 존재 확인
//        RestaurantEntity existing = restaurantJpaRepository.findById(restaurant.getId())
//                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));
//
//        // 2. Restaurant 업데이트 (더티 체킹)
//        RestaurantEntity updatedEntity = RestaurantEntity.fromDomain(restaurant);
//        restaurantJpaRepository.save(updatedEntity);
//
//        // 3. OperatingDays 업데이트
//        if (restaurant.getOperatingDays() != null) {
//            // 기존 데이터 삭제 후 재저장
//            operatingDayJpaRepository.deleteAll(
//                    operatingDayJpaRepository.findAll().stream()
//                            .filter(od -> od.getRestaurantId().equals(restaurant.getId()))
//                            .collect(Collectors.toList())
//            );
//
//            List<OperatingDayEntity> operatingDayEntities = restaurant.getOperatingDays().stream()
//                    .map(OperatingDayEntity::fromDomain)
//                    .collect(Collectors.toList());
//            operatingDayJpaRepository.saveAll(operatingDayEntities);
//        }
//
//        // 4. RestaurantCategoryRelations 업데이트
//        if (restaurant.getCategoryRelations() != null) {
//            // 기존 활성 관계 조회
//            List<RestaurantCategoryRelationEntity> existingRelations =
//                    restaurantCategoryRelationJpaRepository.findAll().stream()
//                            .filter(r -> r.getRestaurantId().equals(restaurant.getId()))
//                            .collect(Collectors.toList());
//
//            // 소프트 삭제
//            existingRelations.forEach(r -> {
//                RestaurantCategoryRelation domain = r.toDomain();
//                domain.delete("SYSTEM");
//                restaurantCategoryRelationJpaRepository.save(
//                        RestaurantCategoryRelationEntity.fromDomain(domain));
//            });
//
//            // 새로운 관계 저장
//            List<RestaurantCategoryRelationEntity> newRelations =
//                    restaurant.getCategoryRelations().stream()
//                            .map(RestaurantCategoryRelationEntity::fromDomain)
//                            .collect(Collectors.toList());
//            restaurantCategoryRelationJpaRepository.saveAll(newRelations);
//        }
//    }
//
//    // ==================== OperatingDay CUD ====================
//
//    @Override
//    @Transactional
//    public OperatingDay saveOperatingDay(OperatingDay operatingDay) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(operatingDay.getRestaurantId())) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. OperatingDay 저장
//        OperatingDayEntity entity = OperatingDayEntity.fromDomain(operatingDay);
//        OperatingDayEntity saved = operatingDayJpaRepository.save(entity);
//
//        return saved.toDomain();
//    }
//
//    @Override
//    @Transactional
//    public Set<OperatingDay> saveAllOperatingDays(Set<OperatingDay> operatingDays) {
//        if (operatingDays == null || operatingDays.isEmpty()) {
//            return new HashSet<>();
//        }
//
//        // 1. Restaurant 존재 확인 (첫 번째 요소의 restaurantId로 확인)
//        String restaurantId = operatingDays.iterator().next().getRestaurantId();
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. 모든 OperatingDay 저장
//        List<OperatingDayEntity> entities = operatingDays.stream()
//                .map(OperatingDayEntity::fromDomain)
//                .collect(Collectors.toList());
//
//        List<OperatingDayEntity> saved = operatingDayJpaRepository.saveAll(entities);
//
//        return saved.stream()
//                .map(OperatingDayEntity::toDomain)
//                .collect(Collectors.toSet());
//    }
//
//    @Override
//    @Transactional
//    public void updateOperatingDay(String restaurantId, OperatingDay operatingDay) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. RestaurantId 일치 확인
//        if (!operatingDay.getRestaurantId().equals(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 3. OperatingDay 업데이트 (merge 형태로 동작)
//        OperatingDayEntity entity = OperatingDayEntity.fromDomain(operatingDay);
//        operatingDayJpaRepository.save(entity);
//    }
//
//    @Override
//    @Transactional
//    public Set<OperatingDay> replaceAllOperatingDays(String restaurantId, Set<OperatingDay> operatingDays) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. 기존 운영 시간 모두 삭제
//        List<OperatingDayEntity> existingOperatingDays =
//                operatingDayJpaRepository.findAll().stream()
//                        .filter(od -> od.getRestaurantId().equals(restaurantId))
//                        .collect(Collectors.toList());
//
//        if (!existingOperatingDays.isEmpty()) {
//            operatingDayJpaRepository.deleteAll(existingOperatingDays);
//        }
//
//        // 3. 새로운 운영 시간 저장
//        if (operatingDays != null && !operatingDays.isEmpty()) {
//            List<OperatingDayEntity> entities = operatingDays.stream()
//                    .map(OperatingDayEntity::fromDomain)
//                    .collect(Collectors.toList());
//
//            List<OperatingDayEntity> saved = operatingDayJpaRepository.saveAll(entities);
//
//            return saved.stream()
//                    .map(OperatingDayEntity::toDomain)
//                    .collect(Collectors.toSet());
//        }
//
//        return new HashSet<>();
//    }
//
//    @Override
//    @Transactional
//    public void deleteOperatingDay(String restaurantId, DayType dayType, OperatingTimeType timeType) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. OperatingDay 조회 및 삭제
//        OperatingDayEntity.OperatingDayId id =
//                new OperatingDayEntity.OperatingDayId(restaurantId, dayType, timeType);
//
//        operatingDayJpaRepository.findById(id).ifPresent(operatingDayJpaRepository::delete);
//    }
//
//    @Override
//    @Transactional
//    public void deleteAllOperatingDays(String restaurantId) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. 해당 레스토랑의 모든 운영 시간 삭제
//        List<OperatingDayEntity> operatingDays =
//                operatingDayJpaRepository.findAll().stream()
//                        .filter(od -> od.getRestaurantId().equals(restaurantId))
//                        .collect(Collectors.toList());
//
//        if (!operatingDays.isEmpty()) {
//            operatingDayJpaRepository.deleteAll(operatingDays);
//        }
//    }
//
//
//
//    // ==================== Menu CUD ====================
//
//    @Override
//    @Transactional
//    public Menu saveMenu(Menu menu) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(menu.getRestaurantId())) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. 레스토랑 ID 설정
//        String restaurantId = menu.getRestaurantId();
//
//        // 3. Menu와 모든 연관 관계 저장
//        return saveMenuWithRelations(menu, restaurantId);
//    }
//
//    @Override
//    @Transactional
//    public void updateMenu(String restaurantId, Menu menu) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. Menu 존재 및 소유권 확인
//        MenuEntity existingMenu = menuJpaRepository.findById(menu.getId())
//                .orElseThrow(() -> new RestaurantException(MenuErrorCode.MENU_NOT_FOUND));
//
//        if (!existingMenu.getRestaurantId().equals(restaurantId)) {
//            throw new RestaurantException(MenuErrorCode.MENU_NOT_BELONG_TO_RESTAURANT);
//        }
//
//        // 3. Menu 업데이트
//        MenuEntity updatedEntity = MenuEntity.fromDomain(menu);
//        menuJpaRepository.save(updatedEntity);
//
//        // 4. CategoryRelations 업데이트
//        if (menu.getCategoryRelations() != null) {
//            // 기존 관계 소프트 삭제
//            List<MenuCategoryRelationEntity> existingRelations =
//                    menuCategoryRelationJpaRepository.findAll().stream()
//                            .filter(r -> r.getMenuId().equals(menu.getId()))
//                            .collect(Collectors.toList());
//
//            existingRelations.forEach(r -> {
//                MenuCategoryRelation domain = r.toDomain();
//                domain.delete("SYSTEM");
//                menuCategoryRelationJpaRepository.save(
//                        MenuCategoryRelationEntity.fromDomain(domain));
//            });
//
//            // 새로운 관계 저장
//            List<MenuCategoryRelationEntity> newRelations =
//                    menu.getCategoryRelations().stream()
//                            .map(MenuCategoryRelationEntity::fromDomain)
//                            .collect(Collectors.toList());
//            menuCategoryRelationJpaRepository.saveAll(newRelations);
//        }
//
//        // 5. OptionGroups 업데이트
//        if (menu.getOptionGroups() != null) {
//            for (MenuOptionGroup optionGroup : menu.getOptionGroups()) {
//                if (optionGroup.getId() != null) {
//                    // 기존 그룹 업데이트
//                    MenuOptionGroupEntity groupEntity = MenuOptionGroupEntity.fromDomain(optionGroup);
//                    menuOptionGroupJpaRepository.save(groupEntity);
//
//                    // Options 업데이트
//                    if (optionGroup.getOptions() != null) {
//                        for (MenuOption option : optionGroup.getOptions()) {
//                            MenuOptionEntity optionEntity = MenuOptionEntity.fromDomain(option);
//                            menuOptionJpaRepository.save(optionEntity);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    @Transactional
//    public void deleteMenu(String restaurantId, String menuId) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. Menu 조회 및 소유권 확인
//        MenuEntity menuEntity = menuJpaRepository.findById(menuId)
//                .orElseThrow(() -> new RestaurantException(MenuErrorCode.MENU_NOT_FOUND));
//
//        if (!menuEntity.getRestaurantId().equals(restaurantId)) {
//            throw new RestaurantException(MenuErrorCode.MENU_NOT_BELONG_TO_RESTAURANT);
//        }
//
//        // 3. Menu 소프트 삭제
//        Menu menu = menuEntity.toDomain();
//        menu.delete("SYSTEM");
//        menuJpaRepository.save(MenuEntity.fromDomain(menu));
//
//        // 4. 연관된 CategoryRelations 소프트 삭제
//        List<MenuCategoryRelationEntity> relations =
//                menuCategoryRelationJpaRepository.findAll().stream()
//                        .filter(r -> r.getMenuId().equals(menuId))
//                        .collect(Collectors.toList());
//
//        relations.forEach(r -> {
//            MenuCategoryRelation domain = r.toDomain();
//            domain.delete("SYSTEM");
//            menuCategoryRelationJpaRepository.save(
//                    MenuCategoryRelationEntity.fromDomain(domain));
//        });
//
//        // 5. 연관된 OptionGroups 소프트 삭제
//        List<MenuOptionGroupEntity> optionGroups =
//                menuOptionGroupJpaRepository.findAll().stream()
//                        .filter(og -> og.getMenuId().equals(menuId))
//                        .collect(Collectors.toList());
//
//        optionGroups.forEach(og -> {
//            MenuOptionGroup domain = og.toDomain();
//            domain.delete("SYSTEM");
//            menuOptionGroupJpaRepository.save(MenuOptionGroupEntity.fromDomain(domain));
//
//            // 6. 연관된 Options 소프트 삭제
//            List<MenuOptionEntity> options =
//                    menuOptionJpaRepository.findAll().stream()
//                            .filter(o -> o.getOptionGroupId().equals(og.getId()))
//                            .collect(Collectors.toList());
//
//            options.forEach(o -> {
//                MenuOption optionDomain = o.toDomain();
//                optionDomain.delete("SYSTEM");
//                menuOptionJpaRepository.save(MenuOptionEntity.fromDomain(optionDomain));
//            });
//        });
//    }
//
//    // ==================== MenuOptionGroup CUD ====================
//
//    @Override
//    @Transactional
//    public MenuOptionGroup saveMenuOptionGroup(MenuOptionGroup optionGroup) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(optionGroup.getRestaurantId())) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. Menu 존재 확인
//        if (!menuJpaRepository.existsById(optionGroup.getMenuId())) {
//            throw new RestaurantException(MenuErrorCode.MENU_NOT_FOUND);
//        }
//
//        // 3. OptionGroup 저장
//        MenuOptionGroupEntity entity = MenuOptionGroupEntity.fromDomain(optionGroup);
//        MenuOptionGroupEntity saved = menuOptionGroupJpaRepository.save(entity);
//
//        // 4. Options 저장
//        if (optionGroup.getOptions() != null && !optionGroup.getOptions().isEmpty()) {
//            List<MenuOptionEntity> optionEntities = optionGroup.getOptions().stream()
//                    .map(option -> {
//                        // Option에 OptionGroupId 설정
//                        MenuOption updatedOption = MenuOption.builder()
//                                .id(option.getId())
//                                .createdAt(option.getCreatedAt())
//                                .optionGroupId(saved.getId())
//                                .menuId(option.getMenuId())
//                                .restaurantId(option.getRestaurantId())
//                                .optionName(option.getOptionName())
//                                .description(option.getDescription())
//                                .additionalPrice(option.getAdditionalPrice())
//                                .isAvailable(option.getIsAvailable())
//                                .isDefault(option.getIsDefault())
//                                .displayOrder(option.getDisplayOrder())
//                                .purchaseCount(option.getPurchaseCount())
//                                .createdBy(option.getCreatedBy())
//                                .updatedAt(option.getUpdatedAt())
//                                .updatedBy(option.getUpdatedBy())
//                                .isDeleted(option.getIsDeleted())
//                                .deletedAt(option.getDeletedAt())
//                                .deletedBy(option.getDeletedBy())
//                                .build();
//                        return MenuOptionEntity.fromDomain(updatedOption);
//                    })
//                    .collect(Collectors.toList());
//            menuOptionJpaRepository.saveAll(optionEntities);
//        }
//
//        // 5. 전체 조회하여 반환
//        return findOptionGroupWithOptions(saved.getId());
//    }
//
//    @Override
//    @Transactional
//    public void updateMenuOptionGroup(String restaurantId, MenuOptionGroup optionGroup) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. OptionGroup 존재 및 소유권 확인
//        MenuOptionGroupEntity existing = menuOptionGroupJpaRepository.findById(optionGroup.getId())
//                .orElseThrow(() -> new RestaurantException(MenuErrorCode.OPTION_GROUP_NOT_FOUND));
//
//        if (!existing.getRestaurantId().equals(restaurantId)) {
//            throw new RestaurantException(MenuErrorCode.OPTION_GROUP_NOT_BELONG_TO_RESTAURANT);
//        }
//
//        // 3. OptionGroup 업데이트
//        MenuOptionGroupEntity entity = MenuOptionGroupEntity.fromDomain(optionGroup);
//        menuOptionGroupJpaRepository.save(entity);
//
//        // 4. Options 업데이트
//        if (optionGroup.getOptions() != null) {
//            for (MenuOption option : optionGroup.getOptions()) {
//                MenuOptionEntity optionEntity = MenuOptionEntity.fromDomain(option);
//                menuOptionJpaRepository.save(optionEntity);
//            }
//        }
//    }
//
//    @Override
//    @Transactional
//    public void deleteMenuOptionGroup(String restaurantId, String optionGroupId) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. OptionGroup 조회 및 소유권 확인
//        MenuOptionGroupEntity entity = menuOptionGroupJpaRepository.findById(optionGroupId)
//                .orElseThrow(() -> new RestaurantException(MenuErrorCode.OPTION_GROUP_NOT_FOUND));
//
//        if (!entity.getRestaurantId().equals(restaurantId)) {
//            throw new RestaurantException(MenuErrorCode.OPTION_GROUP_NOT_BELONG_TO_RESTAURANT);
//        }
//
//        // 3. OptionGroup 소프트 삭제
//        MenuOptionGroup domain = entity.toDomain();
//        domain.delete("SYSTEM");
//        menuOptionGroupJpaRepository.save(MenuOptionGroupEntity.fromDomain(domain));
//
//        // 4. 연관된 Options 소프트 삭제
//        List<MenuOptionEntity> options =
//                menuOptionJpaRepository.findAll().stream()
//                        .filter(o -> o.getOptionGroupId().equals(optionGroupId))
//                        .collect(Collectors.toList());
//
//        options.forEach(o -> {
//            MenuOption optionDomain = o.toDomain();
//            optionDomain.delete("SYSTEM");
//            menuOptionJpaRepository.save(MenuOptionEntity.fromDomain(optionDomain));
//        });
//    }
//
//    // ==================== MenuOption CUD ====================
//
//    @Override
//    @Transactional
//    public MenuOption saveMenuOption(MenuOption option) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(option.getRestaurantId())) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. OptionGroup 존재 확인
//        if (!menuOptionGroupJpaRepository.existsById(option.getOptionGroupId())) {
//            throw new RestaurantException(MenuErrorCode.OPTION_GROUP_NOT_FOUND);
//        }
//
//        // 3. Option 저장
//        MenuOptionEntity entity = MenuOptionEntity.fromDomain(option);
//        MenuOptionEntity saved = menuOptionJpaRepository.save(entity);
//
//        return saved.toDomain();
//    }
//
//    @Override
//    @Transactional
//    public void updateMenuOption(String restaurantId, MenuOption option) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. Option 존재 및 소유권 확인
//        MenuOptionEntity existing = menuOptionJpaRepository.findById(option.getId())
//                .orElseThrow(() -> new RestaurantException(MenuErrorCode.OPTION_NOT_FOUND));
//
//        if (!existing.getRestaurantId().equals(restaurantId)) {
//            throw new RestaurantException(MenuErrorCode.OPTION_NOT_BELONG_TO_RESTAURANT);
//        }
//
//        // 3. Option 업데이트
//        MenuOptionEntity entity = MenuOptionEntity.fromDomain(option);
//        menuOptionJpaRepository.save(entity);
//    }
//
//    @Override
//    @Transactional
//    public void deleteMenuOption(String restaurantId, String optionId) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. Option 조회 및 소유권 확인
//        MenuOptionEntity entity = menuOptionJpaRepository.findById(optionId)
//                .orElseThrow(() -> new RestaurantException(MenuErrorCode.OPTION_NOT_FOUND));
//
//        if (!entity.getRestaurantId().equals(restaurantId)) {
//            throw new RestaurantException(MenuErrorCode.OPTION_NOT_BELONG_TO_RESTAURANT);
//        }
//
//        // 3. Option 소프트 삭제
//        MenuOption domain = entity.toDomain();
//        domain.delete("SYSTEM");
//        menuOptionJpaRepository.save(MenuOptionEntity.fromDomain(domain));
//    }
//
//    // ==================== MenuCategory CUD ====================
//
//    @Override
//    @Transactional
//    public MenuCategory saveMenuCategory(MenuCategory category) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(category.getRestaurantId())) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. MenuCategory 저장
//        MenuCategoryEntity entity = MenuCategoryEntity.fromDomain(category);
//        MenuCategoryEntity saved = menuCategoryJpaRepository.save(entity);
//
//        return saved.toDomain();
//    }
//
//    @Override
//    @Transactional
//    public void updateMenuCategory(String restaurantId, MenuCategory category) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. Category 존재 및 소유권 확인
//        MenuCategoryEntity existing = menuCategoryJpaRepository.findById(category.getId())
//                .orElseThrow(() -> new RestaurantException(MenuErrorCode.CATEGORY_NOT_FOUND));
//
//        if (!existing.getRestaurantId().equals(restaurantId)) {
//            throw new RestaurantException(MenuErrorCode.CATEGORY_NOT_BELONG_TO_RESTAURANT);
//        }
//
//        // 3. Category 업데이트
//        MenuCategoryEntity entity = MenuCategoryEntity.fromDomain(category);
//        menuCategoryJpaRepository.save(entity);
//    }
//
//    @Override
//    @Transactional
//    public void deleteMenuCategory(String restaurantId, String categoryId) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. Category 조회 및 소유권 확인
//        MenuCategoryEntity entity = menuCategoryJpaRepository.findById(categoryId)
//                .orElseThrow(() -> new RestaurantException(MenuErrorCode.CATEGORY_NOT_FOUND));
//
//        if (!entity.getRestaurantId().equals(restaurantId)) {
//            throw new RestaurantException(MenuErrorCode.CATEGORY_NOT_BELONG_TO_RESTAURANT);
//        }
//
//        // 3. Category 소프트 삭제
//        MenuCategory domain = entity.toDomain();
//        domain.delete("SYSTEM");
//        menuCategoryJpaRepository.save(MenuCategoryEntity.fromDomain(domain));
//
//        // 4. 연관된 MenuCategoryRelations 소프트 삭제
//        List<MenuCategoryRelationEntity> relations =
//                menuCategoryRelationJpaRepository.findAll().stream()
//                        .filter(r -> r.getCategoryId().equals(categoryId))
//                        .collect(Collectors.toList());
//
//        relations.forEach(r -> {
//            MenuCategoryRelation relationDomain = r.toDomain();
//            relationDomain.delete("SYSTEM");
//            menuCategoryRelationJpaRepository.save(
//                    MenuCategoryRelationEntity.fromDomain(relationDomain));
//        });
//    }
//
//    // ==================== MenuCategoryRelation CUD ====================
//
//    @Override
//    @Transactional
//    public MenuCategoryRelation saveMenuCategoryRelation(MenuCategoryRelation relation) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(relation.getRestaurantId())) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. Menu 존재 확인
//        if (!menuJpaRepository.existsById(relation.getMenuId())) {
//            throw new RestaurantException(MenuErrorCode.MENU_NOT_FOUND);
//        }
//
//        // 3. Category 존재 확인
//        if (!menuCategoryJpaRepository.existsById(relation.getCategoryId())) {
//            throw new RestaurantException(MenuErrorCode.CATEGORY_NOT_FOUND);
//        }
//
//        // 4. Relation 저장
//        MenuCategoryRelationEntity entity = MenuCategoryRelationEntity.fromDomain(relation);
//        MenuCategoryRelationEntity saved = menuCategoryRelationJpaRepository.save(entity);
//
//        return saved.toDomain();
//    }
//
//    @Override
//    @Transactional
//    public void deleteMenuCategoryRelation(String restaurantId, String menuId, String categoryId) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. Relation 조회
//        MenuCategoryRelationEntity.RelationId relationId =
//                new MenuCategoryRelationEntity.RelationId(menuId, categoryId);
//
//        MenuCategoryRelationEntity entity = menuCategoryRelationJpaRepository.findById(relationId)
//                .orElseThrow(() -> new RestaurantException(MenuErrorCode.RELATION_NOT_FOUND));
//
//        if (!entity.getRestaurantId().equals(restaurantId)) {
//            throw new RestaurantException(MenuErrorCode.RELATION_NOT_BELONG_TO_RESTAURANT);
//        }
//
//        // 3. Relation 소프트 삭제
//        MenuCategoryRelation domain = entity.toDomain();
//        domain.delete("SYSTEM");
//        menuCategoryRelationJpaRepository.save(MenuCategoryRelationEntity.fromDomain(domain));
//    }
//
//    @Override
//    @Transactional
//    public void deleteAllMenuCategoryRelationsByMenuId(String restaurantId, String menuId) {
//        // 1. Restaurant 존재 확인
//        if (!restaurantJpaRepository.existsById(restaurantId)) {
//            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND);
//        }
//
//        // 2. 해당 메뉴의 모든 Relation 조회
//        List<MenuCategoryRelationEntity> relations =
//                menuCategoryRelationJpaRepository.findAll().stream()
//                        .filter(r -> r.getMenuId().equals(menuId) && r.getRestaurantId().equals(restaurantId))
//                        .collect(Collectors.toList());
//
//        // 3. 모든 Relation 소프트 삭제
//        relations.forEach(r -> {
//            MenuCategoryRelation domain = r.toDomain();
//            domain.delete("SYSTEM");
//            menuCategoryRelationJpaRepository.save(MenuCategoryRelationEntity.fromDomain(domain));
//        });
//    }
//
//    // ==================== 기본 조회 ====================
//
//    @Override
//    public Optional<Restaurant> findById(String id) {
//        RestaurantEntity entity = queryFactory
//                .selectFrom(restaurantEntity)
//                .where(
//                        restaurantEntity.id.eq(id),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetchOne();
//
//        return Optional.ofNullable(entity)
//                .map(RestaurantEntity::toDomain);
//    }
//
//    @Override
//    public Optional<Restaurant> findByIdWithAll(String id) {
//        // 1. Restaurant 조회
//        RestaurantEntity restaurant = queryFactory
//                .selectFrom(restaurantEntity)
//                .where(
//                        restaurantEntity.id.eq(id),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetchOne();
//
//        if (restaurant == null) {
//            return Optional.empty();
//        }
//
//        // 2. 모든 데이터를 조인해서 Tuple로 한 번에 가져오기
//        List<Tuple> tuples = queryFactory
//                .select(
//                        menuEntity,
//                        menuOptionGroupEntity,
//                        menuOptionEntity,
//                        menuCategoryRelationEntity
//                )
//                .from(menuEntity)
//                .leftJoin(menuOptionGroupEntity)
//                .on(menuOptionGroupEntity.menuId.eq(menuEntity.id)
//                        .and(menuOptionGroupEntity.isDeleted.isFalse()))
//                .leftJoin(menuOptionEntity)
//                .on(menuOptionEntity.optionGroupId.eq(menuOptionGroupEntity.id)
//                        .and(menuOptionEntity.isDeleted.isFalse()))
//                .leftJoin(menuCategoryRelationEntity)
//                .on(menuCategoryRelationEntity.menuId.eq(menuEntity.id)
//                        .and(menuCategoryRelationEntity.isDeleted.isFalse()))
//                .where(
//                        menuEntity.restaurantId.eq(id),
//                        menuEntity.isDeleted.isFalse()
//                )
//                .fetch();
//
//        // 3. MenuCategory 조회
//        List<MenuCategoryEntity> menuCategories = queryFactory
//                .selectFrom(menuCategoryEntity)
//                .where(
//                        menuCategoryEntity.restaurantId.eq(id),
//                        menuCategoryEntity.isDeleted.isFalse()
//                )
//                .fetch();
//
//        // 4. OperatingDay 조회
//        List<OperatingDayEntity> operatingDays = queryFactory
//                .selectFrom(operatingDayEntity)
//                .where(operatingDayEntity.restaurantId.eq(id))
//                .fetch();
//
//        // 5. RestaurantCategoryRelation 조회
//        List<RestaurantCategoryRelationEntity> categoryRelations = queryFactory
//                .selectFrom(restaurantCategoryRelationEntity)
//                .where(
//                        restaurantCategoryRelationEntity.restaurantId.eq(id),
//                        restaurantCategoryRelationEntity.isDeleted.isFalse()
//                )
//                .fetch();
//
//        // 6. Tuple에서 Aggregate 재구성
//        List<Menu> menus = buildMenusFromTuples(tuples);
//
//        // 7. Restaurant Domain 재구성
//        Restaurant domain = restaurant.toDomain();
//
//        return Optional.of(Restaurant.builder()
//                .id(domain.getId())
//                .createdAt(domain.getCreatedAt())
//                .ownerId(domain.getOwnerId())
//                .ownerName(domain.getOwnerName())
//                .restaurantName(domain.getRestaurantName())
//                .status(domain.getStatus())
//                .address(domain.getAddress())
//                .coordinate(domain.getCoordinate())
//                .contactNumber(domain.getContactNumber())
//                .tags(domain.getTags())
//                .isActive(domain.getIsActive())
//                .viewCount(domain.getViewCount())
//                .wishlistCount(domain.getWishlistCount())
//                .reviewCount(domain.getReviewCount())
//                .reviewRating(domain.getReviewRating())
//                .purchaseCount(domain.getPurchaseCount())
//                .createdBy(domain.getCreatedBy())
//                .updatedAt(domain.getUpdatedAt())
//                .updatedBy(domain.getUpdatedBy())
//                .isDeleted(domain.isDeleted())
//                .deletedAt(domain.getDeletedAt())
//                .deletedBy(domain.getDeletedBy())
//                .menus(menus)
//                .menuCategories(menuCategories.stream()
//                        .map(MenuCategoryEntity::toDomain)
//                        .collect(Collectors.toList()))
//                .operatingDays(operatingDays.stream()
//                        .map(OperatingDayEntity::toDomain)
//                        .collect(Collectors.toSet()))
//                .categoryRelations(categoryRelations.stream()
//                        .map(RestaurantCategoryRelationEntity::toDomain)
//                        .collect(Collectors.toSet()))
//                .build());
//    }
//
//    @Override
//    public Optional<Restaurant> findByIdWithMenus(String id) {
//        // 1. Restaurant 조회
//        RestaurantEntity restaurant = queryFactory
//                .selectFrom(restaurantEntity)
//                .where(
//                        restaurantEntity.id.eq(id),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetchOne();
//
//        if (restaurant == null) {
//            return Optional.empty();
//        }
//
//        // 2. Menu + OptionGroup + Option을 Tuple로 한 번에 조회
//        List<Tuple> tuples = queryFactory
//                .select(
//                        menuEntity,
//                        menuOptionGroupEntity,
//                        menuOptionEntity,
//                        menuCategoryRelationEntity
//                )
//                .from(menuEntity)
//                .leftJoin(menuOptionGroupEntity)
//                .on(menuOptionGroupEntity.menuId.eq(menuEntity.id)
//                        .and(menuOptionGroupEntity.isDeleted.isFalse()))
//                .leftJoin(menuOptionEntity)
//                .on(menuOptionEntity.optionGroupId.eq(menuOptionGroupEntity.id)
//                        .and(menuOptionEntity.isDeleted.isFalse()))
//                .leftJoin(menuCategoryRelationEntity)
//                .on(menuCategoryRelationEntity.menuId.eq(menuEntity.id)
//                        .and(menuCategoryRelationEntity.isDeleted.isFalse()))
//                .where(
//                        menuEntity.restaurantId.eq(id),
//                        menuEntity.isDeleted.isFalse()
//                )
//                .fetch();
//
//        // 3. Tuple에서 Menu Aggregate 재구성
//        List<Menu> menus = buildMenusFromTuples(tuples);
//
//        // 4. Restaurant Domain 재구성
//        Restaurant domain = restaurant.toDomain();
//
//        return Optional.of(Restaurant.builder()
//                .id(domain.getId())
//                .createdAt(domain.getCreatedAt())
//                .ownerId(domain.getOwnerId())
//                .ownerName(domain.getOwnerName())
//                .restaurantName(domain.getRestaurantName())
//                .status(domain.getStatus())
//                .address(domain.getAddress())
//                .coordinate(domain.getCoordinate())
//                .contactNumber(domain.getContactNumber())
//                .tags(domain.getTags())
//                .isActive(domain.getIsActive())
//                .viewCount(domain.getViewCount())
//                .wishlistCount(domain.getWishlistCount())
//                .reviewCount(domain.getReviewCount())
//                .reviewRating(domain.getReviewRating())
//                .purchaseCount(domain.getPurchaseCount())
//                .createdBy(domain.getCreatedBy())
//                .updatedAt(domain.getUpdatedAt())
//                .updatedBy(domain.getUpdatedBy())
//                .isDeleted(domain.isDeleted())
//                .deletedAt(domain.getDeletedAt())
//                .deletedBy(domain.getDeletedBy())
//                .menus(menus)
//                .build());
//    }
//
//    @Override
//    public Optional<Restaurant> findByIdWithMenuCategories(String id) {
//        // 1. Restaurant 조회
//        RestaurantEntity restaurant = queryFactory
//                .selectFrom(restaurantEntity)
//                .where(
//                        restaurantEntity.id.eq(id),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetchOne();
//
//        if (restaurant == null) {
//            return Optional.empty();
//        }
//
//        // 2. MenuCategory 조회
//        List<MenuCategoryEntity> menuCategories = queryFactory
//                .selectFrom(menuCategoryEntity)
//                .where(
//                        menuCategoryEntity.restaurantId.eq(id),
//                        menuCategoryEntity.isDeleted.isFalse()
//                )
//                .fetch();
//
//        // 3. Restaurant Domain 재구성
//        Restaurant domain = restaurant.toDomain();
//
//        return Optional.of(Restaurant.builder()
//                .id(domain.getId())
//                .createdAt(domain.getCreatedAt())
//                .ownerId(domain.getOwnerId())
//                .ownerName(domain.getOwnerName())
//                .restaurantName(domain.getRestaurantName())
//                .status(domain.getStatus())
//                .address(domain.getAddress())
//                .coordinate(domain.getCoordinate())
//                .contactNumber(domain.getContactNumber())
//                .tags(domain.getTags())
//                .isActive(domain.getIsActive())
//                .viewCount(domain.getViewCount())
//                .wishlistCount(domain.getWishlistCount())
//                .reviewCount(domain.getReviewCount())
//                .reviewRating(domain.getReviewRating())
//                .purchaseCount(domain.getPurchaseCount())
//                .createdBy(domain.getCreatedBy())
//                .updatedAt(domain.getUpdatedAt())
//                .updatedBy(domain.getUpdatedBy())
//                .isDeleted(domain.isDeleted())
//                .deletedAt(domain.getDeletedAt())
//                .deletedBy(domain.getDeletedBy())
//                .menuCategories(menuCategories.stream()
//                        .map(MenuCategoryEntity::toDomain)
//                        .collect(Collectors.toList()))
//                .build());
//    }
//
//    // ==================== 레스토랑 목록 조회 ====================
//
//    @Override
//    public List<Restaurant> findByOwnerId(Long ownerId) {
//        return queryFactory
//                .selectFrom(restaurantEntity)
//                .where(
//                        restaurantEntity.ownerId.eq(ownerId),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetch()
//                .stream()
//                .map(RestaurantEntity::toDomain)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Restaurant> findAllActive() {
//        return queryFactory
//                .selectFrom(restaurantEntity)
//                .where(
//                        restaurantEntity.isActive.isTrue(),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetch()
//                .stream()
//                .map(RestaurantEntity::toDomain)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Restaurant> findByCategoryId(String categoryId) {
//        return queryFactory
//                .select(restaurantEntity)
//                .from(restaurantEntity)
//                .join(restaurantCategoryRelationEntity)
//                .on(restaurantCategoryRelationEntity.restaurantId.eq(restaurantEntity.id))
//                .where(
//                        restaurantCategoryRelationEntity.categoryId.eq(categoryId),
//                        restaurantCategoryRelationEntity.isDeleted.isFalse(),
//                        restaurantEntity.isActive.isTrue(),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetch()
//                .stream()
//                .map(RestaurantEntity::toDomain)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Restaurant> findByRestaurantNameContaining(String restaurantName) {
//        return queryFactory
//                .selectFrom(restaurantEntity)
//                .where(
//                        restaurantEntity.restaurantName.contains(restaurantName),
//                        restaurantEntity.isActive.isTrue(),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetch()
//                .stream()
//                .map(RestaurantEntity::toDomain)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Restaurant> findByCategoryIdAndRestaurantName(String categoryId, String restaurantName) {
//        return queryFactory
//                .select(restaurantEntity)
//                .from(restaurantEntity)
//                .join(restaurantCategoryRelationEntity)
//                .on(restaurantCategoryRelationEntity.restaurantId.eq(restaurantEntity.id))
//                .where(
//                        restaurantCategoryRelationEntity.categoryId.eq(categoryId),
//                        restaurantCategoryRelationEntity.isDeleted.isFalse(),
//                        restaurantEntity.restaurantName.contains(restaurantName),
//                        restaurantEntity.isActive.isTrue(),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetch()
//                .stream()
//                .map(RestaurantEntity::toDomain)
//                .collect(Collectors.toList());
//    }
//
//    // ==================== 레스토랑 페이징 조회 ====================
//
//    @Override
//    public Page<Restaurant> findRestaurantsWithCategory(String categoryId, String restaurantName, Pageable pageable) {
//        // 동적 조건 생성
//        BooleanBuilder builder = new BooleanBuilder();
//        builder.and(restaurantEntity.isActive.isTrue());
//        builder.and(restaurantEntity.isDeleted.isFalse());
//
//        if (restaurantName != null && !restaurantName.isEmpty()) {
//            builder.and(restaurantEntity.restaurantName.contains(restaurantName));
//        }
//
//        // 카테고리 조인 여부에 따라 쿼리 분기
//        if (categoryId != null && !categoryId.isEmpty()) {
//            // 카테고리 있을 때: 조인 + 총 개수 조회
//            Long total = queryFactory
//                    .select(restaurantEntity.count())
//                    .from(restaurantEntity)
//                    .join(restaurantCategoryRelationEntity)
//                    .on(restaurantCategoryRelationEntity.restaurantId.eq(restaurantEntity.id))
//                    .where(
//                            restaurantCategoryRelationEntity.categoryId.eq(categoryId),
//                            restaurantCategoryRelationEntity.isDeleted.isFalse(),
//                            builder
//                    )
//                    .fetchOne();
//
//            if (total == null || total == 0) {
//                return Page.empty(pageable);
//            }
//
//            // 페이징 조회
//            List<RestaurantEntity> restaurants = queryFactory
//                    .select(restaurantEntity)
//                    .from(restaurantEntity)
//                    .join(restaurantCategoryRelationEntity)
//                    .on(restaurantCategoryRelationEntity.restaurantId.eq(restaurantEntity.id))
//                    .where(
//                            restaurantCategoryRelationEntity.categoryId.eq(categoryId),
//                            restaurantCategoryRelationEntity.isDeleted.isFalse(),
//                            builder
//                    )
//                    .offset(pageable.getOffset())
//                    .limit(pageable.getPageSize())
//                    .fetch();
//
//            List<Restaurant> content = restaurants.stream()
//                    .map(RestaurantEntity::toDomain)
//                    .collect(Collectors.toList());
//
//            return new PageImpl<>(content, pageable, total);
//
//        } else {
//            // 카테고리 없을 때: 일반 조회
//            Long total = queryFactory
//                    .select(restaurantEntity.count())
//                    .from(restaurantEntity)
//                    .where(builder)
//                    .fetchOne();
//
//            if (total == null || total == 0) {
//                return Page.empty(pageable);
//            }
//
//            List<RestaurantEntity> restaurants = queryFactory
//                    .selectFrom(restaurantEntity)
//                    .where(builder)
//                    .offset(pageable.getOffset())
//                    .limit(pageable.getPageSize())
//                    .fetch();
//
//            List<Restaurant> content = restaurants.stream()
//                    .map(RestaurantEntity::toDomain)
//                    .collect(Collectors.toList());
//
//            return new PageImpl<>(content, pageable, total);
//        }
//    }
//
//    @Override
//    public Page<Restaurant> findAllActive(Pageable pageable) {
//        // 총 개수 조회
//        Long total = queryFactory
//                .select(restaurantEntity.count())
//                .from(restaurantEntity)
//                .where(
//                        restaurantEntity.isActive.isTrue(),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetchOne();
//
//        if (total == null || total == 0) {
//            return Page.empty(pageable);
//        }
//
//        // 페이징 조회
//        List<RestaurantEntity> restaurants = queryFactory
//                .selectFrom(restaurantEntity)
//                .where(
//                        restaurantEntity.isActive.isTrue(),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        List<Restaurant> content = restaurants.stream()
//                .map(RestaurantEntity::toDomain)
//                .collect(Collectors.toList());
//
//        return new PageImpl<>(content, pageable, total);
//    }
//
//    // ==================== 메뉴 조회 ====================
//
//    @Override
//    public Optional<Menu> findMenuByRestaurantIdAndMenuId(String restaurantId, String menuId) {
//        // Menu + OptionGroup + Option + CategoryRelation을 Tuple로 한 번에 조회
//        List<Tuple> tuples = queryFactory
//                .select(
//                        menuEntity,
//                        menuOptionGroupEntity,
//                        menuOptionEntity,
//                        menuCategoryRelationEntity
//                )
//                .from(menuEntity)
//                .leftJoin(menuOptionGroupEntity)
//                .on(menuOptionGroupEntity.menuId.eq(menuEntity.id)
//                        .and(menuOptionGroupEntity.isDeleted.isFalse()))
//                .leftJoin(menuOptionEntity)
//                .on(menuOptionEntity.optionGroupId.eq(menuOptionGroupEntity.id)
//                        .and(menuOptionEntity.isDeleted.isFalse()))
//                .leftJoin(menuCategoryRelationEntity)
//                .on(menuCategoryRelationEntity.menuId.eq(menuEntity.id)
//                        .and(menuCategoryRelationEntity.isDeleted.isFalse()))
//                .where(
//                        menuEntity.restaurantId.eq(restaurantId),
//                        menuEntity.id.eq(menuId),
//                        menuEntity.isDeleted.isFalse()
//                )
//                .fetch();
//
//        if (tuples.isEmpty()) {
//            return Optional.empty();
//        }
//
//        // Tuple에서 Menu Aggregate 재구성
//        List<Menu> menus = buildMenusFromTuples(tuples);
//
//        return menus.isEmpty() ? Optional.empty() : Optional.of(menus.get(0));
//    }
//
//    @Override
//    public List<Menu> findMenusByRestaurantId(String restaurantId) {
//        // Menu + OptionGroup + Option + CategoryRelation을 Tuple로 한 번에 조회
//        List<Tuple> tuples = queryFactory
//                .select(
//                        menuEntity,
//                        menuOptionGroupEntity,
//                        menuOptionEntity,
//                        menuCategoryRelationEntity
//                )
//                .from(menuEntity)
//                .leftJoin(menuOptionGroupEntity)
//                .on(menuOptionGroupEntity.menuId.eq(menuEntity.id)
//                        .and(menuOptionGroupEntity.isDeleted.isFalse()))
//                .leftJoin(menuOptionEntity)
//                .on(menuOptionEntity.optionGroupId.eq(menuOptionGroupEntity.id)
//                        .and(menuOptionEntity.isDeleted.isFalse()))
//                .leftJoin(menuCategoryRelationEntity)
//                .on(menuCategoryRelationEntity.menuId.eq(menuEntity.id)
//                        .and(menuCategoryRelationEntity.isDeleted.isFalse()))
//                .where(
//                        menuEntity.restaurantId.eq(restaurantId),
//                        menuEntity.isDeleted.isFalse()
//                )
//                .fetch();
//
//        return buildMenusFromTuples(tuples);
//    }
//
//    @Override
//    public List<Menu> findMenusByRestaurantIdAndMenuCategoryId(String restaurantId, String menuCategoryId) {
//        // Menu + OptionGroup + Option + CategoryRelation을 Tuple로 한 번에 조회
//        // MenuCategoryRelation 조건 추가
//        List<Tuple> tuples = queryFactory
//                .select(
//                        menuEntity,
//                        menuOptionGroupEntity,
//                        menuOptionEntity,
//                        menuCategoryRelationEntity
//                )
//                .from(menuEntity)
//                .join(menuCategoryRelationEntity)
//                .on(menuCategoryRelationEntity.menuId.eq(menuEntity.id)
//                        .and(menuCategoryRelationEntity.categoryId.eq(menuCategoryId))
//                        .and(menuCategoryRelationEntity.isDeleted.isFalse()))
//                .leftJoin(menuOptionGroupEntity)
//                .on(menuOptionGroupEntity.menuId.eq(menuEntity.id)
//                        .and(menuOptionGroupEntity.isDeleted.isFalse()))
//                .leftJoin(menuOptionEntity)
//                .on(menuOptionEntity.optionGroupId.eq(menuOptionGroupEntity.id)
//                        .and(menuOptionEntity.isDeleted.isFalse()))
//                .where(
//                        menuEntity.restaurantId.eq(restaurantId),
//                        menuEntity.isDeleted.isFalse()
//                )
//                .fetch();
//
//        return buildMenusFromTuples(tuples);
//    }
//
//    // ==================== 메뉴 페이징 조회 ====================
//
//    @Override
//    public Page<Menu> findMenusByRestaurantId(String restaurantId, Pageable pageable) {
//        // 1. 총 개수 조회
//        Long total = queryFactory
//                .select(menuEntity.count())
//                .from(menuEntity)
//                .where(
//                        menuEntity.restaurantId.eq(restaurantId),
//                        menuEntity.isDeleted.isFalse()
//                )
//                .fetchOne();
//
//        if (total == null || total == 0) {
//            return Page.empty(pageable);
//        }
//
//        // 2. 페이징된 메뉴 ID 조회
//        List<String> menuIds = queryFactory
//                .select(menuEntity.id)
//                .from(menuEntity)
//                .where(
//                        menuEntity.restaurantId.eq(restaurantId),
//                        menuEntity.isDeleted.isFalse()
//                )
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        if (menuIds.isEmpty()) {
//            return Page.empty(pageable);
//        }
//
//        // 3. 조인으로 전체 데이터 조회 (페이징된 ID만)
//        List<Tuple> tuples = queryFactory
//                .select(
//                        menuEntity,
//                        menuOptionGroupEntity,
//                        menuOptionEntity,
//                        menuCategoryRelationEntity
//                )
//                .from(menuEntity)
//                .leftJoin(menuOptionGroupEntity)
//                .on(menuOptionGroupEntity.menuId.eq(menuEntity.id)
//                        .and(menuOptionGroupEntity.isDeleted.isFalse()))
//                .leftJoin(menuOptionEntity)
//                .on(menuOptionEntity.optionGroupId.eq(menuOptionGroupEntity.id)
//                        .and(menuOptionEntity.isDeleted.isFalse()))
//                .leftJoin(menuCategoryRelationEntity)
//                .on(menuCategoryRelationEntity.menuId.eq(menuEntity.id)
//                        .and(menuCategoryRelationEntity.isDeleted.isFalse()))
//                .where(menuEntity.id.in(menuIds))
//                .fetch();
//
//        List<Menu> menus = buildMenusFromTuples(tuples);
//
//        return new PageImpl<>(menus, pageable, total);
//    }
//
//    @Override
//    public Page<Menu> findMenusByRestaurantIdAndMenuCategoryId(String restaurantId, String menuCategoryId, Pageable pageable) {
//        // 1. 총 개수 조회
//        Long total = queryFactory
//                .select(menuEntity.count())
//                .from(menuEntity)
//                .join(menuCategoryRelationEntity)
//                .on(menuCategoryRelationEntity.menuId.eq(menuEntity.id)
//                        .and(menuCategoryRelationEntity.categoryId.eq(menuCategoryId))
//                        .and(menuCategoryRelationEntity.isDeleted.isFalse()))
//                .where(
//                        menuEntity.restaurantId.eq(restaurantId),
//                        menuEntity.isDeleted.isFalse()
//                )
//                .fetchOne();
//
//        if (total == null || total == 0) {
//            return Page.empty(pageable);
//        }
//
//        // 2. 페이징된 메뉴 ID 조회
//        List<String> menuIds = queryFactory
//                .select(menuEntity.id)
//                .from(menuEntity)
//                .join(menuCategoryRelationEntity)
//                .on(menuCategoryRelationEntity.menuId.eq(menuEntity.id)
//                        .and(menuCategoryRelationEntity.categoryId.eq(menuCategoryId))
//                        .and(menuCategoryRelationEntity.isDeleted.isFalse()))
//                .where(
//                        menuEntity.restaurantId.eq(restaurantId),
//                        menuEntity.isDeleted.isFalse()
//                )
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        if (menuIds.isEmpty()) {
//            return Page.empty(pageable);
//        }
//
//        // 3. 조인으로 전체 데이터 조회
//        List<Tuple> tuples = queryFactory
//                .select(
//                        menuEntity,
//                        menuOptionGroupEntity,
//                        menuOptionEntity,
//                        menuCategoryRelationEntity
//                )
//                .from(menuEntity)
//                .leftJoin(menuOptionGroupEntity)
//                .on(menuOptionGroupEntity.menuId.eq(menuEntity.id)
//                        .and(menuOptionGroupEntity.isDeleted.isFalse()))
//                .leftJoin(menuOptionEntity)
//                .on(menuOptionEntity.optionGroupId.eq(menuOptionGroupEntity.id)
//                        .and(menuOptionEntity.isDeleted.isFalse()))
//                .leftJoin(menuCategoryRelationEntity)
//                .on(menuCategoryRelationEntity.menuId.eq(menuEntity.id)
//                        .and(menuCategoryRelationEntity.isDeleted.isFalse()))
//                .where(menuEntity.id.in(menuIds))
//                .fetch();
//
//        List<Menu> menus = buildMenusFromTuples(tuples);
//
//        return new PageImpl<>(menus, pageable, total);
//    }
//
//    // ==================== 존재 여부 확인 ====================
//
//    @Override
//    public boolean existsById(String id) {
//        Integer count = queryFactory
//                .selectOne()
//                .from(restaurantEntity)
//                .where(
//                        restaurantEntity.id.eq(id),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetchFirst();
//
//        return count != null;
//    }
//
//    @Override
//    public boolean existsByOwnerId(Long ownerId) {
//        Integer count = queryFactory
//                .selectOne()
//                .from(restaurantEntity)
//                .where(
//                        restaurantEntity.ownerId.eq(ownerId),
//                        restaurantEntity.isDeleted.isFalse()
//                )
//                .fetchFirst();
//
//        return count != null;
//    }
//
//    // ==================== Private Helper Methods ====================
//
//    /**
//     * Menu와 모든 연관 관계를 저장하는 헬퍼 메서드
//     * 저장 순서: Menu → MenuCategoryRelations → MenuOptionGroups → MenuOptions
//     */
//    private Menu saveMenuWithRelations(Menu menu, String restaurantId) {
//        // 1. Menu 저장
//        MenuEntity menuEntity = MenuEntity.fromDomain(menu);
//        MenuEntity savedMenu = menuJpaRepository.save(menuEntity);
//        String menuId = savedMenu.getId();
//
//        // 2. MenuCategoryRelations 저장
//        if (menu.getCategoryRelations() != null && !menu.getCategoryRelations().isEmpty()) {
//            List<MenuCategoryRelationEntity> relationEntities =
//                    menu.getCategoryRelations().stream()
//                            .map(relation -> {
//                                // MenuId 설정
//                                MenuCategoryRelation updatedRelation = MenuCategoryRelation.builder()
//                                        .menuId(menuId)
//                                        .categoryId(relation.getCategoryId())
//                                        .restaurantId(restaurantId)
//                                        .isPrimary(relation.isPrimary())
//                                        .createdAt(relation.getCreatedAt())
//                                        .createdBy(relation.getCreatedBy())
//                                        .isDeleted(relation.isDeleted())
//                                        .deletedAt(relation.getDeletedAt())
//                                        .deletedBy(relation.getDeletedBy())
//                                        .build();
//                                return MenuCategoryRelationEntity.fromDomain(updatedRelation);
//                            })
//                            .collect(Collectors.toList());
//            menuCategoryRelationJpaRepository.saveAll(relationEntities);
//        }
//
//        // 3. MenuOptionGroups와 MenuOptions 저장
//        if (menu.getOptionGroups() != null && !menu.getOptionGroups().isEmpty()) {
//            for (MenuOptionGroup optionGroup : menu.getOptionGroups()) {
//                // OptionGroup에 MenuId 설정
//                MenuOptionGroup updatedGroup = MenuOptionGroup.builder()
//                        .id(optionGroup.getId())
//                        .createdAt(optionGroup.getCreatedAt())
//                        .menuId(menuId)
//                        .restaurantId(restaurantId)
//                        .groupName(optionGroup.getGroupName())
//                        .description(optionGroup.getDescription())
//                        .minSelection(optionGroup.getMinSelection())
//                        .maxSelection(optionGroup.getMaxSelection())
//                        .isRequired(optionGroup.getIsRequired())
//                        .displayOrder(optionGroup.getDisplayOrder())
//                        .isActive(optionGroup.getIsActive())
//                        .createdBy(optionGroup.getCreatedBy())
//                        .updatedAt(optionGroup.getUpdatedAt())
//                        .updatedBy(optionGroup.getUpdatedBy())
//                        .isDeleted(optionGroup.getIsDeleted())
//                        .deletedAt(optionGroup.getDeletedAt())
//                        .deletedBy(optionGroup.getDeletedBy())
//                        .options(optionGroup.getOptions())
//                        .build();
//
//                MenuOptionGroupEntity groupEntity = MenuOptionGroupEntity.fromDomain(updatedGroup);
//                MenuOptionGroupEntity savedGroup = menuOptionGroupJpaRepository.save(groupEntity);
//                String optionGroupId = savedGroup.getId();
//
//                // Options 저장
//                if (optionGroup.getOptions() != null && !optionGroup.getOptions().isEmpty()) {
//                    List<MenuOptionEntity> optionEntities = optionGroup.getOptions().stream()
//                            .map(option -> {
//                                // Option에 OptionGroupId와 MenuId 설정
//                                MenuOption updatedOption = MenuOption.builder()
//                                        .id(option.getId())
//                                        .createdAt(option.getCreatedAt())
//                                        .optionGroupId(optionGroupId)
//                                        .menuId(menuId)
//                                        .restaurantId(restaurantId)
//                                        .optionName(option.getOptionName())
//                                        .description(option.getDescription())
//                                        .additionalPrice(option.getAdditionalPrice())
//                                        .isAvailable(option.getIsAvailable())
//                                        .isDefault(option.getIsDefault())
//                                        .displayOrder(option.getDisplayOrder())
//                                        .purchaseCount(option.getPurchaseCount())
//                                        .createdBy(option.getCreatedBy())
//                                        .updatedAt(option.getUpdatedAt())
//                                        .updatedBy(option.getUpdatedBy())
//                                        .isDeleted(option.getIsDeleted())
//                                        .deletedAt(option.getDeletedAt())
//                                        .deletedBy(option.getDeletedBy())
//                                        .build();
//                                return MenuOptionEntity.fromDomain(updatedOption);
//                            })
//                            .collect(Collectors.toList());
//                    menuOptionJpaRepository.saveAll(optionEntities);
//                }
//            }
//        }
//
//        // 4. 전체 데이터 다시 조회하여 반환
//        return findMenuByRestaurantIdAndMenuId(restaurantId, menuId).orElse(savedMenu.toDomain());
//    }
//
//    /**
//     * OptionGroup과 Options를 함께 조회하는 헬퍼 메서드
//     */
//    private MenuOptionGroup findOptionGroupWithOptions(String optionGroupId) {
//        MenuOptionGroupEntity groupEntity = menuOptionGroupJpaRepository.findById(optionGroupId)
//                .orElseThrow(() -> new RestaurantException(MenuErrorCode.OPTION_GROUP_NOT_FOUND));
//
//        List<MenuOptionEntity> optionEntities =
//                menuOptionJpaRepository.findAll().stream()
//                        .filter(o -> o.getOptionGroupId().equals(optionGroupId) && !o.getIsDeleted())
//                        .collect(Collectors.toList());
//
//        List<MenuOption> options = optionEntities.stream()
//                .map(MenuOptionEntity::toDomain)
//                .collect(Collectors.toList());
//
//        MenuOptionGroup group = groupEntity.toDomain();
//
//        return MenuOptionGroup.builder()
//                .id(group.getId())
//                .createdAt(group.getCreatedAt())
//                .menuId(group.getMenuId())
//                .restaurantId(group.getRestaurantId())
//                .groupName(group.getGroupName())
//                .description(group.getDescription())
//                .minSelection(group.getMinSelection())
//                .maxSelection(group.getMaxSelection())
//                .isRequired(group.getIsRequired())
//                .displayOrder(group.getDisplayOrder())
//                .isActive(group.getIsActive())
//                .createdBy(group.getCreatedBy())
//                .updatedAt(group.getUpdatedAt())
//                .updatedBy(group.getUpdatedBy())
//                .isDeleted(group.getIsDeleted())
//                .deletedAt(group.getDeletedAt())
//                .deletedBy(group.getDeletedBy())
//                .options(options)
//                .build();
//    }
//
//    /**
//     * Tuple 리스트에서 Menu Aggregate 재구성
//     * - Menu → MenuOptionGroup → MenuOption 계층 구조 생성
//     * - MenuCategoryRelation 포함
//     */
//    private List<Menu> buildMenusFromTuples(List<Tuple> tuples) {
//        // 1. Menu 그룹화
//        Map<String, MenuEntity> menuMap = new LinkedHashMap<>();
//        Map<String, Set<MenuCategoryRelationEntity>> menuCategoryRelationMap = new HashMap<>();
//        Map<String, Map<String, MenuOptionGroupEntity>> menuOptionGroupMap = new HashMap<>();
//        Map<String, Map<String, List<MenuOptionEntity>>> optionGroupOptionMap = new HashMap<>();
//
//        for (Tuple tuple : tuples) {
//            MenuEntity menu = tuple.get(menuEntity);
//            MenuOptionGroupEntity optionGroup = tuple.get(menuOptionGroupEntity);
//            MenuOptionEntity option = tuple.get(menuOptionEntity);
//            MenuCategoryRelationEntity categoryRelation = tuple.get(menuCategoryRelationEntity);
//
//            if (menu == null) continue;
//
//            // Menu 저장
//            menuMap.putIfAbsent(menu.getId(), menu);
//
//            // MenuCategoryRelation 저장
//            if (categoryRelation != null) {
//                menuCategoryRelationMap
//                        .computeIfAbsent(menu.getId(), k -> new HashSet<>())
//                        .add(categoryRelation);
//            }
//
//            // MenuOptionGroup 저장
//            if (optionGroup != null) {
//                menuOptionGroupMap
//                        .computeIfAbsent(menu.getId(), k -> new LinkedHashMap<>())
//                        .putIfAbsent(optionGroup.getId(), optionGroup);
//
//                // MenuOption 저장
//                if (option != null) {
//                    optionGroupOptionMap
//                            .computeIfAbsent(menu.getId(), k -> new HashMap<>())
//                            .computeIfAbsent(optionGroup.getId(), k -> new ArrayList<>())
//                            .add(option);
//                }
//            }
//        }
//
//        // 2. Menu Aggregate 재구성
//        return menuMap.values().stream()
//                .map(menuEntity -> {
//                    Menu menuDomain = menuEntity.toDomain();
//
//                    // CategoryRelation 변환
//                    Set<MenuCategoryRelation> categoryRelations =
//                            menuCategoryRelationMap.getOrDefault(menuEntity.getId(), Collections.emptySet())
//                                    .stream()
//                                    .map(MenuCategoryRelationEntity::toDomain)
//                                    .collect(Collectors.toSet());
//
//                    // OptionGroup 변환
//                    List<MenuOptionGroup> optionGroups =
//                            menuOptionGroupMap.getOrDefault(menuEntity.getId(), Collections.emptyMap())
//                                    .values()
//                                    .stream()
//                                    .map(groupEntity -> {
//                                        MenuOptionGroup groupDomain = groupEntity.toDomain();
//
//                                        // Option 변환
//                                        List<MenuOption> options =
//                                                optionGroupOptionMap
//                                                        .getOrDefault(menuEntity.getId(), Collections.emptyMap())
//                                                        .getOrDefault(groupEntity.getId(), Collections.emptyList())
//                                                        .stream()
//                                                        .map(MenuOptionEntity::toDomain)
//                                                        .collect(Collectors.toList());
//
//                                        return MenuOptionGroup.builder()
//                                                .id(groupDomain.getId())
//                                                .createdAt(groupDomain.getCreatedAt())
//                                                .menuId(groupDomain.getMenuId())
//                                                .restaurantId(groupDomain.getRestaurantId())
//                                                .groupName(groupDomain.getGroupName())
//                                                .description(groupDomain.getDescription())
//                                                .minSelection(groupDomain.getMinSelection())
//                                                .maxSelection(groupDomain.getMaxSelection())
//                                                .isRequired(groupDomain.getIsRequired())
//                                                .displayOrder(groupDomain.getDisplayOrder())
//                                                .isActive(groupDomain.getIsActive())
//                                                .createdBy(groupDomain.getCreatedBy())
//                                                .updatedAt(groupDomain.getUpdatedAt())
//                                                .updatedBy(groupDomain.getUpdatedBy())
//                                                .isDeleted(groupDomain.getIsDeleted())
//                                                .deletedAt(groupDomain.getDeletedAt())
//                                                .deletedBy(groupDomain.getDeletedBy())
//                                                .options(options)
//                                                .build();
//                                    })
//                                    .collect(Collectors.toList());
//
//                    return Menu.builder()
//                            .id(menuDomain.getId())
//                            .createdAt(menuDomain.getCreatedAt())
//                            .restaurantId(menuDomain.getRestaurantId())
//                            .menuName(menuDomain.getMenuName())
//                            .description(menuDomain.getDescription())
//                            .ingredients(menuDomain.getIngredients())
//                            .price(menuDomain.getPrice())
//                            .isAvailable(menuDomain.getIsAvailable())
//                            .isMain(menuDomain.getIsMain())
//                            .isPopular(menuDomain.getIsPopular())
//                            .isNew(menuDomain.getIsNew())
//                            .calorie(menuDomain.getCalorie())
//                            .purchaseCount(menuDomain.getPurchaseCount())
//                            .wishlistCount(menuDomain.getWishlistCount())
//                            .reviewCount(menuDomain.getReviewCount())
//                            .reviewRating(menuDomain.getReviewRating())
//                            .createdBy(menuDomain.getCreatedBy())
//                            .updatedAt(menuDomain.getUpdatedAt())
//                            .updatedBy(menuDomain.getUpdatedBy())
//                            .isDeleted(menuDomain.getIsDeleted())
//                            .deletedAt(menuDomain.getDeletedAt())
//                            .deletedBy(menuDomain.getDeletedBy())
//                            .categoryRelations(categoryRelations)
//                            .optionGroups(optionGroups)
//                            .build();
//                })
//                .collect(Collectors.toList());
//    }
//}