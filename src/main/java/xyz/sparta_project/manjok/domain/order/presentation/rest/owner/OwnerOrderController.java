package xyz.sparta_project.manjok.domain.order.presentation.rest.owner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.order.application.service.OrderCommandService;
import xyz.sparta_project.manjok.domain.order.application.service.OrderQueryService;
import xyz.sparta_project.manjok.domain.order.domain.model.Order;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderStatus;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.OrderResponse;
import xyz.sparta_project.manjok.domain.order.presentation.rest.owner.dto.ConfirmOrderResponse;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;

import java.time.LocalDateTime;

/**
 * Owner용 주문 관리 API 컨트롤러
 * - 기본 경로: /v1/owners/orders
 * - 권한: OWNER
 */
@Slf4j
@RestController
@RequestMapping("/v1/owners/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerOrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final xyz.sparta_project.manjok.domain.order.presentation.rest.owner.OwnerOrderMapper mapper;

    /**
     * 레스토랑의 주문 목록 조회
     * GET /v1/owners/orders/restaurants/{restaurantId}
     */
    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Page<OrderResponse>> getRestaurantOrders(
            @PathVariable String restaurantId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("레스토랑 주문 목록 조회: restaurantId={}, ownerId={}", restaurantId, ownerId);

        // TODO: 레스토랑 소유자 검증 로직 추가
        Page<Order> orders = orderQueryService.getRestaurantOrders(restaurantId, pageable);
        Page<OrderResponse> response = orders.map(mapper::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 레스토랑의 주문 목록 조회 (상태 필터)
     * GET /v1/owners/orders/restaurants/{restaurantId}/status/{status}
     */
    @GetMapping("/restaurants/{restaurantId}/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getRestaurantOrdersByStatus(
            @PathVariable String restaurantId,
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("레스토랑 주문 목록 조회 (상태 필터): restaurantId={}, status={}, ownerId={}",
                restaurantId, status, ownerId);

        // TODO: 레스토랑 소유자 검증 로직 추가
        Page<Order> orders = orderQueryService.getRestaurantOrdersByStatus(restaurantId, status, pageable);
        Page<OrderResponse> response = orders.map(mapper::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 레스토랑의 특정 기간 주문 목록 조회
     * GET /v1/owners/orders/restaurants/{restaurantId}/range
     */
    @GetMapping("/restaurants/{restaurantId}/range")
    public ResponseEntity<Page<OrderResponse>> getRestaurantOrdersByDateRange(
            @PathVariable String restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("레스토랑 주문 기간 조회: restaurantId={}, startDate={}, endDate={}, ownerId={}",
                restaurantId, startDate, endDate, ownerId);

        // TODO: 레스토랑 소유자 검증 로직 추가
        Page<Order> orders = orderQueryService.getRestaurantOrdersByDateRange(
                restaurantId, startDate, endDate, pageable
        );
        Page<OrderResponse> response = orders.map(mapper::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 확인 (가게 확인)
     * POST /v1/owners/orders/{orderId}/confirm
     */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ConfirmOrderResponse> confirmOrder(
            @PathVariable String orderId,
            @RequestParam String restaurantId) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("주문 확인 요청: orderId={}, restaurantId={}, ownerId={}", orderId, restaurantId, ownerId);

        // TODO: 레스토랑 소유자 검증 로직 추가
        orderCommandService.confirmOrder(orderId, restaurantId);

        ConfirmOrderResponse response = ConfirmOrderResponse.builder()
                .orderId(orderId)
                .message("주문이 확인되었습니다.")
                .build();

        log.info("주문 확인 완료: orderId={}", orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * 조리 시작
     * POST /v1/owners/orders/{orderId}/prepare
     */
    @PostMapping("/{orderId}/prepare")
    public ResponseEntity<ConfirmOrderResponse> startPreparing(
            @PathVariable String orderId,
            @RequestParam String restaurantId) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("조리 시작 요청: orderId={}, restaurantId={}, ownerId={}", orderId, restaurantId, ownerId);

        // TODO: 레스토랑 소유자 검증 로직 추가
        orderCommandService.startPreparing(orderId, restaurantId);

        ConfirmOrderResponse response = ConfirmOrderResponse.builder()
                .orderId(orderId)
                .message("조리가 시작되었습니다.")
                .build();

        log.info("조리 시작 완료: orderId={}", orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * 배달 시작
     * POST /v1/owners/orders/{orderId}/deliver
     */
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<ConfirmOrderResponse> startDelivering(
            @PathVariable String orderId,
            @RequestParam String restaurantId) {

        String ownerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("배달 시작 요청: orderId={}, restaurantId={}, ownerId={}", orderId, restaurantId, ownerId);

        // TODO: 레스토랑 소유자 검증 로직 추가
        orderCommandService.startDelivering(orderId, restaurantId);

        ConfirmOrderResponse response = ConfirmOrderResponse.builder()
                .orderId(orderId)
                .message("배달이 시작되었습니다.")
                .build();

        log.info("배달 시작 완료: orderId={}", orderId);

        return ResponseEntity.ok(response);
    }
}