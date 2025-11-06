package xyz.sparta_project.manjok.domain.payment.presentation.rest.owner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.payment.application.service.PaymentQueryService;
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentStatus;
import xyz.sparta_project.manjok.domain.payment.presentation.rest.owner.dto.response.PaymentSummaryResponse;
import xyz.sparta_project.manjok.global.common.dto.PageInfo;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 점주용 결제 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/owner/payments")
@RequiredArgsConstructor
public class OwnerPaymentController {

    private final PaymentQueryService paymentQueryService;

    /**
     * 결제 상세 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> getPaymentDetail(
            @PathVariable String paymentId,
            @RequestHeader("X-User-Id") String ownerId
    ) {
        log.info("점주 결제 조회 - ownerId: {}, paymentId: {}", ownerId, paymentId);

        Payment payment = paymentQueryService.getPaymentById(paymentId);

        // TODO: 점주 권한 확인 (해당 주문이 자신의 레스토랑 주문인지 확인 필요)

        PaymentSummaryResponse response = PaymentSummaryResponse.from(payment);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 승인 완료된 결제 목록 조회 (정산용)
     */
    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<PaymentSummaryResponse>>> getApprovedPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader("X-User-Id") String ownerId
    ) {
        log.info("승인 결제 목록 조회 - ownerId: {}, period: {} ~ {}", ownerId, startDate, endDate);

        List<Payment> payments = paymentQueryService.getApprovedPaymentsByDateRange(startDate, endDate);

        // TODO: 점주의 레스토랑 필터링 필요

        List<PaymentSummaryResponse> response = payments.stream()
                .map(PaymentSummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}