package xyz.sparta_project.manjok.domain.aiprompt.presentation.rest.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.aiprompt.application.service.AiPromptCommandService;
import xyz.sparta_project.manjok.domain.aiprompt.application.service.AiPromptQueryService;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.presentation.rest.dto.*;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 프롬프트 Customer API 컨트롤러
 * - 기본 경로: /v1/customers/aiprompt
 * - 권한: CUSTOMER
 */
@Slf4j
@RestController
@RequestMapping("/v1/customers/aiprompt")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class AiPromptCustomerController {

    private final AiPromptCommandService aiPromptCommandService;
    private final AiPromptQueryService aiPromptQueryService;

    // ==================== CREATE ====================

    /**
     * AI 프롬프트 생성
     * POST /v1/customers/aiprompt
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AiPromptHistoryResponse> createPrompt(
            @Valid @RequestBody AiPromptCreateRequest request) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("AI 프롬프트 생성 요청. UserId: {}, Type: {}", userId, request.getPromptType());

        AiPromptHistory history = aiPromptCommandService.generateAndSave(
                userId,
                request.getPromptType(),
                request.getUserInput(),
                userId
        );

        return ApiResponse.success(
                AiPromptHistoryResponse.from(history),
                "AI 프롬프트가 성공적으로 생성되었습니다."
        );
    }

    /**
     * QnA 응답 생성
     * POST /v1/customers/aiprompt/qna
     */
    @PostMapping("/qna")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AiPromptHistoryResponse> createQna(
            @Valid @RequestBody QnaRequest request) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("QnA 응답 생성 요청. UserId: {}", userId);

        AiPromptHistory history = aiPromptCommandService.generateQnaResponse(
                userId,
                request.getQuestion(),
                userId
        );

        return ApiResponse.success(
                AiPromptHistoryResponse.from(history),
                "QnA 응답이 성공적으로 생성되었습니다."
        );
    }

    // ==================== READ ====================

    /**
     * 히스토리 단건 조회
     * GET /v1/customers/aiprompt/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<AiPromptHistoryResponse> getHistory(
            @PathVariable String id) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("히스토리 조회 요청. UserId: {}, ID: {}", userId, id);

        AiPromptHistory history = aiPromptQueryService.getByIdWithOwnerCheck(id, userId);

        return ApiResponse.success(AiPromptHistoryResponse.from(history));
    }

    /**
     * 내 히스토리 목록 조회
     * GET /v1/customers/aiprompt/my
     */
    @GetMapping("/my")
    public ApiResponse<List<AiPromptHistorySummaryResponse>> getMyHistories() {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("내 히스토리 목록 조회 요청. UserId: {}", userId);

        List<AiPromptHistory> histories = aiPromptQueryService.getByOwnerId(userId);

        List<AiPromptHistorySummaryResponse> responses = histories.stream()
                .map(AiPromptHistorySummaryResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * 타입별 히스토리 목록 조회
     * GET /v1/customers/aiprompt/my/type/{promptType}
     */
    @GetMapping("/my/type/{promptType}")
    public ApiResponse<List<AiPromptHistorySummaryResponse>> getMyHistoriesByType(
            @PathVariable PromptType promptType) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("타입별 히스토리 목록 조회 요청. UserId: {}, Type: {}", userId, promptType);

        List<AiPromptHistory> histories = aiPromptQueryService.getByOwnerIdAndPromptType(
                userId,
                promptType
        );

        List<AiPromptHistorySummaryResponse> responses = histories.stream()
                .map(AiPromptHistorySummaryResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * 내 QnA 목록 조회
     * GET /v1/customers/aiprompt/my/qnas
     */
    @GetMapping("/my/qnas")
    public ApiResponse<List<AiPromptHistorySummaryResponse>> getMyQnas() {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("내 QnA 목록 조회 요청. UserId: {}", userId);

        List<AiPromptHistory> histories = aiPromptQueryService.getQnasByOwnerId(userId);

        List<AiPromptHistorySummaryResponse> responses = histories.stream()
                .map(AiPromptHistorySummaryResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * 내 히스토리 통계 조회
     * GET /v1/customers/aiprompt/my/stats
     */
    @GetMapping("/my/stats")
    public ApiResponse<AiPromptStatsResponse> getMyStats() {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("내 히스토리 통계 조회 요청. UserId: {}", userId);

        long totalCount = aiPromptQueryService.countByOwnerId(userId);
        long qnaCount = aiPromptQueryService.countQnasByOwnerId(userId);

        AiPromptStatsResponse stats = AiPromptStatsResponse.builder()
                .totalCount(totalCount)
                .menuDescriptionCount(0L)
                .qnaCount(qnaCount)
                .build();

        return ApiResponse.success(stats);
    }

    // ==================== UPDATE ====================

    /**
     * 응답 재생성
     * POST /v1/customers/aiprompt/{id}/regenerate
     */
    @PostMapping("/{id}/regenerate")
    public ApiResponse<AiPromptHistoryResponse> regenerateResponse(
            @PathVariable String id) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("응답 재생성 요청. UserId: {}, ID: {}", userId, id);

        AiPromptHistory history = aiPromptCommandService.regenerateResponse(
                id,
                userId,
                userId
        );

        return ApiResponse.success(
                AiPromptHistoryResponse.from(history),
                "응답이 성공적으로 재생성되었습니다."
        );
    }

    // ==================== DELETE ====================

    /**
     * 히스토리 삭제
     * DELETE /v1/customers/aiprompt/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteHistory(
            @PathVariable String id) {

        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("히스토리 삭제 요청. UserId: {}, ID: {}", userId, id);

        aiPromptCommandService.delete(id, userId, userId);

        return ApiResponse.success(null, "히스토리가 성공적으로 삭제되었습니다.");
    }
}