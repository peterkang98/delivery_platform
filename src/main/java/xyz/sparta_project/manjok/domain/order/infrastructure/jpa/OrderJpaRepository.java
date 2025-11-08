package xyz.sparta_project.manjok.domain.order.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sparta_project.manjok.domain.order.infrastructure.entity.OrderEntity;

/**
 * Order JPA Repository
 */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {

}