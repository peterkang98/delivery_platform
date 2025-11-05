package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * OperatingDay Domain Model
 * - 레스토랑의 운영 시간 정보
 * - 평일/주말/공휴일별 운영 시간을 별도로 관리
 * - 순수 도메인 모델
 * */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"restaurantId", "dayType", "timeType"})
public class OperatingDay {

    private String restaurantId;
    private DayType dayType;              // 요일
    private OperatingTimeType timeType;   // 운영 시간 타입 (평일/공휴일/특별공휴일)

    private LocalTime startTime;           // 영업 시작 시간
    private LocalTime endTime;             // 영업 종료 시간

    @Builder.Default
    private Boolean isHoliday = false;    // 휴무 여부

    private LocalTime breakStartTime;      // 브레이크 타임 시작
    private LocalTime breakEndTime;        // 브레이크 타임 종료

    private String note;                   // 특이사항 (예: "명절 연휴 휴무")

    /**
     * 운영 시간이 설정되어 있는지 확인
     * (단순히 설정 여부만 체크, 실시간 운영 여부와는 다름)
     */
    public boolean hasOperatingHours() {
        return !isHoliday && startTime != null && endTime != null;
    }

    /**
     * 특정 날짜/시간에 영업 중인지 확인
     * @param localDateTime 확인할 날짜와 시간
     * @return 영업 중이면 true
     * */
    public boolean isOpenAt(LocalDateTime localDateTime) {
        // 1. 요일이 맞는 지 확인
        if (!matchesDayOfWeek(localDateTime.getDayOfWeek())) {
            return false;
        }

        // 2. 휴무일인지 확인
        if (isHoliday) {
            return false;
        }

        // 3. 운영 시간이 설정되어 있는지 확인
        if (startTime == null || endTime == null) {
            return false;
        }

        // 4. 현재 시간이 운영시간 내인지 확인
        return isTimeInOperatingHours(localDateTime.toLocalTime());
    }

    /**
     * 특정 시간이 운영 시간 내인지 확인 (요일 체크 없이 시간만 체크)
     * @param time 확인할 시간
     * @return 운영 시간 내면 true
     * */
    public boolean isTimeInOperatingHours(LocalTime time) {
        if (startTime == null || endTime == null) {
            return false;
        }

        // 브레이크 타임인지 체크
        if (isInBreakTime(time)) {
            return false;
        }

        // 자정을 넘어가는 경우 처리 (예: 20:00 ~ 02:00)
        if (endTime.isBefore(startTime)) {
            // 자정을 넘는 경우: 시작 시간 이후이거나 종료 시간 이전
            return !time.isBefore(startTime) || !time.isAfter(endTime);
        }

        // 일반적인 경우: 시작 시간과 종료 시간 사이
        return (time.equals(startTime) || time.isAfter(startTime)) &&
                (time.equals(endTime) || time.isBefore(endTime));
    }

    /**
     * 브레이크 타임인지 확인
     * @param time 확인할 시간
     * @return 브레이크 타임이면 true
     * */
    public boolean isInBreakTime(LocalTime time) {
        if (breakStartTime == null || breakEndTime == null) {
            return false;
        }
        return !time.isBefore(breakStartTime) && time.isBefore(breakEndTime);
    }

    /**
     * 현재 시점 기준으로 영업 중인지 확인
     * @return 현재 영업 중이면 true
     * */
    public boolean isOpenNow() {
        return isOpenAt(LocalDateTime.now());
    }

    /**
     * 다음 영업 시작 시간까지 남은 시간 정보
     * @param currentTime 현재 시간
     * @return 영업 중이면 null, 휴무일이면 "휴무", 그 외에는 남은 시간
     * */
    public String getTimeUntilOpen(LocalTime currentTime) {
        if (isHoliday) {
            return "휴무";
        }

        if (startTime == null || endTime == null) {
            return "운영 시간 미정";
        }

        // 브레이크 타임 체크를 먼저 수행
        if (isInBreakTime(currentTime)) {
            long minutesUntilReopen = currentTime.until(breakEndTime, java.time.temporal.ChronoUnit.MINUTES);
            return String.format("브레이크 타임 (%d분 후 재오픈)", minutesUntilReopen);
        }

        if (isTimeInOperatingHours(currentTime)) {
            return null; // 영업 중
        }

        // 영업 시작 전인 경우
        if (currentTime.isBefore(startTime)) {
            long minutesUntilOpen = currentTime.until(startTime, java.time.temporal.ChronoUnit.MINUTES);
            return String.format("%d분 후 오픈", minutesUntilOpen);
        }

        // 영업 종료 후인 경우
        return "영업 종료";
    }

    /**
     * 운영 시간 문자열 표현
     */
    public String getOperatingHoursDisplay() {
        if (isHoliday) {
            return "휴무";
        }

        if (startTime == null || endTime == null) {
            return "운영 시간 미정";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String hours = String.format("%s ~ %s",
                startTime.format(formatter),
                endTime.format(formatter));

        if (breakStartTime != null && breakEndTime != null) {
            hours += String.format(" (브레이크타임: %s ~ %s)",
                    breakStartTime.format(formatter),
                    breakEndTime.format(formatter));
        }

        return hours;
    }

    /**
     * 요일과 운영 시간 타입을 포함한 전체 정보 표시
     */
    public String getFullDisplay() {
        String dayName = dayType.getKoreanName();
        String timeTypeDesc = timeType.getDescription();
        String hours = getOperatingHoursDisplay();

        return String.format("[%s/%s] %s", dayName, timeTypeDesc, hours);
    }


    /**
     * DayOfWeek와 DayType 매칭 확인
     * @param dayOfWeek java의 DayOfWeek
     * @return true
     * */
    private boolean matchesDayOfWeek(DayOfWeek dayOfWeek) {
        if (dayOfWeek == DayOfWeek.MONDAY) return dayType == DayType.MON;
        if (dayOfWeek == DayOfWeek.TUESDAY) return dayType == DayType.TUE;
        if (dayOfWeek == DayOfWeek.WEDNESDAY) return dayType == DayType.WED;
        if (dayOfWeek == DayOfWeek.THURSDAY) return dayType == DayType.THU;
        if (dayOfWeek == DayOfWeek.FRIDAY) return dayType == DayType.FRI;
        if (dayOfWeek == DayOfWeek.SATURDAY) return dayType == DayType.SAT;
        return dayType == DayType.SUN;
    }
}
