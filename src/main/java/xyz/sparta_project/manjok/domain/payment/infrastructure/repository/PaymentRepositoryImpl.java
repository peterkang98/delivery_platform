package xyz.sparta_project.manjok.domain.payment.infrastructure.repository;

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
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentStatus;
import xyz.sparta_project.manjok.domain.payment.domain.repository.PaymentRepository;
import xyz.sparta_project.manjok.domain.payment.infrastructure.entity.PaymentEntity;
import xyz.sparta_project.manjok.domain.payment.infrastructure.jpa.PaymentJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static xyz.sparta_project.manjok.domain.payment.infrastructure.entity.QPaymentEntity.paymentEntity;

/**
 * Payment Repository 구현체
 */
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentEntity entity;

        if (payment.getId() == null) {
            // 신규 생성
            entity = PaymentEntity.from(payment);
            entity = paymentJpaRepository.save(entity);
        } else {
            // 기존 엔티티 조회 후 업데이트 (더티체킹)
            entity = paymentJpaRepository.findById(payment.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + payment.getId()));

            entity.updateFromDomain(payment);
            // 더티체킹으로 자동 업데이트됨 (save 호출 불필요)
        }

        return entity.toDomain();
    }

    @Override
    public Optional<Payment> findById(String id) {
        PaymentEntity entity = queryFactory
                .selectFrom(paymentEntity)
                .where(
                        paymentEntity.id.eq(id),
                        isNotDeleted()
                )
                .fetchOne();

        return Optional.ofNullable(entity)
                .map(PaymentEntity::toDomain);
    }

    @Override
    public Optional<Payment> findByIdIncludingDeleted(String id) {
        return paymentJpaRepository.findById(id)
                .map(PaymentEntity::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        PaymentEntity entity = queryFactory
                .selectFrom(paymentEntity)
                .where(
                        paymentEntity.orderId.eq(orderId),
                        isNotDeleted()
                )
                .fetchOne();

        return Optional.ofNullable(entity)
                .map(PaymentEntity::toDomain);
    }

    @Override
    public Optional<Payment> findByTossPaymentKey(String tossPaymentKey) {
        PaymentEntity entity = queryFactory
                .selectFrom(paymentEntity)
                .where(
                        paymentEntity.tossPaymentKey.eq(tossPaymentKey),
                        isNotDeleted()
                )
                .fetchOne();

        return Optional.ofNullable(entity)
                .map(PaymentEntity::toDomain);
    }

    @Override
    public Page<Payment> findByOrdererId(String ordererId, Pageable pageable) {
        List<PaymentEntity> content = queryFactory
                .selectFrom(paymentEntity)
                .where(
                        paymentEntity.ordererId.eq(ordererId),
                        isNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long total = queryFactory
                .select(paymentEntity.count())
                .from(paymentEntity)
                .where(
                        paymentEntity.ordererId.eq(ordererId),
                        isNotDeleted()
                )
                .fetchOne();

        List<Payment> payments = content.stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(payments, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Payment> findByOrdererIdAndStatus(String ordererId, PaymentStatus status, Pageable pageable) {
        List<PaymentEntity> content = queryFactory
                .selectFrom(paymentEntity)
                .where(
                        paymentEntity.ordererId.eq(ordererId),
                        paymentEntity.paymentStatus.eq(status),
                        isNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long total = queryFactory
                .select(paymentEntity.count())
                .from(paymentEntity)
                .where(
                        paymentEntity.ordererId.eq(ordererId),
                        paymentEntity.paymentStatus.eq(status),
                        isNotDeleted()
                )
                .fetchOne();

        List<Payment> payments = content.stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(payments, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Payment> findAll(Pageable pageable) {
        List<PaymentEntity> content = queryFactory
                .selectFrom(paymentEntity)
                .where(isNotDeleted())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long total = queryFactory
                .select(paymentEntity.count())
                .from(paymentEntity)
                .where(isNotDeleted())
                .fetchOne();

        List<Payment> payments = content.stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(payments, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Payment> findAllByStatus(PaymentStatus status, Pageable pageable) {
        List<PaymentEntity> content = queryFactory
                .selectFrom(paymentEntity)
                .where(
                        paymentEntity.paymentStatus.eq(status),
                        isNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long total = queryFactory
                .select(paymentEntity.count())
                .from(paymentEntity)
                .where(
                        paymentEntity.paymentStatus.eq(status),
                        isNotDeleted()
                )
                .fetchOne();

        List<Payment> payments = content.stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(payments, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Payment> findByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        List<PaymentEntity> content = queryFactory
                .selectFrom(paymentEntity)
                .where(
                        paymentEntity.createdAt.between(startDate, endDate),
                        isNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long total = queryFactory
                .select(paymentEntity.count())
                .from(paymentEntity)
                .where(
                        paymentEntity.createdAt.between(startDate, endDate),
                        isNotDeleted()
                )
                .fetchOne();

        List<Payment> payments = content.stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(payments, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Payment> findByStatusAndDateRange(
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        List<PaymentEntity> content = queryFactory
                .selectFrom(paymentEntity)
                .where(
                        paymentEntity.paymentStatus.eq(status),
                        paymentEntity.createdAt.between(startDate, endDate),
                        isNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long total = queryFactory
                .select(paymentEntity.count())
                .from(paymentEntity)
                .where(
                        paymentEntity.paymentStatus.eq(status),
                        paymentEntity.createdAt.between(startDate, endDate),
                        isNotDeleted()
                )
                .fetchOne();

        List<Payment> payments = content.stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(payments, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Payment> findPendingPaymentsBeforeTime(LocalDateTime beforeTime) {
        return queryFactory
                .selectFrom(paymentEntity)
                .where(
                        paymentEntity.paymentStatus.eq(PaymentStatus.PENDING),
                        paymentEntity.createdAt.before(beforeTime),
                        isNotDeleted()
                )
                .fetch()
                .stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findApprovedPaymentsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return queryFactory
                .selectFrom(paymentEntity)
                .where(
                        paymentEntity.paymentStatus.in(PaymentStatus.APPROVED, PaymentStatus.PARTIALLY_CANCELLED),
                        paymentEntity.approvedAt.between(startDate, endDate),
                        isNotDeleted()
                )
                .fetch()
                .stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        Integer count = queryFactory
                .selectOne()
                .from(paymentEntity)
                .where(
                        paymentEntity.id.eq(id),
                        isNotDeleted()
                )
                .fetchFirst();

        return count != null;
    }

    @Override
    public boolean existsByTossPaymentKey(String tossPaymentKey) {
        Integer count = queryFactory
                .selectOne()
                .from(paymentEntity)
                .where(
                        paymentEntity.tossPaymentKey.eq(tossPaymentKey),
                        isNotDeleted()
                )
                .fetchFirst();

        return count != null;
    }

    @Override
    @Transactional
    public void delete(Payment payment) {
        // 도메인에서 이미 softDelete() 호출했으므로
        // 그냥 save만 하면 됨
        save(payment);
    }

    // === Private 헬퍼 메서드들 ===

    /**
     * 소프트 삭제되지 않은 것만
     */
    private BooleanExpression isNotDeleted() {
        return paymentEntity.isDeleted.eq(false);
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
                                paymentEntity.createdAt.asc() : paymentEntity.createdAt.desc();
                        case "approvedAt" -> isAsc ?
                                paymentEntity.approvedAt.asc() : paymentEntity.approvedAt.desc();
                        case "amount" -> isAsc ?
                                paymentEntity.amount.asc() : paymentEntity.amount.desc();
                        case "paymentStatus" -> isAsc ?
                                paymentEntity.paymentStatus.asc() : paymentEntity.paymentStatus.desc();
                        default -> isAsc ?
                                paymentEntity.createdAt.asc() : paymentEntity.createdAt.desc();
                    };
                })
                .toArray(OrderSpecifier[]::new);
    }
}