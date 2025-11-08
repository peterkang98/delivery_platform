package xyz.sparta_project.manjok.domain.reviews.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.reviews.application.ReviewService;
import xyz.sparta_project.manjok.domain.reviews.domain.entity.Review;
import xyz.sparta_project.manjok.domain.reviews.presentation.dto.CreateReviewRequest;
import xyz.sparta_project.manjok.domain.reviews.presentation.dto.UpdateReviewRequest;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;

@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	/**
	 * 가게별 리뷰 조회
	 * GET /api/reviews/restaurant/{restaurantId}
	 */
	@GetMapping("/restaurant/{restaurantId}")
	public ResponseEntity<ApiResponse<Page<Review>>> getReviewsByRestaurant(
			@PathVariable String restaurantId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "dateDesc") String sortType) {

		ApiResponse<Page<Review>> response = reviewService.getReviewsByRestaurant(restaurantId, page, size, sortType);
		return ResponseEntity.ok(response);
	}

	/**
	 * 리뷰 작성
	 * POST /api/reviews
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<?>> createReview(@RequestBody CreateReviewRequest request) {
		ApiResponse<?> response = reviewService.createReview(
				request.reviewerId(),
				request.orderId(),
				request.restaurantId(),
				request.menus(),
                request.rating(),
				request.content()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 회원별 리뷰 조회
	 * GET /api/reviews/reviewer/{reviewerId}
	 */
	@GetMapping("/reviewer/{reviewerId}")
	public ResponseEntity<ApiResponse<Page<Review>>> getReviewsByReviewer(
			@PathVariable String reviewerId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "dateDesc") String sortType) {

		ApiResponse<Page<Review>> response = reviewService.getReviewsByReviewer(reviewerId, page, size, sortType);
		return ResponseEntity.ok(response);
	}

    /**
     * 주문별 리뷰 조회
     * GET /v1/reviews/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<?>> getReviewByOrder(@PathVariable String orderId) {
        ApiResponse<?> response = reviewService.getReviewByOrder(orderId);
        return ResponseEntity.ok(response);
    }

	/**
	 * 리뷰 수정
	 * PUT /api/reviews/{reviewId}
	 */
	@PutMapping("/{reviewId}")
	public ResponseEntity<ApiResponse<?>> updateReview(
			@PathVariable String reviewId,
			@RequestBody UpdateReviewRequest request) {

		ApiResponse<?> response = reviewService.updateReview(
				reviewId,
				request.rating(),
				request.content()
		);
		return ResponseEntity.ok(response);
	}

	/**
	 * 리뷰 삭제
	 * DELETE /api/reviews/{reviewId}
	 */
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<ApiResponse<?>> deleteReview(@PathVariable String reviewId) {
		ApiResponse<?> response = reviewService.deleteReview(reviewId);
		return ResponseEntity.ok(response);
	}
}

