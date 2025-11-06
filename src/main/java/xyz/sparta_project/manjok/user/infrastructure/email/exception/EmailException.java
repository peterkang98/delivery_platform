package xyz.sparta_project.manjok.user.infrastructure.email.exception;

import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalException;

public class EmailException extends GlobalException {
	public EmailException(ErrorCode errorCode) {
		super(errorCode);
	}
}
