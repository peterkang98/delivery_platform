package xyz.sparta_project.manjok.domain.payment.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.payment.application.event.PaymentEventPublisher;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentErrorCode;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentException;
import xyz.sparta_project.manjok.domain.payment.domain.model.*;
import xyz.sparta_project.manjok.domain.payment.domain.repository.PaymentRepository;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.PaymentCanceledEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.PaymentCompletedEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Command Service
 * 결제 생성, 승인, 취소 등 변경 작업 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    /**
     * 결제 생성 (주문에서 결제 요청 받았을 때)
     */
    public Payment createPayment(
            String orderId,
            String ordererId,
            String tossPaymentKey,
            String payToken,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String createdBy
    ) {
        log.info("결제 생성 시작 - orderId: {}, amount: {}", orderId, amount);

        // 중복 결제 확인
        if (paymentRepository.existsByTossPaymentKey(tossPaymentKey)) {
            throw new PaymentException(
                    PaymentErrorCode.PAYMENT_VERIFICATION_FAILED,
                    "이미 존재하는 결제입니다."
            );
        }

        Payment payment = Payment.create(
                orderId,
                ordererId,
                tossPaymentKey,
                payToken,
                amount,
                paymentMethod,
                createdBy,
                LocalDateTime.now()
        );

        Payment savedPayment = paymentRepository.save(payment);
        log.info("결제 생성 완료 - paymentId: {}", savedPayment.getId());

        return savedPayment;
    }

    /**
     * 결제 승인 처리
     */
    public Payment approvePayment(
            String paymentId,
            String approvedBy
    ) {
        log.info("결제 승인 시작 - paymentId: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 결제자 검증
        if (!payment.isPaidBy(approvedBy)) {
            throw new PaymentException(PaymentErrorCode.FORBIDDEN_PAYMENT_ACCESS);
        }

        LocalDateTime approvedAt = LocalDateTime.now();
        payment.approve(approvedAt, approvedBy);

        Payment approvedPayment = paymentRepository.save(payment);

        // 결제 완료 이벤트 발행
        eventPublisher.publishPaymentCompleted(
                PaymentCompletedEvent.builder()
                        .orderId(approvedPayment.getOrderId())
                        .userId(approvedPayment.getOrdererId())
                        .paymentId(approvedPayment.getId())
                        .paymentCompletedAt(approvedAt)
                        .build()
        );

        log.info("결제 승인 완료 - paymentId: {}", paymentId);
        return approvedPayment;
    }

    /**
     * 결제 실패 처리
     */
    public Payment failPayment(
            String paymentId,
            String failureReason,
            String failedBy
    ) {
        log.info("결제 실패 처리 - paymentId: {}, reason: {}", paymentId, failureReason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        payment.fail(failedBy);
        Payment failedPayment = paymentRepository.save(payment);

        // 결제 실패는 별도 이벤트 없이 로그만 남김 (필요시 추가 가능)
        log.info("결제 실패 처리 완료 - paymentId: {}", paymentId);
        return failedPayment;
    }

    /**
     * 결제 취소 (전액)
     */
    public Payment cancelPayment(
            String paymentId,
            CancellationType cancellationType,
            String cancelReason,
            String requestedBy
    ) {
        log.info("결제 취소 시작 - paymentId: {}, type: {}", paymentId, cancellationType);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 취소 권한 검증 (USER_REQUEST인 경우에만)
        if (cancellationType == CancellationType.USER_REQUEST
                && !payment.isPaidBy(requestedBy)) {
            throw new PaymentException(PaymentErrorCode.FORBIDDEN_PAYMENT_CANCEL);
        }

        BigDecimal cancelAmount = payment.getRemainingAmount();
        LocalDateTime cancelledAt = LocalDateTime.now();

        payment.addCancellation(
                cancellationType,
                cancelReason,
                requestedBy,
                cancelAmount,
                cancelledAt
        );

        Payment cancelledPayment = paymentRepository.save(payment);

        // 결제 취소 이벤트 발행
        eventPublisher.publishPaymentCanceled(
                PaymentCanceledEvent.builder()
                        .orderId(cancelledPayment.getOrderId())
                        .paymentId(cancelledPayment.getId())
                        .userId(cancelledPayment.getOrdererId())
                        .refundAmount(cancelAmount)
                        .cancelReason(cancelReason)
                        .paymentCanceledAt(cancelledAt)
                        .isRefundSuccessful(true) // 토스 API 호출 성공 시 true
                        .refundFailureReason(null)
                        .build()
        );

        log.info("결제 취소 완료 - paymentId: {}, cancelAmount: {}", paymentId, cancelAmount);
        return cancelledPayment;
    }

    /**
     * 결제 부분 취소
     */
    public Payment cancelPaymentPartially(
            String paymentId,
            BigDecimal cancelAmount,
            CancellationType cancellationType,
            String cancelReason,
            String requestedBy
    ) {
        log.info("결제 부분 취소 시작 - paymentId: {}, amount: {}", paymentId, cancelAmount);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 취소 권한 검증
        if (cancellationType == CancellationType.USER_REQUEST
                && !payment.isPaidBy(requestedBy)) {
            throw new PaymentException(PaymentErrorCode.FORBIDDEN_PAYMENT_CANCEL);
        }

        LocalDateTime cancelledAt = LocalDateTime.now();

        payment.addCancellation(
                cancellationType,
                cancelReason,
                requestedBy,
                cancelAmount,
                cancelledAt
        );

        Payment cancelledPayment = paymentRepository.save(payment);

        // 결제 취소 이벤트 발행
        eventPublisher.publishPaymentCanceled(
                PaymentCanceledEvent.builder()
                        .orderId(cancelledPayment.getOrderId())
                        .paymentId(cancelledPayment.getId())
                        .userId(cancelledPayment.getOrdererId())
                        .refundAmount(cancelAmount)
                        .cancelReason(cancelReason)
                        .paymentCanceledAt(cancelledAt)
                        .isRefundSuccessful(true)
                        .refundFailureReason(null)
                        .build()
        );

        log.info("결제 부분 취소 완료 - paymentId: {}, cancelAmount: {}", paymentId, cancelAmount);
        return cancelledPayment;
    }

    /**
     * 결제 금액 검증
     */
    public void validatePaymentAmount(String paymentId, BigDecimal orderAmount) {
        log.info("결제 금액 검증 - paymentId: {}, orderAmount: {}", paymentId, orderAmount);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        payment.validatePaymentAmount(orderAmount);

        log.info("결제 금액 검증 완료 - paymentId: {}", paymentId);
    }

    /**
     * 결제 소프트 삭제
     */
    public void deletePayment(String paymentId, String deletedBy) {
        log.info("결제 삭제 시작 - paymentId: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        payment.softDelete(deletedBy, LocalDateTime.now());
        paymentRepository.delete(payment);

        log.info("결제 삭제 완료 - paymentId: {}", paymentId);
    }
}