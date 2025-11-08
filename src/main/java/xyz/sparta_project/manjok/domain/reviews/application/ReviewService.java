package xyz.sparta_project.manjok.domain.reviews.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.reviews.domain.entity.Review;
import xyz.sparta_project.manjok.domain.reviews.domain.vo.Menu;
import xyz.sparta_project.manjok.domain.reviews.exception.ReviewErrorCode;
import xyz.sparta_project.manjok.domain.reviews.exception.ReviewException;
import xyz.sparta_project.manjok.domain.reviews.infrastructure.ReviewRepository;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalErrorCode;
import xyz.sparta_project.manjok.user.domain.vo.Role;
import xyz.sparta_project.manjok.user.exception.UserException;

import java.util.List;

import static xyz.sparta_project.manjok.domain.reviews.exception.ReviewErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

	private final ReviewRepository reviewRepository;

	// 가게 기준으로 리뷰 조회
	public ApiResponse<Page<Review>> getReviewsByRestaurant(String restaurantId, int page, int size, String sortType) {
		Pageable pageable = PageRequest.of(page, size, getSortOption(sortType));
		return ApiResponse.success(reviewRepository.findByRestaurantId(restaurantId, pageable));
	}

	// 회원 기준으로 리뷰 CRUD
	public ApiResponse<?> createReview(String reviewerId, String orderId, String restaurantId, List<Menu> menus, double rating, String content) {
		Review review = Review.builder()
							  .orderId(orderId)
							  .reviewerId(reviewerId)
							  .restaurantId(restaurantId)
							  .menus(menus)
							  .content(content)
                                .rating(rating)
							  .build();

		reviewRepository.save(review);
		return ApiResponse.success(null, "리뷰 저장 성공");
	}

	public ApiResponse<Page<Review>> getReviewsByReviewer(String reviewerId, int page, int size, String sortType) {
		Pageable pageable = PageRequest.of(page, size, getSortOption(sortType));
		return ApiResponse.success(reviewRepository.findByReviewerId(reviewerId, pageable));
	}

	public ApiResponse<?> updateReview(String reviewId, double rating, String content) {
		Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewException(INVALID_REVIEW_ID));
		checkAuthorization(reviewId);
		review.editReview(rating, content);
		return ApiResponse.success(null, "리뷰 수정 성공");
	}

	public ApiResponse<?> deleteReview(String reviewId) {
		Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewException(INVALID_REVIEW_ID));
		checkAuthorization(reviewId);
		review.deleteReview(reviewId);
		return ApiResponse.success(null, "리뷰 삭제 성공");
	}

	private static void checkAuthorization(String reviewId) {
		boolean isOneself = SecurityUtils.getCurrentUserId()
									  .orElseThrow(() -> new ReviewException(GlobalErrorCode.INVALID_SECURITY_CONTEXT))
									  .equals(reviewId);
		boolean hasAuthority = SecurityUtils.getCurrentRole()
								 .orElseThrow(() -> new ReviewException(GlobalErrorCode.INVALID_SECURITY_CONTEXT))
								 .hasHigherAuthorityThan(Role.MANAGER);

		if (!isOneself && !hasAuthority) {
			throw new ReviewException(GlobalErrorCode.FORBIDDEN);
		}
	}

	private Sort getSortOption(String sortType) {
		return switch (sortType) {
			case "dateAsc" -> Sort.by(Sort.Direction.ASC, "createdAt");
			case "dateDesc" -> Sort.by(Sort.Direction.DESC, "createdAt");
			case "ratingAsc" -> Sort.by(Sort.Direction.ASC, "rating");
			case "ratingDesc" -> Sort.by(Sort.Direction.DESC, "rating");
			default -> Sort.by(Sort.Direction.DESC, "createdAt"); // 기본값: 최신순
		};
	}

    public ApiResponse<?> getReviewByOrder(String orderId) {
        // orderId로 리뷰 조회 로직
        Review review = reviewRepository.findByOrderId(orderId)
                .orElse(null);

        return ApiResponse.success(review);
    }
}
