package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;

/**
 * 요일 타입
 * */
@Getter
public enum DayType {
    MON("월요일"),
    TUE("화요일"),
    WED("수요일"),
    THU("목요일"),
    FRI("금요일"),
    SAT("토요일"),
    SUN("일요일");

    private final String KoreanName;

    DayType(String koreanName) {
        this.KoreanName = koreanName;
    }

    /**
     * 평일인지 확인
     * */
    public boolean isWeekday() {
        return this != SAT && this != SUN;
    }

    /**
     * 주말인지 확인
     * */
    public boolean isWeekend() {
        return this == SAT || this == SUN;
    }
}
