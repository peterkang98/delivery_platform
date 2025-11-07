// infrastructure/client/GeminiApiClient.java
package xyz.sparta_project.manjok.domain.aiprompt.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptErrorCode;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptException;
import xyz.sparta_project.manjok.domain.aiprompt.domain.service.AiClient;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.client.dto.GeminiRequest;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.client.dto.GeminiResponse;

/**
 * Gemini API 클라이언트 구현체
 * - RestTemplate을 사용한 동기 방식 구현
 * - AiClient 도메인 인터페이스 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiApiClient implements AiClient {

    private final RestTemplate geminiRestTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    @Override
    public String sendRequest(String prompt) {
        validatePrompt(prompt);

        try {
            log.info("Gemini API 요청 시작. Model: {}, Prompt 길이: {}", model, prompt.length());

            // 1. 요청 DTO 생성
            GeminiRequest request = GeminiRequest.of(prompt);

            // 2. API URL 구성
            String url = buildApiUrl();

            // 3. HTTP 헤더 설정
            HttpHeaders headers = createHeaders();

            // 4. HTTP 엔티티 생성
            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            // 5. API 호출 (동기 방식)
            ResponseEntity<GeminiResponse> response = geminiRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    GeminiResponse.class
            );

            // 6. 응답 검증 및 텍스트 추출
            String generatedText = extractResponseText(response);

            log.info("Gemini API 요청 성공. 응답 길이: {} 자", generatedText.length());

            return generatedText;

        } catch (HttpClientErrorException e) {
            log.error("Gemini API 클라이언트 에러. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw handleClientError(e);

        } catch (HttpServerErrorException e) {
            log.error("Gemini API 서버 에러. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiPromptException(
                    AiPromptErrorCode.GEMINI_API_ERROR,
                    "Gemini API 서버 오류가 발생했습니다."
            );

        } catch (ResourceAccessException e) {
            log.error("Gemini API 연결 실패 또는 타임아웃", e);
            throw new AiPromptException(
                    AiPromptErrorCode.GEMINI_API_TIMEOUT,
                    "Gemini API 요청 시간이 초과되었습니다."
            );

        } catch (AiPromptException e) {
            throw e;

        } catch (Exception e) {
            log.error("Gemini API 호출 중 예상치 못한 에러 발생", e);
            throw new AiPromptException(
                    AiPromptErrorCode.GEMINI_API_ERROR,
                    "AI 응답 생성 중 오류가 발생했습니다: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 프롬프트 검증
     */
    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new AiPromptException(AiPromptErrorCode.REQUEST_PROMPT_REQUIRED);
        }
    }

    /**
     * API URL 구성
     */
    private String buildApiUrl() {
        return String.format("%s/v1beta/models/%s:generateContent?key=%s",
                baseUrl, model, apiKey);
    }

    /**
     * HTTP 헤더 생성
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * 응답에서 텍스트 추출 및 검증
     */
    private String extractResponseText(ResponseEntity<GeminiResponse> response) {
        // HTTP 상태 검증
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Gemini API 응답 상태가 성공이 아닙니다. Status: {}", response.getStatusCode());
            throw new AiPromptException(
                    AiPromptErrorCode.GEMINI_API_ERROR,
                    "Gemini API 호출에 실패했습니다."
            );
        }

        // Body 검증
        GeminiResponse body = response.getBody();
        if (body == null) {
            log.error("Gemini API 응답 Body가 null입니다.");
            throw new AiPromptException(
                    AiPromptErrorCode.GEMINI_API_ERROR,
                    "AI 응답이 비어있습니다."
            );
        }

        // 안전성 검사
        if (!body.isSafe()) {
            log.warn("Gemini API 응답이 안전하지 않은 콘텐츠를 포함합니다.");
            throw new AiPromptException(
                    AiPromptErrorCode.RESPONSE_GENERATION_FAILED,
                    "안전하지 않은 콘텐츠가 감지되어 응답을 생성할 수 없습니다."
            );
        }

        // 텍스트 추출
        String generatedText = body.getGeneratedText();

        if (generatedText == null || generatedText.trim().isEmpty()) {
            log.error("Gemini API 응답 텍스트가 비어있습니다.");
            throw new AiPromptException(
                    AiPromptErrorCode.RESPONSE_GENERATION_FAILED,
                    "AI 응답 생성에 실패했습니다."
            );
        }

        return generatedText;
    }

    /**
     * 클라이언트 에러 처리
     */
    private AiPromptException handleClientError(HttpClientErrorException e) {
        HttpStatus statusCode = (HttpStatus) e.getStatusCode();

        return switch (statusCode) {
            case BAD_REQUEST -> {
                log.error("잘못된 요청: {}", e.getResponseBodyAsString());
                yield new AiPromptException(
                        AiPromptErrorCode.GEMINI_API_ERROR,
                        "잘못된 요청입니다. 프롬프트를 확인해주세요."
                );
            }
            case UNAUTHORIZED, FORBIDDEN -> {
                log.error("인증 실패: API Key 확인 필요");
                yield new AiPromptException(
                        AiPromptErrorCode.GEMINI_API_ERROR,
                        "Gemini API 인증에 실패했습니다. API Key를 확인해주세요."
                );
            }
            case TOO_MANY_REQUESTS -> {
                log.warn("API 할당량 초과");
                yield new AiPromptException(
                        AiPromptErrorCode.GEMINI_API_QUOTA_EXCEEDED,
                        "Gemini API 할당량을 초과했습니다. 잠시 후 다시 시도해주세요."
                );
            }
            case NOT_FOUND -> {
                log.error("API 엔드포인트를 찾을 수 없음: {}", buildApiUrl());
                yield new AiPromptException(
                        AiPromptErrorCode.GEMINI_API_ERROR,
                        "Gemini API 엔드포인트를 찾을 수 없습니다."
                );
            }
            default -> {
                log.error("클라이언트 에러: {} - {}", statusCode, e.getResponseBodyAsString());
                yield new AiPromptException(
                        AiPromptErrorCode.GEMINI_API_ERROR,
                        "Gemini API 호출 중 오류가 발생했습니다: " + statusCode
                );
            }
        };
    }
}