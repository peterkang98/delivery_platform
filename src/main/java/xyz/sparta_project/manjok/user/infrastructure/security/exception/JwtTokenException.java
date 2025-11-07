package xyz.sparta_project.manjok.user.infrastructure.security.exception;

import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalException;

public class JwtTokenException extends GlobalException {
	public JwtTokenException(ErrorCode errorCode) {
		super(errorCode);
	}
}
