package xyz.sparta_project.manjok.domain.order.infrastructure.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.order.domain.model.Order;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderStatus;
import xyz.sparta_project.manjok.domain.order.domain.repository.OrderRepository;
import xyz.sparta_project.manjok.domain.order.infrastructure.jpa.OrderJpaRepository;
import xyz.sparta_project.manjok.domain.order.infrastructure.entity.OrderEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static xyz.sparta_project.manjok.domain.order.infrastructure.entity.QOrderEntity.orderEntity;
import static xyz.sparta_project.manjok.domain.order.infrastructure.entity.QOrderItemEntity.orderItemEntity;

/**
 * Order Repository 구현체 (엔티티 연관관계 기반)
 */
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final JPAQueryFactory queryFactory;

    /**
     * 주문 저장/수정 (더티체킹 활용)
     */
    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity;

        if (order.getId() == null) {
            // 신규 생성
            entity = OrderEntity.from(order);
            entity = orderJpaRepository.save(entity);
        } else {
            // 기존 엔티티 조회 후 업데이트 (더티체킹)
            entity = orderJpaRepository.findById(order.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + order.getId()));

            // 도메인의 상태 변경을 엔티티에 반영 (더티체킹으로 자동 업데이트됨)
            entity.updateFromDomain(order);
            // save 호출 불필요 - 더티체킹으로 자동 업데이트
        }

        return entity.toDomain();
    }

    @Override
    public Optional<Order> findById(String id) {
        OrderEntity entity = queryFactory
                .selectFrom(orderEntity)
                .leftJoin(orderEntity.items, orderItemEntity).fetchJoin()  // N+1 방지
                .where(
                        orderEntity.id.eq(id),
                        isNotDeleted()
                )
                .fetchOne();

        return Optional.ofNullable(entity)
                .map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByIdIncludingDeleted(String id) {
        return orderJpaRepository.findById(id)
                .map(OrderEntity::toDomain);
    }

    @Override
    public Page<Order> findByUserId(String userId, Pageable pageable) {
        // 1. ID만 먼저 조회 (페이징)
        List<String> ids = queryFactory
                .select(orderEntity.id)
                .from(orderEntity)
                .where(
                        orderEntity.orderer.userId.eq(userId),
                        isNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        // 2. ID로 fetch join (N+1 방지)
        List<OrderEntity> content = ids.isEmpty() ? List.of() :
                queryFactory
                        .selectFrom(orderEntity)
                        .leftJoin(orderEntity.items, orderItemEntity).fetchJoin()
                        .where(orderEntity.id.in(ids))
                        .orderBy(getOrderSpecifiers(pageable.getSort()))
                        .fetch();

        // 3. Total count
        Long total = queryFactory
                .select(orderEntity.count())
                .from(orderEntity)
                .where(
                        orderEntity.orderer.userId.eq(userId),
                        isNotDeleted()
                )
                .fetchOne();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(orders, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable) {
        // 1. ID만 먼저 조회 (페이징)
        List<String> ids = queryFactory
                .select(orderEntity.id)
                .from(orderEntity)
                .where(
                        orderEntity.orderer.userId.eq(userId),
                        orderEntity.status.eq(status),
                        isNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        // 2. ID로 fetch join
        List<OrderEntity> content = ids.isEmpty() ? List.of() :
                queryFactory
                        .selectFrom(orderEntity)
                        .leftJoin(orderEntity.items, orderItemEntity).fetchJoin()
                        .where(orderEntity.id.in(ids))
                        .orderBy(getOrderSpecifiers(pageable.getSort()))
                        .fetch();

        // 3. Total count
        Long total = queryFactory
                .select(orderEntity.count())
                .from(orderEntity)
                .where(
                        orderEntity.orderer.userId.eq(userId),
                        orderEntity.status.eq(status),
                        isNotDeleted()
                )
                .fetchOne();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(orders, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Order> findByRestaurantId(String restaurantId, Pageable pageable) {
        // OrderItem이 이제 Entity이므로 join 방식 변경
        // 1. ID만 먼저 조회 (페이징)
        List<String> ids = queryFactory
                .select(orderEntity.id)
                .from(orderEntity)
                .join(orderEntity.items, orderItemEntity)
                .where(
                        orderItemEntity.restaurant.restaurantId.eq(restaurantId),
                        isNotDeleted()
                )
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        // 2. ID로 fetch join
        List<OrderEntity> content = ids.isEmpty() ? List.of() :
                queryFactory
                        .selectFrom(orderEntity)
                        .leftJoin(orderEntity.items, orderItemEntity).fetchJoin()
                        .where(orderEntity.id.in(ids))
                        .orderBy(getOrderSpecifiers(pageable.getSort()))
                        .fetch();

        // 3. Total count
        Long total = queryFactory
                .select(orderEntity.countDistinct())
                .from(orderEntity)
                .join(orderEntity.items, orderItemEntity)
                .where(
                        orderItemEntity.restaurant.restaurantId.eq(restaurantId),
                        isNotDeleted()
                )
                .fetchOne();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(orders, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Order> findByRestaurantIdAndStatus(String restaurantId, OrderStatus status, Pageable pageable) {
        // 1. ID만 먼저 조회 (페이징)
        List<String> ids = queryFactory
                .select(orderEntity.id)
                .from(orderEntity)
                .join(orderEntity.items, orderItemEntity)
                .where(
                        orderItemEntity.restaurant.restaurantId.eq(restaurantId),
                        orderEntity.status.eq(status),
                        isNotDeleted()
                )
                .groupBy(orderEntity.id)
                .orderBy(orderEntity.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. ID로 fetch join
        List<OrderEntity> content = ids.isEmpty() ? List.of() :
                queryFactory
                        .selectFrom(orderEntity)
                        .leftJoin(orderEntity.items, orderItemEntity).fetchJoin()
                        .where(orderEntity.id.in(ids))
                        .orderBy(getOrderSpecifiers(pageable.getSort()))
                        .fetch();

        // 3. Total count
        Long total = queryFactory
                .select(orderEntity.countDistinct())
                .from(orderEntity)
                .join(orderEntity.items, orderItemEntity)
                .where(
                        orderItemEntity.restaurant.restaurantId.eq(restaurantId),
                        orderEntity.status.eq(status),
                        isNotDeleted()
                )
                .fetchOne();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(orders, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Order> findByRestaurantIdAndDateRange(
            String restaurantId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        // 1. ID만 먼저 조회 (페이징)
        List<String> ids = queryFactory
                .select(orderEntity.id)
                .from(orderEntity)
                .join(orderEntity.items, orderItemEntity)
                .where(
                        orderItemEntity.restaurant.restaurantId.eq(restaurantId),
                        orderEntity.createdAt.between(startDate, endDate),
                        isNotDeleted()
                )
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        // 2. ID로 fetch join
        List<OrderEntity> content = ids.isEmpty() ? List.of() :
                queryFactory
                        .selectFrom(orderEntity)
                        .leftJoin(orderEntity.items, orderItemEntity).fetchJoin()
                        .where(orderEntity.id.in(ids))
                        .orderBy(getOrderSpecifiers(pageable.getSort()))
                        .fetch();

        // 3. Total count
        Long total = queryFactory
                .select(orderEntity.countDistinct())
                .from(orderEntity)
                .join(orderEntity.items, orderItemEntity)
                .where(
                        orderItemEntity.restaurant.restaurantId.eq(restaurantId),
                        orderEntity.createdAt.between(startDate, endDate),
                        isNotDeleted()
                )
                .fetchOne();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(orders, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        // 1. ID만 먼저 조회 (페이징)
        List<String> ids = queryFactory
                .select(orderEntity.id)
                .from(orderEntity)
                .where(isNotDeleted())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        // 2. ID로 fetch join
        List<OrderEntity> content = ids.isEmpty() ? List.of() :
                queryFactory
                        .selectFrom(orderEntity)
                        .leftJoin(orderEntity.items, orderItemEntity).fetchJoin()
                        .where(orderEntity.id.in(ids))
                        .orderBy(getOrderSpecifiers(pageable.getSort()))
                        .fetch();

        // 3. Total count
        Long total = queryFactory
                .select(orderEntity.count())
                .from(orderEntity)
                .where(isNotDeleted())
                .fetchOne();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(orders, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Order> findAllByStatus(OrderStatus status, Pageable pageable) {
        // 1. ID만 먼저 조회 (페이징)
        List<String> ids = queryFactory
                .select(orderEntity.id)
                .from(orderEntity)
                .where(
                        orderEntity.status.eq(status),
                        isNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        // 2. ID로 fetch join
        List<OrderEntity> content = ids.isEmpty() ? List.of() :
                queryFactory
                        .selectFrom(orderEntity)
                        .leftJoin(orderEntity.items, orderItemEntity).fetchJoin()
                        .where(orderEntity.id.in(ids))
                        .orderBy(getOrderSpecifiers(pageable.getSort()))
                        .fetch();

        // 3. Total count
        Long total = queryFactory
                .select(orderEntity.count())
                .from(orderEntity)
                .where(
                        orderEntity.status.eq(status),
                        isNotDeleted()
                )
                .fetchOne();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(orders, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Order> findByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        // 1. ID만 먼저 조회 (페이징)
        List<String> ids = queryFactory
                .select(orderEntity.id)
                .from(orderEntity)
                .where(
                        orderEntity.createdAt.between(startDate, endDate),
                        isNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        // 2. ID로 fetch join
        List<OrderEntity> content = ids.isEmpty() ? List.of() :
                queryFactory
                        .selectFrom(orderEntity)
                        .leftJoin(orderEntity.items, orderItemEntity).fetchJoin()
                        .where(orderEntity.id.in(ids))
                        .orderBy(getOrderSpecifiers(pageable.getSort()))
                        .fetch();

        // 3. Total count
        Long total = queryFactory
                .select(orderEntity.count())
                .from(orderEntity)
                .where(
                        orderEntity.createdAt.between(startDate, endDate),
                        isNotDeleted()
                )
                .fetchOne();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(orders, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Order> findPendingOrdersAfterPaymentTime(LocalDateTime beforeTime) {
        return queryFactory
                .selectFrom(orderEntity)
                .leftJoin(orderEntity.items, orderItemEntity).fetchJoin()  // N+1 방지
                .where(
                        orderEntity.status.eq(OrderStatus.PENDING),
                        orderEntity.paymentCompletedAt.before(beforeTime),
                        isNotDeleted()
                )
                .fetch()
                .stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        Integer count = queryFactory
                .selectOne()
                .from(orderEntity)
                .where(
                        orderEntity.id.eq(id),
                        isNotDeleted()
                )
                .fetchFirst();

        return count != null;
    }

    @Override
    @Transactional
    public void delete(Order order) {
        // 도메인에서 이미 softDelete() 호출했으므로
        // 그냥 save만 하면 됨
        save(order);
    }

    // === Private 헬퍼 메서드들 ===

    /**
     * 소프트 삭제되지 않은 것만
     */
    private BooleanExpression isNotDeleted() {
        return orderEntity.isDeleted.eq(false);
    }

    /**
     * Pageable의 Sort를 QueryDSL OrderSpecifier로 변환
     */
    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        return sort.stream()
                .map(order -> {
                    String property = order.getProperty();
                    boolean isAsc = order.isAscending();

                    return switch (property) {
                        case "createdAt" -> isAsc ?
                                orderEntity.createdAt.asc() : orderEntity.createdAt.desc();
                        case "requestedAt" -> isAsc ?
                                orderEntity.requestedAt.asc() : orderEntity.requestedAt.desc();
                        case "totalPrice" -> isAsc ?
                                orderEntity.totalPrice.asc() : orderEntity.totalPrice.desc();
                        case "status" -> isAsc ?
                                orderEntity.status.asc() : orderEntity.status.desc();
                        default -> isAsc ?
                                orderEntity.createdAt.asc() : orderEntity.createdAt.desc();
                    };
                })
                .toArray(OrderSpecifier[]::new);
    }
}