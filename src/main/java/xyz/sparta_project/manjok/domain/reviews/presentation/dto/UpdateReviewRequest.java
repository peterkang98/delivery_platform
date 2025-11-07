package xyz.sparta_project.manjok.domain.reviews.presentation.dto;

public record UpdateReviewRequest(
		double rating,
		String content
) {
}
