package xyz.sparta_project.manjok.domain.payment.presentation.rest.customer;

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
import xyz.sparta_project.manjok.domain.payment.application.service.PaymentQueryService;
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.presentation.rest.customer.dto.response.PaymentDetailResponse;
import xyz.sparta_project.manjok.domain.payment.presentation.rest.customer.dto.response.PaymentResponse;
import xyz.sparta_project.manjok.global.common.dto.PageInfo;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer용 결제 컨트롤러 (조회 전용)
 * - 기본 경로: /v1/customers/payments
 * - 권한: CUSTOMER
 */
@Slf4j
@RestController
@RequestMapping("/v1/customers/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerPaymentController {

    private final PaymentQueryService paymentQueryService;

    /**
     * 결제 상세 조회
     * GET /v1/customers/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPaymentDetail(
            @PathVariable String paymentId) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("결제 상세 조회 - userId: {}, paymentId: {}", userId, paymentId);

        Payment payment = paymentQueryService.getPaymentById(paymentId);

        // 권한 확인
        if (!payment.isPaidBy(userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("FORBIDDEN", "본인의 결제만 조회할 수 있습니다."));
        }

        PaymentDetailResponse response = PaymentDetailResponse.from(payment);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 결제 목록 조회
     * GET /v1/customers/payments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getMyPayments(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("내 결제 목록 조회 - userId: {}", userId);

        Page<Payment> paymentPage = paymentQueryService.getPaymentsByOrdererId(userId, pageable);

        List<PaymentResponse> content = paymentPage.getContent().stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());

        PageInfo pageInfo = PageInfo.of(
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.getNumberOfElements()
        );

        PageResponse<PaymentResponse> response = PageResponse.of(content, pageInfo);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 주문 ID로 결제 조회
     * GET /v1/customers/payments/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPaymentByOrderId(
            @PathVariable String orderId) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("주문 결제 조회 - userId: {}, orderId: {}", userId, orderId);

        Payment payment = paymentQueryService.getPaymentByOrderId(orderId);

        // 권한 확인
        if (!payment.isPaidBy(userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("FORBIDDEN", "본인의 결제만 조회할 수 있습니다."));
        }

        PaymentDetailResponse response = PaymentDetailResponse.from(payment);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}