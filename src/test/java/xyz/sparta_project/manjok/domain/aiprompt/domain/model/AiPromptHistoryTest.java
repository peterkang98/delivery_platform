package xyz.sparta_project.manjok.domain.aiprompt.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptException;

import static org.assertj.core.api.Assertions.*;

class AiPromptHistoryTest {

    @Test
    @DisplayName("유효한 정보로 히스토리를 생성할 수 있다")
    void createHistory() {
        // given
        String ownerId = "user123";
        PromptType promptType = PromptType.QNA;
        String requestPrompt = "테스트 프롬프트";
        String createdBy = "user123";

        // when
        AiPromptHistory history = AiPromptHistory.create(ownerId, promptType, requestPrompt, createdBy);

        // then
        assertThat(history).isNotNull();
        assertThat(history.getOwnerId()).isEqualTo(ownerId);
        assertThat(history.getPromptType()).isEqualTo(promptType);
        assertThat(history.getRequestPrompt()).isEqualTo(requestPrompt);
        assertThat(history.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("ownerId가 null이면 예외가 발생한다")
    void createHistory_OwnerIdNull() {
        // given
        String ownerId = null;
        PromptType promptType = PromptType.QNA;
        String requestPrompt = "테스트 프롬프트";
        String createdBy = "user123";

        // when & then
        assertThatThrownBy(() -> AiPromptHistory.create(ownerId, promptType, requestPrompt, createdBy))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("promptType이 null이면 예외가 발생한다")
    void createHistory_PromptTypeNull() {
        // given
        String ownerId = "user123";
        PromptType promptType = null;
        String requestPrompt = "테스트 프롬프트";
        String createdBy = "user123";

        // when & then
        assertThatThrownBy(() -> AiPromptHistory.create(ownerId, promptType, requestPrompt, createdBy))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("requestPrompt가 null이면 예외가 발생한다")
    void createHistory_RequestPromptNull() {
        // given
        String ownerId = "user123";
        PromptType promptType = PromptType.QNA;
        String requestPrompt = null;
        String createdBy = "user123";

        // when & then
        assertThatThrownBy(() -> AiPromptHistory.create(ownerId, promptType, requestPrompt, createdBy))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("응답을 업데이트할 수 있다")
    void updateResponse() {
        // given
        AiPromptHistory history = AiPromptHistory.create("user123", PromptType.QNA, "질문", "user123");
        String responseContent = "응답 내용";
        String updatedBy = "user123";

        // when
        history.updateResponse(responseContent, updatedBy);

        // then
        assertThat(history.getResponseContent()).isEqualTo(responseContent);
        assertThat(history.hasResponse()).isTrue();
    }

    @Test
    @DisplayName("삭제된 히스토리는 응답을 업데이트할 수 없다")
    void updateResponse_DeletedHistory() {
        // given
        AiPromptHistory history = AiPromptHistory.create("user123", PromptType.QNA, "질문", "user123");
        history.delete("user123");

        // when & then
        assertThatThrownBy(() -> history.updateResponse("응답", "user123"))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("히스토리를 삭제할 수 있다")
    void delete() {
        // given
        AiPromptHistory history = AiPromptHistory.create("user123", PromptType.QNA, "질문", "user123");

        // when
        history.delete("user123");

        // then
        assertThat(history.getIsDeleted()).isTrue();
        assertThat(history.isActive()).isFalse();
    }

    @Test
    @DisplayName("이미 삭제된 히스토리는 다시 삭제할 수 없다")
    void delete_AlreadyDeleted() {
        // given
        AiPromptHistory history = AiPromptHistory.create("user123", PromptType.QNA, "질문", "user123");
        history.delete("user123");

        // when & then
        assertThatThrownBy(() -> history.delete("user123"))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("삭제된 히스토리를 복구할 수 있다")
    void restore() {
        // given
        AiPromptHistory history = AiPromptHistory.create("user123", PromptType.QNA, "질문", "user123");
        history.delete("user123");

        // when
        history.restore("user123");

        // then
        assertThat(history.getIsDeleted()).isFalse();
        assertThat(history.isActive()).isTrue();
    }

    @Test
    @DisplayName("삭제되지 않은 히스토리는 복구할 수 없다")
    void restore_NotDeleted() {
        // given
        AiPromptHistory history = AiPromptHistory.create("user123", PromptType.QNA, "질문", "user123");

        // when & then
        assertThatThrownBy(() -> history.restore("user123"))
                .isInstanceOf(AiPromptException.class);
    }
}