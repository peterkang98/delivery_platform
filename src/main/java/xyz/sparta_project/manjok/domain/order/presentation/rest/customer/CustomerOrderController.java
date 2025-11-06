package xyz.sparta_project.manjok.domain.order.presentation.rest.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.order.application.service.OrderCommandService;
import xyz.sparta_project.manjok.domain.order.application.service.OrderQueryService;
import xyz.sparta_project.manjok.domain.order.domain.model.*;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.CancelOrderRequest;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.CreateOrderRequest;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.OrderResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer용 주문 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/customers/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final xyz.sparta_project.manjok.domain.order.presentation.rest.customer.CustomerOrderMapper mapper;

    /**
     * 주문 생성
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("주문 생성 요청: userId={}", userId);

        // DTO → 도메인 변환
        Orderer orderer = mapper.toOrderer(request.getOrderer());
        List<OrderItem> items = request.getItems().stream()
                .map(mapper::toOrderItem)
                .collect(Collectors.toList());

        // 주문 생성
        Order order = orderCommandService.createOrder(
                orderer,
                items,
                request.getPaymentKey(),
                userId
        );

        // 도메인 → DTO 변환
        OrderResponse response = mapper.toOrderResponse(order);

        log.info("주문 생성 완료: orderId={}", order.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 주문 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestHeader("X-User-Id") String userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("주문 목록 조회: userId={}", userId);

        Page<Order> orders = orderQueryService.getUserOrders(userId, pageable);
        Page<OrderResponse> response = orders.map(mapper::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 내 주문 목록 조회 (상태 필터)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getMyOrdersByStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("주문 목록 조회 (상태 필터): userId={}, status={}", userId, status);

        Page<Order> orders = orderQueryService.getUserOrdersByStatus(userId, status, pageable);
        Page<OrderResponse> response = orders.map(mapper::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId
    ) {
        log.info("주문 상세 조회: userId={}, orderId={}", userId, orderId);

        Order order = orderQueryService.getOrder(orderId, userId);
        OrderResponse response = mapper.toOrderResponse(order);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId,
            @Valid @RequestBody CancelOrderRequest request
    ) {
        log.info("주문 취소 요청: userId={}, orderId={}", userId, orderId);

        orderCommandService.cancelOrder(orderId, request.getCancelReason(), userId);

        Order order = orderQueryService.getOrder(orderId, userId);
        OrderResponse response = mapper.toOrderResponse(order);

        log.info("주문 취소 완료: orderId={}", orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 완료 (배달 완료 확인)
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<OrderResponse> completeOrder(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId
    ) {
        log.info("주문 완료 요청: userId={}, orderId={}", userId, orderId);

        orderCommandService.completeOrder(orderId, userId);

        Order order = orderQueryService.getOrder(orderId, userId);
        OrderResponse response = mapper.toOrderResponse(order);

        log.info("주문 완료 처리 완료: orderId={}", orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 삭제 (소프트 삭제)
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId
    ) {
        log.info("주문 삭제 요청: userId={}, orderId={}", userId, orderId);

        orderCommandService.deleteOrder(orderId, userId);

        log.info("주문 삭제 완료: orderId={}", orderId);

        return ResponseEntity.noContent().build();
    }
}