package xyz.sparta_project.manjok.domain.aiprompt.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

/**
 * AI 프롬프트 도메인 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum AiPromptErrorCode implements ErrorCode {

    // 생성 관련 (AIPROMPT_001~009)
    OWNER_ID_REQUIRED("AIPROMPT_001", "소유자 ID는 필수입니다.", 400),
    PROMPT_TYPE_REQUIRED("AIPROMPT_002", "프롬프트 타입은 필수입니다.", 400),
    REQUEST_PROMPT_REQUIRED("AIPROMPT_003", "요청 프롬프트는 필수입니다.", 400),
    REQUEST_PROMPT_TOO_LONG("AIPROMPT_004", "요청 프롬프트가 너무 깁니다. (최대 10,000자)", 400),

    // 응답 관련 (AIPROMPT_010~019)
    RESPONSE_CONTENT_REQUIRED("AIPROMPT_010", "응답 내용은 필수입니다.", 400),
    CANNOT_UPDATE_DELETED_HISTORY("AIPROMPT_011", "삭제된 히스토리는 수정할 수 없습니다.", 400),
    RESPONSE_GENERATION_FAILED("AIPROMPT_012", "AI 응답 생성 중 오류가 발생했습니다.", 500),

    // 삭제 관련 (AIPROMPT_020~029)
    ALREADY_DELETED("AIPROMPT_020", "이미 삭제된 히스토리입니다.", 400),
    NOT_DELETED("AIPROMPT_021", "삭제되지 않은 히스토리입니다.", 400),

    // 조회 관련 (AIPROMPT_030~039)
    HISTORY_NOT_FOUND("AIPROMPT_030", "히스토리를 찾을 수 없습니다.", 404),
    HISTORY_NOT_BELONG_TO_OWNER("AIPROMPT_031", "해당 히스토리는 이 사용자에게 속하지 않습니다.", 403),

    // API 관련 (AIPROMPT_040~049)
    GEMINI_API_ERROR("AIPROMPT_040", "Gemini API 호출 중 오류가 발생했습니다.", 500),
    GEMINI_API_TIMEOUT("AIPROMPT_041", "Gemini API 요청 시간이 초과되었습니다.", 504),
    GEMINI_API_QUOTA_EXCEEDED("AIPROMPT_042", "Gemini API 할당량을 초과했습니다.", 429),

    // 이벤트 처리 (AIPROMPT_050~059)
    EVENT_PROCESSING_FAILED("AIPROMPT_050", "이벤트 처리 중 오류가 발생했습니다.", 500);

    private final String code;
    private final String message;
    private final int status;
}