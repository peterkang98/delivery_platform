// FavoriteRepositoryImpl.java
package xyz.sparta_project.manjok.domain.favorites.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.favorites.domain.exception.FavoriteErrorCode;
import xyz.sparta_project.manjok.domain.favorites.domain.exception.FavoriteException;
import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;
import xyz.sparta_project.manjok.domain.favorites.domain.repository.FavoriteRepository;
import xyz.sparta_project.manjok.domain.favorites.infrastructure.entity.FavoriteEntity;
import xyz.sparta_project.manjok.domain.favorites.infrastructure.jpa.FavoriteJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 찜하기 Repository 구현체
 * - 도메인 리포지토리 인터페이스 구현
 * - 도메인 ↔ 엔티티 변환 처리
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteRepositoryImpl implements FavoriteRepository {

    private final FavoriteJpaRepository jpaRepository;

    // ==================== CREATE ====================

    @Override
    @Transactional
    public Favorite save(Favorite favorite) {
        try {
            // 신규 생성만 처리 (ID가 없는 경우)
            if (favorite.getId() != null && jpaRepository.existsById(favorite.getId())) {
                throw new FavoriteException(
                        FavoriteErrorCode.EVENT_PROCESSING_FAILED,
                        "이미 존재하는 찜하기입니다."
                );
            }

            // 도메인 → 엔티티 변환
            FavoriteEntity entity = FavoriteEntity.fromDomain(favorite);

            // 저장
            FavoriteEntity savedEntity = jpaRepository.save(entity);

            log.info("찜하기 생성 성공. ID: {}, CustomerId: {}, Type: {}",
                    savedEntity.getId(), savedEntity.getCustomerId(), savedEntity.getType());

            // 엔티티 → 도메인 변환하여 반환
            return savedEntity.toDomain();

        } catch (FavoriteException e) {
            throw e;
        } catch (Exception e) {
            log.error("찜하기 생성 실패: {}", favorite.getCustomerId(), e);
            throw new FavoriteException(
                    FavoriteErrorCode.FAVORITE_SAVE_FAILED,
                    "찜하기 생성 중 오류가 발생했습니다: " + e.getMessage(),
                    e
            );
        }
    }

    // ==================== DELETE ====================

    @Override
    @Transactional
    public void delete(String id) {
        try {
            if (!jpaRepository.existsById(id)) {
                throw new FavoriteException(FavoriteErrorCode.FAVORITE_NOT_FOUND);
            }

            jpaRepository.deleteById(id);

            log.info("찜하기 삭제 성공. ID: {}", id);

        } catch (FavoriteException e) {
            throw e;
        } catch (Exception e) {
            log.error("찜하기 삭제 실패: {}", id, e);
            throw new FavoriteException(
                    FavoriteErrorCode.FAVORITE_DELETE_FAILED,
                    "찜하기 삭제 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== READ - 단건 조회 ====================

    @Override
    public Optional<Favorite> findById(String id) {
        try {
            return jpaRepository.findById(id)
                    .map(FavoriteEntity::toDomain);
        } catch (Exception e) {
            log.error("찜하기 조회 실패: {}", id, e);
            throw new FavoriteException(
                    FavoriteErrorCode.EVENT_PROCESSING_FAILED,
                    "찜하기 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== READ - 목록 조회 ====================

    @Override
    public List<Favorite> findByCustomerId(String customerId) {
        try {
            return jpaRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                    .stream()
                    .map(FavoriteEntity::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("고객 ID로 찜하기 조회 실패: {}", customerId, e);
            throw new FavoriteException(
                    FavoriteErrorCode.EVENT_PROCESSING_FAILED,
                    "찜하기 목록 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    @Override
    public List<Favorite> findByCustomerIdAndType(String customerId, FavoriteType type) {
        try {
            return jpaRepository.findByCustomerIdAndTypeOrderByCreatedAtDesc(customerId, type)
                    .stream()
                    .map(FavoriteEntity::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("고객 ID와 타입으로 찜하기 조회 실패: {}, {}", customerId, type, e);
            throw new FavoriteException(
                    FavoriteErrorCode.EVENT_PROCESSING_FAILED,
                    "찜하기 목록 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    @Override
    public List<Favorite> findByRestaurantId(String restaurantId) {
        try {
            return jpaRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                    .stream()
                    .map(FavoriteEntity::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("레스토랑 ID로 찜하기 조회 실패: {}", restaurantId, e);
            throw new FavoriteException(
                    FavoriteErrorCode.EVENT_PROCESSING_FAILED,
                    "찜하기 목록 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== 존재 확인 ====================

    @Override
    public boolean existsById(String id) {
        try {
            return jpaRepository.existsById(id);
        } catch (Exception e) {
            log.error("찜하기 존재 여부 확인 실패: {}", id, e);
            return false;
        }
    }

    @Override
    public boolean existsByCustomerAndTarget(
            String customerId,
            FavoriteType type,
            String restaurantId,
            String menuId
    ) {
        try {
            return jpaRepository.existsByCustomerAndTarget(customerId, type, restaurantId, menuId);
        } catch (Exception e) {
            log.error("찜하기 존재 여부 확인 실패: {}, {}, {}, {}", customerId, type, restaurantId, menuId, e);
            return false;
        }
    }

    // ==================== 통계 ====================

    @Override
    public long countByCustomerId(String customerId) {
        try {
            return jpaRepository.countByCustomerId(customerId);
        } catch (Exception e) {
            log.error("고객의 찜하기 개수 조회 실패: {}", customerId, e);
            return 0;
        }
    }

    @Override
    public long countByCustomerIdAndType(String customerId, FavoriteType type) {
        try {
            return jpaRepository.countByCustomerIdAndType(customerId, type);
        } catch (Exception e) {
            log.error("고객의 타입별 찜하기 개수 조회 실패: {}, {}", customerId, type, e);
            return 0;
        }
    }

    @Override
    public long countByRestaurantId(String restaurantId) {
        try {
            return jpaRepository.countByRestaurantId(restaurantId);
        } catch (Exception e) {
            log.error("레스토랑의 찜하기 개수 조회 실패: {}", restaurantId, e);
            return 0;
        }
    }
}