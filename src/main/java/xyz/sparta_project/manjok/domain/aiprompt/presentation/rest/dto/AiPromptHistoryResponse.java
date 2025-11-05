package xyz.sparta_project.manjok.domain.aiprompt.presentation.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;

import java.time.LocalDateTime;

/**
 * AI 프롬프트 히스토리 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPromptHistoryResponse {

    private String id;
    private String ownerId;
    private PromptType promptType;
    private String promptTypeName;
    private String requestPrompt;
    private String responseContent;
    private Integer responseLength;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 도메인 → 응답 DTO 변환
     */
    public static AiPromptHistoryResponse from(AiPromptHistory domain) {
        return AiPromptHistoryResponse.builder()
                .id(domain.getId())
                .ownerId(domain.getOwnerId())
                .promptType(domain.getPromptType())
                .promptTypeName(domain.getPromptType().getDisplayName())
                .requestPrompt(domain.getRequestPrompt())
                .responseContent(domain.getResponseContent())
                .responseLength(domain.getResponseLength())
                .isDeleted(domain.getIsDeleted())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}