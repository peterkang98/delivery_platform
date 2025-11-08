package xyz.sparta_project.manjok.domain.aiprompt.presentation.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.aiprompt.application.service.AiPromptCommandService;
import xyz.sparta_project.manjok.domain.aiprompt.application.service.AiPromptQueryService;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.presentation.rest.dto.*;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 프롬프트 API 컨트롤러
 * - 기본 경로: /v1/aiprompt
 */
@Tag(name = "AI 프롬프트", description = "AI 프롬프트 생성 및 조회 API")
@Slf4j
@RestController
@RequestMapping("/v1/aiprompt")
@RequiredArgsConstructor
public class AiPromptController {

    private final AiPromptCommandService aiPromptCommandService;
    private final AiPromptQueryService aiPromptQueryService;

    // TODO: 실제 인증된 사용자 ID를 가져오는 로직으로 대체 필요
    private static final String TEMP_USER_ID = "USER123";

    // ==================== CREATE ====================

    /**
     * AI 프롬프트 생성
     * POST /v1/aiprompt
     */
	@Operation(summary = "AI 프롬프트 생성", description = "사용자 입력을 기반으로 AI 프롬프트를 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AiPromptHistoryResponse> createPrompt(
            @Valid @RequestBody AiPromptCreateRequest request) {

        log.info("AI 프롬프트 생성 요청. Type: {}", request.getPromptType());

        AiPromptHistory history = aiPromptCommandService.generateAndSave(
                TEMP_USER_ID,
                request.getPromptType(),
                request.getUserInput(),
                TEMP_USER_ID
        );

        return ApiResponse.success(
                AiPromptHistoryResponse.from(history),
                "AI 프롬프트가 성공적으로 생성되었습니다."
        );
    }

    /**
     * 메뉴 설명 생성
     * POST /v1/aiprompt/menu-description
     */
	@Operation(summary = "메뉴 설명 생성", description = "메뉴 정보를 기반으로 AI 메뉴 설명을 생성합니다.")
    @PostMapping("/menu-description")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AiPromptHistoryResponse> createMenuDescription(
            @Valid @RequestBody MenuDescriptionRequest request) {

        log.info("메뉴 설명 생성 요청");

        AiPromptHistory history = aiPromptCommandService.generateMenuDescription(
                TEMP_USER_ID,
                request.getMenuInfo(),
                TEMP_USER_ID
        );

        return ApiResponse.success(
                AiPromptHistoryResponse.from(history),
                "메뉴 설명이 성공적으로 생성되었습니다."
        );
    }

    /**
     * QnA 응답 생성
     * POST /v1/aiprompt/qna
     */
	@Operation(summary = "QnA 응답 생성", description = "질문에 대한 AI QnA 응답을 생성합니다.")
    @PostMapping("/qna")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AiPromptHistoryResponse> createQna(
            @Valid @RequestBody QnaRequest request) {

        log.info("QnA 응답 생성 요청");

        AiPromptHistory history = aiPromptCommandService.generateQnaResponse(
                TEMP_USER_ID,
                request.getQuestion(),
                TEMP_USER_ID
        );

        return ApiResponse.success(
                AiPromptHistoryResponse.from(history),
                "QnA 응답이 성공적으로 생성되었습니다."
        );
    }

    // ==================== READ ====================

    /**
     * 히스토리 단건 조회
     * GET /v1/aiprompt/{id}
     */
	@Operation(summary = "히스토리 단건 조회", description = "특정 프롬프트 히스토리를 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<AiPromptHistoryResponse> getHistory(
            @PathVariable String id) {

        log.info("히스토리 조회 요청. ID: {}", id);

        AiPromptHistory history = aiPromptQueryService.getByIdWithOwnerCheck(id, TEMP_USER_ID);

        return ApiResponse.success(AiPromptHistoryResponse.from(history));
    }

