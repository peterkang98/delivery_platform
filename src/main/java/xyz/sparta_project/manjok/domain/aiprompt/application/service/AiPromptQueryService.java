// application/service/AiPromptQueryService.java
package xyz.sparta_project.manjok.domain.aiprompt.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptErrorCode;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptException;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.domain.repository.AiPromptHistoryRepository;

import java.util.List;

/**
 * AI 프롬프트 Query Service
 * - 조회 작업 처리 (읽기 전용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiPromptQueryService {

    private final AiPromptHistoryRepository aiPromptHistoryRepository;

    /**
     * ID로 히스토리 조회
     *
     * @param id 히스토리 ID
     * @return 히스토리
     */
    public AiPromptHistory getById(String id) {
        log.debug("히스토리 조회. ID: {}", id);

        return aiPromptHistoryRepository.findById(id)
                .orElseThrow(() -> new AiPromptException(AiPromptErrorCode.HISTORY_NOT_FOUND));
    }

    /**
     * ID로 히스토리 조회 (소유자 확인 포함)
     *
     * @param id 히스토리 ID
     * @param ownerId 소유자 ID
     * @return 히스토리
     */
    public AiPromptHistory getByIdWithOwnerCheck(String id, String ownerId) {
        log.debug("히스토리 조회 (소유자 확인). ID: {}, OwnerId: {}", id, ownerId);

        AiPromptHistory history = aiPromptHistoryRepository.findById(id)
                .orElseThrow(() -> new AiPromptException(AiPromptErrorCode.HISTORY_NOT_FOUND));

        // 소유자 확인
        if (!history.getOwnerId().equals(ownerId)) {
            throw new AiPromptException(AiPromptErrorCode.HISTORY_NOT_BELONG_TO_OWNER);
        }

        return history;
    }

    /**
     * 소유자 ID로 히스토리 목록 조회
     *
     * @param ownerId 소유자 ID
     * @return 히스토리 목록
     */
    public List<AiPromptHistory> getByOwnerId(String ownerId) {
        log.debug("소유자별 히스토리 목록 조회. OwnerId: {}", ownerId);

        return aiPromptHistoryRepository.findByOwnerId(ownerId);
    }

    /**
     * 소유자 ID와 프롬프트 타입으로 히스토리 목록 조회
     *
     * @param ownerId 소유자 ID
     * @param promptType 프롬프트 타입
     * @return 히스토리 목록
     */
    public List<AiPromptHistory> getByOwnerIdAndPromptType(String ownerId, PromptType promptType) {
        log.debug("소유자별 타입별 히스토리 목록 조회. OwnerId: {}, Type: {}", ownerId, promptType);

        return aiPromptHistoryRepository.findByOwnerIdAndPromptType(ownerId, promptType);
    }

    /**
     * 소유자의 메뉴 설명 히스토리 목록 조회
     *
     * @param ownerId 소유자 ID
     * @return 메뉴 설명 히스토리 목록
     */
    public List<AiPromptHistory> getMenuDescriptionsByOwnerId(String ownerId) {
        log.debug("메뉴 설명 히스토리 목록 조회. OwnerId: {}", ownerId);

        return aiPromptHistoryRepository.findByOwnerIdAndPromptType(
                ownerId,
                PromptType.MENU_DESCRIPTION
        );
    }

    /**
     * 소유자의 QnA 히스토리 목록 조회
     *
     * @param ownerId 소유자 ID
     * @return QnA 히스토리 목록
     */
    public List<AiPromptHistory> getQnasByOwnerId(String ownerId) {
        log.debug("QnA 히스토리 목록 조회. OwnerId: {}", ownerId);

        return aiPromptHistoryRepository.findByOwnerIdAndPromptType(
                ownerId,
                PromptType.QNA
        );
    }

    /**
     * 프롬프트 타입별 히스토리 목록 조회
     *
     * @param promptType 프롬프트 타입
     * @return 히스토리 목록
     */
    public List<AiPromptHistory> getByPromptType(PromptType promptType) {
        log.debug("타입별 히스토리 목록 조회. Type: {}", promptType);

        return aiPromptHistoryRepository.findByPromptType(promptType);
    }

    /**
     * 히스토리 존재 여부 확인
     *
     * @param id 히스토리 ID
     * @return 존재 여부
     */
    public boolean existsById(String id) {
        return aiPromptHistoryRepository.existsById(id);
    }

    /**
     * 소유자의 총 히스토리 개수 조회
     *
     * @param ownerId 소유자 ID
     * @return 히스토리 개수
     */
    public long countByOwnerId(String ownerId) {
        log.debug("소유자별 히스토리 개수 조회. OwnerId: {}", ownerId);

        return aiPromptHistoryRepository.countByOwnerId(ownerId);
    }

    /**
     * 소유자의 프롬프트 타입별 히스토리 개수 조회
     *
     * @param ownerId 소유자 ID
     * @param promptType 프롬프트 타입
     * @return 히스토리 개수
     */
    public long countByOwnerIdAndPromptType(String ownerId, PromptType promptType) {
        log.debug("소유자별 타입별 히스토리 개수 조회. OwnerId: {}, Type: {}", ownerId, promptType);

        return aiPromptHistoryRepository.countByOwnerIdAndPromptType(ownerId, promptType);
    }

    /**
     * 소유자의 메뉴 설명 히스토리 개수 조회
     *
     * @param ownerId 소유자 ID
     * @return 메뉴 설명 개수
     */
    public long countMenuDescriptionsByOwnerId(String ownerId) {
        return aiPromptHistoryRepository.countByOwnerIdAndPromptType(
                ownerId,
                PromptType.MENU_DESCRIPTION
        );
    }

    /**
     * 소유자의 QnA 히스토리 개수 조회
     *
     * @param ownerId 소유자 ID
     * @return QnA 개수
     */
    public long countQnasByOwnerId(String ownerId) {
        return aiPromptHistoryRepository.countByOwnerIdAndPromptType(
                ownerId,
                PromptType.QNA
        );
    }
}