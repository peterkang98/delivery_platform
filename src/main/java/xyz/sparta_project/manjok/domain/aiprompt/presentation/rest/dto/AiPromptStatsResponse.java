// presentation/rest/dto/AiPromptStatsResponse.java
package xyz.sparta_project.manjok.domain.aiprompt.presentation.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 프롬프트 통계 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPromptStatsResponse {

    private Long totalCount;
    private Long menuDescriptionCount;
    private Long qnaCount;
}