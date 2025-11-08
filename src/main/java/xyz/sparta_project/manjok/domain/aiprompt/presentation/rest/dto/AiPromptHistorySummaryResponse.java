package xyz.sparta_project.manjok.domain.aiprompt.presentation.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;

import java.time.LocalDateTime;

/**
 * AI 프롬프트 히스토리 요약 응답 DTO (목록용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPromptHistorySummaryResponse {

    private String id;
    private PromptType promptType;
    private String promptTypeName;
    private String requestPrompt;
    private String responseContent;
    private Boolean hasResponse;
    private LocalDateTime createdAt;

    /**
     * 도메인 → 요약 응답 DTO 변환
     */
    public static AiPromptHistorySummaryResponse from(AiPromptHistory domain) {
        return AiPromptHistorySummaryResponse.builder()
                .id(domain.getId())
                .promptType(domain.getPromptType())
                .promptTypeName(domain.getPromptType().getDisplayName())
                .requestPrompt(getPreview(domain.getRequestPrompt(), 100))
                .responseContent(getPreview(domain.getResponseContent(), 200))
                .hasResponse(domain.hasResponse())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private static String getPreview(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}