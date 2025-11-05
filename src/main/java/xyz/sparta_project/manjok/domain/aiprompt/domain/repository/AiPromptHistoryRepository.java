package xyz.sparta_project.manjok.domain.aiprompt.domain.repository;

import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;

import java.util.List;
import java.util.Optional;

/**
 * AI 프롬프트 히스토리 도메인 리포지토리
 * - 도메인 계층에서 필요한 저장소 기능 정의
 */
public interface AiPromptHistoryRepository {

    // ==================== CREATE & UPDATE ====================

    /**
     * AI 프롬프트 히스토리 저장 (생성 및 수정)
     */
    AiPromptHistory save(AiPromptHistory aiPromptHistory);

    // ==================== UPDATE ====================

    /**
     * AI 응답 업데이트 (더티체킹)
     * @param id 히스토리 ID
     * @param responseContent 응답 내용
     * @param updatedBy 수정자
     */
    void updateResponse(String id, String responseContent, String updatedBy);

    // ==================== DELETE ====================

    /**
     * AI 프롬프트 히스토리 소프트 삭제
     */
    void delete(String id, String deletedBy);

    // ==================== READ - 단건 조회 ====================

    /**
     * ID로 히스토리 조회 (활성 상태만)
     */
    Optional<AiPromptHistory> findById(String id);

    /**
     * ID로 히스토리 조회 (삭제된 것 포함)
     */
    Optional<AiPromptHistory> findByIdIncludingDeleted(String id);

    // ==================== READ - 목록 조회 ====================

    /**
     * 소유자 ID로 히스토리 목록 조회 (활성 상태만)
     */
    List<AiPromptHistory> findByOwnerId(String ownerId);

    /**
     * 소유자 ID로 히스토리 목록 조회 (삭제된 것 포함)
     */
    List<AiPromptHistory> findByOwnerIdIncludingDeleted(String ownerId);

    /**
     * 소유자 ID와 프롬프트 타입으로 히스토리 목록 조회 (활성 상태만)
     */
    List<AiPromptHistory> findByOwnerIdAndPromptType(String ownerId, PromptType promptType);

    /**
     * 소유자 ID와 프롬프트 타입으로 히스토리 목록 조회 (삭제된 것 포함)
     */
    List<AiPromptHistory> findByOwnerIdAndPromptTypeIncludingDeleted(String ownerId, PromptType promptType);

    /**
     * 프롬프트 타입으로 히스토리 목록 조회 (활성 상태만)
     */
    List<AiPromptHistory> findByPromptType(PromptType promptType);

    // ==================== 존재 확인 ====================

    /**
     * ID로 히스토리 존재 여부 확인 (활성 상태만)
     */
    boolean existsById(String id);

    /**
     * 소유자 ID와 프롬프트 타입으로 히스토리 존재 여부 확인
     */
    boolean existsByOwnerIdAndPromptType(String ownerId, PromptType promptType);

    // ==================== 통계 ====================

    /**
     * 소유자의 총 히스토리 개수 (활성 상태만)
     */
    long countByOwnerId(String ownerId);

    /**
     * 소유자의 프롬프트 타입별 히스토리 개수 (활성 상태만)
     */
    long countByOwnerIdAndPromptType(String ownerId, PromptType promptType);
}