package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MenuOptionGroup 도메인 모델 테스트")
class MenuOptionGroupTest {

    private MenuOptionGroup optionGroup;
    private final String menuId = "MENU001";
    private final String restaurantId = "REST001";
    private final String userId = "USER001";

    @BeforeEach
    void setUp() {
        optionGroup = MenuOptionGroup.builder()
                .id("GROUP001")
                .createdAt(LocalDateTime.now())
                .menuId(menuId)
                .restaurantId(restaurantId)
                .groupName("사이즈 선택")
                .description("음료 사이즈를 선택하세요")
                .minSelection(1)
                .maxSelection(1)
                .isRequired(true)
                .createdBy(userId)
                .build();
    }

    @Test
    @DisplayName("옵션 그룹 생성 - 정상")
    void createOptionGroup_Success() {
        // then
        assertThat(optionGroup).isNotNull();
        assertThat(optionGroup.getGroupName()).isEqualTo("사이즈 선택");
        assertThat(optionGroup.getIsRequired()).isTrue();
        assertThat(optionGroup.getMinSelection()).isEqualTo(1);
        assertThat(optionGroup.getMaxSelection()).isEqualTo(1);
    }

    @Test
    @DisplayName("선택 규칙 검증 - 최대값이 최소값보다 작을 때 예외")
    void validateSelectionRule_InvalidRange_ThrowsException() {
        // given
        MenuOptionGroup invalidGroup = MenuOptionGroup.builder()
                .groupName("잘못된 그룹")
                .minSelection(3)
                .maxSelection(1)  // 최대가 최소보다 작음
                .build();

        // when & then
        assertThatThrownBy(() -> invalidGroup.validateSelectionRule())
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", MenuErrorCode.INVALID_MAX_SELECTION);
    }

    @Test
    @DisplayName("필수 옵션 그룹의 최소 선택 개수 자동 보정")
    void validateSelectionRule_RequiredWithZeroMin_AutoCorrect() {
        // given
        MenuOptionGroup requiredGroup = MenuOptionGroup.builder()
                .groupName("필수 그룹")
                .isRequired(true)
                .minSelection(0)  // 필수인데 최소가 0
                .maxSelection(3)
                .build();

        // when
        requiredGroup.validateSelectionRule();

        // then
        assertThat(requiredGroup.getMinSelection()).isEqualTo(1);  // 자동으로 1로 보정
    }

    @Test
    @DisplayName("옵션 추가")
    void addOption() {
        // when
        MenuOption option = optionGroup.addOption(
                "Large",
                1000,  // 추가 금액
                1,     // 표시 순서
                userId
        );

        // then
        assertThat(optionGroup.getOptions()).hasSize(1);
        assertThat(option.getOptionName()).isEqualTo("Large");
        assertThat(option.getAdditionalPrice()).isEqualTo(new BigDecimal(1000));
        assertThat(option.getOptionGroupId()).isEqualTo(optionGroup.getId());
    }

    @Test
    @DisplayName("옵션 제거")
    void removeOption() {
        // given
        MenuOption option = optionGroup.addOption("Large", 1000, 1, userId);
        option = MenuOption.builder()
                .id("OPTION001")
                .createdAt(LocalDateTime.now())
                .optionGroupId(option.getOptionGroupId())
                .menuId(option.getMenuId())
                .restaurantId(option.getRestaurantId())
                .optionName(option.getOptionName())
                .additionalPrice(option.getAdditionalPrice())
                .displayOrder(option.getDisplayOrder())
                .createdBy(option.getCreatedBy())
                .build();
        optionGroup.getOptions().clear();
        optionGroup.getOptions().add(option);

        // when
        optionGroup.removeOption("OPTION001");

        // then
        assertThat(optionGroup.getOptions()).isEmpty();
    }

    @Test
    @DisplayName("그룹 정보 업데이트")
    void updateGroup() {
        // when
        optionGroup.update(
                "토핑 선택",
                "원하는 토핑을 선택하세요",
                false,  // 필수 아님
                0,
                3,
                userId
        );

        // then
        assertThat(optionGroup.getGroupName()).isEqualTo("토핑 선택");
        assertThat(optionGroup.getDescription()).isEqualTo("원하는 토핑을 선택하세요");
        assertThat(optionGroup.getIsRequired()).isFalse();
        assertThat(optionGroup.getMinSelection()).isEqualTo(0);
        assertThat(optionGroup.getMaxSelection()).isEqualTo(3);
        assertThat(optionGroup.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("그룹 소프트 삭제 - 하위 옵션도 함께 삭제")
    void deleteGroup_WithOptions() {
        // given
        MenuOption option = optionGroup.addOption("Large", 1000, 1, userId);

        // when
        optionGroup.delete(userId);

        // then
        assertThat(optionGroup.getIsDeleted()).isTrue();
        assertThat(optionGroup.getIsActive()).isFalse();
        assertThat(optionGroup.getOptions().get(0).getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("활성화된 옵션 개수 확인")
    void getActiveOptionCount() {
        // given
        MenuOption option1 = optionGroup.addOption("Small", 0, 1, userId);
        MenuOption option2 = optionGroup.addOption("Medium", 500, 2, userId);
        MenuOption option3 = optionGroup.addOption("Large", 1000, 3, userId);

        // when
        option2.delete(userId);  // 하나만 삭제

        // then
        assertThat(optionGroup.getActiveOptionCount()).isEqualTo(2);
    }
}