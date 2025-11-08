package xyz.sparta_project.manjok.domain.payment.presentation.rest.admin;

import jakarta.validation.Valid;
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
import xyz.sparta_project.manjok.domain.payment.application.service.PaymentCommandService;
import xyz.sparta_project.manjok.domain.payment.application.service.PaymentQueryService;
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentStatus;
import xyz.sparta_project.manjok.domain.payment.presentation.rest.admin.dto.request.PaymentAdminCancelRequest;
import xyz.sparta_project.manjok.domain.payment.presentation.rest.customer.dto.response.PaymentDetailResponse;
import xyz.sparta_project.manjok.domain.payment.presentation.rest.customer.dto.response.PaymentResponse;
import xyz.sparta_project.manjok.global.common.dto.PageInfo;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin용 결제 관리 API 컨트롤러
 * - 기본 경로: /v1/admin/payments
 * - 권한: MANAGER, MASTER
 */
@Slf4j
@RestController
@RequestMapping("/v1/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
public class AdminPaymentController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    /**
     * 전체 결제 목록 조회
     * GET /v1/admin/payments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("전체 결제 목록 조회 - adminId: {}, status: {}", adminId, status);

        Page<Payment> paymentPage = status != null
                ? paymentQueryService.getAllPaymentsByStatus(status, pageable)
                : paymentQueryService.getAllPayments(pageable);

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
     * 결제 상세 조회
     * GET /v1/admin/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPaymentDetail(
            @PathVariable String paymentId) {

        String adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("결제 상세 조회 - adminId: {}, paymentId: {}", adminId, paymentId);

        Payment payment = paymentQueryService.getPaymentById(paymentId);
        PaymentDetailResponse response = PaymentDetailResponse.from(payment);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 기간별 결제 목록 조회
     * GET /v1/admin/payments/period
     */
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPaymentsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("기간별 결제 조회 - adminId: {}, period: {} ~ {}", adminId, startDate, endDate);

        Page<Payment> paymentPage = status != null
                ? paymentQueryService.getPaymentsByStatusAndDateRange(status, startDate, endDate, pageable)
                : paymentQueryService.getPaymentsByDateRange(startDate, endDate, pageable);

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
     * 관리자 결제 취소
     * POST /v1/admin/payments/{paymentId}/cancel
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody PaymentAdminCancelRequest request) {

        String adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("관리자 결제 취소 - adminId: {}, paymentId: {}", adminId, paymentId);

        Payment payment;
        if (request.getCancelAmount() == null) {
            // 전액 취소
            payment = paymentCommandService.cancelPayment(
                    paymentId,
                    request.getCancellationType(),
                    request.getCancelReason(),
                    adminId
            );
        } else {
            // 부분 취소
            payment = paymentCommandService.cancelPaymentPartially(
                    paymentId,
                    request.getCancelAmount(),
                    request.getCancellationType(),
                    request.getCancelReason(),
                    adminId
            );
        }

        PaymentResponse response = PaymentResponse.from(payment);
        return ResponseEntity.ok(ApiResponse.success(response, "결제가 취소되었습니다."));
    }

    /**
     * 결제 소프트 삭제
     * DELETE /v1/admin/payments/{paymentId}
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable String paymentId) {

        String adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("결제 삭제 - adminId: {}, paymentId: {}", adminId, paymentId);

        paymentCommandService.deletePayment(paymentId, adminId);

        return ResponseEntity.ok(ApiResponse.success(null, "결제가 삭제되었습니다."));
    }

    /**
     * 결제 대기 중인 결제 목록 조회 (배치 모니터링용)
     * GET /v1/admin/payments/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPendingPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeTime) {

        String adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("대기 중 결제 조회 - adminId: {}, beforeTime: {}", adminId, beforeTime);

        List<Payment> payments = paymentQueryService.getPendingPaymentsBeforeTime(beforeTime);

        List<PaymentResponse> response = payments.stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}