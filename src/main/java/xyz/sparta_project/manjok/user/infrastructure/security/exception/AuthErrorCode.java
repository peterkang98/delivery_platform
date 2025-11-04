package xyz.sparta_project.manjok.user.infrastructure.security.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	ACCOUNT_LOCKED("AUTH_001", "계정이 잠겨있습니다.", 423),
	ACCOUNT_DISABLED("AUTH_002", "비활성화된 계정입니다.", 403),
	ACCOUNT_EXPIRED("AUTH_003", "만료된 계정입니다.", 403),
	CREDENTIALS_EXPIRED("AUTH_004", "비밀번호가 만료되었습니다.", 403),
	INVALID_CREDENTIALS("AUTH_005", "이메일 또는 비밀번호가 올바르지 않습니다.", 401),
	AUTHENTICATION_FAILED("AUTH_006", "인증에 실패했습니다.", 401);

	private final String code;
	private final String message;
	private final int status;
}
