package xyz.sparta_project.manjok.global.infrastructure.security.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode implements ErrorCode {
	INVALID_TOKEN("AUTH_001", "유효한 토큰이 아닙니다. 다시 로그인 부탁드립니다", 401);

	private final String code;
	private final String message;
	private final int status;

}
