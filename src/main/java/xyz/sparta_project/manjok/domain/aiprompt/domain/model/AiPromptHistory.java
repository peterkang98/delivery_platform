package xyz.sparta_project.manjok.domain.aiprompt.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptErrorCode;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptException;

import java.time.LocalDateTime;

/**
 * AI 프롬프트 히스토리 Aggregate Root
 * - AI 프롬프트 요청/응답 히스토리의 모든 정보와 비즈니스 규칙을 관리
 * - 순수 도메인 모델
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AiPromptHistory {

    // 식별자
    private String id;
    private LocalDateTime createdAt;

    // 소유자 정보
    private String ownerId;

    // 프롬프트 타입
    private PromptType promptType;

    // 요청/응답 내용
    private String requestPrompt;
    private String responseContent;

    // 상태 관리
    @Builder.Default
    private Boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;

    // 감사 필드
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    // ==================== 생성 ====================

    /**
     * AI 프롬프트 히스토리 생성
     */
    public static AiPromptHistory create(String ownerId, PromptType promptType,
                                         String requestPrompt, String createdBy) {
        AiPromptHistory history = AiPromptHistory.builder()
                .ownerId(ownerId)
                .promptType(promptType)
                .requestPrompt(requestPrompt)
                .isDeleted(false)
                .createdBy(createdBy)
                .build();

        history.validate();
        return history;
    }

    // ==================== 응답 관리 ====================

    /**
     * AI 응답 업데이트
     */
    public void updateResponse(String responseContent, String updatedBy) {
        if (responseContent == null || responseContent.trim().isEmpty()) {
            throw new AiPromptException(AiPromptErrorCode.RESPONSE_CONTENT_REQUIRED);
        }

        if (this.isDeleted) {
            throw new AiPromptException(AiPromptErrorCode.CANNOT_UPDATE_DELETED_HISTORY);
        }

        this.responseContent = responseContent;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 응답이 있는지 확인
     */
    public boolean hasResponse() {
        return this.responseContent != null && !this.responseContent.trim().isEmpty();
    }

    // ==================== 상태 관리 ====================

    /**
     * Soft Delete
     */
    public void delete(String deletedBy) {
        if (this.isDeleted) {
            throw new AiPromptException(AiPromptErrorCode.ALREADY_DELETED);
        }

        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 복구
     */
    public void restore(String updatedBy) {
        if (!this.isDeleted) {
            throw new AiPromptException(AiPromptErrorCode.NOT_DELETED);
        }

        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 활성 상태인지 확인
     */
    public boolean isActive() {
        return !this.isDeleted;
    }

    // ==================== 유틸리티 메서드 ====================

    /**
     * 검증
     */
    public void validate() {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new AiPromptException(AiPromptErrorCode.OWNER_ID_REQUIRED);
        }
        if (promptType == null) {
            throw new AiPromptException(AiPromptErrorCode.PROMPT_TYPE_REQUIRED);
        }
        if (requestPrompt == null || requestPrompt.trim().isEmpty()) {
            throw new AiPromptException(AiPromptErrorCode.REQUEST_PROMPT_REQUIRED);
        }
        if (requestPrompt.length() > 10000) {
            throw new AiPromptException(AiPromptErrorCode.REQUEST_PROMPT_TOO_LONG);
        }
    }

    /**
     * 프롬프트 길이 제한 확인
     */
    public boolean isRequestPromptTooLong(int maxLength) {
        return requestPrompt != null && requestPrompt.length() > maxLength;
    }

    /**
     * 응답 길이 확인
     */
    public int getResponseLength() {
        return responseContent != null ? responseContent.length() : 0;
    }
}