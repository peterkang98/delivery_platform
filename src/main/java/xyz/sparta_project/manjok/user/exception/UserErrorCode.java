package xyz.sparta_project.manjok.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
	INVALID_USERNAME("USER_001", "닉네임은 4~10자의 알파벳 소문자와 숫자만 가능합니다", 400),
	INVALID_PASSWORD("USER_002", "비밀번호는 8~15자이며 알파벳의 대문자와 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.", 400),
	INVALID_CONFIRM_PASSWORD("USER_003", "처음 입력한 비밀번호와 재입력 비밀번호가 불일치합니다.", 400),
	DUPLICATE_USERNAME("USER_004", "이미 존재하는 닉네임입니다", 400),
	DUPLICATE_EMAIL("USER_005", "이미 존재하는 이메일입니다.", 400);

	private final String code;
	private final String message;
	private final int status;
}
