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
    INVALID_COORDINATE("RESTAURANT_004", "유효한 좌표가 필요합니다.", 400),

    // Restaurant 관련 에러 (RESTAURANT_010~029)
    RESTAURANT_NAME_REQUIRED("RESTAURANT_010", "레스토랑명은 필수입니다.", 400),
    OWNER_REQUIRED("RESTAURANT_011", "소유자 정보는 필수입니다.", 400),
    INVALID_ADDRESS("RESTAURANT_012", "유효한 주소가 필요합니다.", 400),
    RESTAURANT_NOT_FOUND("RESTAURANT_013", "레스토랑을 찾을 수 없습니다.", 404),
    RESTAURANT_ALREADY_DELETED("RESTAURANT_014", "이미 삭제된 레스토랑입니다.", 400),
    CANNOT_MODIFY_MENU_WHILE_OPEN("RESTAURANT_015", "영업 중에는 메뉴를 수정할 수 없습니다.", 400),

    // OperatingDay 관련 에러 (RESTAURANT_030~039)
    OPERATING_DAY_NOT_FOUND("RESTAURANT_030", "운영시간 정보를 찾을 수 없습니다.", 404),
    INVALID_OPERATING_TIME("RESTAURANT_031", "유효하지 않은 운영시간입니다.", 400),
    BREAK_TIME_OUT_OF_OPERATING_TIME("RESTAURANT_032", "브레이크 타임은 운영시간 내에 있어야 합니다.", 400);

    private final String code;
    private final String message;
    private final int status;
}
