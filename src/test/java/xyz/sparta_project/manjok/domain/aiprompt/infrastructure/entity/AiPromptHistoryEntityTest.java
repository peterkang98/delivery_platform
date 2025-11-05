package xyz.sparta_project.manjok.domain.aiprompt.infrastructure.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AiPromptHistoryEntityTest {

    private String ownerId;
    private String createdBy;
    private String requestPrompt;

    @BeforeEach
    void setUp() {
        ownerId = "user123";
        createdBy = "user123";
        requestPrompt = "테스트 질문";
    }

    private AiPromptHistory createDefaultDomain(PromptType promptType) {
        return AiPromptHistory.create(ownerId, promptType, requestPrompt, createdBy);
    }

    private AiPromptHistoryEntity createDefaultEntity(PromptType promptType) {
        return AiPromptHistoryEntity.builder()
                .ownerId(ownerId)
                .promptType(promptType)
                .requestPrompt(requestPrompt)
                .isDeleted(false)
                .createdBy(createdBy)
                .build();
    }

    @Test
    @DisplayName("도메인 모델을 엔티티로 변환할 수 있다")
    void fromDomain() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);

        // when
        AiPromptHistoryEntity entity = AiPromptHistoryEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getOwnerId()).isEqualTo(domain.getOwnerId());
        assertThat(entity.getPromptType()).isEqualTo(domain.getPromptType());
        assertThat(entity.getRequestPrompt()).isEqualTo(domain.getRequestPrompt());
        assertThat(entity.getIsDeleted()).isEqualTo(domain.getIsDeleted());
        assertThat(entity.getCreatedBy()).isEqualTo(domain.getCreatedBy());
    }

    @Test
    @DisplayName("응답이 있는 도메인 모델을 엔티티로 변환할 수 있다")
    void fromDomain_WithResponse() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.MENU_DESCRIPTION);
        domain.updateResponse("메뉴 설명 응답", createdBy);

        // when
        AiPromptHistoryEntity entity = AiPromptHistoryEntity.fromDomain(domain);

        // then
        assertThat(entity.getResponseContent()).isEqualTo(domain.getResponseContent());
        assertThat(entity.getUpdatedBy()).isEqualTo(domain.getUpdatedBy());
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("삭제된 도메인 모델을 엔티티로 변환할 수 있다")
    void fromDomain_Deleted() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);
        domain.delete("admin");

        // when
        AiPromptHistoryEntity entity = AiPromptHistoryEntity.fromDomain(domain);

        // then
        assertThat(entity.getIsDeleted()).isTrue();
        assertThat(entity.getDeletedBy()).isEqualTo("admin");
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("null 도메인은 null 엔티티로 변환된다")
    void fromDomain_Null() {
        // when
        AiPromptHistoryEntity entity = AiPromptHistoryEntity.fromDomain(null);

        // then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("엔티티를 도메인 모델로 변환할 수 있다")
    void toDomain() {
        // given
        AiPromptHistoryEntity entity = createDefaultEntity(PromptType.QNA);

        // when
        AiPromptHistory domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getOwnerId()).isEqualTo(entity.getOwnerId());
        assertThat(domain.getPromptType()).isEqualTo(entity.getPromptType());
        assertThat(domain.getRequestPrompt()).isEqualTo(entity.getRequestPrompt());
        assertThat(domain.getIsDeleted()).isEqualTo(entity.getIsDeleted());
        assertThat(domain.getCreatedBy()).isEqualTo(entity.getCreatedBy());
    }

    @Test
    @DisplayName("응답이 있는 엔티티를 도메인 모델로 변환할 수 있다")
    void toDomain_WithResponse() {
        // given
        LocalDateTime now = LocalDateTime.now();
        AiPromptHistoryEntity entity = AiPromptHistoryEntity.builder()
                .ownerId(ownerId)
                .promptType(PromptType.MENU_DESCRIPTION)
                .requestPrompt(requestPrompt)
                .responseContent("메뉴 설명 응답")
                .isDeleted(false)
                .createdBy(createdBy)
                .updatedAt(now)
                .updatedBy(createdBy)
                .build();

        // when
        AiPromptHistory domain = entity.toDomain();

        // then
        assertThat(domain.getResponseContent()).isEqualTo(entity.getResponseContent());
        assertThat(domain.hasResponse()).isTrue();
        assertThat(domain.getUpdatedBy()).isEqualTo(entity.getUpdatedBy());
    }

    @Test
    @DisplayName("삭제된 엔티티를 도메인 모델로 변환할 수 있다")
    void toDomain_Deleted() {
        // given
        LocalDateTime deletedAt = LocalDateTime.now();
        AiPromptHistoryEntity entity = AiPromptHistoryEntity.builder()
                .ownerId(ownerId)
                .promptType(PromptType.QNA)
                .requestPrompt(requestPrompt)
                .isDeleted(true)
                .deletedAt(deletedAt)
                .deletedBy("admin")
                .createdBy(createdBy)
                .build();

        // when
        AiPromptHistory domain = entity.toDomain();

        // then
        assertThat(domain.getIsDeleted()).isTrue();
        assertThat(domain.isActive()).isFalse();
        assertThat(domain.getDeletedBy()).isEqualTo("admin");
        assertThat(domain.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("도메인 → 엔티티 → 도메인 변환 시 데이터가 보존된다")
    void domainToEntityToDomain_DataPreserved() {
        // given
        AiPromptHistory originalDomain = createDefaultDomain(PromptType.QNA);
        originalDomain.updateResponse("테스트 응답", createdBy);

        // when
        AiPromptHistoryEntity entity = AiPromptHistoryEntity.fromDomain(originalDomain);
        AiPromptHistory convertedDomain = entity.toDomain();

        // then
        assertThat(convertedDomain.getOwnerId()).isEqualTo(originalDomain.getOwnerId());
        assertThat(convertedDomain.getPromptType()).isEqualTo(originalDomain.getPromptType());
        assertThat(convertedDomain.getRequestPrompt()).isEqualTo(originalDomain.getRequestPrompt());
        assertThat(convertedDomain.getResponseContent()).isEqualTo(originalDomain.getResponseContent());
        assertThat(convertedDomain.getIsDeleted()).isEqualTo(originalDomain.getIsDeleted());
        assertThat(convertedDomain.getCreatedBy()).isEqualTo(originalDomain.getCreatedBy());
    }

    @Test
    @DisplayName("엔티티의 응답을 업데이트할 수 있다")
    void updateResponse() {
        // given
        AiPromptHistoryEntity entity = createDefaultEntity(PromptType.QNA);
        String responseContent = "테스트 응답";
        String updatedBy = "user123";

        // when
        entity.updateResponse(responseContent, updatedBy);

        // then
        assertThat(entity.getResponseContent()).isEqualTo(responseContent);
        assertThat(entity.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("엔티티를 논리적으로 삭제할 수 있다")
    void markAsDeleted() {
        // given
        AiPromptHistoryEntity entity = createDefaultEntity(PromptType.QNA);
        String deletedBy = "admin";

        // when
        entity.markAsDeleted(deletedBy);

        // then
        assertThat(entity.getIsDeleted()).isTrue();
        assertThat(entity.getDeletedBy()).isEqualTo(deletedBy);
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("모든 PromptType이 엔티티로 변환된다")
    void fromDomain_AllPromptTypes() {
        // given & when & then
        for (PromptType type : PromptType.values()) {
            AiPromptHistory domain = createDefaultDomain(type);
            AiPromptHistoryEntity entity = AiPromptHistoryEntity.fromDomain(domain);

            assertThat(entity.getPromptType()).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("모든 PromptType이 도메인으로 변환된다")
    void toDomain_AllPromptTypes() {
        // given & when & then
        for (PromptType type : PromptType.values()) {
            AiPromptHistoryEntity entity = createDefaultEntity(type);
            AiPromptHistory domain = entity.toDomain();

            assertThat(domain.getPromptType()).isEqualTo(type);
        }
    }
}