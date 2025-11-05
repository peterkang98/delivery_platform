package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class OperatingTimeTypeTest {

    @Test
    @DisplayName("모든 운영 시간 타입 상수가 정상적으로 생성되는지 확인")
    void should_create_all_operating_time_constants() {
        // when & then
        assertThat(OperatingTimeType.values()).hasSize(3);
        assertThat(OperatingTimeType.values()).containsExactly(
                OperatingTimeType.REGULAR,
                OperatingTimeType.HOLIDAY,
                OperatingTimeType.SPECIAL_HOLIDAY
        );
    }

    @Test
    @DisplayName("isRegular 메서드가 정규 운영 시간만 true 반환하는지 확인")
    void should_identify_regular_operating_time() {

    }

    @Test
    @DisplayName("isHolidayRelated 메서드가 공휴일 관련 타입만 true 반환하는지 확인")
    void should_identify_holiday_related_operating_time() {
        // when & then
        assertThat(OperatingTimeType.REGULAR.isHolidayRelated()).isFalse();
        assertThat(OperatingTimeType.HOLIDAY.isHolidayRelated()).isTrue();
        assertThat(OperatingTimeType.SPECIAL_HOLIDAY.isHolidayRelated()).isTrue();
    }

    @ParameterizedTest(name = "{0} should have non-null description")
    @EnumSource(OperatingTimeType.class)
    @DisplayName("모든 타입이 null이 아닌 설명을 가지는지 확인")
    void should_have_non_null_description(OperatingTimeType type) {
        // when & then
        assertThat(type.getDescription()).isNotNull();
        assertThat(type.getDescription()).isNotBlank();
    }

    @Test
    @DisplayName("Enum의 ordinal 값 확인")
    void should_have_correct_ordinal_values() {
        // when & then
        assertThat(OperatingTimeType.REGULAR.ordinal()).isEqualTo(0);
        assertThat(OperatingTimeType.HOLIDAY.ordinal()).isEqualTo(1);
        assertThat(OperatingTimeType.SPECIAL_HOLIDAY.ordinal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Enum의 name 메서드 확인")
    void should_have_correct_names() {
        // when & then
        assertThat(OperatingTimeType.REGULAR.name()).isEqualTo("REGULAR");
        assertThat(OperatingTimeType.HOLIDAY.name()).isEqualTo("HOLIDAY");
        assertThat(OperatingTimeType.SPECIAL_HOLIDAY.name()).isEqualTo("SPECIAL_HOLIDAY");
    }
}