package xyz.sparta_project.manjok.domain.order.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;
import xyz.sparta_project.manjok.domain.order.domain.model.Order;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderStatus;
import xyz.sparta_project.manjok.domain.order.domain.repository.OrderRepository;

import java.time.LocalDateTime;

/**
 * Order Query Service
 * 주문 조회 전용 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;

    /**
     * 주문 단건 조회
     */
    public Order getOrder(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 권한 확인
        if (!order.isOrderedBy(userId)) {
            throw new OrderException(OrderErrorCode.FORBIDDEN_ORDER_ACCESS);
        }

        return order;
    }

    /**
     * 사용자의 주문 목록 조회
     */
    public Page<Order> getUserOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    /**
     * 사용자의 주문 목록 조회 (상태 필터)
     */
    public Page<Order> getUserOrdersByStatus(String userId, OrderStatus status, Pageable pageable) {
        return orderRepository.findByUserIdAndStatus(userId, status, pageable);
    }

    /**
     * 레스토랑의 주문 목록 조회
     */
    public Page<Order> getRestaurantOrders(String restaurantId, Pageable pageable) {
        return orderRepository.findByRestaurantId(restaurantId, pageable);
    }

    /**
     * 레스토랑의 주문 목록 조회 (상태 필터)
     */
    public Page<Order> getRestaurantOrdersByStatus(String restaurantId, OrderStatus status, Pageable pageable) {
        return orderRepository.findByRestaurantIdAndStatus(restaurantId, status, pageable);
    }

    /**
     * 레스토랑의 특정 기간 주문 목록 조회
     */
    public Page<Order> getRestaurantOrdersByDateRange(
            String restaurantId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return orderRepository.findByRestaurantIdAndDateRange(restaurantId, startDate, endDate, pageable);
    }

    /**
     * 전체 주문 목록 조회 (관리자용)
     */
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /**
     * 전체 주문 목록 조회 (상태 필터, 관리자용)
     */
    public Page<Order> getAllOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findAllByStatus(status, pageable);
    }

    /**
     * 특정 기간 주문 목록 조회 (관리자용)
     */
    public Page<Order> getOrdersByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return orderRepository.findByDateRange(startDate, endDate, pageable);
    }
}