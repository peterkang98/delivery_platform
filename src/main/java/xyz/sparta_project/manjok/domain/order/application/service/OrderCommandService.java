package xyz.sparta_project.manjok.domain.order.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.order.application.event.OrderEventPublisher;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;
import xyz.sparta_project.manjok.domain.order.domain.model.*;
import xyz.sparta_project.manjok.domain.order.domain.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Command Service
 * 주문 생성, 수정, 삭제 등의 커맨드 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    /**
     * 주문 생성 (결제 대기 상태)
     */
    public Order createOrder(
            Orderer orderer,
            List<OrderItem> items,
            String paymentKey, // 임시 결제 ID (토스페이먼츠 등에서 발급한 paymentKey)
            String createdBy
    ) {
        log.info("주문 생성 시작: userId={}", orderer.getUserId());

        // Payment 생성 (결제 대기 상태) - 아직 Payment 도메인의 ID가 없으므로 paymentKey 사용
        Payment payment = Payment.createPending(paymentKey);

        // 주문 생성
        Order order = Order.create(
                orderer,
                items,
                payment,
                LocalDateTime.now(),
                createdBy
        );

        // 저장
        Order savedOrder = orderRepository.save(order);

        log.info("주문 생성 완료: orderId={}, status=PAYMENT_PENDING, paymentKey={}",
                savedOrder.getId(), paymentKey);

        // Payment 도메인에게 결제 요청 이벤트 발행
        orderEventPublisher.publishPaymentRequested(savedOrder, paymentKey);

        return savedOrder;
    }

    /**
     * 결제 완료 처리 (Payment 도메인으로부터 이벤트 수신)
     * Payment 도메인이 결제 검증 및 저장을 완료한 후 호출됨
     */
    public void completePayment(
            String orderId,
            String paymentId, // Payment 도메인의 실제 엔티티 ID
            LocalDateTime paymentCompletedAt,
            String userId
    ) {
        log.info("결제 완료 처리 시작: orderId={}, paymentId={}", orderId, paymentId);

        // Order 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 주문한 사용자 확인
        if (!order.isOrderedBy(userId)) {
            log.error("주문자 불일치: orderId={}, expectedUserId={}, actualUserId={}",
                    orderId, order.getOrderer().getUserId(), userId);
            throw new OrderException(OrderErrorCode.FORBIDDEN_ORDER_ACCESS);
        }

        // 이미 결제 완료된 주문인지 확인
        if (order.getStatus().isPaymentCompleted()) {
            log.warn("이미 결제 완료된 주문: orderId={}, status={}", orderId, order.getStatus());
            return;
        }

        // 도메인 로직으로 결제 완료 처리
        // paymentId: Payment 도메인의 실제 ID, isPaid: true
        order.completePayment(paymentId, paymentCompletedAt, "SYSTEM");

        // 저장 (더티체킹)
        orderRepository.save(order);

        log.info("결제 완료 처리 완료: orderId={}, paymentId={}, status=PAYMENT_COMPLETED",
                orderId, paymentId);

        // 주문 대기 상태로 자동 전환
        order.toPending("SYSTEM");
        orderRepository.save(order);

        log.info("주문 대기 상태 전환 완료: orderId={}, status=PENDING", orderId);
    }

    /**
     * 가게 확인
     */
    public void confirmOrder(String orderId, String restaurantId) {
        log.info("주문 확인 시작: orderId={}, restaurantId={}", orderId, restaurantId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 레스토랑 확인 (OrderItem에서 레스토랑 ID 확인)
        boolean isValidRestaurant = order.getItems().stream()
                .anyMatch(item -> item.getRestaurantId().equals(restaurantId));

        if (!isValidRestaurant) {
            log.error("레스토랑 권한 없음: orderId={}, restaurantId={}", orderId, restaurantId);
            throw new OrderException(OrderErrorCode.FORBIDDEN_ORDER_ACCESS);
        }

        order.confirm(LocalDateTime.now(), restaurantId);
        orderRepository.save(order);

        log.info("주문 확인 완료: orderId={}, status=CONFIRMED", orderId);
    }

    /**
     * 조리 시작
     */
    public void startPreparing(String orderId, String restaurantId) {
        log.info("조리 시작: orderId={}, restaurantId={}", orderId, restaurantId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 레스토랑 확인
        validateRestaurantAccess(order, restaurantId);

        order.startPreparing(restaurantId);
        orderRepository.save(order);

        log.info("조리 시작 완료: orderId={}, status=PREPARING", orderId);
    }

    /**
     * 배달 시작
     */
    public void startDelivering(String orderId, String restaurantId) {
        log.info("배달 시작: orderId={}, restaurantId={}", orderId, restaurantId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 레스토랑 확인
        validateRestaurantAccess(order, restaurantId);

        order.startDelivering(restaurantId);
        orderRepository.save(order);

        log.info("배달 시작 완료: orderId={}, status=DELIVERING", orderId);
    }

    /**
     * 주문 완료
     */
    public void completeOrder(String orderId, String userId) {
        log.info("주문 완료 처리 시작: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 사용자 확인
        if (!order.isOrderedBy(userId)) {
            throw new OrderException(OrderErrorCode.FORBIDDEN_ORDER_ACCESS);
        }

        order.complete(LocalDateTime.now(), userId);
        orderRepository.save(order);

        log.info("주문 완료 처리 완료: orderId={}, status=COMPLETED", orderId);
    }

    /**
     * 주문 취소 요청
     * 취소 가능 여부만 검증하고 Payment에 환불 요청 이벤트 발행
     * 실제 상태 변경은 환불 완료 후 completeOrderCancellation()에서 수행
     */
    public void cancelOrder(String orderId, String cancelReason, String userId) {
        log.info("주문 취소 요청 시작: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 권한 확인
        if (!order.isOrderedBy(userId)) {
            throw new OrderException(OrderErrorCode.FORBIDDEN_ORDER_CANCEL);
        }

        // 취소 가능 여부만 검증 (상태 변경하지 않음)
        // Order 도메인에 취소 가능 여부 검증 메서드 필요
        if (!order.isCancelable()) {
            throw new OrderException(OrderErrorCode.CANNOT_CANCEL_ORDER,
                    "취소 가능한 상태가 아니거나 취소 가능 시간이 지났습니다.");
        }

        // 취소 사유 임시 저장을 위한 필드 필요 (또는 이벤트에만 포함)
        // 실제 상태는 환불 완료 후 변경됨

        log.info("주문 취소 가능 검증 완료: orderId={}", orderId);

        // Payment에 환불 요청 이벤트 발행
        // OrderCancelRequestedEvent에 cancelReason 포함
        orderEventPublisher.publishOrderCancelRequested(order, cancelReason, userId);

        log.info("환불 요청 이벤트 발행 완료: orderId={}, paymentId={}",
                orderId, order.getPayment().getPaymentId());
    }

    /**
     * 주문 취소 완료 처리
     * Payment에서 환불이 완료된 후 호출됨 (PaymentCanceledEvent 수신 시)
     */
    public void completeOrderCancellation(String orderId, String paymentId,
                                          String cancelReason, String userId) {
        log.info("주문 취소 완료 처리 시작: orderId={}, paymentId={}", orderId, paymentId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // Payment ID 확인
        if (!order.getPayment().getPaymentId().equals(paymentId)) {
            log.error("결제 정보 불일치: orderId={}, expectedPaymentId={}, actualPaymentId={}",
                    orderId, paymentId, order.getPayment().getPaymentId());
            throw new OrderException(OrderErrorCode.INVALID_PAYMENT,
                    "결제 정보가 일치하지 않습니다.");
        }

        // 취소 가능 여부 재확인
        if (!order.isCancelable()) {
            log.error("취소 불가능한 상태: orderId={}, status={}", orderId, order.getStatus());
            throw new OrderException(OrderErrorCode.CANNOT_CANCEL_ORDER,
                    "취소 가능한 상태가 아닙니다.");
        }

        // 환불 완료 후 주문 상태를 CANCELED로 변경
        order.cancel(cancelReason, LocalDateTime.now(), "SYSTEM");

        orderRepository.save(order);

        log.info("주문 취소 완료 처리 완료: orderId={}, status=CANCELED", orderId);
    }

    /**
     * 환불 실패 처리
     * Payment에서 환불이 실패한 경우 호출됨 (PaymentCanceledEvent에서 isRefundSuccessful=false)
     */
    public void handleRefundFailure(String orderId, String failureReason, String userId) {
        log.error("주문 환불 실패 처리 시작: orderId={}, reason={}", orderId, failureReason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // TODO: 환불 실패 시 보상 로직 구현
        // 구체적인 구현은 비즈니스 요구사항에 따라 결정
        // 예시:
        // 1. 관리자에게 알림 전송
        //    notificationService.notifyAdminRefundFailure(orderId, failureReason);
        //
        // 2. 환불 재시도 큐에 추가
        //    refundRetryQueue.enqueue(orderId, order.getPayment().getPaymentId());
        //
        // 3. 주문에 환불 실패 플래그 추가 (도메인 모델 확장 필요)
        //    order.markRefundFailed(failureReason);
        //
        // 4. 고객 센터 티켓 자동 생성
        //    customerServiceTicketService.createRefundFailureTicket(orderId, failureReason);
        //
        // 5. 고객 알림
        //    notificationService.notifyUserRefundIssue(userId, orderId);

        log.warn("환불 실패 - 수동 처리 필요: orderId={}, failureReason={}, userId={}",
                orderId, failureReason, userId);

        // 임시: 로그만 남기고 추후 보상 로직 추가
    }

    /**
     * 소프트 삭제
     */
    public void deleteOrder(String orderId, String userId) {
        log.info("주문 삭제 시작: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 권한 확인
        if (!order.isOrderedBy(userId)) {
            throw new OrderException(OrderErrorCode.FORBIDDEN_ORDER_ACCESS);
        }

        order.softDelete(userId, LocalDateTime.now());
        orderRepository.save(order);

        log.info("주문 삭제 완료: orderId={}", orderId);
    }

    /**
     * 레스토랑 접근 권한 확인
     */
    private void validateRestaurantAccess(Order order, String restaurantId) {
        boolean hasAccess = order.getItems().stream()
                .anyMatch(item -> item.getRestaurantId().equals(restaurantId));

        if (!hasAccess) {
            throw new OrderException(OrderErrorCode.FORBIDDEN_ORDER_ACCESS);
        }
    }
}