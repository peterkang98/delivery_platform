package xyz.sparta_project.manjok.domain.aiprompt.infrastructure.service.prompt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptException;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.domain.service.PromptService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromptServiceImplTest {

    private PromptService promptService;

    @BeforeEach
    void setUp() {
        promptService = new PromptServiceImpl();
    }

    // ==================== createPrompt ====================

    @Test
    @DisplayName("메뉴 설명 타입으로 프롬프트를 생성할 수 있다")
    void createPrompt_MenuDescription() {
        // given
        String menuInfo = "불고기 버거 - 달콤한 불고기 소스와 신선한 야채";

        // when
        String prompt = promptService.createPrompt(PromptType.MENU_DESCRIPTION, menuInfo);

        // then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains(menuInfo);
        assertThat(prompt).contains("메뉴 정보");
        assertThat(prompt).contains("음식 전문가");
    }

    @Test
    @DisplayName("QnA 타입으로 프롬프트를 생성할 수 있다")
    void createPrompt_Qna() {
        // given
        String question = "배달 시간은 얼마나 걸리나요?";

        // when
        String prompt = promptService.createPrompt(PromptType.QNA, question);

        // then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains(question);
        assertThat(prompt).contains("사용자 질문");
        assertThat(prompt).contains("고객 서비스");
    }

    // ==================== createMenuDescriptionPrompt ====================

    @Test
    @DisplayName("메뉴 설명 프롬프트를 생성할 수 있다")
    void createMenuDescriptionPrompt() {
        // given
        String menuInfo = "김치찌개 - 얼큰하고 시원한 국물에 푹 익은 김치";

        // when
        String prompt = promptService.createMenuDescriptionPrompt(menuInfo);

        // then
        assertThat(prompt).isNotNull();
        assertThat(prompt).isNotEmpty();
        assertThat(prompt).contains(menuInfo);
        assertThat(prompt).contains("메뉴 정보");
        assertThat(prompt).contains("작성 가이드");
        assertThat(prompt).contains("특징과 맛");
        assertThat(prompt).contains("재료");
    }

    @Test
    @DisplayName("메뉴 정보가 템플릿에 올바르게 포함된다")
    void createMenuDescriptionPrompt_ContainsMenuInfo() {
        // given
        String menuInfo = "테스트 메뉴";

        // when
        String prompt = promptService.createMenuDescriptionPrompt(menuInfo);

        // then
        assertThat(prompt).contains("[메뉴 정보]");
        assertThat(prompt).contains(menuInfo);
    }

    @Test
    @DisplayName("메뉴 정보가 null이면 예외가 발생한다")
    void createMenuDescriptionPrompt_NullInput() {
        // when & then
        assertThatThrownBy(() -> promptService.createMenuDescriptionPrompt(null))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("메뉴 정보가 빈 문자열이면 예외가 발생한다")
    void createMenuDescriptionPrompt_EmptyInput() {
        // when & then
        assertThatThrownBy(() -> promptService.createMenuDescriptionPrompt(""))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("메뉴 정보가 공백만 있으면 예외가 발생한다")
    void createMenuDescriptionPrompt_BlankInput() {
        // when & then
        assertThatThrownBy(() -> promptService.createMenuDescriptionPrompt("   "))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("메뉴 정보가 1000자를 초과하면 예외가 발생한다")
    void createMenuDescriptionPrompt_TooLong() {
        // given
        String longMenuInfo = "a".repeat(1001);

        // when & then
        assertThatThrownBy(() -> promptService.createMenuDescriptionPrompt(longMenuInfo))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("메뉴 정보가 1000자이면 정상 처리된다")
    void createMenuDescriptionPrompt_ExactlyMaxLength() {
        // given
        String menuInfo = "a".repeat(1000);

        // when
        String prompt = promptService.createMenuDescriptionPrompt(menuInfo);

        // then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains(menuInfo);
    }

    // ==================== createQnaPrompt ====================

    @Test
    @DisplayName("QnA 프롬프트를 생성할 수 있다")
    void createQnaPrompt() {
        // given
        String question = "주문 취소는 어떻게 하나요?";

        // when
        String prompt = promptService.createQnaPrompt(question);

        // then
        assertThat(prompt).isNotNull();
        assertThat(prompt).isNotEmpty();
        assertThat(prompt).contains(question);
        assertThat(prompt).contains("사용자 질문");
        assertThat(prompt).contains("답변 가이드");
        assertThat(prompt).contains("고객 서비스");
    }

    @Test
    @DisplayName("질문이 템플릿에 올바르게 포함된다")
    void createQnaPrompt_ContainsQuestion() {
        // given
        String question = "배송비는 얼마인가요?";

        // when
        String prompt = promptService.createQnaPrompt(question);

        // then
        assertThat(prompt).contains("[사용자 질문]");
        assertThat(prompt).contains(question);
    }

    @Test
    @DisplayName("질문이 null이면 예외가 발생한다")
    void createQnaPrompt_NullInput() {
        // when & then
        assertThatThrownBy(() -> promptService.createQnaPrompt(null))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("질문이 빈 문자열이면 예외가 발생한다")
    void createQnaPrompt_EmptyInput() {
        // when & then
        assertThatThrownBy(() -> promptService.createQnaPrompt(""))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("질문이 공백만 있으면 예외가 발생한다")
    void createQnaPrompt_BlankInput() {
        // when & then
        assertThatThrownBy(() -> promptService.createQnaPrompt("   "))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("질문이 1000자를 초과하면 예외가 발생한다")
    void createQnaPrompt_TooLong() {
        // given
        String longQuestion = "질문입니다. ".repeat(200);

        // when & then
        assertThatThrownBy(() -> promptService.createQnaPrompt(longQuestion))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("질문이 1000자이면 정상 처리된다")
    void createQnaPrompt_ExactlyMaxLength() {
        // given
        String question = "a".repeat(1000);

        // when
        String prompt = promptService.createQnaPrompt(question);

        // then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains(question);
    }

    // ==================== 통합 시나리오 ====================

    @Test
    @DisplayName("모든 PromptType에 대해 프롬프트를 생성할 수 있다")
    void createPrompt_AllTypes() {
        // given
        String menuInfo = "테스트 메뉴";
        String question = "테스트 질문";

        // when & then
        String menuPrompt = promptService.createPrompt(PromptType.MENU_DESCRIPTION, menuInfo);
        assertThat(menuPrompt).isNotNull();
        assertThat(menuPrompt).contains(menuInfo);

        String qnaPrompt = promptService.createPrompt(PromptType.QNA, question);
        assertThat(qnaPrompt).isNotNull();
        assertThat(qnaPrompt).contains(question);
    }

    @Test
    @DisplayName("같은 입력으로 여러 번 호출해도 동일한 결과를 반환한다")
    void createPrompt_Idempotent() {
        // given
        String input = "테스트 입력";

        // when
        String prompt1 = promptService.createPrompt(PromptType.QNA, input);
        String prompt2 = promptService.createPrompt(PromptType.QNA, input);

        // then
        assertThat(prompt1).isEqualTo(prompt2);
    }

    @Test
    @DisplayName("서로 다른 타입은 서로 다른 프롬프트를 생성한다")
    void createPrompt_DifferentTypes() {
        // given
        String input = "동일한 입력";

        // when
        String menuPrompt = promptService.createPrompt(PromptType.MENU_DESCRIPTION, input);
        String qnaPrompt = promptService.createPrompt(PromptType.QNA, input);

        // then
        assertThat(menuPrompt).isNotEqualTo(qnaPrompt);
        assertThat(menuPrompt).contains("음식 전문가");
        assertThat(qnaPrompt).contains("고객 서비스");
    }

    @Test
    @DisplayName("특수문자가 포함된 입력도 처리할 수 있다")
    void createPrompt_SpecialCharacters() {
        // given
        String input = "메뉴: 불고기 (특), 가격: 15,000원, 평점: ★★★★★";

        // when
        String prompt = promptService.createPrompt(PromptType.MENU_DESCRIPTION, input);

        // then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains(input);
    }

    @Test
    @DisplayName("줄바꿈이 포함된 입력도 처리할 수 있다")
    void createPrompt_WithNewlines() {
        // given
        String input = "메뉴 이름: 김치찌개\n재료: 김치, 돼지고기\n특징: 얼큰함";

        // when
        String prompt = promptService.createPrompt(PromptType.MENU_DESCRIPTION, input);

        // then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains(input);
    }
}