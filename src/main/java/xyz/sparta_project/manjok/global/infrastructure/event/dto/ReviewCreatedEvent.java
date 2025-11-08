package xyz.sparta_project.manjok.global.infrastructure.event.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * 리뷰 생성 이벤트
 */
@Getter
@RequiredArgsConstructor
public class ReviewCreatedEvent {
    private final String reviewId;
    private final String restaurantId;
    private final BigDecimal rating;
}