package xyz.sparta_project.manjok.domain.aiprompt.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptException;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.domain.repository.AiPromptHistoryRepository;
import xyz.sparta_project.manjok.domain.aiprompt.domain.service.AiClient;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.jpa.AiPromptHistoryJpaRepository;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.repository.AiPromptHistoryRepositoryImpl;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.service.prompt.PromptServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@DataJpaTest
@Import({
        AiPromptHistoryRepositoryImpl.class,
        PromptServiceImpl.class,
        AiPromptCommandService.class
})
@ActiveProfiles("test")
class AiPromptCommandServiceTest {

    @Autowired
    private AiPromptCommandService commandService;

    @Autowired
    private AiPromptHistoryRepository repository;

    @Autowired
    private AiPromptHistoryJpaRepository jpaRepository;

    @MockitoBean
    private AiClient aiClient;

    @Autowired
    private AiClient aiClient2;

    private String ownerId;
    private String createdBy;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();

        ownerId = "user123";
        createdBy = "user123";

        // AI Client Mock 기본 응답 설정
        when(aiClient.sendRequest(anyString()))
                .thenReturn("AI가 생성한 응답입니다.");
    }

    // ==================== generateAndSave ====================

    @Test
    @DisplayName("AI 프롬프트를 생성하고 히스토리를 저장할 수 있다")
    void generateAndSave() {
        // given
        String userInput = "불고기 버거에 대해 설명해주세요";
        String aiResponse = "불고기 버거는 달콤한 불고기 소스가 특징입니다.";
        when(aiClient.sendRequest(anyString())).thenReturn(aiResponse);

        // when
        AiPromptHistory result = commandService.generateAndSave(
                ownerId,
                PromptType.MENU_DESCRIPTION,
                userInput,
                createdBy
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOwnerId()).isEqualTo(ownerId);
        assertThat(result.getPromptType()).isEqualTo(PromptType.MENU_DESCRIPTION);
        assertThat(result.getRequestPrompt()).isEqualTo(userInput);
        assertThat(result.getResponseContent()).isEqualTo(aiResponse);
        assertThat(result.hasResponse()).isTrue();

        verify(aiClient, times(1)).sendRequest(anyString());
    }

    @Test
    @DisplayName("QnA 타입으로 프롬프트를 생성할 수 있다")
    void generateAndSave_QnaType() {
        // given
        String question = "배달 시간은 얼마나 걸리나요?";
        String aiResponse = "배달 시간은 평균 30분 정도 소요됩니다.";
        when(aiClient.sendRequest(anyString())).thenReturn(aiResponse);

        // when
        AiPromptHistory result = commandService.generateAndSave(
                ownerId,
                PromptType.QNA,
                question,
                createdBy
        );

        // then
        assertThat(result.getPromptType()).isEqualTo(PromptType.QNA);
        assertThat(result.getRequestPrompt()).isEqualTo(question);
        assertThat(result.getResponseContent()).isEqualTo(aiResponse);
    }

    @Test
    @DisplayName("AI 응답이 히스토리에 저장된다")
    void generateAndSave_SavesAiResponse() {
        // given
        String userInput = "테스트 입력";
        String aiResponse = "테스트 AI 응답입니다.";
        when(aiClient.sendRequest(anyString())).thenReturn(aiResponse);

        // when
        AiPromptHistory saved = commandService.generateAndSave(
                ownerId,
                PromptType.QNA,
                userInput,
                createdBy
        );

        // then - DB에서 다시 조회하여 검증
        AiPromptHistory found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getResponseContent()).isEqualTo(aiResponse);
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    // ==================== generateMenuDescription ====================

    @Test
    @DisplayName("메뉴 설명을 생성할 수 있다")
    void generateMenuDescription() {
        // given
        String menuInfo = "김치찌개 - 얼큰하고 시원한";
        String aiResponse = "김치찌개는 한국의 대표 음식입니다.";
        when(aiClient.sendRequest(anyString())).thenReturn(aiResponse);

        // when
        AiPromptHistory result = commandService.generateMenuDescription(
                ownerId,
                menuInfo,
                createdBy
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPromptType()).isEqualTo(PromptType.MENU_DESCRIPTION);
        assertThat(result.getRequestPrompt()).isEqualTo(menuInfo);
        assertThat(result.getResponseContent()).isEqualTo(aiResponse);
    }

    // ==================== generateQnaResponse ====================

    @Test
    @DisplayName("QnA 응답을 생성할 수 있다")
    void generateQnaResponse() {
        // given
        String question = "주문 취소는 어떻게 하나요?";
        String aiResponse = "주문 취소는 마이페이지에서 가능합니다.";
        when(aiClient.sendRequest(anyString())).thenReturn(aiResponse);

        // when
        AiPromptHistory result = commandService.generateQnaResponse(
                ownerId,
                question,
                createdBy
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPromptType()).isEqualTo(PromptType.QNA);
        assertThat(result.getRequestPrompt()).isEqualTo(question);
        assertThat(result.getResponseContent()).isEqualTo(aiResponse);
    }

    // ==================== delete ====================

    @Test
    @DisplayName("히스토리를 삭제할 수 있다")
    void delete() {
        // given
        AiPromptHistory saved = repository.save(
                AiPromptHistory.create(ownerId, PromptType.QNA, "질문", createdBy)
        );

        // when
        commandService.delete(saved.getId(), ownerId, "admin");

        // then
        assertThat(repository.findById(saved.getId())).isEmpty();
        assertThat(repository.findByIdIncludingDeleted(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 히스토리를 삭제하면 예외가 발생한다")
    void delete_NotFound() {
        // given
        String nonExistingId = "non-existing-id";

        // when & then
        assertThatThrownBy(() -> commandService.delete(nonExistingId, ownerId, "admin"))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("다른 소유자의 히스토리를 삭제하면 예외가 발생한다")
    void delete_DifferentOwner() {
        // given
        AiPromptHistory saved = repository.save(
                AiPromptHistory.create(ownerId, PromptType.QNA, "질문", createdBy)
        );
        String otherOwnerId = "other-user";

        // when & then
        assertThatThrownBy(() -> commandService.delete(saved.getId(), otherOwnerId, "admin"))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("이미 삭제된 히스토리를 다시 삭제하면 예외가 발생한다")
    void delete_AlreadyDeleted() {
        // given
        AiPromptHistory saved = repository.save(
                AiPromptHistory.create(ownerId, PromptType.QNA, "질문", createdBy)
        );
        commandService.delete(saved.getId(), ownerId, "admin");

        // when & then
        assertThatThrownBy(() -> commandService.delete(saved.getId(), ownerId, "admin2"))
                .isInstanceOf(AiPromptException.class);
    }

    // ==================== regenerateResponse ====================

    @Test
    @DisplayName("응답을 재생성할 수 있다")
    void regenerateResponse() {
        // given
        String userInput = "테스트 질문";
        String initialResponse = "초기 응답";
        String newResponse = "재생성된 응답";

        when(aiClient.sendRequest(anyString()))
                .thenReturn(initialResponse)
                .thenReturn(newResponse);

        AiPromptHistory saved = commandService.generateAndSave(
                ownerId,
                PromptType.QNA,
                userInput,
                createdBy
        );

        // when
        AiPromptHistory regenerated = commandService.regenerateResponse(
                saved.getId(),
                ownerId,
                createdBy
        );

        // then
        assertThat(regenerated.getResponseContent()).isEqualTo(newResponse);
        assertThat(regenerated.getRequestPrompt()).isEqualTo(userInput);
        verify(aiClient, times(2)).sendRequest(anyString());
    }

    @Test
    @DisplayName("존재하지 않는 히스토리의 응답을 재생성하면 예외가 발생한다")
    void regenerateResponse_NotFound() {
        // given
        String nonExistingId = "non-existing-id";

        // when & then
        assertThatThrownBy(() -> commandService.regenerateResponse(nonExistingId, ownerId, createdBy))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("다른 소유자의 히스토리 응답을 재생성하면 예외가 발생한다")
    void regenerateResponse_DifferentOwner() {
        // given
        AiPromptHistory saved = repository.save(
                AiPromptHistory.create(ownerId, PromptType.QNA, "질문", createdBy)
        );
        String otherOwnerId = "other-user";

        // when & then
        assertThatThrownBy(() -> commandService.regenerateResponse(saved.getId(), otherOwnerId, createdBy))
                .isInstanceOf(AiPromptException.class);
    }

    // ==================== 통합 시나리오 ====================

    @Test
    @DisplayName("생성, 조회, 삭제를 순차적으로 수행할 수 있다")
    void fullLifecycle() {
        // given
        String userInput = "테스트 메뉴";
        String aiResponse = "테스트 응답";
        when(aiClient.sendRequest(anyString())).thenReturn(aiResponse);

        // when - 생성
        AiPromptHistory created = commandService.generateMenuDescription(
                ownerId,
                userInput,
                createdBy
        );

        // then - 생성 확인
        assertThat(created.getId()).isNotNull();
        assertThat(created.getResponseContent()).isEqualTo(aiResponse);

        // when - 조회
        AiPromptHistory found = repository.findById(created.getId()).orElseThrow();

        // then - 조회 확인
        assertThat(found.getId()).isEqualTo(created.getId());

        // when - 삭제
        commandService.delete(created.getId(), ownerId, "admin");

        // then - 삭제 확인
        assertThat(repository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("여러 타입의 히스토리를 생성할 수 있다")
    void generateMultipleTypes() {
        // given
        when(aiClient.sendRequest(anyString()))
                .thenReturn("메뉴 응답")
                .thenReturn("QnA 응답");

        // when
        AiPromptHistory menu = commandService.generateMenuDescription(
                ownerId,
                "메뉴 정보",
                createdBy
        );

        AiPromptHistory qna = commandService.generateQnaResponse(
                ownerId,
                "질문",
                createdBy
        );

        // then
        assertThat(menu.getPromptType()).isEqualTo(PromptType.MENU_DESCRIPTION);
        assertThat(qna.getPromptType()).isEqualTo(PromptType.QNA);
        assertThat(repository.countByOwnerId(ownerId)).isEqualTo(2);
    }

    @Test
    @DisplayName("응답 재생성 후 이전 응답은 덮어써진다")
    void regenerateResponse_OverwritesPreviousResponse() {
        // given
        String userInput = "질문";
        String firstResponse = "첫 번째 응답";
        String secondResponse = "두 번째 응답";

        when(aiClient.sendRequest(anyString()))
                .thenReturn(firstResponse)
                .thenReturn(secondResponse);

        AiPromptHistory saved = commandService.generateQnaResponse(
                ownerId,
                userInput,
                createdBy
        );

        assertThat(saved.getResponseContent()).isEqualTo(firstResponse);

        // when
        AiPromptHistory regenerated = commandService.regenerateResponse(
                saved.getId(),
                ownerId,
                createdBy
        );

        // then
        assertThat(regenerated.getResponseContent()).isEqualTo(secondResponse);
        assertThat(regenerated.getResponseContent()).isNotEqualTo(firstResponse);
    }

    @Test
    @DisplayName("AI Client 호출 실패 시 예외가 발생한다")
    void generateAndSave_AiClientFails() {
        // given
        when(aiClient.sendRequest(anyString()))
                .thenThrow(new RuntimeException("AI API 호출 실패"));

        // when & then
        assertThatThrownBy(() -> commandService.generateQnaResponse(
                ownerId,
                "질문",
                createdBy
        )).isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("동일한 소유자가 여러 개의 히스토리를 생성할 수 있다")
    void generateMultipleHistoriesBySameOwner() {
        // given
        when(aiClient.sendRequest(anyString()))
                .thenReturn("응답1")
                .thenReturn("응답2")
                .thenReturn("응답3");

        // when
        commandService.generateQnaResponse(ownerId, "질문1", createdBy);
        commandService.generateQnaResponse(ownerId, "질문2", createdBy);
        commandService.generateQnaResponse(ownerId, "질문3", createdBy);

        // then
        assertThat(repository.countByOwnerId(ownerId)).isEqualTo(3);
    }

    @Test
    @DisplayName("소유자 확인이 정확하게 동작한다")
    void ownershipValidation() {
        // given
        String owner1 = "user1";
        String owner2 = "user2";

        when(aiClient.sendRequest(anyString())).thenReturn("응답");

        AiPromptHistory history1 = commandService.generateQnaResponse(
                owner1,
                "질문",
                owner1
        );

        // when & then - owner2가 owner1의 히스토리를 삭제하려고 시도
        assertThatThrownBy(() -> commandService.delete(history1.getId(), owner2, owner2))
                .isInstanceOf(AiPromptException.class);

        // when & then - owner2가 owner1의 히스토리를 재생성하려고 시도
        assertThatThrownBy(() -> commandService.regenerateResponse(history1.getId(), owner2, owner2))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("실제 AI API를 호출하여 메뉴 설명을 생성하고 응답을 확인한다")
    void realApiTest_GenerateMenuDescription() {
        // given
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

        // when
        long startTime = System.currentTimeMillis();
        AiPromptHistory result = commandService.generateMenuDescription(
                ownerId,
                menuInfo,
                createdBy
        );
        long endTime = System.currentTimeMillis();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOwnerId()).isEqualTo(ownerId);
        assertThat(result.getPromptType()).isEqualTo(PromptType.MENU_DESCRIPTION);
        assertThat(result.getRequestPrompt()).isEqualTo(menuInfo);
        assertThat(result.hasResponse()).isTrue();
        assertThat(result.getResponseContent()).isNotNull();
        assertThat(result.getResponseContent()).isNotEmpty();

        // 결과 출력
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
        System.out.println("\n" + result.getResponseContent() + "\n");
        System.out.println("=".repeat(80));

        // DB 저장 확인
        AiPromptHistory saved = repository.findById(result.getId()).orElseThrow();
        assertThat(saved.getResponseContent()).isEqualTo(result.getResponseContent());

        System.out.println("DB 저장 확인 완료 ✓");
        System.out.println("=".repeat(80) + "\n");
    }
}