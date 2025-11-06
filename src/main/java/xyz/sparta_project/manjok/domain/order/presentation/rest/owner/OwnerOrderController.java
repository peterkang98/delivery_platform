package xyz.sparta_project.manjok.domain.order.presentation.rest.owner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.order.application.service.OrderCommandService;
import xyz.sparta_project.manjok.domain.order.application.service.OrderQueryService;
import xyz.sparta_project.manjok.domain.order.domain.model.Order;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderStatus;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.OrderResponse;
import xyz.sparta_project.manjok.domain.order.presentation.rest.owner.dto.ConfirmOrderResponse;

import java.time.LocalDateTime;

/**
 * Owner용 주문 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/owners/orders")
@RequiredArgsConstructor
public class OwnerOrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final xyz.sparta_project.manjok.domain.order.presentation.rest.owner.OwnerOrderMapper mapper;

    /**
     * 레스토랑의 주문 목록 조회
     */
    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Page<OrderResponse>> getRestaurantOrders(
            @PathVariable String restaurantId,
            @RequestHeader("X-User-Id") String ownerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("레스토랑 주문 목록 조회: restaurantId={}, ownerId={}", restaurantId, ownerId);

        Page<Order> orders = orderQueryService.getRestaurantOrders(restaurantId, pageable);
        Page<OrderResponse> response = orders.map(mapper::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 레스토랑의 주문 목록 조회 (상태 필터)
     */
    @GetMapping("/restaurants/{restaurantId}/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getRestaurantOrdersByStatus(
            @PathVariable String restaurantId,
            @PathVariable OrderStatus status,
            @RequestHeader("X-User-Id") String ownerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("레스토랑 주문 목록 조회 (상태 필터): restaurantId={}, status={}", restaurantId, status);

        Page<Order> orders = orderQueryService.getRestaurantOrdersByStatus(restaurantId, status, pageable);
        Page<OrderResponse> response = orders.map(mapper::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 레스토랑의 특정 기간 주문 목록 조회
     */
    @GetMapping("/restaurants/{restaurantId}/range")
    public ResponseEntity<Page<OrderResponse>> getRestaurantOrdersByDateRange(
            @PathVariable String restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader("X-User-Id") String ownerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("레스토랑 주문 기간 조회: restaurantId={}, startDate={}, endDate={}",
                restaurantId, startDate, endDate);

        Page<Order> orders = orderQueryService.getRestaurantOrdersByDateRange(
                restaurantId, startDate, endDate, pageable
        );
        Page<OrderResponse> response = orders.map(mapper::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 확인 (가게 확인)
     */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ConfirmOrderResponse> confirmOrder(
            @PathVariable String orderId,
            @RequestHeader("X-User-Id") String ownerId,
            @RequestParam String restaurantId
    ) {
        log.info("주문 확인 요청: orderId={}, restaurantId={}", orderId, restaurantId);

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
     */
    @PostMapping("/{orderId}/prepare")
    public ResponseEntity<ConfirmOrderResponse> startPreparing(
            @PathVariable String orderId,
            @RequestHeader("X-User-Id") String ownerId,
            @RequestParam String restaurantId
    ) {
        log.info("조리 시작 요청: orderId={}, restaurantId={}", orderId, restaurantId);

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
     */
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<ConfirmOrderResponse> startDelivering(
            @PathVariable String orderId,
            @RequestHeader("X-User-Id") String ownerId,
            @RequestParam String restaurantId
    ) {
        log.info("배달 시작 요청: orderId={}, restaurantId={}", orderId, restaurantId);

        orderCommandService.startDelivering(orderId, restaurantId);

        ConfirmOrderResponse response = ConfirmOrderResponse.builder()
                .orderId(orderId)
                .message("배달이 시작되었습니다.")
                .build();

        log.info("배달 시작 완료: orderId={}", orderId);

        return ResponseEntity.ok(response);
    }
}