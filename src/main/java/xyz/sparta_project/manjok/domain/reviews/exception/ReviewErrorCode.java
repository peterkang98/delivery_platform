package xyz.sparta_project.manjok.domain.reviews.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {
	INVALID_RATING("REVIEW_001", "리뷰 평점은 1.0 ~ 5.0만 가능합니다", 400),
	INVALID_REVIEW_ID("REVIEW_002", "존재하지 않는 리뷰입니다.", 404);

	private final String code;
	private final String message;
	private final int status;
}
