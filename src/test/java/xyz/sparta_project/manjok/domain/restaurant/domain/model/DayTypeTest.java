package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DayType Enum 테스트")
class DayTypeTest {

    @Test
    @DisplayName("모든 요일 상수가 정상적으로 생성되는지 확인")
    void should_create_all_day_constants() {
        // when & then
        assertThat(DayType.values()).hasSize(7);
        assertThat(DayType.values()).containsExactly(
                DayType.MON, DayType.TUE, DayType.WED,
                DayType.THU, DayType.FRI, DayType.SAT, DayType.SUN
        );
    }

    @Test
    @DisplayName("각 요일의 한국어 이름이 올바르게 설정되는지 확인")
    void should_have_correct_korean_names() {
        // when & then
        assertThat(DayType.MON.getKoreanName()).isEqualTo("월요일");
        assertThat(DayType.TUE.getKoreanName()).isEqualTo("화요일");
        assertThat(DayType.WED.getKoreanName()).isEqualTo("수요일");
        assertThat(DayType.THU.getKoreanName()).isEqualTo("목요일");
        assertThat(DayType.FRI.getKoreanName()).isEqualTo("금요일");
        assertThat(DayType.SAT.getKoreanName()).isEqualTo("토요일");
        assertThat(DayType.SUN.getKoreanName()).isEqualTo("일요일");
    }

    @ParameterizedTest(name = "{0} should be weekday")
    @EnumSource(value = DayType.class, names = {"MON", "TUE", "WED", "THU", "FRI"})
    @DisplayName("평일 확인 메서드가 올바르게 동작하는지 확인")
    void should_identify_weekdays(DayType day) {
        // when & then
        assertThat(day.isWeekday()).isTrue();
        assertThat(day.isWeekend()).isFalse();
    }

    @ParameterizedTest(name = "{0} should be weekend")
    @EnumSource(value = DayType.class, names = {"SAT", "SUN"})
    @DisplayName("주말 확인 메서드가 올바르게 동작하는지 확인")
    void should_identify_weekends(DayType day) {
        // when & then
        assertThat(day.isWeekend()).isTrue();
        assertThat(day.isWeekday()).isFalse();
    }

    @Test
    @DisplayName("isWeekday 메서드 전체 요일 검증")
    void should_validate_is_weekday_for_all_days() {
        // when & then
        assertThat(DayType.MON.isWeekday()).isTrue();
        assertThat(DayType.TUE.isWeekday()).isTrue();
        assertThat(DayType.WED.isWeekday()).isTrue();
        assertThat(DayType.THU.isWeekday()).isTrue();
        assertThat(DayType.FRI.isWeekday()).isTrue();
        assertThat(DayType.SAT.isWeekday()).isFalse();
        assertThat(DayType.SUN.isWeekday()).isFalse();
    }

    @Test
    @DisplayName("isWeekend 메서드 전체 요일 검증")
    void should_validate_is_weekend_for_all_days() {
        // when & then
        assertThat(DayType.MON.isWeekend()).isFalse();
        assertThat(DayType.TUE.isWeekend()).isFalse();
        assertThat(DayType.WED.isWeekend()).isFalse();
        assertThat(DayType.THU.isWeekend()).isFalse();
        assertThat(DayType.FRI.isWeekend()).isFalse();
        assertThat(DayType.SAT.isWeekend()).isTrue();
        assertThat(DayType.SUN.isWeekend()).isTrue();
    }
}