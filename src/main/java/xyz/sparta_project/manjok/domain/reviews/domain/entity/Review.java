package xyz.sparta_project.manjok.domain.reviews.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import xyz.sparta_project.manjok.domain.reviews.domain.vo.Menu;
import xyz.sparta_project.manjok.domain.reviews.exception.ReviewException;
import xyz.sparta_project.manjok.domain.reviews.infrastructure.converter.MenuConverter;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static xyz.sparta_project.manjok.domain.reviews.exception.ReviewErrorCode.INVALID_RATING;

@Entity
@Table(name = "p_review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Review extends BaseEntity {

	@Column(nullable = false)
	private String reviewerId;

	@Column(nullable = false)
	private String orderId;

	@Column(nullable = false)
	private String restaurantId;

	@Convert(converter = MenuConverter.class)
	@Column(length=500)
	private List<Menu> menus;

	@Column(nullable = false)
	private double rating;

	@Column(nullable = false, length = 1000)
	private String content;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "updated_by", length = 36)
	private String updatedBy;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by", length = 36)
	private String deletedBy;

	@Column(name = "created_by", length = 36)
	private String createdBy;

	@Builder
	public Review(String reviewerId, String orderId, String restaurantId, String content, List<Menu> menus, double rating) {
		this.reviewerId = reviewerId;
		this.orderId = orderId;
		this.menus = menus;
		this.restaurantId = restaurantId;
		this.content = content;
		setRating(rating);
	}

	public void editReview(double rating, String content) {
		this.content = content;
		setRating(rating);
	}

	public void deleteReview(String reviewerId) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = reviewerId;
	}

	public void setRating(double rating) {
		if (rating < 1.0 || rating > 5.0) {
			throw new ReviewException(INVALID_RATING);
		}

		this.rating = rating;
	}
}
