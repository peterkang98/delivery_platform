package xyz.sparta_project.manjok.domain.restaurant.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

/**
 * 레스토랑 도메인 관련 에러 코드
 */
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
    DUPLICATE_RESTAURANT_NAME("RESTAURANT_016", "동일한 이름의 레스토랑이 이미 존재합니다.", 400),
    NOT_RESTAURANT_OWNER("RESTAURANT_017", "해당 레스토랑의 소유자가 아닙니다.", 403),
    ADDRESS_REQUIRED("RESTAURANT_018", "주소는 필수입니다.", 400),

    // OperatingDay 관련 에러 (RESTAURANT_030~039)
    OPERATING_DAY_NOT_FOUND("RESTAURANT_030", "운영시간 정보를 찾을 수 없습니다.", 404),
    INVALID_OPERATING_TIME("RESTAURANT_031", "유효하지 않은 운영시간입니다.", 400),
    BREAK_TIME_OUT_OF_OPERATING_TIME("RESTAURANT_032", "브레이크 타임은 운영시간 내에 있어야 합니다.", 400),

    // Category 관련 에러 (RESTAURANT_040~049)
    CATEGORY_NOT_FOUND("RESTAURANT_040", "카테고리를 찾을 수 없습니다.", 404),
    CATEGORY_NOT_AVAILABLE("RESTAURANT_041", "사용할 수 없는 카테고리입니다.", 400),
    CATEGORY_ALREADY_EXISTS("RESTAURANT_042", "이미 존재하는 카테고리입니다.", 409),
    INVALID_CATEGORY_DEPTH("RESTAURANT_043", "유효하지 않은 카테고리 계층입니다.", 400),

    // Event 처리 에러 (RESTAURANT_050~059)
    EVENT_PROCESSING_FAILED("RESTAURANT_050", "이벤트 처리 중 오류가 발생했습니다.", 500),
    ORDER_EVENT_PROCESSING_FAILED("RESTAURANT_051", "주문 이벤트 처리 중 오류가 발생했습니다.", 500),
    REVIEW_EVENT_PROCESSING_FAILED("RESTAURANT_052", "리뷰 이벤트 처리 중 오류가 발생했습니다.", 500),
    WISHLIST_EVENT_PROCESSING_FAILED("RESTAURANT_053", "찜 이벤트 처리 중 오류가 발생했습니다.", 500),
    STATISTICS_UPDATE_FAILED("RESTAURANT_054", "통계 업데이트 중 오류가 발생했습니다.", 500),

    // Relation 관련 에러 (RESTAURANT_060~069)
    RELATION_NOT_FOUND("RESTAURANT_060", "관계 정보를 찾을 수 없습니다.", 404),
    RELATION_NOT_BELONG_TO_RESTAURANT("RESTAURANT_061", "해당 관계는 이 레스토랑에 속하지 않습니다.", 403);


    private final String code;
    private final String message;
    private final int status;
}