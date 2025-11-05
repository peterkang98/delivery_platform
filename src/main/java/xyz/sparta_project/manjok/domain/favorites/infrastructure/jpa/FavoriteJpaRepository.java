// FavoriteJpaRepository.java
package xyz.sparta_project.manjok.domain.favorites.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;
import xyz.sparta_project.manjok.domain.favorites.infrastructure.entity.FavoriteEntity;

import java.util.List;

public interface FavoriteJpaRepository extends JpaRepository<FavoriteEntity, String> {

    List<FavoriteEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<FavoriteEntity> findByCustomerIdAndTypeOrderByCreatedAtDesc(String customerId, FavoriteType type);

    List<FavoriteEntity> findByRestaurantIdOrderByCreatedAtDesc(String restaurantId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM FavoriteEntity f " +
            "WHERE f.customerId = :customerId " +
            "AND f.type = :type " +
            "AND f.restaurantId = :restaurantId " +
            "AND (:menuId IS NULL OR f.menuId = :menuId)")
    boolean existsByCustomerAndTarget(
            @Param("customerId") String customerId,
            @Param("type") FavoriteType type,
            @Param("restaurantId") String restaurantId,
            @Param("menuId") String menuId
    );

    long countByCustomerId(String customerId);

    long countByCustomerIdAndType(String customerId, FavoriteType type);

    long countByRestaurantId(String restaurantId);
}