    /**
     * 내 히스토리 목록 조회
     * GET /v1/aiprompt/my
     */
	@Operation(summary = "내 히스토리 목록 조회", description = "내가 생성한 모든 프롬프트 히스토리를 조회합니다.")
    @GetMapping("/my")
    public ApiResponse<List<AiPromptHistorySummaryResponse>> getMyHistories() {

        log.info("내 히스토리 목록 조회 요청");

        List<AiPromptHistory> histories = aiPromptQueryService.getByOwnerId(TEMP_USER_ID);

        List<AiPromptHistorySummaryResponse> responses = histories.stream()
                .map(AiPromptHistorySummaryResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * 타입별 히스토리 목록 조회
     * GET /v1/aiprompt/my/type/{promptType}
     */
	@Operation(summary = "타입별 히스토리 목록 조회", description = "특정 타입의 프롬프트 히스토리를 조회합니다.")
    @GetMapping("/my/type/{promptType}")
    public ApiResponse<List<AiPromptHistorySummaryResponse>> getMyHistoriesByType(
            @PathVariable PromptType promptType) {

        log.info("타입별 히스토리 목록 조회 요청. Type: {}", promptType);

        List<AiPromptHistory> histories = aiPromptQueryService.getByOwnerIdAndPromptType(
                TEMP_USER_ID,
                promptType
        );

        List<AiPromptHistorySummaryResponse> responses = histories.stream()
                .map(AiPromptHistorySummaryResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * 내 메뉴 설명 목록 조회
     * GET /v1/aiprompt/my/menu-descriptions
     */
	@Operation(summary = "내 메뉴 설명 목록 조회", description = "내가 생성한 메뉴 설명 목록을 조회합니다.")
    @GetMapping("/my/menu-descriptions")
    public ApiResponse<List<AiPromptHistorySummaryResponse>> getMyMenuDescriptions() {

        log.info("내 메뉴 설명 목록 조회 요청");

        List<AiPromptHistory> histories = aiPromptQueryService.getMenuDescriptionsByOwnerId(TEMP_USER_ID);

        List<AiPromptHistorySummaryResponse> responses = histories.stream()
                .map(AiPromptHistorySummaryResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * 내 QnA 목록 조회
     * GET /v1/aiprompt/my/qnas
     */
	@Operation(summary = "내 QnA 목록 조회", description = "내가 생성한 QnA 목록을 조회합니다.")
    @GetMapping("/my/qnas")
    public ApiResponse<List<AiPromptHistorySummaryResponse>> getMyQnas() {

        log.info("내 QnA 목록 조회 요청");

        List<AiPromptHistory> histories = aiPromptQueryService.getQnasByOwnerId(TEMP_USER_ID);

        List<AiPromptHistorySummaryResponse> responses = histories.stream()
                .map(AiPromptHistorySummaryResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * 내 히스토리 통계 조회
     * GET /v1/aiprompt/my/stats
     */
	@Operation(summary = "내 히스토리 통계 조회", description = "내 프롬프트 히스토리 통계 정보를 조회합니다.")
    @GetMapping("/my/stats")
    public ApiResponse<AiPromptStatsResponse> getMyStats() {

        log.info("내 히스토리 통계 조회 요청");

        long totalCount = aiPromptQueryService.countByOwnerId(TEMP_USER_ID);
        long menuDescriptionCount = aiPromptQueryService.countMenuDescriptionsByOwnerId(TEMP_USER_ID);
        long qnaCount = aiPromptQueryService.countQnasByOwnerId(TEMP_USER_ID);

        AiPromptStatsResponse stats = AiPromptStatsResponse.builder()
                .totalCount(totalCount)
                .menuDescriptionCount(menuDescriptionCount)
                .qnaCount(qnaCount)
                .build();

        return ApiResponse.success(stats);
    }

    // ==================== UPDATE ====================

    /**
     * 응답 재생성
     * POST /v1/aiprompt/{id}/regenerate
     */
	@Operation(summary = "응답 재생성", description = "기존 프롬프트의 응답을 재생성합니다.")
    @PostMapping("/{id}/regenerate")
    public ApiResponse<AiPromptHistoryResponse> regenerateResponse(
            @PathVariable String id) {

        log.info("응답 재생성 요청. ID: {}", id);

        AiPromptHistory history = aiPromptCommandService.regenerateResponse(
                id,
                TEMP_USER_ID,
                TEMP_USER_ID
        );

        return ApiResponse.success(
                AiPromptHistoryResponse.from(history),
                "응답이 성공적으로 재생성되었습니다."
        );
    }

    // ==================== DELETE ====================

    /**
     * 히스토리 삭제
     * DELETE /v1/aiprompt/{id}
     */
	@Operation(summary = "히스토리 삭제", description = "특정 프롬프트 히스토리를 삭제합니다.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteHistory(
            @PathVariable String id) {

        log.info("히스토리 삭제 요청. ID: {}", id);

        aiPromptCommandService.delete(id, TEMP_USER_ID, TEMP_USER_ID);

        return ApiResponse.success(null, "히스토리가 성공적으로 삭제되었습니다.");
    }
}