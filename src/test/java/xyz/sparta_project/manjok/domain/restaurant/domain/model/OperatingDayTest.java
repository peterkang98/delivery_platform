package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OperatingDay 도메인 모델 테스트")
class OperatingDayTest {

    @Test
    @DisplayName("Builder를 통한 정상 생성 테스트")
    void should_create_operating_day_with_builder() {
        // given
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(22, 0);

        // when
        OperatingDay operatingDay = OperatingDay.builder()
                .restaurantId("REST001")
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        // then
        assertThat(operatingDay.getRestaurantId()).isEqualTo("REST001");
        assertThat(operatingDay.getDayType()).isEqualTo(DayType.MON);
        assertThat(operatingDay.getTimeType()).isEqualTo(OperatingTimeType.REGULAR);
        assertThat(operatingDay.getStartTime()).isEqualTo(startTime);
        assertThat(operatingDay.getEndTime()).isEqualTo(endTime);
        assertThat(operatingDay.getIsHoliday()).isFalse();
    }

    @Test
    @DisplayName("브레이크 타입 포함 생성 테스트")
    void should_create_operating_day_with_break_time() {
        // given & when
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(21, 0);
        LocalTime breakStartTime = LocalTime.of(15, 0);
        LocalTime breakEndTime = LocalTime.of(17, 0);

        OperatingDay operatingDay = OperatingDay.builder()
                .restaurantId("REST001")
                .dayType(DayType.TUE)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(startTime)
                .endTime(endTime)
                .breakStartTime(breakStartTime)
                .breakEndTime(breakEndTime)
                .note("평일 브레이크 타임 있음")
                .build();

        // then
        assertThat(operatingDay.getBreakStartTime()).isEqualTo(breakStartTime);
        assertThat(operatingDay.getBreakEndTime()).isEqualTo(breakEndTime);
        assertThat(operatingDay.getNote()).isEqualTo("평일 브레이크 타임 있음");
    }

    @Test
    @DisplayName("휴무일 생성 테스트")
    void should_create_holiday_operating_day() {
        // given & when
        OperatingDay operatingDay = OperatingDay.builder()
                .restaurantId("REST001")
                .dayType(DayType.SUN)
                .timeType(OperatingTimeType.REGULAR)
                .isHoliday(true)
                .note("일요일 정기 휴무")
                .build();

        // then
        assertThat(operatingDay.getIsHoliday()).isTrue();
        assertThat(operatingDay.getNote()).isEqualTo("일요일 정기 휴무");
    }

    @Test
    @DisplayName("hasOperatingHours - 운영 시간 설정 여부 확인")
    void should_check_if_has_operating_hours() {
        // given
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(22, 0);
        OperatingDay withHours = OperatingDay.builder()
                .startTime(startTime)
                .endTime(endTime)
                .build();

        OperatingDay holiday = OperatingDay.builder()
                .startTime(startTime)
                .endTime(endTime)
                .isHoliday(true)
                .build();

        OperatingDay noHours = OperatingDay.builder().build();

        // when & then
        assertThat(withHours.hasOperatingHours()).isTrue();
        assertThat(holiday.hasOperatingHours()).isFalse();
        assertThat(noHours.hasOperatingHours()).isFalse();

    }

    @Test
    @DisplayName("요일 매칭 테스트 - 월요일부터 일요일까지")
    void should_match_correct_day_of_week() {
        // given
        OperatingDay monday = createOperatingDayForDay(DayType.MON);
        OperatingDay tuesday = createOperatingDayForDay(DayType.TUE);
        OperatingDay wednesday = createOperatingDayForDay(DayType.WED);
        OperatingDay thursday = createOperatingDayForDay(DayType.THU);
        OperatingDay friday = createOperatingDayForDay(DayType.FRI);
        OperatingDay saturday = createOperatingDayForDay(DayType.SAT);
        OperatingDay sunday = createOperatingDayForDay(DayType.SUN);

        LocalDateTime mon = LocalDateTime.of(2025, 11, 3, 12, 0);
        LocalDateTime tue = LocalDateTime.of(2025, 11, 4, 12, 0);
        LocalDateTime wed = LocalDateTime.of(2025, 11, 5, 12, 0);
        LocalDateTime thu = LocalDateTime.of(2025, 11, 6, 12, 0);
        LocalDateTime fri = LocalDateTime.of(2025, 11, 7, 12, 0);
        LocalDateTime sat = LocalDateTime.of(2025, 11, 8, 12, 0);
        LocalDateTime sun = LocalDateTime.of(2025, 11, 9, 12, 0);

        // when & then
        assertThat(monday.isOpenAt(mon)).isTrue();
        assertThat(monday.isOpenAt(tue)).isFalse();

        assertThat(tuesday.isOpenAt(tue)).isTrue();
        assertThat(tuesday.isOpenAt(wed)).isFalse();

        assertThat(wednesday.isOpenAt(wed)).isTrue();
        assertThat(thursday.isOpenAt(thu)).isTrue();
        assertThat(friday.isOpenAt(fri)).isTrue();
        assertThat(saturday.isOpenAt(sat)).isTrue();
        assertThat(sunday.isOpenAt(sun)).isTrue();
    }
    @Test
    @DisplayName("해당 도메인은 어떤 역할을 한다.")
    void shoud_function() {

    }

