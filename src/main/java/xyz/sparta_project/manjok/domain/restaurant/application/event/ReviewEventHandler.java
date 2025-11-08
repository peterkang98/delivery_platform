package xyz.sparta_project.manjok.domain.restaurant.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.ReviewCreatedEvent;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Restaurant;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandler;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 리뷰 생성 이벤트 핸들러
 * - 리뷰 생성 시 레스토랑 통계 업데이트
 * - @EventHandler 어노테이션으로 자동 등록
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EventHandler(eventType = ReviewCreatedEvent.class)
public class ReviewEventHandler implements EventHandlerProcessor<ReviewCreatedEvent> {

    private final RestaurantRepository restaurantRepository;

    /**
     * 리뷰 생성 이벤트 처리
     * - 리뷰 개수 증가
     * - 평균 평점 재계산
     */
    @Override
    @Transactional
    public void handle(ReviewCreatedEvent event) throws Exception {
        log.info("리뷰 생성 이벤트 처리 시작: reviewId={}, restaurantId={}, rating={}",
                event.getReviewId(), event.getRestaurantId(), event.getRating());

        // 1. Restaurant 조회
        Restaurant restaurant = restaurantRepository
                .findById(event.getRestaurantId())
                .orElseThrow(() -> new RestaurantException(
                        RestaurantErrorCode.RESTAURANT_NOT_FOUND,
                        "레스토랑을 찾을 수 없습니다: " + event.getRestaurantId()
                ));

        // 2. 새로운 평균 평점 계산
        int currentCount = restaurant.getReviewCount();
        BigDecimal currentRating = restaurant.getReviewRating();
        BigDecimal newRating = event.getRating();

        BigDecimal updatedRating = calculateNewAverageRating(
                currentRating,
                currentCount,
                newRating
        );

        // 3. 리뷰 통계 업데이트
        restaurant.updateReviewStats(currentCount + 1, updatedRating);

        log.debug("리뷰 통계 업데이트: restaurantId={}, reviewCount={} -> {}, rating={} -> {}",
                event.getRestaurantId(),
                currentCount, currentCount + 1,
                currentRating, updatedRating);

        // 4. 변경사항 저장 (더티체킹)
        restaurantRepository.save(restaurant);

        log.info("리뷰 생성 이벤트 처리 성공: reviewId={}, restaurantId={}, newReviewCount={}, newRating={}",
                event.getReviewId(), event.getRestaurantId(), currentCount + 1, updatedRating);
    }

    /**
     * 새로운 평균 평점 계산
     *
     * @param currentRating 현재 평균 평점
     * @param currentCount 현재 리뷰 개수
     * @param newRating 새 리뷰 평점
     * @return 업데이트된 평균 평점 (소수점 2자리)
     */
    private BigDecimal calculateNewAverageRating(BigDecimal currentRating,
                                                 int currentCount,
                                                 BigDecimal newRating) {
        if (currentCount == 0) {
            return newRating;
        }

        try {
            // (현재 평균 * 현재 개수 + 새 평점) / (현재 개수 + 1)
            BigDecimal totalRating = currentRating
                    .multiply(BigDecimal.valueOf(currentCount))
                    .add(newRating);

            return totalRating
                    .divide(BigDecimal.valueOf(currentCount + 1), 2, RoundingMode.HALF_UP);

        } catch (ArithmeticException e) {
            log.error("평점 계산 중 오류 발생: currentRating={}, currentCount={}, newRating={}",
                    currentRating, currentCount, newRating, e);
            throw new RestaurantException(
                    RestaurantErrorCode.STATISTICS_UPDATE_FAILED,
                    "평점 계산 중 오류 발생",
                    e
            );
        }
    }
}