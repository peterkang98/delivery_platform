// FavoriteRepository.java
package xyz.sparta_project.manjok.domain.favorites.domain.repository;

import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;

import java.util.List;
import java.util.Optional;

/**
 * 찜하기 도메인 리포지토리
 * - 도메인 계층에서 필요한 저장소 기능 정의
 */
public interface FavoriteRepository {

    // ==================== CREATE ====================

    /**
     * 찜하기 저장 (생성만 처리)
     */
    Favorite save(Favorite favorite);

    // ==================== DELETE ====================

    /**
     * 찜하기 삭제 (하드 삭제)
     */
    void delete(String id);

    // ==================== READ - 단건 조회 ====================

    /**
     * ID로 찜하기 조회
     */
    Optional<Favorite> findById(String id);

    // ==================== READ - 목록 조회 ====================

    /**
     * 고객 ID로 찜하기 목록 조회
     */
    List<Favorite> findByCustomerId(String customerId);

    /**
     * 고객 ID와 타입으로 찜하기 목록 조회
     */
    List<Favorite> findByCustomerIdAndType(String customerId, FavoriteType type);

    /**
     * 레스토랑 ID로 찜하기 목록 조회
     */
    List<Favorite> findByRestaurantId(String restaurantId);

    // ==================== 존재 확인 ====================

    /**
     * ID로 찜하기 존재 여부 확인
     */
    boolean existsById(String id);

    /**
     * 고객과 대상으로 찜하기 존재 여부 확인
     */
    boolean existsByCustomerAndTarget(
            String customerId,
            FavoriteType type,
            String restaurantId,
            String menuId
    );

    // ==================== 통계 ====================

    /**
     * 고객의 총 찜하기 개수
     */
    long countByCustomerId(String customerId);

    /**
     * 고객의 타입별 찜하기 개수
     */
    long countByCustomerIdAndType(String customerId, FavoriteType type);

    /**
     * 레스토랑의 총 찜하기 개수
     */
    long countByRestaurantId(String restaurantId);
}