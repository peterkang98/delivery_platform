package xyz.sparta_project.manjok.domain.restaurant.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

/**
 * 레스토랑 도메인 관련 에러 코드
 * */
@Getter
@RequiredArgsConstructor
public enum RestaurantErrorCode implements ErrorCode {

    // Coordinate 관련 에러 (RESTAURANT_001~009)
    COORDINATE_REQUIRED("RESTAURANT_001", "위도와 경도는 필수입니다.", 400),
    INVALID_LATITUDE_RANGE("RESTAURANT_002", "위도는 -90도에서 90도 사이여야 합니다.", 400),
    INVALID_LONGITUDE_RANGE("RESTAURANT_003", "경도는 -180도에서 180도 사이여야 합니다.", 400),
    INVALID_COORDINATE("RESTAURANT_004", "유효한 좌표가 필요합니다.", 400);

    private final String code;
    private final String message;
    private final int status;
}
