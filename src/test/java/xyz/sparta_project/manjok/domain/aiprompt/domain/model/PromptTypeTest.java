package xyz.sparta_project.manjok.domain.aiprompt.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PromptTypeTest {

    @Test
    @DisplayName("대소문자 구분 없이 문자열로 PromptType을 찾을 수 있다 - QNA")
    void fromString_QNA() {
        // when & then
        assertThat(PromptType.fromString("QNA")).isEqualTo(PromptType.QNA);
        assertThat(PromptType.fromString("qna")).isEqualTo(PromptType.QNA);
        assertThat(PromptType.fromString("Qna")).isEqualTo(PromptType.QNA);
    }

    @Test
    @DisplayName("대소문자 구분 없이 문자열로 PromptType을 찾을 수 있다 - MENU_DESCRIPTION")
    void fromString_MenuDescription() {
        // when & then
        assertThat(PromptType.fromString("MENU_DESCRIPTION")).isEqualTo(PromptType.MENU_DESCRIPTION);
        assertThat(PromptType.fromString("menu_description")).isEqualTo(PromptType.MENU_DESCRIPTION);
        assertThat(PromptType.fromString("Menu_Description")).isEqualTo(PromptType.MENU_DESCRIPTION);
    }

    @Test
    @DisplayName("유효하지 않은 문자열은 예외를 발생시킨다")
    void fromString_Invalid() {
        // when & then
        assertThatThrownBy(() -> PromptType.fromString("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid PromptType");
    }

    @Test
    @DisplayName("null 값은 예외를 발생시킨다")
    void fromString_Null() {
        // when & then
        assertThatThrownBy(() -> PromptType.fromString(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("MENU_DESCRIPTION은 메뉴 관련 타입이다")
    void isMenuRelated_MenuDescription() {
        // when & then
        assertThat(PromptType.MENU_DESCRIPTION.isMenuRelated()).isTrue();
    }

    @Test
    @DisplayName("QNA는 메뉴 관련 타입이 아니다")
    void isMenuRelated_QNA() {
        // when & then
        assertThat(PromptType.QNA.isMenuRelated()).isFalse();
    }
}