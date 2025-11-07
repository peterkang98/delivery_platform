package xyz.sparta_project.manjok.domain.aiprompt.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.entity.AiPromptHistoryEntity;

import java.util.List;
import java.util.Optional;

/**
 * AI 프롬프트 히스토리 JPA Repository
 */
public interface AiPromptHistoryJpaRepository extends JpaRepository<AiPromptHistoryEntity, String> {

    // ==================== 단건 조회 ====================

    /**
     * ID로 조회 (활성 상태만)
     */
    @Query("SELECT a FROM AiPromptHistoryEntity a WHERE a.id = :id AND a.isDeleted = false")
    Optional<AiPromptHistoryEntity> findByIdAndNotDeleted(@Param("id") String id);

    // ==================== 목록 조회 ====================

    /**
     * 소유자 ID로 조회 (활성 상태만)
     */
    List<AiPromptHistoryEntity> findByOwnerIdAndIsDeletedOrderByCreatedAtDesc(String ownerId, Boolean isDeleted);

    /**
     * 소유자 ID로 조회 (삭제된 것 포함)
     */
    List<AiPromptHistoryEntity> findByOwnerIdOrderByCreatedAtDesc(String ownerId);

    /**
     * 소유자 ID와 프롬프트 타입으로 조회 (활성 상태만)
     */
    List<AiPromptHistoryEntity> findByOwnerIdAndPromptTypeAndIsDeletedOrderByCreatedAtDesc(
            String ownerId, PromptType promptType, Boolean isDeleted);

    /**
     * 소유자 ID와 프롬프트 타입으로 조회 (삭제된 것 포함)
     */
    List<AiPromptHistoryEntity> findByOwnerIdAndPromptTypeOrderByCreatedAtDesc(
            String ownerId, PromptType promptType);

    /**
     * 프롬프트 타입으로 조회 (활성 상태만)
     */
    List<AiPromptHistoryEntity> findByPromptTypeAndIsDeletedOrderByCreatedAtDesc(
            PromptType promptType, Boolean isDeleted);

    // ==================== 존재 확인 ====================

    /**
     * ID로 존재 여부 확인 (활성 상태만)
     */
    boolean existsByIdAndIsDeleted(String id, Boolean isDeleted);

    /**
     * 소유자 ID와 프롬프트 타입으로 존재 여부 확인 (활성 상태만)
     */
    boolean existsByOwnerIdAndPromptTypeAndIsDeleted(String ownerId, PromptType promptType, Boolean isDeleted);

    // ==================== 통계 ====================

    /**
     * 소유자의 총 히스토리 개수 (활성 상태만)
     */
    long countByOwnerIdAndIsDeleted(String ownerId, Boolean isDeleted);

    /**
     * 소유자의 프롬프트 타입별 히스토리 개수 (활성 상태만)
     */
    long countByOwnerIdAndPromptTypeAndIsDeleted(String ownerId, PromptType promptType, Boolean isDeleted);
}