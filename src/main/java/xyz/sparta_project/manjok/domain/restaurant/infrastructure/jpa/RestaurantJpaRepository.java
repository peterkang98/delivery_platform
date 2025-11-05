package xyz.sparta_project.manjok.domain.restaurant.infrastructure.jpa;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.MenuEntity;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.RestaurantEntity;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.MenuOptionEntity;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.MenuOptionGroupEntity;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.MenuCategoryEntity;
import xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.MenuCategoryRelationEntity;

import java.util.List;
import java.util.Optional;

/**
 * RestaurantEntity JPA Repository
 */
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, String> {

}