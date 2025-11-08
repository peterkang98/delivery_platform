package xyz.sparta_project.manjok.domain.payment.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

/**
 * 결제 도메인 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    // 생성 관련 (PAYMENT_001~019)
    INVALID_ORDER_ID("PAYMENT_001", "유효하지 않은 주문 ID입니다.", 400),
    INVALID_ORDERER_ID("PAYMENT_002", "유효하지 않은 주문자 ID입니다.", 400),
    INVALID_TOSS_PAYMENT_KEY("PAYMENT_003", "유효하지 않은 토스 결제 키입니다.", 400),
    INVALID_PAY_TOKEN("PAYMENT_004", "유효하지 않은 결제 토큰입니다.", 400),
    INVALID_AMOUNT("PAYMENT_005", "결제 금액은 0보다 커야 합니다.", 400),
    INVALID_PAYMENT_METHOD("PAYMENT_006", "유효하지 않은 결제 수단입니다.", 400),
    INVALID_PAYMENT_ID("PAYMENT_007", "유효하지 않은 결제 ID입니다.", 400),

    // 조회 관련 (PAYMENT_020~029)
    PAYMENT_NOT_FOUND("PAYMENT_020", "결제 정보를 찾을 수 없습니다.", 404),

    // 권한 관련 (PAYMENT_030~039)
    FORBIDDEN_PAYMENT_ACCESS("PAYMENT_030", "본인의 결제만 접근할 수 있습니다.", 403),
    FORBIDDEN_PAYMENT_CANCEL("PAYMENT_031", "본인의 결제만 취소할 수 있습니다.", 403),

    // 상태 전환 관련 (PAYMENT_040~059)
    INVALID_PAYMENT_STATUS("PAYMENT_040", "유효하지 않은 결제 상태입니다.", 400),
    CANNOT_CANCEL_PAYMENT("PAYMENT_041", "취소할 수 없는 결제 상태입니다.", 400),
    ALREADY_APPROVED("PAYMENT_042", "이미 승인된 결제입니다.", 400),
    ALREADY_FAILED("PAYMENT_043", "이미 실패한 결제입니다.", 400),
    ALREADY_CANCELLED("PAYMENT_044", "이미 취소된 결제입니다.", 400),

    // 취소 관련 (PAYMENT_060~079)
    INVALID_CANCELLATION_TYPE("PAYMENT_060", "유효하지 않은 취소 유형입니다.", 400),
    INVALID_CANCEL_AMOUNT("PAYMENT_061", "취소 금액은 0보다 커야 합니다.", 400),
    CANCEL_AMOUNT_EXCEEDS_REMAINING("PAYMENT_062", "취소 금액이 남은 금액을 초과합니다.", 400),
    CANCEL_REASON_REQUIRED("PAYMENT_063", "취소 사유는 필수입니다.", 400),
    CANCEL_REASON_TOO_LONG("PAYMENT_064", "취소 사유는 500자를 초과할 수 없습니다.", 400),
    INVALID_REQUESTER("PAYMENT_065", "유효하지 않은 요청자입니다.", 400),
    PAYMENT_CANCEL_FAILED("PAYMENT_066", "결제 취소에 실패했습니다.", 500),

    // 검증 관련 (PAYMENT_080~089)
    AMOUNT_MISMATCH("PAYMENT_080", "결제 금액이 주문 금액과 일치하지 않습니다.", 400),
    PAYMENT_VERIFICATION_FAILED("PAYMENT_081", "결제 검증에 실패했습니다.", 400),
    TOSS_API_ERROR("PAYMENT_082", "토스 API 호출 중 오류가 발생했습니다.", 500),

    // 저장/처리 관련 (PAYMENT_090~099)
    PAYMENT_SAVE_FAILED("PAYMENT_090", "결제 저장에 실패했습니다.", 500),
    PAYMENT_UPDATE_FAILED("PAYMENT_091", "결제 수정에 실패했습니다.", 500),
    CANNOT_DELETE_PAYMENT("PAYMENT_092", "삭제할 수 없는 결제 상태입니다.", 400),
    PAYMENT_DELETE_FAILED("PAYMENT_093", "결제 삭제에 실패했습니다.", 500),

    // 이벤트 처리 (PAYMENT_100~109)
    EVENT_PROCESSING_FAILED("PAYMENT_100", "이벤트 처리 중 오류가 발생했습니다.", 500),
    TOSS_API_CONNECTION_FAILED("PAYMENT_101", "토스 API 연결에 실패했습니다.", 500),
    TOSS_API_UNAUTHORIZED("PAYMENT_102", "토스 API 인증에 실패했습니다.", 500),
    TOSS_API_PAYMENT_NOT_FOUND("PAYMENT_103", "토스에서 결제를 찾을 수 없습니다.", 500),
    TOSS_PAYMENT_AMOUNT_MISMATCH("PAYMENT_104", "결제 금액이 일치하지 않습니다.", 500),
    TOSS_ORDER_ID_MISMATCH("PAYMENT_105", "주문 ID가 일치하지 않습니다.", 500),
    TOSS_PAYMENT_NOT_COMPLETED("PAYMENT_106", "토스 결제가 완료되지 않았습니다.", 500);

    private final String code;
    private final String message;
    private final int status;
}