package xyz.sparta_project.manjok.domain.aiprompt.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AI 프롬프트 타입
 */
@Getter
@RequiredArgsConstructor
public enum PromptType {
    QNA("질의응답", "일반적인 질의응답 프롬프트"),
    MENU_DESCRIPTION("메뉴설명", "음식 메뉴에 대한 설명 생성 프롬프트");

    private final String displayName;
    private final String description;

    /**
     * 문자열로부터 PromptType 찾기
     */
    public static PromptType fromString(String value) {
        for (PromptType type : PromptType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid PromptType: " + value);
    }

    /**
     * 메뉴 관련 타입인지 확인
     */
    public boolean isMenuRelated() {
        return this == MENU_DESCRIPTION;
    }
}