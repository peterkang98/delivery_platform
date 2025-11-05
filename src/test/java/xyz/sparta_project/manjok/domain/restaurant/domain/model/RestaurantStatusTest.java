package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RestaurantStatus Enum 테스트")
class RestaurantStatusTest {

    @Test
    @DisplayName("모든 레스토랑 상태 상수가 정상적으로 생성되는지 확인")
    void should_create_all_status_constants() {
        // when & then
        assertThat(RestaurantStatus.values()).hasSize(4);
        assertThat(RestaurantStatus.values()).containsExactly(
                RestaurantStatus.OPEN,
                RestaurantStatus.CLOSED,
                RestaurantStatus.TEMPORARILY_CLOSED,
                RestaurantStatus.PREPARING
        );
    }

    @Test
    @DisplayName("각 상태의 표시 이름이 올바르게 설정되는지 확인")
    void should_have_correct_display_names() {
        // when & then
        assertThat(RestaurantStatus.OPEN.getDisplayName()).isEqualTo("영업중");
        assertThat(RestaurantStatus.CLOSED.getDisplayName()).isEqualTo("영업종료");
        assertThat(RestaurantStatus.TEMPORARILY_CLOSED.getDisplayName()).isEqualTo("임시휴업");
        assertThat(RestaurantStatus.PREPARING.getDisplayName()).isEqualTo("준비중");
    }

    @Test
    @DisplayName("각 상태의 설명이 올바르게 설정되는지 확인")
    void should_have_correct_descriptions() {
        // when & then
        assertThat(RestaurantStatus.OPEN.getDescription()).isEqualTo("영업 중입니다");
        assertThat(RestaurantStatus.CLOSED.getDescription()).isEqualTo("영업이 종료되었습니다");
        assertThat(RestaurantStatus.TEMPORARILY_CLOSED.getDescription()).isEqualTo("임시 휴업 중입니다");
        assertThat(RestaurantStatus.PREPARING.getDescription()).isEqualTo("영업 준비 중입니다");
    }

    @Test
    @DisplayName("OPEN 상태에서만 주문을 받을 수 있다")
    void only_open_status_can_accept_order() {
        // when & then
        assertThat(RestaurantStatus.OPEN.canAcceptOrder()).isTrue();
        assertThat(RestaurantStatus.CLOSED.canAcceptOrder()).isFalse();
        assertThat(RestaurantStatus.TEMPORARILY_CLOSED.canAcceptOrder()).isFalse();
        assertThat(RestaurantStatus.PREPARING.canAcceptOrder()).isFalse();
    }

    @Test
    @DisplayName("OPEN 상태에서는 메뉴 수정을 할 수 없다")
    void cannot_modify_menu_when_open() {
        // when & then
        assertThat(RestaurantStatus.OPEN.canModifyMenu()).isFalse();
        assertThat(RestaurantStatus.CLOSED.canModifyMenu()).isTrue();
        assertThat(RestaurantStatus.TEMPORARILY_CLOSED.canModifyMenu()).isTrue();
        assertThat(RestaurantStatus.PREPARING.canModifyMenu()).isTrue();
    }
}