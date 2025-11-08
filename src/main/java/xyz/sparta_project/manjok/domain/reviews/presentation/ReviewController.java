package xyz.sparta_project.manjok.domain.reviews.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "리뷰 API", description = "회원 및 가게 리뷰 조회, 작성, 수정, 삭제 기능 제공")
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@Operation(summary = "가게별 리뷰 조회", description = "특정 가게에 대한 리뷰 목록을 조회합니다. (페이징, 정렬 지원)")
	@GetMapping("/restaurant/{restaurantId}")
	public ResponseEntity<ApiResponse<Page<Review>>> getReviewsByRestaurant(
			@PathVariable String restaurantId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "dateDesc") String sortType) {

		ApiResponse<Page<Review>> response = reviewService.getReviewsByRestaurant(restaurantId, page, size, sortType);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "리뷰 작성", description = "회원이 주문 완료 후 리뷰를 작성합니다.")
	@PostMapping
	public ResponseEntity<ApiResponse<?>> createReview(@RequestBody CreateReviewRequest request) {
		ApiResponse<?> response = reviewService.createReview(
				request.reviewerId(),
				request.orderId(),
				request.restaurantId(),
				request.menus(),
				request.content()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "회원별 리뷰 조회", description = "특정 회원이 작성한 리뷰 목록을 조회합니다. (페이징, 정렬 지원)")
	@GetMapping("/reviewer/{reviewerId}")
	public ResponseEntity<ApiResponse<Page<Review>>> getReviewsByReviewer(
			@PathVariable String reviewerId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "dateDesc") String sortType) {

		ApiResponse<Page<Review>> response = reviewService.getReviewsByReviewer(reviewerId, page, size, sortType);
		return ResponseEntity.ok(response);
	}


	@Operation(summary = "리뷰 수정", description = "리뷰 ID에 해당하는 리뷰를 수정합니다. 평점과 내용을 수정할 수 있고 본인 또는 담당자만 수정 가능")
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

	@Operation(summary = "리뷰 삭제", description = "리뷰 ID에 해당하는 리뷰를 삭제합니다. 본인 또는 담당자만 삭제 가능")
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<ApiResponse<?>> deleteReview(@PathVariable String reviewId) {
		ApiResponse<?> response = reviewService.deleteReview(reviewId);
		return ResponseEntity.ok(response);
	}
}

