package xyz.sparta_project.manjok.domain.payment.infrastructure.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.payment.domain.model.*;
import xyz.sparta_project.manjok.domain.payment.domain.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("PaymentRepository 통합 테스트")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment samplePayment;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        samplePayment = Payment.create(
                "ORD-001",
                "USER-001",
                "TOSS-KEY-001",
                "PAY-TOKEN-001",
                BigDecimal.valueOf(10000),
                PaymentMethod.CARD,
                "system",
                now
        );
    }

    @Nested
    @DisplayName("결제 저장 및 조회")
    class SaveAndFind {

        @Test
        @DisplayName("새로운 결제 저장 성공")
        void save_new_payment() {
            // when
            Payment saved = paymentRepository.save(samplePayment);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getOrderId()).isEqualTo("ORD-001");
            assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("ID로 결제 조회 성공")
        void findById_success() {
            // given
            Payment saved = paymentRepository.save(samplePayment);

            // when
            Optional<Payment> found = paymentRepository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getOrderId()).isEqualTo("ORD-001");
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 빈 Optional 반환")
        void findById_not_found() {
            // when
            Optional<Payment> found = paymentRepository.findById("NON-EXISTENT");

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Order ID로 결제 조회 성공")
        void findByOrderId_success() {
            // given
            paymentRepository.save(samplePayment);

            // when
            Optional<Payment> found = paymentRepository.findByOrderId("ORD-001");

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getOrderId()).isEqualTo("ORD-001");
        }

        @Test
        @DisplayName("Toss Payment Key로 결제 조회 성공")
        void findByTossPaymentKey_success() {
            // given
            paymentRepository.save(samplePayment);

            // when
            Optional<Payment> found = paymentRepository.findByTossPaymentKey("TOSS-KEY-001");

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getTossPaymentKey()).isEqualTo("TOSS-KEY-001");
        }
    }

    @Nested
    @DisplayName("결제 업데이트")
    class UpdatePayment {

        @Test
        @DisplayName("결제 승인 후 업데이트 성공")
        void update_payment_after_approval() {
            // given
            Payment saved = paymentRepository.save(samplePayment);
            saved.approve(now, "approver");

            // when
            Payment updated = paymentRepository.save(saved);

            // then
            assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(updated.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 취소 추가 후 업데이트 성공")
        void update_payment_with_cancellation() {
            // given
            Payment saved = paymentRepository.save(samplePayment);
            saved.approve(now, "approver");
            saved.addCancellation(
                    CancellationType.USER_REQUEST,
                    "고객 변심",
                    "USER-001",
                    BigDecimal.valueOf(3000),
                    now
            );

            // when
            Payment updated = paymentRepository.save(saved);

            // then
            assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELLED);
            assertThat(updated.getCancellations()).hasSize(1);
            assertThat(updated.getTotalCancelledAmount()).isEqualTo(BigDecimal.valueOf(3000));
        }

        @Test
        @DisplayName("저장 후 조회하여 도메인 변경 후 재저장 - 더티체킹 동작 확인")
        void update_with_dirty_checking() {
            // given - 초기 저장
            Payment saved = paymentRepository.save(samplePayment);
            String paymentId = saved.getId();

            // when - 조회 후 승인 처리
            Payment found = paymentRepository.findById(paymentId).orElseThrow();
            found.approve(now, "approver");
            Payment afterApproval = paymentRepository.save(found);

            // then - 승인 상태 확인
            assertThat(afterApproval.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);

            // when - 다시 조회 후 취소 추가
            Payment foundAgain = paymentRepository.findById(paymentId).orElseThrow();
            foundAgain.addCancellation(
                    CancellationType.USER_REQUEST,
                    "부분 취소",
                    "USER-001",
                    BigDecimal.valueOf(3000),
                    now
            );
            Payment afterCancellation = paymentRepository.save(foundAgain);

            // then - 부분 취소 상태 및 취소 내역 확인
            assertThat(afterCancellation.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELLED);
            assertThat(afterCancellation.getCancellations()).hasSize(1);
            assertThat(afterCancellation.getTotalCancelledAmount()).isEqualTo(BigDecimal.valueOf(3000));
        }

        @Test
        @DisplayName("여러 번의 부분 취소 업데이트")
        void multiple_partial_cancellations() {
            // given
            Payment saved = paymentRepository.save(samplePayment);
            saved.approve(now, "approver");
            paymentRepository.save(saved);

            // when - 첫 번째 부분 취소
            Payment found1 = paymentRepository.findById(saved.getId()).orElseThrow();
            found1.addCancellation(
                    CancellationType.USER_REQUEST,
                    "부분 취소 1",
                    "USER-001",
                    BigDecimal.valueOf(3000),
                    now
            );
            paymentRepository.save(found1);

            // when - 두 번째 부분 취소
            Payment found2 = paymentRepository.findById(saved.getId()).orElseThrow();
            found2.addCancellation(
                    CancellationType.USER_REQUEST,
                    "부분 취소 2",
                    "USER-001",
                    BigDecimal.valueOf(2000),
                    now
            );
            Payment final1 = paymentRepository.save(found2);

            // then
            assertThat(final1.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELLED);
            assertThat(final1.getCancellations()).hasSize(2);
            assertThat(final1.getTotalCancelledAmount()).isEqualTo(BigDecimal.valueOf(5000));
            assertThat(final1.getRemainingAmount()).isEqualTo(BigDecimal.valueOf(5000));
        }
    }

    @Nested
    @DisplayName("사용자별 결제 조회")
    class FindByOrderer {

        @Test
        @DisplayName("사용자의 결제 목록 페이징 조회")
        void findByOrdererId_with_pagination() {
            // given
            for (int i = 1; i <= 5; i++) {
                Payment payment = Payment.create(
                        "ORD-00" + i,
                        "USER-001",
                        "TOSS-KEY-00" + i,
                        "PAY-TOKEN-00" + i,
                        BigDecimal.valueOf(10000),
                        PaymentMethod.CARD,
                        "system",
                        now
                );
                paymentRepository.save(payment);
            }

            Pageable pageable = PageRequest.of(0, 3, Sort.by("createdAt").descending());

            // when
            Page<Payment> page = paymentRepository.findByOrdererId("USER-001", pageable);

            // then
            assertThat(page.getContent()).hasSize(3);
            assertThat(page.getTotalElements()).isEqualTo(5);
            assertThat(page.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("사용자의 결제 목록을 상태로 필터링")
        void findByOrdererIdAndStatus() {
            // given
            Payment payment1 = paymentRepository.save(samplePayment);

            Payment payment2 = Payment.create(
                    "ORD-002",
                    "USER-001",
                    "TOSS-KEY-002",
                    "PAY-TOKEN-002",
                    BigDecimal.valueOf(20000),
                    PaymentMethod.CARD,
                    "system",
                    now
            );
            payment2 = paymentRepository.save(payment2);
            payment2.approve(now, "approver");
            paymentRepository.save(payment2);

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Payment> approvedPayments = paymentRepository.findByOrdererIdAndStatus(
                    "USER-001", PaymentStatus.APPROVED, pageable);
            Page<Payment> pendingPayments = paymentRepository.findByOrdererIdAndStatus(
                    "USER-001", PaymentStatus.PENDING, pageable);

            // then
            assertThat(approvedPayments.getContent()).hasSize(1);
            assertThat(pendingPayments.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("전체 결제 조회 (관리자)")
    class FindAll {

        @Test
        @DisplayName("전체 결제 목록 페이징 조회")
        void findAll_with_pagination() {
            // given
            for (int i = 1; i <= 10; i++) {
                Payment payment = Payment.create(
                        "ORD-00" + i,
                        "USER-00" + (i % 3),
                        "TOSS-KEY-00" + i,
                        "PAY-TOKEN-00" + i,
                        BigDecimal.valueOf(10000 * i),
                        PaymentMethod.CARD,
                        "system",
                        now
                );
                paymentRepository.save(payment);
            }

            Pageable pageable = PageRequest.of(0, 5);

            // when
            Page<Payment> page = paymentRepository.findAll(pageable);

            // then
            assertThat(page.getContent()).hasSize(5);
            assertThat(page.getTotalElements()).isEqualTo(10);
        }

        @Test
        @DisplayName("전체 결제 목록을 상태로 필터링")
        void findAllByStatus() {
            // given
            for (int i = 1; i <= 5; i++) {
                Payment payment = Payment.create(
                        "ORD-00" + i,
                        "USER-001",
                        "TOSS-KEY-00" + i,
                        "PAY-TOKEN-00" + i,
                        BigDecimal.valueOf(10000),
                        PaymentMethod.CARD,
                        "system",
                        now
                );
                payment = paymentRepository.save(payment);

                if (i % 2 == 0) {
                    payment.approve(now, "approver");
                    paymentRepository.save(payment);
                }
            }

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Payment> approvedPage = paymentRepository.findAllByStatus(
                    PaymentStatus.APPROVED, pageable);
            Page<Payment> pendingPage = paymentRepository.findAllByStatus(
                    PaymentStatus.PENDING, pageable);

            // then
            assertThat(approvedPage.getContent()).hasSize(2);
            assertThat(pendingPage.getContent()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("기간별 결제 조회")
    class FindByDateRange {

        @Test
        @DisplayName("특정 기간의 결제 목록 조회")
        void findByDateRange() {
            // given
            // 충분히 넓은 범위로 설정하여 저장된 결제가 포함되도록 함
            LocalDateTime startDate = now.minusDays(1);
            LocalDateTime endDate = now.plusDays(1);

            Payment payment = paymentRepository.save(samplePayment);

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Payment> page = paymentRepository.findByDateRange(startDate, endDate, pageable);

            // then - 최소 1개 이상 조회되고, 저장한 결제가 포함되어 있는지 확인
            assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(1);
            assertThat(page.getContent()).anyMatch(p -> p.getOrderId().equals("ORD-001"));
        }

        @Test
        @DisplayName("특정 기간 및 상태로 결제 목록 조회")
        void findByStatusAndDateRange() {
            // given
            LocalDateTime startDate = now.minusDays(1);
            LocalDateTime endDate = now.plusDays(1);

            Payment payment1 = paymentRepository.save(samplePayment);
            payment1.approve(now, "approver");
            paymentRepository.save(payment1);

            Payment payment2 = Payment.create(
                    "ORD-002",
                    "USER-001",
                    "TOSS-KEY-002",
                    "PAY-TOKEN-002",
                    BigDecimal.valueOf(20000),
                    PaymentMethod.CARD,
                    "system",
                    now
            );
            paymentRepository.save(payment2);

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Payment> approvedPage = paymentRepository.findByStatusAndDateRange(
                    PaymentStatus.APPROVED, startDate, endDate, pageable);
            Page<Payment> pendingPage = paymentRepository.findByStatusAndDateRange(
                    PaymentStatus.PENDING, startDate, endDate, pageable);

            // then - 각 상태별로 최소 1개씩 있고, 해당 주문이 포함되어 있는지 확인
            assertThat(approvedPage.getContent()).hasSizeGreaterThanOrEqualTo(1);
            assertThat(approvedPage.getContent()).anyMatch(p -> p.getOrderId().equals("ORD-001"));
            assertThat(approvedPage.getContent()).allMatch(p -> p.getPaymentStatus() == PaymentStatus.APPROVED);

            assertThat(pendingPage.getContent()).hasSizeGreaterThanOrEqualTo(1);
            assertThat(pendingPage.getContent()).anyMatch(p -> p.getOrderId().equals("ORD-002"));
            assertThat(pendingPage.getContent()).allMatch(p -> p.getPaymentStatus() == PaymentStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("특수 조회 (배치/정산)")
    class SpecialQueries {

        @Test
        @DisplayName("특정 시간 이전의 결제 대기 건 조회 - 실제 저장 시간 기준")
        void findPendingPaymentsBeforeTime() {
            // given - 첫 번째 결제 저장
            Payment firstPayment = Payment.create(
                    "ORD-FIRST",
                    "USER-001",
                    "TOSS-KEY-FIRST",
                    "PAY-TOKEN-FIRST",
                    BigDecimal.valueOf(5000),
                    PaymentMethod.CARD,
                    "system",
                    now
            );
            Payment saved1 = paymentRepository.save(firstPayment);

            // 약간의 시간 대기 (저장 시간 차이 만들기)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            LocalDateTime betweenTime = LocalDateTime.now();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 두 번째 결제 저장
            Payment secondPayment = Payment.create(
                    "ORD-SECOND",
                    "USER-001",
                    "TOSS-KEY-SECOND",
                    "PAY-TOKEN-SECOND",
                    BigDecimal.valueOf(10000),
                    PaymentMethod.CARD,
                    "system",
                    now
            );
            paymentRepository.save(secondPayment);

            // when - betweenTime 이전 것만 조회
            List<Payment> pendingList = paymentRepository.findPendingPaymentsBeforeTime(betweenTime);

            // then - 첫 번째 결제만 조회되어야 함
            assertThat(pendingList).hasSizeGreaterThanOrEqualTo(1);
            assertThat(pendingList).anyMatch(p -> p.getOrderId().equals("ORD-FIRST"));
            assertThat(pendingList).noneMatch(p -> p.getOrderId().equals("ORD-SECOND"));
        }

        @Test
        @DisplayName("특정 기간의 승인 완료 결제 조회 (정산용)")
        void findApprovedPaymentsByDateRange() {
            // given
            LocalDateTime startDate = now.minusDays(1);
            LocalDateTime endDate = now.plusDays(1);

            Payment approved = paymentRepository.save(samplePayment);
            approved.approve(now, "approver");
            paymentRepository.save(approved);

            Payment pending = Payment.create(
                    "ORD-002",
                    "USER-001",
                    "TOSS-KEY-002",
                    "PAY-TOKEN-002",
                    BigDecimal.valueOf(20000),
                    PaymentMethod.CARD,
                    "system",
                    now
            );
            paymentRepository.save(pending);

            // when
            List<Payment> approvedList = paymentRepository.findApprovedPaymentsByDateRange(
                    startDate, endDate);

            // then
            assertThat(approvedList).hasSizeGreaterThanOrEqualTo(1);
            assertThat(approvedList).anyMatch(p -> p.getPaymentStatus() == PaymentStatus.APPROVED);
        }

        @Test
        @DisplayName("부분 취소 결제도 정산 대상에 포함")
        void findApprovedPayments_includes_partially_cancelled() {
            // given
            LocalDateTime startDate = now.minusDays(1);
            LocalDateTime endDate = now.plusDays(1);

            Payment payment = paymentRepository.save(samplePayment);
            payment.approve(now, "approver");
            payment.addCancellation(
                    CancellationType.USER_REQUEST,
                    "부분 취소",
                    "USER-001",
                    BigDecimal.valueOf(3000),
                    now
            );
            paymentRepository.save(payment);

            // when
            List<Payment> approvedList = paymentRepository.findApprovedPaymentsByDateRange(
                    startDate, endDate);

            // then
            assertThat(approvedList).hasSizeGreaterThanOrEqualTo(1);
            assertThat(approvedList).anyMatch(p -> p.getPaymentStatus() == PaymentStatus.PARTIALLY_CANCELLED);
        }
    }

    @Nested
    @DisplayName("존재 여부 확인")
    class ExistsChecks {

        @Test
        @DisplayName("ID로 결제 존재 여부 확인")
        void existsById() {
            // given
            Payment saved = paymentRepository.save(samplePayment);

            // when
            boolean exists = paymentRepository.existsById(saved.getId());
            boolean notExists = paymentRepository.existsById("NON-EXISTENT");

            // then
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }

        @Test
        @DisplayName("Toss Payment Key 존재 여부 확인")
        void existsByTossPaymentKey() {
            // given
            paymentRepository.save(samplePayment);

            // when
            boolean exists = paymentRepository.existsByTossPaymentKey("TOSS-KEY-001");
            boolean notExists = paymentRepository.existsByTossPaymentKey("NON-EXISTENT");

            // then
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }
    }

    @Nested
    @DisplayName("소프트 삭제")
    class SoftDelete {

        @Test
        @DisplayName("취소된 결제 소프트 삭제 성공")
        void delete_cancelled_payment() {
            // given
            Payment saved = paymentRepository.save(samplePayment);
            saved.approve(now, "approver");
            saved.addCancellation(
                    CancellationType.USER_REQUEST,
                    "전액 취소",
                    "USER-001",
                    BigDecimal.valueOf(10000),
                    now
            );
            saved = paymentRepository.save(saved);

            // when
            saved.softDelete("deleter", now);
            paymentRepository.save(saved);  // 소프트 삭제는 save로 업데이트

            // then
            Optional<Payment> found = paymentRepository.findById(saved.getId());
            assertThat(found).isEmpty();  // 일반 조회에서는 안 보임

            Optional<Payment> foundIncludingDeleted = paymentRepository.findByIdIncludingDeleted(saved.getId());
            assertThat(foundIncludingDeleted).isPresent();
            assertThat(foundIncludingDeleted.get().getIsDeleted()).isTrue();
        }

        @Test
        @DisplayName("소프트 삭제된 결제는 일반 조회에서 제외")
        void soft_deleted_payment_not_in_normal_queries() {
            // given
            Payment saved = paymentRepository.save(samplePayment);
            saved.approve(now, "approver");
            saved.addCancellation(
                    CancellationType.USER_REQUEST,
                    "전액 취소",
                    "USER-001",
                    BigDecimal.valueOf(10000),
                    now
            );
            saved = paymentRepository.save(saved);

            saved.softDelete("deleter", now);
            paymentRepository.save(saved);  // 소프트 삭제 저장

            // when
            Optional<Payment> foundById = paymentRepository.findById(saved.getId());
            Optional<Payment> foundByOrderId = paymentRepository.findByOrderId(saved.getOrderId());
            boolean exists = paymentRepository.existsById(saved.getId());

            // then
            assertThat(foundById).isEmpty();
            assertThat(foundByOrderId).isEmpty();
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("정렬 테스트")
    class SortingTests {

        @Test
        @DisplayName("생성일자 기준 정렬")
        void sort_by_createdAt() {
            // given
            for (int i = 1; i <= 3; i++) {
                Payment payment = Payment.create(
                        "ORD-00" + i,
                        "USER-001",
                        "TOSS-KEY-00" + i,
                        "PAY-TOKEN-00" + i,
                        BigDecimal.valueOf(10000 * i),
                        PaymentMethod.CARD,
                        "system",
                        now.plusMinutes(i)
                );
                paymentRepository.save(payment);
            }

            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").ascending());

            // when
            Page<Payment> page = paymentRepository.findAll(pageable);

            // then
            assertThat(page.getContent()).hasSize(3);
            assertThat(page.getContent().get(0).getOrderId()).isEqualTo("ORD-001");
            assertThat(page.getContent().get(2).getOrderId()).isEqualTo("ORD-003");
        }

        @Test
        @DisplayName("금액 기준 정렬")
        void sort_by_amount() {
            // given
            for (int i = 1; i <= 3; i++) {
                Payment payment = Payment.create(
                        "ORD-00" + i,
                        "USER-001",
                        "TOSS-KEY-00" + i,
                        "PAY-TOKEN-00" + i,
                        BigDecimal.valueOf(10000 * i),
                        PaymentMethod.CARD,
                        "system",
                        now
                );
                paymentRepository.save(payment);
            }

            Pageable pageable = PageRequest.of(0, 10, Sort.by("amount").descending());

            // when
            Page<Payment> page = paymentRepository.findAll(pageable);

            // then
            assertThat(page.getContent()).hasSize(3);
            assertThat(page.getContent().get(0).getAmount()).isEqualTo(BigDecimal.valueOf(30000));
            assertThat(page.getContent().get(2).getAmount()).isEqualTo(BigDecimal.valueOf(10000));
        }
    }
}