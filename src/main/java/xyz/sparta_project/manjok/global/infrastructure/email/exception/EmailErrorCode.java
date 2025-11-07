package xyz.sparta_project.manjok.global.infrastructure.email.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum EmailErrorCode implements ErrorCode {
	EMAIL_ERROR("EMAIL_001", "이메일 전송 실패", 500),
	INVALID_EMAIL_TYPE("EMAIL_002", "알 수 없는 이메일 타입", 500);

	private final String code;
	private final String message;
	private final int status;
}
