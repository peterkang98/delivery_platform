package xyz.sparta_project.manjok.domain.payment.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentErrorCode;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentException;
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentStatus;
import xyz.sparta_project.manjok.domain.payment.domain.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Query Service
 * 결제 조회 작업 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    /**
     * 결제 ID로 조회
     */
    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    /**
     * 결제 ID로 조회 (삭제된 것 포함)
     */
    public Payment getPaymentByIdIncludingDeleted(String paymentId) {
        return paymentRepository.findByIdIncludingDeleted(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    /**
     * 주문 ID로 결제 조회
     */
    public Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    /**
     * Toss Payment Key로 조회
     */
    public Payment getPaymentByTossPaymentKey(String tossPaymentKey) {
        return paymentRepository.findByTossPaymentKey(tossPaymentKey)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    /**
     * 사용자의 결제 목록 조회
     */
    public Page<Payment> getPaymentsByOrdererId(String ordererId, Pageable pageable) {
        return paymentRepository.findByOrdererId(ordererId, pageable);
    }

    /**
     * 사용자의 결제 목록 조회 (상태 필터)
     */
    public Page<Payment> getPaymentsByOrdererIdAndStatus(
            String ordererId,
            PaymentStatus status,
            Pageable pageable
    ) {
        return paymentRepository.findByOrdererIdAndStatus(ordererId, status, pageable);
    }

    /**
     * 전체 결제 목록 조회 (관리자)
     */
    public Page<Payment> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    /**
     * 전체 결제 목록 조회 (상태 필터, 관리자)
     */
    public Page<Payment> getAllPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findAllByStatus(status, pageable);
    }

    /**
     * 기간별 결제 목록 조회
     */
    public Page<Payment> getPaymentsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return paymentRepository.findByDateRange(startDate, endDate, pageable);
    }

    /**
     * 상태 및 기간별 결제 목록 조회
     */
    public Page<Payment> getPaymentsByStatusAndDateRange(
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return paymentRepository.findByStatusAndDateRange(status, startDate, endDate, pageable);
    }

    /**
     * 승인 완료된 결제 목록 조회 (정산용)
     */
    public List<Payment> getApprovedPaymentsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return paymentRepository.findApprovedPaymentsByDateRange(startDate, endDate);
    }

    /**
     * 결제 대기 중인 결제 목록 조회 (배치용)
     */
    public List<Payment> getPendingPaymentsBeforeTime(LocalDateTime beforeTime) {
        return paymentRepository.findPendingPaymentsBeforeTime(beforeTime);
    }

    /**
     * 결제 존재 여부 확인
     */
    public boolean existsPayment(String paymentId) {
        return paymentRepository.existsById(paymentId);
    }
}