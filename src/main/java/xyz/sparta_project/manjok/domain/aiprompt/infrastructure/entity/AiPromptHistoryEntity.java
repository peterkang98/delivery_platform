// domain/aiprompt/infrastructure/entity/AiPromptHistoryEntity.java
package xyz.sparta_project.manjok.domain.aiprompt.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;

import java.time.LocalDateTime;

/**
 * AI 프롬프트 히스토리 JPA Entity
 * - BaseEntity를 상속받아 ID와 createdAt 자동 관리
 */
@Entity
@Table(name = "p_ai_prompt_history", indexes = {
        @Index(name = "idx_ai_prompt_owner_id", columnList = "owner_id"),
        @Index(name = "idx_ai_prompt_type", columnList = "prompt_type"),
        @Index(name = "idx_ai_prompt_is_deleted", columnList = "is_deleted")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AiPromptHistoryEntity extends BaseEntity {

    @Column(name = "owner_id", nullable = false, length = 36)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type", nullable = false, length = 50)
    private PromptType promptType;

    @Column(name = "request_prompt", nullable = false, columnDefinition = "TEXT")
    private String requestPrompt;

    @Column(name = "response_content", columnDefinition = "TEXT")
    private String responseContent;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    // ==================== 도메인 ↔ 엔티티 변환 ====================

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static AiPromptHistoryEntity fromDomain(AiPromptHistory domain) {
        if (domain == null) {
            return null;
        }

        AiPromptHistoryEntity entity = AiPromptHistoryEntity.builder()
                .ownerId(domain.getOwnerId())
                .promptType(domain.getPromptType())
                .requestPrompt(domain.getRequestPrompt())
                .responseContent(domain.getResponseContent())
                .isDeleted(domain.getIsDeleted())
                .deletedAt(domain.getDeletedAt())
                .deletedBy(domain.getDeletedBy())
                .createdBy(domain.getCreatedBy())
                .updatedAt(domain.getUpdatedAt())
                .updatedBy(domain.getUpdatedBy())
                .build();

        // ID와 createdAt 설정 (도메인에서 이미 있는 경우)
        if (domain.getId() != null) {
            entity.setIdFromDomain(domain.getId());
        }
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAtFromDomain(domain.getCreatedAt());
        }

        return entity;
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    public AiPromptHistory toDomain() {
        return AiPromptHistory.builder()
                .id(this.getId())
                .createdAt(this.getCreatedAt())
                .ownerId(this.ownerId)
                .promptType(this.promptType)
                .requestPrompt(this.requestPrompt)
                .responseContent(this.responseContent)
                .isDeleted(this.isDeleted)
                .deletedAt(this.deletedAt)
                .deletedBy(this.deletedBy)
                .createdBy(this.createdBy)
                .updatedAt(this.updatedAt)
                .updatedBy(this.updatedBy)
                .build();
    }

    /**
     * 논리적 삭제 처리
     */
    public void markAsDeleted(String deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 응답 업데이트
     */
    public void updateResponse(String responseContent, String updatedBy) {
        this.responseContent = responseContent;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    // ==================== Helper Methods ====================

    private void setIdFromDomain(String id) {
        try {
            java.lang.reflect.Field field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(this, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID from domain", e);
        }
    }

    private void setCreatedAtFromDomain(LocalDateTime createdAt) {
        try {
            java.lang.reflect.Field field = BaseEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(this, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set createdAt from domain", e);
        }
    }
}