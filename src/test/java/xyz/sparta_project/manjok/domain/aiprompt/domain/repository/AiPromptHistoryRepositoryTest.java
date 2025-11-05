package xyz.sparta_project.manjok.domain.aiprompt.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import xyz.sparta_project.manjok.domain.aiprompt.domain.exception.AiPromptException;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.AiPromptHistory;
import xyz.sparta_project.manjok.domain.aiprompt.domain.model.PromptType;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.jpa.AiPromptHistoryJpaRepository;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.repository.AiPromptHistoryRepositoryImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(AiPromptHistoryRepositoryImpl.class)
@ActiveProfiles("test")
class AiPromptHistoryRepositoryTest {

    @Autowired
    private AiPromptHistoryRepository repository;

    @Autowired
    private AiPromptHistoryJpaRepository jpaRepository;

    private String ownerId;
    private String createdBy;
    private String requestPrompt;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
        ownerId = "user123";
        createdBy = "user123";
        requestPrompt = "테스트 질문";
    }

    private AiPromptHistory createDefaultDomain(PromptType promptType) {
        return AiPromptHistory.create(ownerId, promptType, requestPrompt, createdBy);
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("히스토리를 저장할 수 있다")
    void save() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);

        // when
        AiPromptHistory saved = repository.save(domain);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOwnerId()).isEqualTo(ownerId);
        assertThat(saved.getPromptType()).isEqualTo(PromptType.QNA);
        assertThat(saved.getRequestPrompt()).isEqualTo(requestPrompt);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("여러 타입의 히스토리를 저장할 수 있다")
    void save_MultipleTypes() {
        // given
        AiPromptHistory qna = createDefaultDomain(PromptType.QNA);
        AiPromptHistory menu = createDefaultDomain(PromptType.MENU_DESCRIPTION);

        // when
        AiPromptHistory savedQna = repository.save(qna);
        AiPromptHistory savedMenu = repository.save(menu);

        // then
        assertThat(savedQna.getPromptType()).isEqualTo(PromptType.QNA);
        assertThat(savedMenu.getPromptType()).isEqualTo(PromptType.MENU_DESCRIPTION);
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("응답을 업데이트할 수 있다")
    void updateResponse() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved = repository.save(domain);
        String responseContent = "응답 내용";

        // when
        repository.updateResponse(saved.getId(), responseContent, createdBy);

        // then
        Optional<AiPromptHistory> updated = repository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getResponseContent()).isEqualTo(responseContent);
        assertThat(updated.get().getUpdatedBy()).isEqualTo(createdBy);
        assertThat(updated.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 히스토리의 응답을 업데이트하면 예외가 발생한다")
    void updateResponse_NotFound() {
        // given
        String nonExistingId = "non-existing-id";

        // when & then
        assertThatThrownBy(() -> repository.updateResponse(nonExistingId, "응답", createdBy))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("삭제된 히스토리의 응답을 업데이트하면 예외가 발생한다")
    void updateResponse_Deleted() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved = repository.save(domain);
        repository.delete(saved.getId(), "admin");

        // when & then
        assertThatThrownBy(() -> repository.updateResponse(saved.getId(), "응답", createdBy))
                .isInstanceOf(AiPromptException.class);
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("히스토리를 소프트 삭제할 수 있다")
    void delete() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved = repository.save(domain);
        String deletedBy = "admin";

        // when
        repository.delete(saved.getId(), deletedBy);

        // then
        Optional<AiPromptHistory> deleted = repository.findById(saved.getId());
        assertThat(deleted).isEmpty();

        Optional<AiPromptHistory> deletedIncluding = repository.findByIdIncludingDeleted(saved.getId());
        assertThat(deletedIncluding).isPresent();
        assertThat(deletedIncluding.get().getIsDeleted()).isTrue();
        assertThat(deletedIncluding.get().getDeletedBy()).isEqualTo(deletedBy);
        assertThat(deletedIncluding.get().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 히스토리를 삭제하면 예외가 발생한다")
    void delete_NotFound() {
        // given
        String nonExistingId = "non-existing-id";

        // when & then
        assertThatThrownBy(() -> repository.delete(nonExistingId, "admin"))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("이미 삭제된 히스토리를 다시 삭제하면 예외가 발생한다")
    void delete_AlreadyDeleted() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved = repository.save(domain);
        repository.delete(saved.getId(), "admin");

        // when & then
        assertThatThrownBy(() -> repository.delete(saved.getId(), "admin2"))
                .isInstanceOf(AiPromptException.class);
    }

    // ==================== READ - 단건 조회 ====================

    @Test
    @DisplayName("ID로 히스토리를 조회할 수 있다")
    void findById() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved = repository.save(domain);

        // when
        Optional<AiPromptHistory> found = repository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
    void findById_NotFound() {
        // given
        String nonExistingId = "non-existing-id";

        // when
        Optional<AiPromptHistory> found = repository.findById(nonExistingId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("삭제된 히스토리는 일반 조회에서 제외된다")
    void findById_ExcludeDeleted() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved = repository.save(domain);
        repository.delete(saved.getId(), "admin");

        // when
        Optional<AiPromptHistory> found = repository.findById(saved.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("삭제된 것을 포함하여 ID로 히스토리를 조회할 수 있다")
    void findByIdIncludingDeleted() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved = repository.save(domain);
        repository.delete(saved.getId(), "admin");

        // when
        Optional<AiPromptHistory> found = repository.findByIdIncludingDeleted(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getIsDeleted()).isTrue();
    }

    // ==================== READ - 목록 조회 ====================

    @Test
    @DisplayName("소유자 ID로 히스토리 목록을 조회할 수 있다")
    void findByOwnerId() {
        // given
        repository.save(createDefaultDomain(PromptType.QNA));
        repository.save(createDefaultDomain(PromptType.MENU_DESCRIPTION));
        repository.save(AiPromptHistory.create("other-user", PromptType.QNA, "다른 질문", "other-user"));

        // when
        List<AiPromptHistory> result = repository.findByOwnerId(ownerId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(h -> h.getOwnerId().equals(ownerId));
    }

    @Test
    @DisplayName("삭제된 히스토리는 목록 조회에서 제외된다")
    void findByOwnerId_ExcludeDeleted() {
        // given
        AiPromptHistory domain1 = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved1 = repository.save(domain1);
        repository.save(createDefaultDomain(PromptType.MENU_DESCRIPTION));
        repository.delete(saved1.getId(), "admin");

        // when
        List<AiPromptHistory> result = repository.findByOwnerId(ownerId);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("삭제된 것을 포함하여 소유자 ID로 히스토리 목록을 조회할 수 있다")
    void findByOwnerIdIncludingDeleted() {
        // given
        AiPromptHistory domain1 = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved1 = repository.save(domain1);
        repository.save(createDefaultDomain(PromptType.MENU_DESCRIPTION));
        repository.delete(saved1.getId(), "admin");

        // when
        List<AiPromptHistory> result = repository.findByOwnerIdIncludingDeleted(ownerId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.stream().filter(AiPromptHistory::getIsDeleted).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("소유자 ID와 프롬프트 타입으로 히스토리 목록을 조회할 수 있다")
    void findByOwnerIdAndPromptType() {
        // given
        repository.save(createDefaultDomain(PromptType.QNA));
        repository.save(createDefaultDomain(PromptType.QNA));
        repository.save(createDefaultDomain(PromptType.MENU_DESCRIPTION));

        // when
        List<AiPromptHistory> result = repository.findByOwnerIdAndPromptType(ownerId, PromptType.QNA);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(h -> h.getPromptType() == PromptType.QNA);
    }

    @Test
    @DisplayName("삭제된 것을 포함하여 소유자 ID와 프롬프트 타입으로 히스토리 목록을 조회할 수 있다")
    void findByOwnerIdAndPromptTypeIncludingDeleted() {
        // given
        AiPromptHistory domain1 = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved1 = repository.save(domain1);
        repository.save(createDefaultDomain(PromptType.QNA));
        repository.delete(saved1.getId(), "admin");

        // when
        List<AiPromptHistory> result = repository.findByOwnerIdAndPromptTypeIncludingDeleted(
                ownerId, PromptType.QNA);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.stream().filter(AiPromptHistory::getIsDeleted).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("프롬프트 타입으로 히스토리 목록을 조회할 수 있다")
    void findByPromptType() {
        // given
        repository.save(createDefaultDomain(PromptType.QNA));
        repository.save(AiPromptHistory.create("other-user", PromptType.QNA, "다른 질문", "other-user"));
        repository.save(createDefaultDomain(PromptType.MENU_DESCRIPTION));

        // when
        List<AiPromptHistory> result = repository.findByPromptType(PromptType.QNA);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(h -> h.getPromptType() == PromptType.QNA);
    }

    // ==================== 존재 확인 ====================

    @Test
    @DisplayName("ID로 히스토리 존재 여부를 확인할 수 있다")
    void existsById() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved = repository.save(domain);

        // when
        boolean exists = repository.existsById(saved.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 ID는 false를 반환한다")
    void existsById_NotFound() {
        // given
        String nonExistingId = "non-existing-id";

        // when
        boolean exists = repository.existsById(nonExistingId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("삭제된 히스토리는 존재하지 않는 것으로 판단된다")
    void existsById_Deleted() {
        // given
        AiPromptHistory domain = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved = repository.save(domain);
        repository.delete(saved.getId(), "admin");

        // when
        boolean exists = repository.existsById(saved.getId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("소유자 ID와 프롬프트 타입으로 히스토리 존재 여부를 확인할 수 있다")
    void existsByOwnerIdAndPromptType() {
        // given
        repository.save(createDefaultDomain(PromptType.QNA));

        // when
        boolean exists = repository.existsByOwnerIdAndPromptType(ownerId, PromptType.QNA);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 조건은 false를 반환한다")
    void existsByOwnerIdAndPromptType_NotFound() {
        // when
        boolean exists = repository.existsByOwnerIdAndPromptType(ownerId, PromptType.QNA);

        // then
        assertThat(exists).isFalse();
    }

    // ==================== 통계 ====================

    @Test
    @DisplayName("소유자의 총 히스토리 개수를 조회할 수 있다")
    void countByOwnerId() {
        // given
        repository.save(createDefaultDomain(PromptType.QNA));
        repository.save(createDefaultDomain(PromptType.MENU_DESCRIPTION));
        repository.save(AiPromptHistory.create("other-user", PromptType.QNA, "다른 질문", "other-user"));

        // when
        long count = repository.countByOwnerId(ownerId);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("삭제된 히스토리는 개수에 포함되지 않는다")
    void countByOwnerId_ExcludeDeleted() {
        // given
        AiPromptHistory domain1 = createDefaultDomain(PromptType.QNA);
        AiPromptHistory saved1 = repository.save(domain1);
        repository.save(createDefaultDomain(PromptType.MENU_DESCRIPTION));
        repository.delete(saved1.getId(), "admin");

        // when
        long count = repository.countByOwnerId(ownerId);

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("소유자의 프롬프트 타입별 히스토리 개수를 조회할 수 있다")
    void countByOwnerIdAndPromptType() {
        // given
        repository.save(createDefaultDomain(PromptType.QNA));
        repository.save(createDefaultDomain(PromptType.QNA));
        repository.save(createDefaultDomain(PromptType.MENU_DESCRIPTION));

        // when
        long count = repository.countByOwnerIdAndPromptType(ownerId, PromptType.QNA);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("히스토리가 없으면 0을 반환한다")
    void countByOwnerId_Empty() {
        // when
        long count = repository.countByOwnerId(ownerId);

        // then
        assertThat(count).isEqualTo(0);
    }
}