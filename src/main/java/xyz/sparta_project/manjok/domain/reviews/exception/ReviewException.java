package xyz.sparta_project.manjok.domain.reviews.exception;

import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalException;

public class ReviewException extends GlobalException {
	public ReviewException(ErrorCode errorCode) {
		super(errorCode);
	}
}
