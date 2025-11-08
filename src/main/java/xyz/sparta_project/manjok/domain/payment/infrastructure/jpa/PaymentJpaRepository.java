package xyz.sparta_project.manjok.domain.payment.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sparta_project.manjok.domain.payment.infrastructure.entity.PaymentEntity;

/**
 * Payment JPA Repository
 */
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, String> {

}