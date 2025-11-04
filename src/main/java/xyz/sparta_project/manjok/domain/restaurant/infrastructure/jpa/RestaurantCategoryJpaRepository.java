package xyz.sparta_project.manjok.domain.restaurant.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.RestaurantCategoryEntity;

public interface RestaurantCategoryJpaRepository extends JpaRepository<RestaurantCategoryEntity, String> {

}
