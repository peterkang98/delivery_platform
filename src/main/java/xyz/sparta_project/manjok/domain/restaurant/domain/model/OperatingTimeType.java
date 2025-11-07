package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;

/**
 * 운영 시간 타입
 * - 평일, 공휴일, 특별 공휴일별 운영 시간 구분
 * */
@Getter
public enum OperatingTimeType {
    REGULAR("평일/정규"),           // 일반 운영 시간
    HOLIDAY("일반 공휴일"),          // 일반 공휴일 (빨간날)
    SPECIAL_HOLIDAY("특별 공휴일");  // 명절 등 특별 공휴일

    private final String description;

    OperatingTimeType(String description) {
        this.description = description;
    }

    /**
     * 정규 운영 시간인지 확인
     */
    public boolean isRegular() {
        return this == REGULAR;
    }

    /**
     * 공휴일 관련 운영 시간인지 확인 (일반 공휴일 + 특별 공휴일)
     */
    public boolean isHolidayRelated() {
        return this == HOLIDAY || this == SPECIAL_HOLIDAY;
    }
}
