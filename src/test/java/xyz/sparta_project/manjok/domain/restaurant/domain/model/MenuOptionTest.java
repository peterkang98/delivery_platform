package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MenuOption 도메인 모델 테스트")
class MenuOptionTest {

    private MenuOption option;
    private final String optionGroupId = "GROUP001";
    private final String menuId = "MENU001";
    private final String restaurantId = "REST001";
    private final String userId = "USER001";

    @BeforeEach
    void setUp() {
        option = MenuOption.builder()
                .id("OPTION001")
                .createdAt(LocalDateTime.now())
                .optionGroupId(optionGroupId)
                .menuId(menuId)
                .restaurantId(restaurantId)
                .optionName("Large")
                .description("큰 사이즈")
                .additionalPrice(new BigDecimal("1000"))
                .isAvailable(true)
                .isDefault(false)
                .displayOrder(1)
                .createdBy(userId)
                .build();
    }

    @Test
    @DisplayName("옵션 생성 - 정상")
    void createOption_Success() {
        // then
        assertThat(option).isNotNull();
        assertThat(option.getOptionName()).isEqualTo("Large");
        assertThat(option.getAdditionalPrice()).isEqualTo(new BigDecimal("1000"));
        assertThat(option.getIsAvailable()).isTrue();
        assertThat(option.getIsDefault()).isFalse();
        assertThat(option.getPurchaseCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("추가 가격 설정 - 0원 허용")
    void setAdditionalPrice_ZeroPrice_Success() {
        // when
        option.setAdditionalPrice(BigDecimal.ZERO);

        // then
        assertThat(option.getAdditionalPrice()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("옵션 정보 업데이트")
    void updateOption() {
        // when
        option.update(
                "Extra Large",
                "특대 사이즈",
                new BigDecimal("1500"),
                2,
                userId
        );

        // then
        assertThat(option.getOptionName()).isEqualTo("Extra Large");
        assertThat(option.getDescription()).isEqualTo("특대 사이즈");
        assertThat(option.getAdditionalPrice()).isEqualTo(new BigDecimal("1500"));
        assertThat(option.getDisplayOrder()).isEqualTo(2);
        assertThat(option.getUpdatedAt()).isNotNull();
        assertThat(option.getUpdatedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("기본 옵션으로 설정")
    void setDefault() {
        // when
        option.setDefault(true, userId);

        // then
        assertThat(option.getIsDefault()).isTrue();
        assertThat(option.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("판매 가능 여부 설정")
    void setAvailable() {
        // when
        option.setAvailable(false, userId);

        // then
        assertThat(option.getIsAvailable()).isFalse();
        assertThat(option.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("구매 카운트 증가")
    void incrementPurchaseCount() {
        // given
        int initialCount = option.getPurchaseCount();

        // when
        option.incrementPurchaseCount();
        option.incrementPurchaseCount();

        // then
        assertThat(option.getPurchaseCount()).isEqualTo(initialCount + 2);
    }

    @Test
    @DisplayName("옵션 소프트 삭제")
    void deleteOption() {
        // when
        option.delete(userId);

        // then
        assertThat(option.getIsDeleted()).isTrue();
        assertThat(option.getIsAvailable()).isFalse();
        assertThat(option.getDeletedAt()).isNotNull();
        assertThat(option.getDeletedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("선택 가능 여부 확인 - 정상 상태")
    void isSelectable_Available() {
        // then
        assertThat(option.isSelectable()).isTrue();
    }

    @Test
    @DisplayName("선택 가능 여부 확인 - 비활성 상태")
    void isSelectable_NotAvailable() {
        // when
        option.setAvailable(false, userId);

        // then
        assertThat(option.isSelectable()).isFalse();
    }

    @Test
    @DisplayName("선택 가능 여부 확인 - 삭제된 상태")
    void isSelectable_Deleted() {
        // when
        option.delete(userId);

        // then
        assertThat(option.isSelectable()).isFalse();
    }
}