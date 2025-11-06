package xyz.sparta_project.manjok.user.infrastructure.security.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode implements ErrorCode {
	INVALID_TOKEN("JWT_001", "유효하지 않은 토큰입니다. 다시 로그인해주세요", 401);

	private final String code;
	private final String message;
	private final int status;
}
