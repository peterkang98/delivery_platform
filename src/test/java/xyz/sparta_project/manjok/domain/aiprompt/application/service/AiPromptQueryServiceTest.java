package xyz.sparta_project.manjok.domain.aiprompt.application.service;

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
import xyz.sparta_project.manjok.domain.aiprompt.domain.repository.AiPromptHistoryRepository;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.jpa.AiPromptHistoryJpaRepository;
import xyz.sparta_project.manjok.domain.aiprompt.infrastructure.repository.AiPromptHistoryRepositoryImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({AiPromptHistoryRepositoryImpl.class, AiPromptQueryService.class})
@ActiveProfiles("test")
class AiPromptQueryServiceTest {

    @Autowired
    private AiPromptQueryService queryService;

    @Autowired
    private AiPromptHistoryRepository repository;

    @Autowired
    private AiPromptHistoryJpaRepository jpaRepository;

    private String ownerId;
    private String otherOwnerId;
    private AiPromptHistory savedQna;
    private AiPromptHistory savedMenu;
    private AiPromptHistory otherUserHistory;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();

        ownerId = "user123";
        otherOwnerId = "other-user";

        // 테스트 데이터 준비
        savedQna = repository.save(
                AiPromptHistory.create(ownerId, PromptType.QNA, "배달 시간은?", ownerId)
        );

        savedMenu = repository.save(
                AiPromptHistory.create(ownerId, PromptType.MENU_DESCRIPTION, "불고기 버거", ownerId)
        );

