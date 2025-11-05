package xyz.sparta_project.manjok.domain.aiprompt.domain.service;

import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;

/**
 * 프롬프트 생성 도메인 서비스
 * - 각 타입별 프롬프트 생성 규칙 정의
 */
public interface PromptService {

    /**
     * 프롬프트 생성
     * @param promptType 프롬프트 타입
     * @param input 사용자 입력
     * @return 완성된 프롬프트
     */
    String createPrompt(PromptType promptType, String input);

    /**
     * 메뉴 설명 프롬프트 생성
     * @param menuInfo 메뉴 정보
     * @return 메뉴 설명 프롬프트
     */
    String createMenuDescriptionPrompt(String menuInfo);

    /**
     * QnA 프롬프트 생성
     * @param question 질문
     * @return QnA 프롬프트
     */
    String createQnaPrompt(String question);
}