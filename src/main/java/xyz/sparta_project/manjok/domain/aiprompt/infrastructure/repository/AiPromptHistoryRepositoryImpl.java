package xyz.sparta_project.manjok.domain.aiprompt.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptErrorCode;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptException;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.domain.repository.AiPromptHistoryRepository;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.entity.AiPromptHistoryEntity;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.jpa.AiPromptHistoryJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI 프롬프트 히스토리 Repository 구현체
 * - 도메인 리포지토리 인터페이스 구현
 * - 도메인 ↔ 엔티티 변환 처리
 * - 업데이트는 더티체킹으로 처리
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiPromptHistoryRepositoryImpl implements AiPromptHistoryRepository {

    private final AiPromptHistoryJpaRepository jpaRepository;

    // ==================== CREATE ====================

    @Override
    @Transactional
    public AiPromptHistory save(AiPromptHistory aiPromptHistory) {
        try {
            // 신규 생성만 처리 (ID가 없는 경우)
            if (aiPromptHistory.getId() != null && jpaRepository.existsById(aiPromptHistory.getId())) {
                throw new AiPromptException(
                        AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                        "이미 존재하는 히스토리입니다. 업데이트는 별도 메서드를 사용하세요."
                );
            }

            // 도메인 → 엔티티 변환
            AiPromptHistoryEntity entity = AiPromptHistoryEntity.fromDomain(aiPromptHistory);

            // 저장
            AiPromptHistoryEntity savedEntity = jpaRepository.save(entity);

            log.info("AI 프롬프트 히스토리 생성 성공. ID: {}, OwnerId: {}, Type: {}",
                    savedEntity.getId(), savedEntity.getOwnerId(), savedEntity.getPromptType());

            // 엔티티 → 도메인 변환하여 반환
            return savedEntity.toDomain();

        } catch (AiPromptException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 프롬프트 히스토리 생성 실패: {}", aiPromptHistory.getOwnerId(), e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "히스토리 생성 중 오류가 발생했습니다: " + e.getMessage(),
                    e
            );
        }
    }

    // ==================== UPDATE ====================

    @Override
    @Transactional
    public void updateResponse(String id, String responseContent, String updatedBy) {
        try {
            // 엔티티 조회
            AiPromptHistoryEntity entity = jpaRepository.findById(id)
                    .orElseThrow(() -> new AiPromptException(AiPromptErrorCode.HISTORY_NOT_FOUND));

            // 삭제된 히스토리는 수정 불가
            if (entity.getIsDeleted()) {
                throw new AiPromptException(AiPromptErrorCode.CANNOT_UPDATE_DELETED_HISTORY);
            }

            // 엔티티의 업데이트 메서드 호출 (더티체킹으로 자동 반영)
            entity.updateResponse(responseContent, updatedBy);

            log.info("AI 프롬프트 응답 업데이트 성공. ID: {}, UpdatedBy: {}", id, updatedBy);

        } catch (AiPromptException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 프롬프트 응답 업데이트 실패: {}", id, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "응답 업데이트 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== DELETE ====================

    @Override
    @Transactional
    public void delete(String id, String deletedBy) {
        try {
            // 엔티티 조회
            AiPromptHistoryEntity entity = jpaRepository.findById(id)
                    .orElseThrow(() -> new AiPromptException(AiPromptErrorCode.HISTORY_NOT_FOUND));

            // 이미 삭제된 경우
            if (entity.getIsDeleted()) {
                throw new AiPromptException(AiPromptErrorCode.ALREADY_DELETED);
            }

            // 엔티티의 삭제 메서드 호출 (더티체킹으로 자동 반영)
            entity.markAsDeleted(deletedBy);

            log.info("AI 프롬프트 히스토리 삭제 성공. ID: {}, DeletedBy: {}", id, deletedBy);

        } catch (AiPromptException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 프롬프트 히스토리 삭제 실패: {}", id, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "히스토리 삭제 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== READ - 단건 조회 ====================

    @Override
    public Optional<AiPromptHistory> findById(String id) {
        try {
            return jpaRepository.findByIdAndNotDeleted(id)
                    .map(AiPromptHistoryEntity::toDomain);
        } catch (Exception e) {
            log.error("AI 프롬프트 히스토리 조회 실패: {}", id, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "히스토리 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    @Override
    public Optional<AiPromptHistory> findByIdIncludingDeleted(String id) {
        try {
            return jpaRepository.findById(id)
                    .map(AiPromptHistoryEntity::toDomain);
        } catch (Exception e) {
            log.error("AI 프롬프트 히스토리 조회 실패 (삭제 포함): {}", id, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "히스토리 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== READ - 목록 조회 ====================

    @Override
    public List<AiPromptHistory> findByOwnerId(String ownerId) {
        try {
            return jpaRepository.findByOwnerIdAndIsDeletedOrderByCreatedAtDesc(ownerId, false)
                    .stream()
                    .map(AiPromptHistoryEntity::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("소유자 ID로 히스토리 조회 실패: {}", ownerId, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "히스토리 목록 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    @Override
    public List<AiPromptHistory> findByOwnerIdIncludingDeleted(String ownerId) {
        try {
            return jpaRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                    .stream()
                    .map(AiPromptHistoryEntity::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("소유자 ID로 히스토리 조회 실패 (삭제 포함): {}", ownerId, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "히스토리 목록 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    @Override
    public List<AiPromptHistory> findByOwnerIdAndPromptType(String ownerId, PromptType promptType) {
        try {
            return jpaRepository.findByOwnerIdAndPromptTypeAndIsDeletedOrderByCreatedAtDesc(
                            ownerId, promptType, false)
                    .stream()
                    .map(AiPromptHistoryEntity::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("소유자 ID와 타입으로 히스토리 조회 실패: {}, {}", ownerId, promptType, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "히스토리 목록 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    @Override
    public List<AiPromptHistory> findByOwnerIdAndPromptTypeIncludingDeleted(String ownerId, PromptType promptType) {
        try {
            return jpaRepository.findByOwnerIdAndPromptTypeOrderByCreatedAtDesc(ownerId, promptType)
                    .stream()
                    .map(AiPromptHistoryEntity::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("소유자 ID와 타입으로 히스토리 조회 실패 (삭제 포함): {}, {}", ownerId, promptType, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "히스토리 목록 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    @Override
    public List<AiPromptHistory> findByPromptType(PromptType promptType) {
        try {
            return jpaRepository.findByPromptTypeAndIsDeletedOrderByCreatedAtDesc(promptType, false)
                    .stream()
                    .map(AiPromptHistoryEntity::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("프롬프트 타입으로 히스토리 조회 실패: {}", promptType, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "히스토리 목록 조회 중 오류가 발생했습니다",
                    e
            );
        }
    }

    // ==================== 존재 확인 ====================

    @Override
    public boolean existsById(String id) {
        try {
            return jpaRepository.existsByIdAndIsDeleted(id, false);
        } catch (Exception e) {
            log.error("히스토리 존재 여부 확인 실패: {}", id, e);
            return false;
        }
    }

    @Override
    public boolean existsByOwnerIdAndPromptType(String ownerId, PromptType promptType) {
        try {
            return jpaRepository.existsByOwnerIdAndPromptTypeAndIsDeleted(ownerId, promptType, false);
        } catch (Exception e) {
            log.error("히스토리 존재 여부 확인 실패: {}, {}", ownerId, promptType, e);
            return false;
        }
    }

    // ==================== 통계 ====================

    @Override
    public long countByOwnerId(String ownerId) {
        try {
            return jpaRepository.countByOwnerIdAndIsDeleted(ownerId, false);
        } catch (Exception e) {
            log.error("소유자의 히스토리 개수 조회 실패: {}", ownerId, e);
            return 0;
        }
    }

    @Override
    public long countByOwnerIdAndPromptType(String ownerId, PromptType promptType) {
        try {
            return jpaRepository.countByOwnerIdAndPromptTypeAndIsDeleted(ownerId, promptType, false);
        } catch (Exception e) {
            log.error("소유자의 타입별 히스토리 개수 조회 실패: {}, {}", ownerId, promptType, e);
            return 0;
        }
    }
}