        otherUserHistory = repository.save(
                AiPromptHistory.create(otherOwnerId, PromptType.QNA, "다른 사용자 질문", otherOwnerId)
        );
    }

    // ==================== 단건 조회 ====================

    @Test
    @DisplayName("ID로 히스토리를 조회할 수 있다")
    void getById() {
        // when
        AiPromptHistory found = queryService.getById(savedQna.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(savedQna.getId());
        assertThat(found.getOwnerId()).isEqualTo(ownerId);
        assertThat(found.getPromptType()).isEqualTo(PromptType.QNA);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
    void getById_NotFound() {
        // given
        String nonExistingId = "non-existing-id";

        // when & then
        assertThatThrownBy(() -> queryService.getById(nonExistingId))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("소유자 확인과 함께 히스토리를 조회할 수 있다")
    void getByIdWithOwnerCheck() {
        // when
        AiPromptHistory found = queryService.getByIdWithOwnerCheck(savedQna.getId(), ownerId);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(savedQna.getId());
        assertThat(found.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("다른 소유자의 히스토리를 조회하면 예외가 발생한다")
    void getByIdWithOwnerCheck_DifferentOwner() {
        // when & then
        assertThatThrownBy(() -> queryService.getByIdWithOwnerCheck(savedQna.getId(), otherOwnerId))
                .isInstanceOf(AiPromptException.class);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 소유자 확인 조회하면 예외가 발생한다")
    void getByIdWithOwnerCheck_NotFound() {
        // given
        String nonExistingId = "non-existing-id";

        // when & then
        assertThatThrownBy(() -> queryService.getByIdWithOwnerCheck(nonExistingId, ownerId))
                .isInstanceOf(AiPromptException.class);
    }

    // ==================== 목록 조회 ====================

    @Test
    @DisplayName("소유자 ID로 히스토리 목록을 조회할 수 있다")
    void getByOwnerId() {
        // when
        List<AiPromptHistory> result = queryService.getByOwnerId(ownerId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AiPromptHistory::getOwnerId)
                .containsOnly(ownerId);
        assertThat(result).extracting(AiPromptHistory::getPromptType)
                .containsExactlyInAnyOrder(PromptType.QNA, PromptType.MENU_DESCRIPTION);
    }

    @Test
    @DisplayName("히스토리가 없는 소유자는 빈 목록을 반환한다")
    void getByOwnerId_Empty() {
        // given
        String newOwnerId = "new-user";

        // when
        List<AiPromptHistory> result = queryService.getByOwnerId(newOwnerId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("소유자 ID와 프롬프트 타입으로 히스토리 목록을 조회할 수 있다")
    void getByOwnerIdAndPromptType() {
        // when
        List<AiPromptHistory> result = queryService.getByOwnerIdAndPromptType(ownerId, PromptType.QNA);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPromptType()).isEqualTo(PromptType.QNA);
        assertThat(result.get(0).getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("소유자의 메뉴 설명 히스토리 목록을 조회할 수 있다")
    void getMenuDescriptionsByOwnerId() {
        // when
        List<AiPromptHistory> result = queryService.getMenuDescriptionsByOwnerId(ownerId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPromptType()).isEqualTo(PromptType.MENU_DESCRIPTION);
        assertThat(result.get(0).getId()).isEqualTo(savedMenu.getId());
    }

    @Test
    @DisplayName("소유자의 QnA 히스토리 목록을 조회할 수 있다")
    void getQnasByOwnerId() {
        // when
        List<AiPromptHistory> result = queryService.getQnasByOwnerId(ownerId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPromptType()).isEqualTo(PromptType.QNA);
        assertThat(result.get(0).getId()).isEqualTo(savedQna.getId());
    }

    @Test
    @DisplayName("프롬프트 타입별 히스토리 목록을 조회할 수 있다")
    void getByPromptType() {
        // when
        List<AiPromptHistory> qnaResults = queryService.getByPromptType(PromptType.QNA);
        List<AiPromptHistory> menuResults = queryService.getByPromptType(PromptType.MENU_DESCRIPTION);

        // then
        assertThat(qnaResults).hasSize(2); // user123, other-user
        assertThat(qnaResults).allMatch(h -> h.getPromptType() == PromptType.QNA);

        assertThat(menuResults).hasSize(1);
        assertThat(menuResults).allMatch(h -> h.getPromptType() == PromptType.MENU_DESCRIPTION);
    }

    // ==================== 존재 확인 ====================

    @Test
    @DisplayName("히스토리 존재 여부를 확인할 수 있다")
    void existsById() {
        // when
        boolean exists = queryService.existsById(savedQna.getId());
        boolean notExists = queryService.existsById("non-existing-id");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    // ==================== 통계 ====================

    @Test
    @DisplayName("소유자의 총 히스토리 개수를 조회할 수 있다")
    void countByOwnerId() {
        // when
        long count = queryService.countByOwnerId(ownerId);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("히스토리가 없는 소유자는 0을 반환한다")
    void countByOwnerId_Zero() {
        // given
        String newOwnerId = "new-user";

        // when
        long count = queryService.countByOwnerId(newOwnerId);

        // then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("소유자의 프롬프트 타입별 히스토리 개수를 조회할 수 있다")
    void countByOwnerIdAndPromptType() {
        // when
        long qnaCount = queryService.countByOwnerIdAndPromptType(ownerId, PromptType.QNA);
        long menuCount = queryService.countByOwnerIdAndPromptType(ownerId, PromptType.MENU_DESCRIPTION);

        // then
        assertThat(qnaCount).isEqualTo(1);
        assertThat(menuCount).isEqualTo(1);
    }

    @Test
    @DisplayName("소유자의 메뉴 설명 히스토리 개수를 조회할 수 있다")
    void countMenuDescriptionsByOwnerId() {
        // when
        long count = queryService.countMenuDescriptionsByOwnerId(ownerId);

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("소유자의 QnA 히스토리 개수를 조회할 수 있다")
    void countQnasByOwnerId() {
        // when
        long count = queryService.countQnasByOwnerId(ownerId);

        // then
        assertThat(count).isEqualTo(1);
    }

    // ==================== 복합 시나리오 ====================

    @Test
    @DisplayName("여러 타입의 히스토리가 혼합되어 있어도 타입별로 올바르게 조회된다")
    void getByType_MixedHistories() {
        // given - 추가 데이터 삽입
        repository.save(AiPromptHistory.create(ownerId, PromptType.QNA, "추가 질문 1", ownerId));
        repository.save(AiPromptHistory.create(ownerId, PromptType.QNA, "추가 질문 2", ownerId));
        repository.save(AiPromptHistory.create(ownerId, PromptType.MENU_DESCRIPTION, "추가 메뉴", ownerId));

        // when
        List<AiPromptHistory> qnaList = queryService.getQnasByOwnerId(ownerId);
        List<AiPromptHistory> menuList = queryService.getMenuDescriptionsByOwnerId(ownerId);

        // then
        assertThat(qnaList).hasSize(3);
        assertThat(menuList).hasSize(2);
    }

    @Test
    @DisplayName("삭제된 히스토리는 조회되지 않는다")
    void query_ExcludeDeleted() {
        // given - 히스토리 삭제
        repository.delete(savedQna.getId(), "admin");

        // when
        List<AiPromptHistory> result = queryService.getByOwnerId(ownerId);
        long count = queryService.countByOwnerId(ownerId);

        // then
        assertThat(result).hasSize(1); // savedMenu만 조회됨
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 소유자의 히스토리가 섞여있어도 소유자별로 올바르게 조회된다")
    void getByOwnerId_MultipleOwners() {
        // given - 추가 데이터
        String thirdOwnerId = "third-user";
        repository.save(AiPromptHistory.create(thirdOwnerId, PromptType.QNA, "세번째 사용자", thirdOwnerId));

        // when
        List<AiPromptHistory> owner1Result = queryService.getByOwnerId(ownerId);
        List<AiPromptHistory> owner2Result = queryService.getByOwnerId(otherOwnerId);
        List<AiPromptHistory> owner3Result = queryService.getByOwnerId(thirdOwnerId);

        // then
        assertThat(owner1Result).hasSize(2);
        assertThat(owner2Result).hasSize(1);
        assertThat(owner3Result).hasSize(1);
    }

    @Test
    @DisplayName("응답이 업데이트된 히스토리도 정상적으로 조회된다")
    void query_WithUpdatedResponse() {
        // given - 응답 업데이트
        repository.updateResponse(savedQna.getId(), "배달 시간은 30분입니다.", ownerId);

        // when
        AiPromptHistory found = queryService.getById(savedQna.getId());

        // then
        assertThat(found.getResponseContent()).isNotNull();
        assertThat(found.getResponseContent()).contains("30분");
        assertThat(found.hasResponse()).isTrue();
    }

    @Test
    @DisplayName("통계 조회 시 타입별로 정확한 개수가 조회된다")
    void count_ByType() {
        // given - 다양한 타입의 히스토리 추가
        repository.save(AiPromptHistory.create(ownerId, PromptType.QNA, "질문1", ownerId));
        repository.save(AiPromptHistory.create(ownerId, PromptType.QNA, "질문2", ownerId));
        repository.save(AiPromptHistory.create(ownerId, PromptType.MENU_DESCRIPTION, "메뉴1", ownerId));

        // when
        long totalCount = queryService.countByOwnerId(ownerId);
        long qnaCount = queryService.countQnasByOwnerId(ownerId);
        long menuCount = queryService.countMenuDescriptionsByOwnerId(ownerId);

        // then
        assertThat(totalCount).isEqualTo(5); // 기존 2개 + 추가 3개
        assertThat(qnaCount).isEqualTo(3);   // 기존 1개 + 추가 2개
        assertThat(menuCount).isEqualTo(2);  // 기존 1개 + 추가 1개
    }
}