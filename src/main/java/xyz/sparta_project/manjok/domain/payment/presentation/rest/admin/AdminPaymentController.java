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
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.payment.application.service.PaymentCommandService;
import xyz.sparta_project.manjok.domain.payment.application.service.PaymentQueryService;
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentStatus;
import xyz.sparta_project.manjok.domain.payment.presentation.rest.admin.dto.request.PaymentAdminCancelRequest;
import xyz.sparta_project.manjok.domain.payment.presentation.rest.customer.dto.response.PaymentDetailResponse;
import xyz.sparta_project.manjok.domain.payment.presentation.rest.customer.dto.response.PaymentResponse;
import xyz.sparta_project.manjok.global.common.dto.PageInfo;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자용 결제 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    /**
     * 전체 결제 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("X-User-Id") String adminId
    ) {
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
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPaymentDetail(
            @PathVariable String paymentId,
            @RequestHeader("X-User-Id") String adminId
    ) {
        log.info("결제 상세 조회 - adminId: {}, paymentId: {}", adminId, paymentId);

        Payment payment = paymentQueryService.getPaymentById(paymentId);
        PaymentDetailResponse response = PaymentDetailResponse.from(payment);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 기간별 결제 목록 조회
     */
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPaymentsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("X-User-Id") String adminId
    ) {
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
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody PaymentAdminCancelRequest request,
            @RequestHeader("X-User-Id") String adminId
    ) {
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
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable String paymentId,
            @RequestHeader("X-User-Id") String adminId
    ) {
        log.info("결제 삭제 - adminId: {}, paymentId: {}", adminId, paymentId);

        paymentCommandService.deletePayment(paymentId, adminId);

        return ResponseEntity.ok(ApiResponse.success(null, "결제가 삭제되었습니다."));
    }

    /**
     * 결제 대기 중인 결제 목록 조회 (배치 모니터링용)
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPendingPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeTime,
            @RequestHeader("X-User-Id") String adminId
    ) {
        log.info("대기 중 결제 조회 - adminId: {}, beforeTime: {}", adminId, beforeTime);

        List<Payment> payments = paymentQueryService.getPendingPaymentsBeforeTime(beforeTime);

        List<PaymentResponse> response = payments.stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}