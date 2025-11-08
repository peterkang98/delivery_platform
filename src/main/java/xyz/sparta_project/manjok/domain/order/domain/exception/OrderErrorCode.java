package xyz.sparta_project.manjok.domain.order.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

/**
 * 주문 도메인 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    // 생성 관련 (ORDER_001~009)
    INVALID_ORDERER("ORDER_001", "유효하지 않은 주문자 정보입니다.", 400),
    INVALID_ORDER_ITEMS("ORDER_002", "주문 항목이 비어있습니다.", 400),
    INVALID_PAYMENT("ORDER_003", "유효하지 않은 결제 정보입니다.", 400),
    INVALID_ORDER_ITEM_QUANTITY("ORDER_004", "주문 수량은 1개 이상이어야 합니다.", 400),
    INVALID_PRICE("ORDER_005", "가격은 0보다 커야 합니다.", 400),
    INVALID_ADDRESS("ORDER_006", "유효하지 않은 주소 정보입니다.", 400),
    INVALID_COORDINATE("ORDER_007", "유효하지 않은 좌표 정보입니다.", 400),

    // 조회 관련 (ORDER_020~029)
    ORDER_NOT_FOUND("ORDER_020", "주문을 찾을 수 없습니다.", 404),

    // 권한 관련 (ORDER_030~039)
    FORBIDDEN_ORDER_ACCESS("ORDER_030", "본인의 주문만 접근할 수 있습니다.", 403),
    FORBIDDEN_ORDER_CANCEL("ORDER_031", "본인의 주문만 취소할 수 있습니다.", 403),

    // 상태 전환 관련 (ORDER_040~059)
    INVALID_STATUS_TRANSITION("ORDER_040", "유효하지 않은 주문 상태 전환입니다.", 400),
    PAYMENT_NOT_COMPLETED("ORDER_041", "결제가 완료되지 않았습니다.", 400),
    ALREADY_CONFIRMED("ORDER_042", "이미 확인된 주문입니다.", 400),
    ALREADY_PREPARING("ORDER_043", "이미 조리 중인 주문입니다.", 400),
    ALREADY_DELIVERING("ORDER_044", "이미 배달 중인 주문입니다.", 400),
    ALREADY_COMPLETED("ORDER_045", "이미 완료된 주문입니다.", 400),
    ALREADY_CANCELED("ORDER_046", "이미 취소된 주문입니다.", 400),
    CANNOT_CANCEL_ORDER("ORDER_047", "취소할 수 없는 주문 상태입니다.", 400),
    CANCEL_REASON_REQUIRED("ORDER_048", "취소 사유는 필수입니다.", 400),

    // 저장/처리 관련 (ORDER_060~069)
    ORDER_SAVE_FAILED("ORDER_060", "주문 저장에 실패했습니다.", 500),
    ORDER_UPDATE_FAILED("ORDER_061", "주문 수정에 실패했습니다.", 500),
    ORDER_DELETE_FAILED("ORDER_062", "주문 삭제에 실패했습니다.", 500),

    // 이벤트 처리 (ORDER_070~079)
    EVENT_PROCESSING_FAILED("ORDER_070", "이벤트 처리 중 오류가 발생했습니다.", 500);

    private final String code;
    private final String message;
    private final int status;
}