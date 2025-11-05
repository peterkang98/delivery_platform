// FavoriteErrorCode.java
package xyz.sparta_project.manjok.domain.favorites.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

/**
 * 찜하기 도메인 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum FavoriteErrorCode implements ErrorCode {

    // 생성 관련 (FAVORITE_001~009)
    INVALID_CUSTOMER_ID("FAVORITE_001", "유효하지 않은 고객 ID입니다.", 400),
    INVALID_FAVORITE_TYPE("FAVORITE_002", "유효하지 않은 찜하기 타입입니다.", 400),
    INVALID_RESTAURANT_ID("FAVORITE_003", "유효하지 않은 레스토랑 ID입니다.", 400),
    INVALID_MENU_ID("FAVORITE_004", "유효하지 않은 메뉴 ID입니다.", 400),
    MENU_ID_REQUIRED("FAVORITE_005", "메뉴 타입인 경우 메뉴 ID는 필수입니다.", 400),
    MENU_ID_NOT_ALLOWED("FAVORITE_006", "레스토랑 타입인 경우 메뉴 ID는 포함될 수 없습니다.", 400),

    // 중복 관련 (FAVORITE_010~019)
    ALREADY_FAVORITED("FAVORITE_010", "이미 찜한 항목입니다.", 409),
    DUPLICATE_FAVORITE("FAVORITE_011", "중복된 찜하기입니다.", 409),

    // 조회 관련 (FAVORITE_020~029)
    FAVORITE_NOT_FOUND("FAVORITE_020", "찜하기를 찾을 수 없습니다.", 404),

    // 권한 관련 (FAVORITE_030~039)
    FORBIDDEN_FAVORITE_ACCESS("FAVORITE_030", "본인의 찜하기만 접근할 수 있습니다.", 403),
    FORBIDDEN_FAVORITE_DELETE("FAVORITE_031", "본인의 찜하기만 삭제할 수 있습니다.", 403),

    // 저장/삭제 관련 (FAVORITE_040~049)
    FAVORITE_SAVE_FAILED("FAVORITE_040", "찜하기 저장에 실패했습니다.", 500),
    FAVORITE_DELETE_FAILED("FAVORITE_041", "찜하기 삭제에 실패했습니다.", 500),

    // 이벤트 처리 (FAVORITE_050~059)
    EVENT_PROCESSING_FAILED("FAVORITE_050", "이벤트 처리 중 오류가 발생했습니다.", 500);

    private final String code;
    private final String message;
    private final int status;
}