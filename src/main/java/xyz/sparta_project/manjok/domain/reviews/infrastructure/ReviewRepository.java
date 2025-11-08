package xyz.sparta_project.manjok.domain.reviews.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sparta_project.manjok.domain.reviews.domain.entity.Review;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {
	Page<Review> findByRestaurantId(String restaurantId, Pageable pageable);

	Page<Review> findByReviewerId(String reviewerId, Pageable pageable);

    Optional<Review> findByOrderId(String orderId);
}
