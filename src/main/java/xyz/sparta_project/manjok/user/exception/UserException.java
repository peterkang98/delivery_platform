package xyz.sparta_project.manjok.user.exception;

import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalException;

public class UserException extends GlobalException {
	public UserException(ErrorCode errorCode) {
		super(errorCode);
	}
}
