package xyz.sparta_project.manjok.domain.aiprompt.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.domain.repository.AiPromptHistoryRepository;
import xyz.sparta_project.manjok.domain.aiprompt.domain.service.AiClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("실제 AI API 호출이 필요한 테스트. 필요시 주석 해제하고 실행하세요.")
class AiPromptCommandServiceRealApiTest {

    @Autowired
    private AiPromptCommandService commandService;

    @Autowired
    private AiPromptHistoryRepository repository;

    @Autowired
    private AiClient aiClient;

    private String ownerId;
    private String createdBy;

    @BeforeEach
    void setUp() {
        ownerId = "integration-test-user";
        createdBy = "integration-test-user";

        System.out.println("=".repeat(80));
        System.out.println("실제 AI API 통합 테스트 시작");
        System.out.println("=".repeat(80));
    }

    @Test
    @DisplayName("실제 AI API를 호출하여 메뉴 설명을 생성하고 응답을 확인한다")
    void realApiTest_GenerateMenuDescription() {
        String menuInfo = """
                메뉴명: 불고기 버거
                가격: 8,500원
                재료: 프리미엄 소고기 패티, 특제 불고기 소스, 신선한 양상추, 토마토
                특징: 달콤하고 짭짤한 불고기 소스가 패티와 환상의 조화를 이룹니다.
                """;

        System.out.println("\n" + "=".repeat(80));
        System.out.println("테스트 시작: 메뉴 설명 생성");
        System.out.println("메뉴 정보:\n" + menuInfo);
        System.out.println("=".repeat(80));

        long startTime = System.currentTimeMillis();
        AiPromptHistory result = commandService.generateMenuDescription(
                ownerId,
                menuInfo,
                createdBy
        );
        long endTime = System.currentTimeMillis();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOwnerId()).isEqualTo(ownerId);
        assertThat(result.getPromptType()).isEqualTo(PromptType.MENU_DESCRIPTION);
        assertThat(result.getRequestPrompt()).isEqualTo(menuInfo);
        assertThat(result.hasResponse()).isTrue();
        assertThat(result.getResponseContent()).isNotEmpty();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("메뉴 설명 생성 완료!");
        System.out.println("=".repeat(80));
        System.out.println("히스토리 ID: " + result.getId());
        System.out.println("프롬프트 타입: " + result.getPromptType());
        System.out.println("요청 시간: " + (endTime - startTime) + " ms");
        System.out.println("응답 길이: " + result.getResponseContent().length() + " 자");
        System.out.println("-".repeat(80));
        System.out.println("AI 생성 응답:");
        System.out.println("-".repeat(80));
        System.out.println(result.getResponseContent());
        System.out.println("=".repeat(80));

        AiPromptHistory saved = repository.findById(result.getId()).orElseThrow();
        assertThat(saved.getResponseContent()).isEqualTo(result.getResponseContent());

        System.out.println("DB 저장 확인 완료 ✓");
        System.out.println("=".repeat(80) + "\n");
    }

    @Test
    @DisplayName("실제 AI API를 호출하여 QnA 응답을 생성하고 응답을 확인한다")
    void realApiTest_GenerateQnaResponse() {
        String question = "배달 음식 주문 시 배달비는 어떻게 계산되나요? 거리에 따라 달라지나요?";

        System.out.println("\n" + "=".repeat(80));
        System.out.println("테스트 시작: QnA 응답 생성");
        System.out.println("질문: " + question);
        System.out.println("=".repeat(80));

        long startTime = System.currentTimeMillis();
        AiPromptHistory result = commandService.generateQnaResponse(ownerId, question, createdBy);
        long endTime = System.currentTimeMillis();

        assertThat(result).isNotNull();
        assertThat(result.getPromptType()).isEqualTo(PromptType.QNA);
        assertThat(result.getResponseContent()).isNotEmpty();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("QnA 응답 생성 완료!");
        System.out.println("응답 길이: " + result.getResponseContent().length() + " 자");
        System.out.println("-".repeat(80));
        System.out.println(result.getResponseContent());
        System.out.println("=".repeat(80));

        AiPromptHistory saved = repository.findById(result.getId()).orElseThrow();
        assertThat(saved.getResponseContent()).isEqualTo(result.getResponseContent());

        System.out.println("DB 저장 확인 완료 ✓");
        System.out.println("=".repeat(80) + "\n");
    }

    @Test
    @DisplayName("AiClient가 정상적으로 주입되었는지 확인한다")
    void verifyAiClientInjection() {
        assertThat(aiClient).isNotNull();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("AiClient 주입 확인");
        System.out.println("AiClient 클래스: " + aiClient.getClass().getName());
        System.out.println("AiClient 주입 완료 ✓");
        System.out.println("=".repeat(80) + "\n");
    }
}
