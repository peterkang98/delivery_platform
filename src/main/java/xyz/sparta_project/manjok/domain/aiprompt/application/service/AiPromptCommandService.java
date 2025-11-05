// application/service/AiPromptCommandService.java
package xyz.sparta_project.manjok.domain.aiprompt.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptErrorCode;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptException;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.domain.repository.AiPromptHistoryRepository;
import xyz.sparta_project.manjok.domain.aiprompt.domain.service.AiClient;
import xyz.sparta_project.manjok.domain.aiprompt.domain.service.PromptService;

/**
 * AI 프롬프트 Command Service
 * - 생성, 수정, 삭제 등 쓰기 작업 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiPromptCommandService {

    private final AiPromptHistoryRepository aiPromptHistoryRepository;
    private final PromptService promptService;
    private final AiClient aiClient;

    /**
     * AI 프롬프트 요청 및 히스토리 저장
     *
     * @param ownerId 소유자 ID
     * @param promptType 프롬프트 타입
     * @param userInput 사용자 입력
     * @param createdBy 생성자
     * @return 저장된 히스토리 (응답 포함)
     */
    public AiPromptHistory generateAndSave(
            String ownerId,
            PromptType promptType,
            String userInput,
            String createdBy) {

        try {
            log.info("AI 프롬프트 생성 요청. OwnerId: {}, Type: {}", ownerId, promptType);

            // 1. 프롬프트 생성
            String prompt = promptService.createPrompt(promptType, userInput);

            // 2. 도메인 모델 생성 (응답 없는 상태로 저장)
            AiPromptHistory history = AiPromptHistory.create(
                    ownerId,
                    promptType,
                    userInput,  // 원본 입력 저장
                    createdBy
            );

            // 3. 히스토리 저장 (응답 받기 전)
            AiPromptHistory savedHistory = aiPromptHistoryRepository.save(history);

            log.info("AI 프롬프트 히스토리 생성 완료. ID: {}", savedHistory.getId());

            // 4. AI API 호출 (동기)
            String aiResponse = aiClient.sendRequest(prompt);

            log.info("AI 응답 수신 완료. HistoryId: {}, 응답 길이: {}",
                    savedHistory.getId(), aiResponse.length());

            // 5. 응답 업데이트 (더티체킹)
            aiPromptHistoryRepository.updateResponse(
                    savedHistory.getId(),
                    aiResponse,
                    createdBy
            );

            // 6. 업데이트된 히스토리 조회 및 반환
            return aiPromptHistoryRepository.findById(savedHistory.getId())
                    .orElseThrow(() -> new AiPromptException(AiPromptErrorCode.HISTORY_NOT_FOUND));

        } catch (AiPromptException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 프롬프트 생성 실패. OwnerId: {}, Type: {}", ownerId, promptType, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "AI 프롬프트 처리 중 오류가 발생했습니다: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 메뉴 설명 생성 전용 메서드
     *
     * @param ownerId 소유자 ID
     * @param menuInfo 메뉴 정보
     * @param createdBy 생성자
     * @return 저장된 히스토리 (메뉴 설명 포함)
     */
    public AiPromptHistory generateMenuDescription(
            String ownerId,
            String menuInfo,
            String createdBy) {

        log.info("메뉴 설명 생성 요청. OwnerId: {}", ownerId);

        return generateAndSave(
                ownerId,
                PromptType.MENU_DESCRIPTION,
                menuInfo,
                createdBy
        );
    }

    /**
     * QnA 응답 생성 전용 메서드
     *
     * @param ownerId 소유자 ID
     * @param question 질문
     * @param createdBy 생성자
     * @return 저장된 히스토리 (답변 포함)
     */
    public AiPromptHistory generateQnaResponse(
            String ownerId,
            String question,
            String createdBy) {

        log.info("QnA 응답 생성 요청. OwnerId: {}", ownerId);

        return generateAndSave(
                ownerId,
                PromptType.QNA,
                question,
                createdBy
        );
    }

    /**
     * 히스토리 소프트 삭제
     *
     * @param id 히스토리 ID
     * @param ownerId 소유자 ID (권한 확인용)
     * @param deletedBy 삭제자
     */
    public void delete(String id, String ownerId, String deletedBy) {
        try {
            log.info("AI 프롬프트 히스토리 삭제 요청. ID: {}, OwnerId: {}", id, ownerId);

            // 1. 히스토리 조회
            AiPromptHistory history = aiPromptHistoryRepository.findById(id)
                    .orElseThrow(() -> new AiPromptException(AiPromptErrorCode.HISTORY_NOT_FOUND));

            // 2. 소유자 확인
            if (!history.getOwnerId().equals(ownerId)) {
                throw new AiPromptException(AiPromptErrorCode.HISTORY_NOT_BELONG_TO_OWNER);
            }

            // 3. 삭제 처리 (더티체킹)
            aiPromptHistoryRepository.delete(id, deletedBy);

            log.info("AI 프롬프트 히스토리 삭제 완료. ID: {}", id);

        } catch (AiPromptException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 프롬프트 히스토리 삭제 실패. ID: {}", id, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "히스토리 삭제 중 오류가 발생했습니다",
                    e
            );
        }
    }

    /**
     * 응답 재생성
     * - 기존 히스토리의 요청 프롬프트로 다시 AI 응답 생성
     *
     * @param id 히스토리 ID
     * @param ownerId 소유자 ID (권한 확인용)
     * @param updatedBy 수정자
     * @return 업데이트된 히스토리
     */
    public AiPromptHistory regenerateResponse(String id, String ownerId, String updatedBy) {
        try {
            log.info("AI 응답 재생성 요청. ID: {}, OwnerId: {}", id, ownerId);

            // 1. 히스토리 조회
            AiPromptHistory history = aiPromptHistoryRepository.findById(id)
                    .orElseThrow(() -> new AiPromptException(AiPromptErrorCode.HISTORY_NOT_FOUND));

            // 2. 소유자 확인
            if (!history.getOwnerId().equals(ownerId)) {
                throw new AiPromptException(AiPromptErrorCode.HISTORY_NOT_BELONG_TO_OWNER);
            }

            // 3. 프롬프트 재생성
            String prompt = promptService.createPrompt(
                    history.getPromptType(),
                    history.getRequestPrompt()
            );

            // 4. AI API 호출
            String aiResponse = aiClient.sendRequest(prompt);

            log.info("AI 응답 재생성 완료. HistoryId: {}, 응답 길이: {}",
                    id, aiResponse.length());

            // 5. 응답 업데이트 (더티체킹)
            aiPromptHistoryRepository.updateResponse(id, aiResponse, updatedBy);

            // 6. 업데이트된 히스토리 조회 및 반환
            return aiPromptHistoryRepository.findById(id)
                    .orElseThrow(() -> new AiPromptException(AiPromptErrorCode.HISTORY_NOT_FOUND));

        } catch (AiPromptException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 응답 재생성 실패. ID: {}", id, e);
            throw new AiPromptException(
                    AiPromptErrorCode.EVENT_PROCESSING_FAILED,
                    "응답 재생성 중 오류가 발생했습니다",
                    e
            );
        }
    }
}