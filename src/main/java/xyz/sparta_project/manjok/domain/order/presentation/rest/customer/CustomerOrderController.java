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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.order.application.service.OrderCommandService;
import xyz.sparta_project.manjok.domain.order.application.service.OrderQueryService;
import xyz.sparta_project.manjok.domain.order.domain.model.*;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.CancelOrderRequest;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.CreateOrderRequest;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.OrderResponse;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer용 주문 API 컨트롤러
 * - 기본 경로: /v1/customers/orders
 * - 권한: CUSTOMER
 */
@Slf4j
@RestController
@RequestMapping("/v1/customers/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final xyz.sparta_project.manjok.domain.order.presentation.rest.customer.CustomerOrderMapper mapper;

    /**
     * 주문 생성
     * POST /v1/customers/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("주문 생성 요청: userId={}", userId);

        // DTO → 도메인 변환
        Orderer orderer = mapper.toOrderer(request.getOrderer());

        orderer = Orderer.create(
                userId,
                orderer.getName(),
                orderer.getPhone(),
                orderer.getAddress(),
                orderer.getDeliveryRequest()
        );

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
     * GET /v1/customers/orders
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("주문 목록 조회: userId={}", userId);

        Page<Order> orders = orderQueryService.getUserOrders(userId, pageable);
        Page<OrderResponse> response = orders.map(mapper::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 내 주문 목록 조회 (상태 필터)
     * GET /v1/customers/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getMyOrdersByStatus(
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("주문 목록 조회 (상태 필터): userId={}, status={}", userId, status);

        Page<Order> orders = orderQueryService.getUserOrdersByStatus(userId, status, pageable);
        Page<OrderResponse> response = orders.map(mapper::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세 조회
     * GET /v1/customers/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable String orderId) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("주문 상세 조회: userId={}, orderId={}", userId, orderId);

        Order order = orderQueryService.getOrder(orderId, userId);
        OrderResponse response = mapper.toOrderResponse(order);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 취소
     * POST /v1/customers/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable String orderId,
            @Valid @RequestBody CancelOrderRequest request) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("주문 취소 요청: userId={}, orderId={}", userId, orderId);

        orderCommandService.cancelOrder(orderId, request.getCancelReason(), userId);

        Order order = orderQueryService.getOrder(orderId, userId);
        OrderResponse response = mapper.toOrderResponse(order);

        log.info("주문 취소 완료: orderId={}", orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 완료 (배달 완료 확인)
     * POST /v1/customers/orders/{orderId}/complete
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<OrderResponse> completeOrder(
            @PathVariable String orderId) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("주문 완료 요청: userId={}, orderId={}", userId, orderId);

        orderCommandService.completeOrder(orderId, userId);

        Order order = orderQueryService.getOrder(orderId, userId);
        OrderResponse response = mapper.toOrderResponse(order);

        log.info("주문 완료 처리 완료: orderId={}", orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 삭제 (소프트 삭제)
     * DELETE /v1/customers/orders/{orderId}
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable String orderId) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("주문 삭제 요청: userId={}, orderId={}", userId, orderId);

        orderCommandService.deleteOrder(orderId, userId);

        log.info("주문 삭제 완료: orderId={}", orderId);

        return ResponseEntity.noContent().build();
    }
}