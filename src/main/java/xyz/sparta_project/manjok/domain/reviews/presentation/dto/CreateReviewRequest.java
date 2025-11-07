package xyz.sparta_project.manjok.domain.reviews.presentation.dto;

import xyz.sparta_project.manjok.domain.reviews.domain.vo.Menu;

import java.util.List;

public record CreateReviewRequest(
		String reviewerId,
		String orderId,
		String restaurantId,
		List<Menu> menus,
		String content
) {
}
