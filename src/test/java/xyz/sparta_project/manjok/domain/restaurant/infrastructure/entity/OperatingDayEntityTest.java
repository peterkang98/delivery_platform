package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.DayType;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.OperatingDay;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.OperatingTimeType;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OperatingDayEntity 변환 테스트 (연관관계 매핑 적용)
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
        // Restaurant 연관관계는 null (Restaurant에서 설정해야 함)
        assertThat(entity.getRestaurant()).isNull();
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
        // Restaurant 엔티티 생성
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId(1L)
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        OperatingDayEntity entity = OperatingDayEntity.builder()
                .dayType(DayType.SUN)
                .timeType(OperatingTimeType.HOLIDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(20, 0))
                .isHoliday(false)
                .breakStartTime(null)
                .breakEndTime(null)
                .note("일요일 공휴일 운영")
                .build();

        // 양방향 연관관계 설정
        restaurant.addOperatingDay(entity);

        // when
        OperatingDay domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getRestaurantId()).isEqualTo(restaurant.getId());
        assertThat(domain.getDayType()).isEqualTo(DayType.SUN);
        assertThat(domain.getTimeType()).isEqualTo(OperatingTimeType.HOLIDAY);
        assertThat(domain.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(domain.getEndTime()).isEqualTo(LocalTime.of(20, 0));
        assertThat(domain.getIsHoliday()).isFalse();
        assertThat(domain.getBreakStartTime()).isNull();
        assertThat(domain.getBreakEndTime()).isNull();
        assertThat(domain.getNote()).isEqualTo("일요일 공휴일 운영");
    }

    @Test
    @DisplayName("OperatingDay와 Restaurant 양방향 연관관계 설정")
    void addOperatingDay_ShouldSetBidirectionalRelation() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId(1L)
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        OperatingDayEntity monday = OperatingDayEntity.builder()
                .dayType(DayType.MON)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .isHoliday(false)
                .build();

        OperatingDayEntity tuesday = OperatingDayEntity.builder()
                .dayType(DayType.TUE)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(22, 0))
                .isHoliday(false)
                .build();

        // when
        restaurant.addOperatingDay(monday);
        restaurant.addOperatingDay(tuesday);

        // then
        assertThat(restaurant.getOperatingDays()).hasSize(2);
        assertThat(restaurant.getOperatingDays()).contains(monday, tuesday);
        assertThat(monday.getRestaurant()).isEqualTo(restaurant);
        assertThat(tuesday.getRestaurant()).isEqualTo(restaurant);
    }

    @Test
    @DisplayName("휴무일 OperatingDay 설정")
    void holidayOperatingDay_ShouldWork() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId(1L)
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        OperatingDayEntity holiday = OperatingDayEntity.builder()
                .dayType(DayType.WED)
                .timeType(OperatingTimeType.REGULAR)
                .startTime(null)
                .endTime(null)
                .isHoliday(true)
                .note("정기 휴무일")
                .build();

        // when
        restaurant.addOperatingDay(holiday);

        // then
        assertThat(holiday.getIsHoliday()).isTrue();
        assertThat(holiday.getStartTime()).isNull();
        assertThat(holiday.getEndTime()).isNull();
        assertThat(holiday.getNote()).isEqualTo("정기 휴무일");
    }
}