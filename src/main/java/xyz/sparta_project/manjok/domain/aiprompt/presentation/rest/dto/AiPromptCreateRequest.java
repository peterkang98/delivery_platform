package xyz.sparta_project.manjok.domain.aiprompt.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;

/**
 * AI 프롬프트 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPromptCreateRequest {

    @NotNull(message = "프롬프트 타입은 필수입니다.")
    private PromptType promptType;

    @NotBlank(message = "입력 내용은 필수입니다.")
    @Size(max = 5000, message = "입력 내용은 5000자를 초과할 수 없습니다.")
    private String userInput;
}