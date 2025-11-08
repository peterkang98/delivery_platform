package xyz.sparta_project.manjok.domain.payment.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Domain Repository
 */
public interface PaymentRepository {

    /**
     * 결제 저장 (생성 or 업데이트)
     * - ID가 없으면 신규 생성
     * - ID가 있으면 더티체킹으로 업데이트
     */
    Payment save(Payment payment);

    /**
     * ID로 결제 조회
     */
    Optional<Payment> findById(String id);

    /**
     * ID로 결제 조회 (삭제된 것 포함)
     */
    Optional<Payment> findByIdIncludingDeleted(String id);

    /**
     * Order ID로 결제 조회
     */
    Optional<Payment> findByOrderId(String orderId);

    /**
     * Toss Payment Key로 결제 조회
     */
    Optional<Payment> findByTossPaymentKey(String tossPaymentKey);

    /**
     * 사용자의 결제 목록 조회 (페이징)
     */
    Page<Payment> findByOrdererId(String ordererId, Pageable pageable);

    /**
     * 사용자의 결제 목록 조회 with 상태 필터 (페이징)
     */
    Page<Payment> findByOrdererIdAndStatus(String ordererId, PaymentStatus status, Pageable pageable);

    /**
     * 전체 결제 목록 조회 (페이징)
     * 관리자용
     */
    Page<Payment> findAll(Pageable pageable);

    /**
     * 전체 결제 목록 조회 with 상태 필터 (페이징)
     * 관리자용
     */
    Page<Payment> findAllByStatus(PaymentStatus status, Pageable pageable);

    /**
     * 특정 기간 결제 목록 조회
     * 관리자용 - 통계/분석
     */
    Page<Payment> findByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * 특정 상태의 결제 목록 조회 (기간 필터 포함)
     * 관리자용 - 통계/분석
     */
    Page<Payment> findByStatusAndDateRange(
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * 결제 대기 상태로 일정 시간 지난 결제 조회
     * 배치용 - 자동 실패 처리 등
     */
    List<Payment> findPendingPaymentsBeforeTime(LocalDateTime beforeTime);

    /**
     * 승인 완료되고 취소되지 않은 결제 목록 조회 (기간 필터)
     * 정산용
     */
    List<Payment> findApprovedPaymentsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 결제 존재 여부 확인
     */
    boolean existsById(String id);

    /**
     * Toss Payment Key 존재 여부 확인
     */
    boolean existsByTossPaymentKey(String tossPaymentKey);

    /**
     * 소프트 삭제
     * (실제로는 도메인의 softDelete() 호출 후 save()하면 됨)
     */
    void delete(Payment payment);
}