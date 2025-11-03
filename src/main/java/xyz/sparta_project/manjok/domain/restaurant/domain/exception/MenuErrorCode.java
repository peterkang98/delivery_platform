package xyz.sparta_project.manjok.domain.restaurant.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

/**
 * 메뉴 도메인 관련 에러 코드
 * 최소 구현을 위한 필수 에러만 정의
 */
@Getter
@RequiredArgsConstructor
public enum MenuErrorCode implements ErrorCode {

    // Menu 관련 에러 (MENU_001~019)
    MENU_NAME_REQUIRED("MENU_001", "메뉴명은 필수입니다.", 400),
    MENU_PRICE_REQUIRED("MENU_002", "메뉴 가격은 필수입니다.", 400),
    INVALID_MENU_PRICE("MENU_003", "메뉴 가격은 0원 이상이어야 합니다.", 400),
    MENU_NOT_FOUND("MENU_004", "메뉴를 찾을 수 없습니다.", 404),
    MENU_ALREADY_DELETED("MENU_005", "이미 삭제된 메뉴입니다.", 400),

    // MenuCategory 관련 에러 (MENU_020~039)
    CATEGORY_NAME_REQUIRED("MENU_020", "카테고리명은 필수입니다.", 400),
    CATEGORY_NOT_FOUND("MENU_021", "카테고리를 찾을 수 없습니다.", 404),
    INVALID_CATEGORY_DEPTH("MENU_022", "카테고리 계층은 3단계를 초과할 수 없습니다.", 400),
    CIRCULAR_CATEGORY_REFERENCE("MENU_023", "카테고리 순환 참조가 발생했습니다.", 400),

    // MenuOptionGroup 관련 에러 (MENU_040~059)
    OPTION_GROUP_NAME_REQUIRED("MENU_040", "옵션 그룹명은 필수입니다.", 400),
    INVALID_MAX_SELECTION("MENU_041", "최대 선택 가능 수는 최소 선택 수보다 작을 수 없습니다.", 400),
    OPTION_GROUP_NOT_FOUND("MENU_042", "옵션 그룹을 찾을 수 없습니다.", 404),

    // MenuOption 관련 에러 (MENU_060~079)
    OPTION_NAME_REQUIRED("MENU_060", "옵션명은 필수입니다.", 400),
    INVALID_OPTION_PRICE("MENU_061", "옵션 가격은 음수일 수 없습니다.", 400),
    OPTION_NOT_FOUND("MENU_062", "옵션을 찾을 수 없습니다.", 404),
    REQUIRED_OPTION_NOT_SELECTED("MENU_063", "필수 옵션을 선택해야 합니다.", 400);

    private final String code;
    private final String message;
    private final int status;
}
