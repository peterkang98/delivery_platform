package xyz.sparta_project.manjok.domain.restaurant.infrastructure.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.*;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.jpa.RestaurantCategoryJpaRepository;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.jpa.RestaurantJpaRepository;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QRestaurantEntity.restaurantEntity;
import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QMenuEntity.menuEntity;
import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QMenuCategoryEntity.menuCategoryEntity;
import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QMenuOptionGroupEntity.menuOptionGroupEntity;
import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QMenuOptionEntity.menuOptionEntity;
import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QOperatingDayEntity.operatingDayEntity;
import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QRestaurantCategoryRelationEntity.restaurantCategoryRelationEntity;
import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QMenuCategoryRelationEntity.menuCategoryRelationEntity;

/**
 * Restaurant Repository 구현체
 * - QueryDSL을 활용한 효율적인 조회
 * - Fetch Join으로 N+1 문제 해결
 * - DDD Aggregate Root 패턴 준수
 * - 체계적인 예외 처리
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantCategoryJpaRepository restaurantCategoryJpaRepository;
    private final JPAQueryFactory queryFactory;

    // ==================== CREATE & UPDATE ====================

    @Override
    @Transactional
    public Restaurant save(Restaurant restaurant) {
        try {
            // Restaurant 도메인 → 엔티티 변환 (하위 엔티티 모두 변환됨)
            RestaurantEntity entity = RestaurantEntity.fromDomain(restaurant);

            resolveAllRelations(entity, restaurant);

            // 저장 (Cascade.ALL로 하위 엔티티 모두 저장)
            RestaurantEntity savedEntity = restaurantJpaRepository.save(entity);

            log.info("Restaurant saved successfully. ID: {}, Name: {}",
                    savedEntity.getId(), savedEntity.getRestaurantName());

            // 엔티티 → 도메인 변환하여 반환
            return savedEntity.toDomain();

        } catch (Exception e) {
            log.error("Failed to save restaurant: {}", restaurant.getRestaurantName(), e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "레스토랑 저장 중 오류가 발생했습니다: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 모든 연관관계 처리를 한 곳에서
     */
    private void resolveAllRelations(RestaurantEntity entity, Restaurant restaurant) {
        String restaurantId = entity.getId();

        // 1. RestaurantCategoryRelation
        resolveRestaurantCategoryRelations(entity, restaurant);

        // 2. Menu의 하위 엔티티들
        for (MenuEntity menuEntity : entity.getMenus()) {
            String menuId = menuEntity.getId();

            // 2-1. MenuCategoryRelation
            for (MenuCategoryRelationEntity relation : menuEntity.getCategoryRelations()) {
                setRestaurantId(relation, "restaurantId", restaurantId);

                // Category 참조 설정
                restaurant.getMenus().stream()
                        .filter(menu -> menu.getId().equals(menuId))
                        .findFirst()
                        .flatMap(domainMenu -> domainMenu.getCategoryRelations().stream()
                                .filter(dr -> relation.getIsPrimary().equals(dr.isPrimary()))
                                .findFirst())
                        .ifPresent(domainRelation -> {
                            entity.getMenuCategories().stream()
                                    .filter(cat -> cat.getId().equals(domainRelation.getCategoryId()))
                                    .findFirst()
                                    .ifPresent(relation::setCategory);
                        });
            }

            // 2-2. MenuOptionGroup
            for (MenuOptionGroupEntity optionGroup : menuEntity.getOptionGroups()) {
                setRestaurantId(optionGroup, "restaurantId", restaurantId);

                // 2-3. MenuOption (OptionGroup의 하위)
                for (MenuOptionEntity option : optionGroup.getOptions()) {
                    setRestaurantId(option, "restaurantId", restaurantId);
                    setRestaurantId(option, "menuId", menuId);
                }
            }
        }
    }

    /**
     * 리플렉션으로 restaurantId 설정하는 헬퍼 메서드
     */
    private void setRestaurantId(Object target, String fieldName, String restaurantId) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, restaurantId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set " + fieldName + " on " + target.getClass().getSimpleName(), e);
        }
    }

    /**
     * RestaurantCategoryRelation의 Category 참조 설정
     */
    private void resolveRestaurantCategoryRelations(RestaurantEntity entity, Restaurant restaurant) {
        for (RestaurantCategoryRelationEntity relationEntity : entity.getCategoryRelations()) {
            // 도메인에서 매칭되는 categoryId 찾기
            restaurant.getCategoryRelations().stream()
                    .filter(domainRelation ->
                            relationEntity.getIsPrimary().equals(domainRelation.isPrimary()))
                    .findFirst()
                    .ifPresent(domainRelation -> {
                        String categoryId = domainRelation.getCategoryId();
                        if (categoryId != null) {
                            RestaurantCategoryEntity categoryEntity =
                                    restaurantCategoryJpaRepository.findById(categoryId)
                                            .orElseThrow(() -> new RestaurantException(
                                                    RestaurantErrorCode.CATEGORY_NOT_FOUND,
                                                    "카테고리를 찾을 수 없습니다: " + categoryId
                                            ));
                            relationEntity.setCategory(categoryEntity);
                        }
                    });
        }
    }

    // ==================== DELETE ====================

    @Override
    @Transactional
    public void delete(String restaurantId, String deletedBy) {
        RestaurantEntity entity = restaurantJpaRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 이미 삭제된 경우
        if (entity.getIsDeleted()) {
            throw new RestaurantException(RestaurantErrorCode.RESTAURANT_ALREADY_DELETED);
        }

        try {
            // 엔티티의 비즈니스 메서드로 상태만 변경
            entity.markAsDeleted(deletedBy);

            // 저장 (연관관계 그대로 유지)
            restaurantJpaRepository.save(entity);

            log.info("Restaurant deleted successfully. ID: {}, DeletedBy: {}", restaurantId, deletedBy);

        } catch (Exception e) {
            log.error("Failed to delete restaurant: {}", restaurantId, e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "레스토랑 삭제 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== READ - Restaurant 단건 조회 ====================

    @Override
    public Optional<Restaurant> findById(String restaurantId) {
        return findByIdInternal(restaurantId, false);
    }

    @Override
    public Optional<Restaurant> findByIdIncludingDeleted(String restaurantId) {
        return findByIdInternal(restaurantId, true);
    }

    /**
     * Restaurant 단건 조회 (내부 메서드)
     * - Fetch Join으로 모든 연관 엔티티 한 번에 로딩
     * - N+1 문제 완전 해결
     */
    private Optional<Restaurant> findByIdInternal(String restaurantId, boolean includeDeleted) {
        try {
            // 1. Restaurant 기본 정보 조회
            BooleanExpression condition = restaurantEntity.id.eq(restaurantId);
            if (!includeDeleted) {
                condition = condition.and(restaurantEntity.isDeleted.eq(false));
            }

            RestaurantEntity entity = queryFactory
                    .selectFrom(restaurantEntity)
                    .where(condition)
                    .fetchOne();

            if (entity == null) {
                return Optional.empty();
            }

            // 2. 연관 데이터 Fetch Join으로 한 번에 조회
            loadRestaurantAssociations(entity);

            return Optional.of(entity.toDomain());

        } catch (Exception e) {
            log.error("Failed to find restaurant by ID: {}", restaurantId, e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "레스토랑 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    /**
     * Restaurant의 모든 연관 엔티티를 Fetch Join으로 로딩
     * - N+1 문제 방지를 위해 한 번에 로딩
     */
    private void loadRestaurantAssociations(RestaurantEntity restaurant) {
        String restaurantId = restaurant.getId();

        // Menu + OptionGroup + Option 조회 (3단계 계층 구조)
        List<MenuEntity> menus = queryFactory
                .selectFrom(menuEntity)
                .leftJoin(menuEntity.optionGroups, menuOptionGroupEntity).fetchJoin()
                .leftJoin(menuOptionGroupEntity.options, menuOptionEntity).fetchJoin()
                .leftJoin(menuEntity.categoryRelations, menuCategoryRelationEntity).fetchJoin()
                .where(menuEntity.restaurant.id.eq(restaurantId))
                .distinct()
                .fetch();

        // MenuCategory 조회
        List<MenuCategoryEntity> menuCategories = queryFactory
                .selectFrom(menuCategoryEntity)
                .leftJoin(menuCategoryEntity.menuRelations, menuCategoryRelationEntity).fetchJoin()
                .where(menuCategoryEntity.restaurant.id.eq(restaurantId))
                .distinct()
                .fetch();

        // OperatingDay 조회
        Set<OperatingDayEntity> operatingDays = new HashSet<>(queryFactory
                .selectFrom(operatingDayEntity)
                .where(operatingDayEntity.restaurant.id.eq(restaurantId))
                .fetch());

        // RestaurantCategoryRelation 조회
        Set<RestaurantCategoryRelationEntity> categoryRelations = new HashSet<>(queryFactory
                .selectFrom(restaurantCategoryRelationEntity)
                .where(restaurantCategoryRelationEntity.restaurant.id.eq(restaurantId))
                .fetch());

        // 연관관계 설정
        restaurant.getMenus().clear();
        restaurant.getMenus().addAll(menus);

        restaurant.getMenuCategories().clear();
        restaurant.getMenuCategories().addAll(menuCategories);

        restaurant.getOperatingDays().clear();
        restaurant.getOperatingDays().addAll(operatingDays);

        restaurant.getCategoryRelations().clear();
        restaurant.getCategoryRelations().addAll(categoryRelations);
    }

    // ==================== READ - Restaurant 목록 조회 ====================

    @Override
    public Page<Restaurant> findAll(Pageable pageable) {
        return findAllInternal(null, false, pageable);
    }

    @Override
    public Page<Restaurant> findByOwnerId(Long ownerId, Pageable pageable) {
        if (ownerId == null) {
            throw new RestaurantException(RestaurantErrorCode.OWNER_REQUIRED);
        }
        return findAllInternal(ownerId, false, pageable);
    }

    @Override
    public Page<Restaurant> findAllIncludingDeleted(Pageable pageable) {
        return findAllInternal(null, true, pageable);
    }

    /**
     * Restaurant 목록 조회 (내부 메서드)
     * - 기본 정보만 조회 (성능 최적화)
     * - 연관 엔티티는 필요시 개별 로딩
     */
    private Page<Restaurant> findAllInternal(Long ownerId, boolean includeDeleted, Pageable pageable) {
        try {
            // 조건 구성
            BooleanExpression condition = null;

            if (ownerId != null) {
                condition = restaurantEntity.ownerId.eq(ownerId);
            }

            if (!includeDeleted) {
                BooleanExpression notDeleted = restaurantEntity.isDeleted.eq(false)
                        .and(restaurantEntity.isActive.eq(true));
                condition = condition != null ? condition.and(notDeleted) : notDeleted;
            }

            // Count 쿼리
            long total = queryFactory
                    .selectFrom(restaurantEntity)
                    .where(condition)
                    .fetchCount();

            if (total == 0) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            // 목록 쿼리
            List<RestaurantEntity> entities = queryFactory
                    .selectFrom(restaurantEntity)
                    .where(condition)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(getOrderSpecifiers(pageable))
                    .fetch();

            List<Restaurant> restaurants = entities.stream()
                    .map(RestaurantEntity::toDomain)
                    .collect(Collectors.toList());

            return new PageImpl<>(restaurants, pageable, total);

        } catch (RestaurantException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to find restaurants", e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "레스토랑 목록 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== READ - Restaurant 검색 ====================

    @Override
    public Page<Restaurant> searchRestaurants(
            String province,
            String city,
            String district,
            Set<String> categoryIds,
            String keyword,
            Pageable pageable
    ) {
        try {
            // 기본 조건: 삭제되지 않고 활성화된 레스토랑만
            BooleanExpression condition = restaurantEntity.isDeleted.eq(false)
                    .and(restaurantEntity.isActive.eq(true));

            // 지역 필터
            if (province != null && !province.isBlank()) {
                condition = condition.and(restaurantEntity.address.province.eq(province));
            }
            if (city != null && !city.isBlank()) {
                condition = condition.and(restaurantEntity.address.city.eq(city));
            }
            if (district != null && !district.isBlank()) {
                condition = condition.and(restaurantEntity.address.district.eq(district));
            }

            // 키워드 검색 (레스토랑명, 태그)
            if (keyword != null && !keyword.isBlank()) {
                BooleanExpression keywordCondition = restaurantEntity.restaurantName.containsIgnoreCase(keyword)
                        .or(restaurantEntity.tagsJson.containsIgnoreCase(keyword));
                condition = condition.and(keywordCondition);
            }

            // 카테고리 필터
            JPAQuery<RestaurantEntity> query;
            if (categoryIds != null && !categoryIds.isEmpty()) {
                query = queryFactory
                        .selectFrom(restaurantEntity)
                        .join(restaurantEntity.categoryRelations, restaurantCategoryRelationEntity)
                        .where(
                                condition,
                                restaurantCategoryRelationEntity.category.id.in(categoryIds),
                                restaurantCategoryRelationEntity.isDeleted.eq(false)
                        )
                        .distinct();
            } else {
                query = queryFactory
                        .selectFrom(restaurantEntity)
                        .where(condition);
            }

            // Count 쿼리
            long total = query.fetchCount();

            if (total == 0) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            // 목록 쿼리
            List<RestaurantEntity> entities = query
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(getOrderSpecifiers(pageable))
                    .fetch();

            List<Restaurant> restaurants = entities.stream()
                    .map(RestaurantEntity::toDomain)
                    .collect(Collectors.toList());

            return new PageImpl<>(restaurants, pageable, total);

        } catch (Exception e) {
            log.error("Failed to search restaurants", e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "레스토랑 검색 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== READ - Menu 단건 조회 ====================

    @Override
    public Optional<Menu> findMenuByRestaurantIdAndMenuId(String restaurantId, String menuId) {
        return findMenuInternal(restaurantId, menuId, false);
    }

    @Override
    public Optional<Menu> findMenuByRestaurantIdAndMenuIdIncludingHidden(String restaurantId, String menuId) {
        return findMenuInternal(restaurantId, menuId, true);
    }

    /**
     * Menu 단건 조회 (내부 메서드)
     * - Fetch Join으로 OptionGroup, Option 한 번에 로딩
     */
    private Optional<Menu> findMenuInternal(String restaurantId, String menuId, boolean includeHidden) {
        try {
            BooleanExpression condition = menuEntity.restaurant.id.eq(restaurantId)
                    .and(menuEntity.id.eq(menuId));

            if (!includeHidden) {
                condition = condition.and(menuEntity.isDeleted.eq(false))
                        .and(menuEntity.isAvailable.eq(true));
            }

            MenuEntity entity = queryFactory
                    .selectFrom(menuEntity)
                    .leftJoin(menuEntity.optionGroups, menuOptionGroupEntity).fetchJoin()
                    .leftJoin(menuOptionGroupEntity.options, menuOptionEntity).fetchJoin()
                    .leftJoin(menuEntity.categoryRelations, menuCategoryRelationEntity).fetchJoin()
                    .where(condition)
                    .distinct()
                    .fetchOne();

            return Optional.ofNullable(entity).map(MenuEntity::toDomain);

        } catch (Exception e) {
            log.error("Failed to find menu: restaurantId={}, menuId={}", restaurantId, menuId, e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "메뉴 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== READ - Menu 목록 조회 ====================

    @Override
    public Page<Menu> findMenusByRestaurantId(String restaurantId, Pageable pageable) {
        return findMenusInternal(restaurantId, null, null, false, pageable);
    }

    @Override
    public Page<Menu> findMenusByRestaurantIdIncludingHidden(String restaurantId, Pageable pageable) {
        return findMenusInternal(restaurantId, null, null, true, pageable);
    }

    @Override
    public Page<Menu> findMenusByRestaurantIdAndCategory(String restaurantId, String categoryId, Pageable pageable) {
        if (categoryId == null || categoryId.isBlank()) {
            throw new RestaurantException(MenuErrorCode.CATEGORY_NOT_FOUND);
        }
        return findMenusInternal(restaurantId, categoryId, null, false, pageable);
    }

    @Override
    public Page<Menu> searchMenusByRestaurantIdAndName(String restaurantId, String menuName, Pageable pageable) {
        return findMenusInternal(restaurantId, null, menuName, false, pageable);
    }

    /**
     * Menu 목록 조회 (내부 메서드)
     */
    private Page<Menu> findMenusInternal(
            String restaurantId,
            String categoryId,
            String menuName,
            boolean includeHidden,
            Pageable pageable
    ) {
        try {
            // 조건 구성
            BooleanExpression condition = menuEntity.restaurant.id.eq(restaurantId);

            if (!includeHidden) {
                condition = condition.and(menuEntity.isDeleted.eq(false))
                        .and(menuEntity.isAvailable.eq(true));
            }

            if (menuName != null && !menuName.isBlank()) {
                condition = condition.and(menuEntity.menuName.containsIgnoreCase(menuName));
            }

            // 카테고리 필터
            JPAQuery<MenuEntity> query;
            if (categoryId != null) {
                query = queryFactory
                        .selectFrom(menuEntity)
                        .join(menuEntity.categoryRelations, menuCategoryRelationEntity)
                        .where(
                                condition,
                                menuCategoryRelationEntity.category.id.eq(categoryId),
                                menuCategoryRelationEntity.isDeleted.eq(false)
                        )
                        .distinct();
            } else {
                query = queryFactory
                        .selectFrom(menuEntity)
                        .where(condition);
            }

            // Count 쿼리
            long total = query.fetchCount();

            if (total == 0) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            // 목록 쿼리
            List<MenuEntity> entities = query
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(getMenuOrderSpecifiers(pageable))
                    .fetch();

            List<Menu> menus = entities.stream()
                    .map(MenuEntity::toDomain)
                    .collect(Collectors.toList());

            return new PageImpl<>(menus, pageable, total);

        } catch (RestaurantException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to find menus: restaurantId={}", restaurantId, e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "메뉴 목록 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== READ - MenuCategory ====================

    @Override
    public List<MenuCategory> findMenuCategoriesByRestaurantId(String restaurantId) {
        try {
            List<MenuCategoryEntity> entities = queryFactory
                    .selectFrom(menuCategoryEntity)
                    .where(
                            menuCategoryEntity.restaurant.id.eq(restaurantId),
                            menuCategoryEntity.isActive.eq(true),
                            menuCategoryEntity.isDeleted.eq(false)
                    )
                    .orderBy(menuCategoryEntity.displayOrder.asc())
                    .fetch();

            return entities.stream()
                    .map(MenuCategoryEntity::toDomain)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to find menu categories: restaurantId={}", restaurantId, e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "메뉴 카테고리 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    @Override
    public List<MenuCategory> findRootMenuCategoriesByRestaurantId(String restaurantId) {
        try {
            List<MenuCategoryEntity> entities = queryFactory
                    .selectFrom(menuCategoryEntity)
                    .where(
                            menuCategoryEntity.restaurant.id.eq(restaurantId),
                            menuCategoryEntity.depth.eq(1),
                            menuCategoryEntity.isActive.eq(true),
                            menuCategoryEntity.isDeleted.eq(false)
                    )
                    .orderBy(menuCategoryEntity.displayOrder.asc())
                    .fetch();

            return entities.stream()
                    .map(MenuCategoryEntity::toDomain)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to find root menu categories: restaurantId={}", restaurantId, e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "최상위 메뉴 카테고리 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== READ - OperatingDay ====================

    @Override
    public Set<OperatingDay> findOperatingDaysByRestaurantId(String restaurantId) {
        try {
            List<OperatingDayEntity> entities = queryFactory
                    .selectFrom(operatingDayEntity)
                    .where(operatingDayEntity.restaurant.id.eq(restaurantId))
                    .fetch();

            return entities.stream()
                    .map(OperatingDayEntity::toDomain)
                    .collect(Collectors.toSet());

        } catch (Exception e) {
            log.error("Failed to find operating days: restaurantId={}", restaurantId, e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "운영시간 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    @Override
    public Optional<OperatingDay> findOperatingDayByRestaurantIdAndDayType(String restaurantId, DayType dayType) {
        try {
            if (dayType == null) {
                throw new RestaurantException(RestaurantErrorCode.INVALID_OPERATING_TIME);
            }

            OperatingDayEntity entity = queryFactory
                    .selectFrom(operatingDayEntity)
                    .where(
                            operatingDayEntity.restaurant.id.eq(restaurantId),
                            operatingDayEntity.dayType.eq(dayType)
                    )
                    .fetchOne();

            return Optional.ofNullable(entity).map(OperatingDayEntity::toDomain);

        } catch (RestaurantException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to find operating day: restaurantId={}, dayType={}", restaurantId, dayType, e);
            throw new RestaurantException(
                    RestaurantErrorCode.OPERATING_DAY_NOT_FOUND,
                    "특정 요일의 운영시간 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== 존재 확인 ====================

    @Override
    public boolean existsById(String restaurantId) {
        try {
            Integer count = queryFactory
                    .selectOne()
                    .from(restaurantEntity)
                    .where(
                            restaurantEntity.id.eq(restaurantId),
                            restaurantEntity.isDeleted.eq(false)
                    )
                    .fetchFirst();

            return count != null;

        } catch (Exception e) {
            log.error("Failed to check restaurant exists: {}", restaurantId, e);
            return false;
        }
    }

    @Override
    public boolean existsByOwnerIdAndName(Long ownerId, String restaurantName) {
        try {
            if (ownerId == null || restaurantName == null || restaurantName.isBlank()) {
                return false;
            }

            Integer count = queryFactory
                    .selectOne()
                    .from(restaurantEntity)
                    .where(
                            restaurantEntity.ownerId.eq(ownerId),
                            restaurantEntity.restaurantName.eq(restaurantName),
                            restaurantEntity.isDeleted.eq(false)
                    )
                    .fetchFirst();

            return count != null;

        } catch (Exception e) {
            log.error("Failed to check restaurant exists by owner and name: ownerId={}, name={}",
                    ownerId, restaurantName, e);
            return false;
        }
    }

    @Override
    public boolean existsMenuByRestaurantIdAndMenuId(String restaurantId, String menuId) {
        try {
            if (restaurantId == null || menuId == null) {
                return false;
            }

            Integer count = queryFactory
                    .selectOne()
                    .from(menuEntity)
                    .where(
                            menuEntity.restaurant.id.eq(restaurantId),
                            menuEntity.id.eq(menuId),
                            menuEntity.isDeleted.eq(false)
                    )
                    .fetchFirst();

            return count != null;

        } catch (Exception e) {
            log.error("Failed to check menu exists: restaurantId={}, menuId={}", restaurantId, menuId, e);
            return false;
        }
    }

    // ==================== 이벤트 처리용 ====================

    @Override
    public Optional<Restaurant> findByIdWithMenus(String restaurantId) {
        try {
            RestaurantEntity entity = queryFactory
                    .selectFrom(restaurantEntity)
                    .leftJoin(restaurantEntity.menus, menuEntity).fetchJoin()
                    .where(
                            restaurantEntity.id.eq(restaurantId),
                            restaurantEntity.isDeleted.eq(false)
                    )
                    .distinct()
                    .fetchOne();

            return Optional.ofNullable(entity).map(RestaurantEntity::toDomain);

        } catch (Exception e) {
            log.error("Failed to find restaurant with menus: {}", restaurantId, e);
            throw new RestaurantException(
                    RestaurantErrorCode.EVENT_PROCESSING_FAILED,
                    "레스토랑 및 메뉴 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== 정렬 Helper 메서드 ====================

    /**
     * Restaurant 정렬 조건 생성
     */
    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        pageable.getSort().forEach(order -> {
            String property = order.getProperty();
            boolean isAsc = order.isAscending();

            switch (property) {
                case "createdAt":
                    orders.add(isAsc ? restaurantEntity.createdAt.asc() : restaurantEntity.createdAt.desc());
                    break;
                case "restaurantName":
                    orders.add(isAsc ? restaurantEntity.restaurantName.asc() : restaurantEntity.restaurantName.desc());
                    break;
                case "reviewRating":
                    orders.add(isAsc ? restaurantEntity.reviewRating.asc() : restaurantEntity.reviewRating.desc());
                    break;
                case "reviewCount":
                    orders.add(isAsc ? restaurantEntity.reviewCount.asc() : restaurantEntity.reviewCount.desc());
                    break;
                case "purchaseCount":
                    orders.add(isAsc ? restaurantEntity.purchaseCount.asc() : restaurantEntity.purchaseCount.desc());
                    break;
                default:
                    orders.add(restaurantEntity.createdAt.desc());
            }
        });

        // 기본 정렬이 없으면 최신순
        if (orders.isEmpty()) {
            orders.add(restaurantEntity.createdAt.desc());
        }

        return orders.toArray(new OrderSpecifier[0]);
    }

    /**
     * Menu 정렬 조건 생성
     */
    private OrderSpecifier<?>[] getMenuOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        pageable.getSort().forEach(order -> {
            String property = order.getProperty();
            boolean isAsc = order.isAscending();

            switch (property) {
                case "price":
                    orders.add(isAsc ? menuEntity.price.asc() : menuEntity.price.desc());
                    break;
                case "menuName":
                    orders.add(isAsc ? menuEntity.menuName.asc() : menuEntity.menuName.desc());
                    break;
                case "purchaseCount":
                    orders.add(isAsc ? menuEntity.purchaseCount.asc() : menuEntity.purchaseCount.desc());
                    break;
                case "reviewRating":
                    orders.add(isAsc ? menuEntity.reviewRating.asc() : menuEntity.reviewRating.desc());
                    break;
                case "createdAt":
                    orders.add(isAsc ? menuEntity.createdAt.asc() : menuEntity.createdAt.desc());
                    break;
                default:
                    orders.add(menuEntity.createdAt.desc());
            }
        });

        // 기본 정렬이 없으면 최신순
        if (orders.isEmpty()) {
            orders.add(menuEntity.createdAt.desc());
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
}