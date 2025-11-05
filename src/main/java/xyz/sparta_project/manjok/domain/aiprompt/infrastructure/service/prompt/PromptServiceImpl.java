// infrastructure/service/prompt/PromptServiceImpl.java
package xyz.sparta_project.manjok.domain.aiprompt.infrastructure.service.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptErrorCode;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptException;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.domain.service.PromptService;

/**
 * 프롬프트 생성 서비스 구현체
 */
@Slf4j
@Service
public class PromptServiceImpl implements PromptService {

    @Override
    public String createPrompt(PromptType promptType, String input) {
        validateInput(input);

        return switch (promptType) {
            case MENU_DESCRIPTION -> createMenuDescriptionPrompt(input);
            case QNA -> createQnaPrompt(input);
        };
    }

    @Override
    public String createMenuDescriptionPrompt(String menuInfo) {
        validateInput(menuInfo);

        String prompt = String.format(PromptTemplates.MENU_DESCRIPTION_TEMPLATE, menuInfo);

        log.debug("메뉴 설명 프롬프트 생성 완료. 길이: {}", prompt.length());

        return prompt;
    }

    @Override
    public String createQnaPrompt(String question) {
        validateInput(question);

        String prompt = String.format(PromptTemplates.QNA_TEMPLATE, question);

        log.debug("QnA 프롬프트 생성 완료. 길이: {}", prompt.length());

        return prompt;
    }

    /**
     * 입력값 검증
     */
    private void validateInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new AiPromptException(AiPromptErrorCode.REQUEST_PROMPT_REQUIRED);
        }

        if (input.length() > 1000) {
            throw new AiPromptException(
                    AiPromptErrorCode.REQUEST_PROMPT_TOO_LONG,
                    "입력값이 너무 깁니다. (최대 1,000자)"
            );
        }
    }
}