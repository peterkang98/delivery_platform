package xyz.sparta_project.manjok.domain.aiprompt.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * QnA 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaRequest {

    @NotBlank(message = "질문은 필수입니다.")
    @Size(max = 5000, message = "질문은 5000자를 초과할 수 없습니다.")
    private String question;
}