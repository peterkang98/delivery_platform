package xyz.sparta_project.manjok.domain.restaurant.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.global.presentation.exception.ErrorCode;

/**
 * 레스토랑 도메인 관련 에러 코드
 * */
@Getter
@RequiredArgsConstructor
public enum RestaurantErrorCode implements ErrorCode {

    PLACEHOLDER("RESTAURANT_000", "임시 에러 코드입니다.", 500);

    private final String code;
    private final String message;
    private final int status;
}
