package xyz.sparta_project.manjok.domain.restaurant.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategory;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantCategoryRepository;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.RestaurantCategoryEntity;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.jpa.RestaurantCategoryJpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.QRestaurantCategoryEntity.restaurantCategoryEntity;

/**
 * RestaurantCategory Repository 구현체
 * - QueryDSL을 사용한 복잡한 조회
 * - Entity ↔ Domain 변환
 * - 트랜잭션 관리
 */
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantCategoryRepositoryImpl implements RestaurantCategoryRepository {

    private final RestaurantCategoryJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    /**
     * 카테고리 저장
     * - ID가 있으면: 기존 엔티티 조회 후 업데이트
     * - ID가 없으면: 새로 생성
     */
    @Override
    @Transactional
    public RestaurantCategory save(RestaurantCategory category) {
        RestaurantCategoryEntity entityToSave;

        if (category.getId() != null) {
            // ID가 있는 경우: 기존 엔티티 조회 후 업데이트
            Optional<RestaurantCategoryEntity> existingEntity = jpaRepository.findById(category.getId());

            if (existingEntity.isPresent()) {
                // 기존 엔티티를 도메인 정보로 업데이트
                entityToSave = existingEntity.get();
                entityToSave.updateFromDomain(category);
            } else {
                // ID는 있지만 DB에 없는 경우: 새로 생성
                entityToSave = RestaurantCategoryEntity.fromDomain(category);
            }
        } else {
            // ID가 없는 경우: 새로 생성
            entityToSave = RestaurantCategoryEntity.fromDomain(category);
        }

        RestaurantCategoryEntity saved = jpaRepository.save(entityToSave);
        return saved.toDomain();
    }

    @Override
    public Optional<RestaurantCategory> findById(String id) {
        RestaurantCategoryEntity entity = queryFactory
                .selectFrom(restaurantCategoryEntity)
                .where(
                        restaurantCategoryEntity.id.eq(id),
                        restaurantCategoryEntity.isDeleted.isFalse()
                )
                .fetchOne();

        return Optional.ofNullable(entity)
                .map(RestaurantCategoryEntity::toDomain);
    }

    /**
     * 여러 ID로 카테고리 일괄 조회
     * - N+1 문제 방지
     */
    @Override
    public List<RestaurantCategory> findAllByIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(restaurantCategoryEntity)
                .where(
                        restaurantCategoryEntity.id.in(ids),
                        restaurantCategoryEntity.isDeleted.isFalse()
                )
                .fetch()
                .stream()
                .map(RestaurantCategoryEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RestaurantCategory> findByCategoryCode(String categoryCode) {
        RestaurantCategoryEntity entity = queryFactory
                .selectFrom(restaurantCategoryEntity)
                .where(
                        restaurantCategoryEntity.categoryCode.eq(categoryCode),
                        restaurantCategoryEntity.isDeleted.isFalse()
                )
                .fetchOne();

        return Optional.ofNullable(entity)
                .map(RestaurantCategoryEntity::toDomain);
    }

    @Override
    public List<RestaurantCategory> findAllActive() {
        return queryFactory
                .selectFrom(restaurantCategoryEntity)
                .where(
                        restaurantCategoryEntity.isActive.isTrue(),
                        restaurantCategoryEntity.isDeleted.isFalse()
                )
                .orderBy(restaurantCategoryEntity.displayOrder.asc())
                .fetch()
                .stream()
                .map(RestaurantCategoryEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantCategory> findRootCategories() {
        return queryFactory
                .selectFrom(restaurantCategoryEntity)
                .where(
                        restaurantCategoryEntity.depth.eq(1),
                        restaurantCategoryEntity.isActive.isTrue(),
                        restaurantCategoryEntity.isDeleted.isFalse()
                )
                .orderBy(restaurantCategoryEntity.displayOrder.asc())
                .fetch()
                .stream()
                .map(RestaurantCategoryEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantCategory> findByParentCategoryId(String parentCategoryId) {
        return queryFactory
                .selectFrom(restaurantCategoryEntity)
                .where(
                        restaurantCategoryEntity.parentCategoryId.eq(parentCategoryId),
                        restaurantCategoryEntity.isActive.isTrue(),
                        restaurantCategoryEntity.isDeleted.isFalse()
                )
                .orderBy(restaurantCategoryEntity.displayOrder.asc())
                .fetch()
                .stream()
                .map(RestaurantCategoryEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantCategory> findPopularCategories() {
        return queryFactory
                .selectFrom(restaurantCategoryEntity)
                .where(
                        restaurantCategoryEntity.isPopular.isTrue(),
                        restaurantCategoryEntity.isActive.isTrue(),
                        restaurantCategoryEntity.isDeleted.isFalse()
                )
                .orderBy(restaurantCategoryEntity.displayOrder.asc())
                .fetch()
                .stream()
                .map(RestaurantCategoryEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        Integer count = queryFactory
                .selectOne()
                .from(restaurantCategoryEntity)
                .where(
                        restaurantCategoryEntity.id.eq(id),
                        restaurantCategoryEntity.isDeleted.isFalse()
                )
                .fetchFirst();

        return count != null;
    }
}