    @Test
    @DisplayName("isOpenAt(LocalDateTime) - 특정 날짜/시간에 영업 중인지 확인")
    void should_check_if_open_at_specific_datetime() {
        // given
        OperatingDay mondayOperating = OperatingDay.builder()
                .dayType(DayType.MON)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(22, 0))
                .build();

        LocalDateTime monday10am = LocalDateTime.of(2025, 11, 3, 10, 0);
        LocalDateTime monday8am = LocalDateTime.of(2025, 11, 3, 8, 0);
        LocalDateTime tuesday10am = LocalDateTime.of(2025, 11, 4, 10, 0);

        // when & then
        assertThat(mondayOperating.isOpenAt(monday10am)).isTrue();  // 월요일 영업시간 내
        assertThat(mondayOperating.isOpenAt(monday8am)).isFalse();  // 월요일 영업시간 전
        assertThat(mondayOperating.isOpenAt(tuesday10am)).isFalse(); // 다른 요일
    }

    @Test
    @DisplayName("isOpenAt - 휴무일 처리")
    void should_return_false_when_holday() {
        // given
        OperatingDay holiday = OperatingDay.builder()
                .dayType(DayType.SUN)
                .isHoliday(true)
                .build();

        LocalDateTime sunday10am = LocalDateTime.of(2025, 11, 2, 10, 0);

        // when & then
        assertThat(holiday.isOpenAt(sunday10am)).isFalse();
    }

    @Test
    @DisplayName("isOpenAt - 브레이크 타임 처리")
    void should_handle_break_time_correctly() {
        // given
        OperatingDay withBreak = OperatingDay.builder()
                .dayType(DayType.MON)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .breakStartTime(LocalTime.of(15, 0))
                .breakEndTime(LocalTime.of(17, 0))
                .build();

        LocalDateTime monday14 = LocalDateTime.of(2025, 11, 3, 14, 0);  // 브레이크 타임 전
        LocalDateTime monday16 = LocalDateTime.of(2025, 11, 3, 16, 0);  // 브레이크 타임 중
        LocalDateTime monday18 = LocalDateTime.of(2025, 11, 3, 18, 0);  // 브레이크 타임 후

        // when & then
        assertThat(withBreak.isOpenAt(monday14)).isTrue();
        assertThat(withBreak.isOpenAt(monday16)).isFalse();
        assertThat(withBreak.isOpenAt(monday18)).isTrue();
    }

    @Test
    @DisplayName("isTImeInOperatingHours - 자정을 넘어가는 운영 시간")
    void should_handle_overnight_hour() {
        // given
        OperatingDay overnight = OperatingDay.builder()
                .startTime(LocalTime.of(20, 0))  // 20:00 시작
                .endTime(LocalTime.of(2, 0))     // 02:00 종료
                .build();

        // when & then
        assertThat(overnight.isTimeInOperatingHours(LocalTime.of(21, 0))).isTrue();  // 저녁
        assertThat(overnight.isTimeInOperatingHours(LocalTime.of(23, 59))).isTrue(); // 자정 전
        assertThat(overnight.isTimeInOperatingHours(LocalTime.of(0, 30))).isTrue();  // 자정 후
        assertThat(overnight.isTimeInOperatingHours(LocalTime.of(1, 30))).isTrue();  // 새벽
        assertThat(overnight.isTimeInOperatingHours(LocalTime.of(2, 1))).isFalse();  // 종료 후
        assertThat(overnight.isTimeInOperatingHours(LocalTime.of(10, 0))).isFalse(); // 낮
    }

    @Test
    @DisplayName("isInBreakTIme - 브레이크 타임 체크")
    void should_identify_break_time() {
        // given
        OperatingDay withBreak = OperatingDay.builder()
                .breakStartTime(LocalTime.of(15, 0))
                .breakEndTime(LocalTime.of(17, 0))
                .build();

        // when & then
        assertThat(withBreak.isInBreakTime(LocalTime.of(14, 59))).isFalse();
        assertThat(withBreak.isInBreakTime(LocalTime.of(15, 0))).isTrue();
        assertThat(withBreak.isInBreakTime(LocalTime.of(16, 0))).isTrue();
        assertThat(withBreak.isInBreakTime(LocalTime.of(16, 59))).isTrue();
        assertThat(withBreak.isInBreakTime(LocalTime.of(17, 0))).isFalse();
    }

    @Test
    @DisplayName("getTimeUntilOpen - 영업 시작까지 남은 시간")
    void should_calculate_time_until_open() {
        // given
        OperatingDay operating = OperatingDay.builder()
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .build();

        // when & then
        assertThat(operating.getTimeUntilOpen(LocalTime.of(9, 0))).isEqualTo("60분 후 오픈");
        assertThat(operating.getTimeUntilOpen(LocalTime.of(9, 30))).isEqualTo("30분 후 오픈");
        assertThat(operating.getTimeUntilOpen(LocalTime.of(12, 0))).isNull(); // 영업 중
        assertThat(operating.getTimeUntilOpen(LocalTime.of(23, 0))).isEqualTo("영업 종료");
    }

    @Test
    @DisplayName("getTimeUntilOpen - 브레이크 타임 중")
    void should_show_break_time_info() {
        // given
        OperatingDay withBreak = OperatingDay.builder()
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .breakStartTime(LocalTime.of(15, 0))
                .breakEndTime(LocalTime.of(17, 0))
                .build();

        // when & then
        assertThat(withBreak.getTimeUntilOpen(LocalTime.of(15, 30)))
                .isEqualTo("브레이크 타임 (90분 후 재오픈)");
    }

    @Test
    @DisplayName("getFullDisplay - 전체 정보 표시")
    void should_display_full_information() {
        // given
        OperatingDay operating = OperatingDay.builder()
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .breakStartTime(LocalTime.of(15, 0))
                .breakEndTime(LocalTime.of(17, 0))
                .build();

        // when
        String display = operating.getFullDisplay();

        // then
        assertThat(display).isEqualTo("[월요일/평일/정규] 10:00 ~ 22:00 (브레이크타임: 15:00 ~ 17:00)");
    }

    @Test
    @DisplayName("getOperatingHoursDisplay - 다양한 상태 표시")
    void should_display_various_states() {
        // given
        OperatingDay normal = OperatingDay.builder()
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(22, 0))
                .build();

        OperatingDay holiday = OperatingDay.builder()
                .isHoliday(true)
                .build();

        OperatingDay undecided = OperatingDay.builder()
                .build();

        // when & then
        assertThat(normal.getOperatingHoursDisplay()).isEqualTo("09:00 ~ 22:00");
        assertThat(holiday.getOperatingHoursDisplay()).isEqualTo("휴무");
        assertThat(undecided.getOperatingHoursDisplay()).isEqualTo("운영 시간 미정");
    }
    
    @Test
    @DisplayName("equals와 hashCode - restaurantId, dayType, timeType 기준")
    void should_be_equal_based_on_key_field() {
        // given
        OperatingDay day1 = OperatingDay.builder()
                .restaurantId("REST001")
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(9, 0))
                .build();

        OperatingDay day2 = OperatingDay.builder()
                .restaurantId("REST001")
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(10, 0))  // 다른 시작 시간
                .build();

        OperatingDay day3 = OperatingDay.builder()
                .restaurantId("REST002")  // 다른 레스토랑
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .build();

        // when & then
        assertThat(day1).isEqualTo(day2);
        assertThat(day1.hashCode()).isEqualTo(day2.hashCode());
        assertThat(day1).isNotEqualTo(day3);
    }

    private OperatingDay createOperatingDayForDay(DayType dayType) {
        return OperatingDay.builder()
                .dayType(dayType)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .build();
    }
}