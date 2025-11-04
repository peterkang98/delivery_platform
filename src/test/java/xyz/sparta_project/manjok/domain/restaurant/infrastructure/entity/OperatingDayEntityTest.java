package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.DayType;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.OperatingDay;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.OperatingTimeType;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OperatingDayEntity 변환 테스트
 */
class OperatingDayEntityTest {

    @Test
    @DisplayName("도메인 OperatingDay를 OperatingDayEntity로 변환")
    void fromDomain_ShouldConvertOperatingDayToEntity() {
        // given
        OperatingDay domain = OperatingDay.builder()
                .restaurantId("REST123")
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(22, 0))
                .isHoliday(false)
                .breakStartTime(LocalTime.of(15, 0))
                .breakEndTime(LocalTime.of(17, 0))
                .note("월요일 정규 운영")
                .build();

        // when
        OperatingDayEntity entity = OperatingDayEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getRestaurantId()).isEqualTo("REST123");
        assertThat(entity.getDayType()).isEqualTo(DayType.MON);
        assertThat(entity.getTimeType()).isEqualTo(OperatingTimeType.REGULAR);
        assertThat(entity.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(entity.getEndTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(entity.getIsHoliday()).isFalse();
        assertThat(entity.getBreakStartTime()).isEqualTo(LocalTime.of(15, 0));
        assertThat(entity.getBreakEndTime()).isEqualTo(LocalTime.of(17, 0));
        assertThat(entity.getNote()).isEqualTo("월요일 정규 운영");
    }

    @Test
    @DisplayName("OperatingDayEntity를 도메인 OperatingDay로 변환")
    void toDomain_ShouldConvertEntityToOperatingDay() {
        // given
        OperatingDayEntity entity = OperatingDayEntity.builder()
                .restaurantId("REST456")
                .dayType(DayType.SUN)
                .timeType(OperatingTimeType.HOLIDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(20, 0))
                .isHoliday(false)
                .breakStartTime(null)
                .breakEndTime(null)
                .note("일요일 공휴일 운영")
                .build();

        // when
        OperatingDay domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getRestaurantId()).isEqualTo("REST456");
        assertThat(domain.getDayType()).isEqualTo(DayType.SUN);
        assertThat(domain.getTimeType()).isEqualTo(OperatingTimeType.HOLIDAY);
        assertThat(domain.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(domain.getEndTime()).isEqualTo(LocalTime.of(20, 0));
        assertThat(domain.getIsHoliday()).isFalse();
        assertThat(domain.getBreakStartTime()).isNull();
        assertThat(domain.getBreakEndTime()).isNull();
        assertThat(domain.getNote()).isEqualTo("일요일 공휴일 운영");
    }
}