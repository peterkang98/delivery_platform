package xyz.sparta_project.manjok.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
	INVALID_SIGNUP_USERNAME("USER_001", "닉네임은 4~10자의 알파벳 소문자와 숫자만 가능합니다", 400),
	INVALID_SIGNUP_PASSWORD("USER_002", "비밀번호는 8~15자이며 알파벳의 대문자와 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.", 400),
	INVALID_SIGNUP_CONFIRM_PASSWORD("USER_003", "처음 입력한 비밀번호와 재입력 비밀번호가 불일치합니다.", 400),
	DUPLICATE_SIGNUP_USERNAME("USER_004", "이미 존재하는 닉네임입니다", 400),
	DUPLICATE_SIGNUP_EMAIL("USER_005", "이미 존재하는 이메일입니다.", 400),
	INVALID_LOGIN_EMAIL("USER_006", "이메일이 존재하지 않습니다", 404),
	INVALID_USER_ID("USER_007", "존재하지 않는 사용자 ID입니다.", 404),
	UNAUTHORIZED_USER("USER_008", "다른 사용자의 정보를 수정할 수 없습니다", 403),
	VERIFICATION_TOKEN("TOKEN_002", "유효하지 않은 검증 토큰입니다", 404),
	USED_VERIFICATION_TOKEN("TOKEN_003", "이미 검증된 토큰입니다", 409),
	EXPIRED_VERIFICATION_TOKEN("TOKEN_004", "만료된 검증 토큰입니다", 410),
	EXPIRED_REFRESH_TOKEN("TOKEN_005", "갱신 토큰이 만료되었습니다. 다시 로그인 부탁드립니다.", 410);

	private final String code;
	private final String message;
	private final int status;
}
