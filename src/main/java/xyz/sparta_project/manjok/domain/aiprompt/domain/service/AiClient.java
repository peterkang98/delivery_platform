package xyz.sparta_project.manjok.domain.aiprompt.domain.service;

/**
 * AI API 클라이언트 도메인 인터페이스
 * - 인프라 계층의 구현체와 도메인을 분리
 */
public interface AiClient {

    /**
     * AI API로 요청 전송 및 응답 수신
     * @param prompt 완성된 프롬프트
     * @return AI 응답 텍스트
     */
    String sendRequest(String prompt);
}