package xyz.sparta_project.manjok.domain.order.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.sparta_project.manjok.domain.order.domain.model.Order;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Domain Repository
 */
public interface OrderRepository {

    /**
     * 주문 저장 (생성 or 업데이트)
     * - ID가 없으면 신규 생성
     * - ID가 있으면 더티체킹으로 업데이트
     */
    Order save(Order order);

    /**
     * ID로 주문 조회
     */
    Optional<Order> findById(String id);

    /**
     * ID로 주문 조회 (삭제된 것 포함)
     */
    Optional<Order> findByIdIncludingDeleted(String id);

    /**
     * 사용자의 주문 목록 조회 (페이징)
     */
    Page<Order> findByUserId(String userId, Pageable pageable);

    /**
     * 사용자의 주문 목록 조회 with 상태 필터 (페이징)
     */
    Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);

    /**
     * 레스토랑의 주문 목록 조회 (페이징)
     * 매장 주인용
     */
    Page<Order> findByRestaurantId(String restaurantId, Pageable pageable);

    /**
     * 레스토랑의 주문 목록 조회 with 상태 필터 (페이징)
     * 매장 주인용
     */
    Page<Order> findByRestaurantIdAndStatus(String restaurantId, OrderStatus status, Pageable pageable);

    /**
     * 레스토랑의 특정 기간 주문 목록 조회
     * 매장 주인용 - 통계/분석
     */
    Page<Order> findByRestaurantIdAndDateRange(
            String restaurantId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * 전체 주문 목록 조회 (페이징)
     * 관리자용
     */
    Page<Order> findAll(Pageable pageable);

    /**
     * 전체 주문 목록 조회 with 상태 필터 (페이징)
     * 관리자용
     */
    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    /**
     * 특정 기간 주문 목록 조회
     * 관리자용
     */
    Page<Order> findByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * 결제 완료 후 일정 시간 지난 미확인 주문 조회
     * 배치용 - 자동 취소 등
     */
    List<Order> findPendingOrdersAfterPaymentTime(LocalDateTime beforeTime);

    /**
     * 주문 존재 여부 확인
     */
    boolean existsById(String id);

    /**
     * 소프트 삭제
     * (실제로는 도메인의 softDelete() 호출 후 save()하면 됨)
     */
    void delete(Order order);
}