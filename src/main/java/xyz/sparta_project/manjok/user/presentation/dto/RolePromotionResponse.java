package xyz.sparta_project.manjok.user.presentation.dto;

import xyz.sparta_project.manjok.user.domain.entity.RolePromotionRequest;

import java.time.LocalDateTime;

public record RolePromotionResponse(
		String id,
		String currentRole,
		String requestedRole,
		String status,
		String reason,
		String reviewComment,
		LocalDateTime updatedAt
) {
	public static RolePromotionResponse from(RolePromotionRequest entity) {
		return new RolePromotionResponse(
				entity.getId(),
				entity.getCurrentRole().getDescription(),
				entity.getRequestedRole().getDescription(),
				entity.getStatus().getDescription(),
				entity.getReason(),
				entity.getReviewComment(),
				entity.getUpdatedAt()
		);
	}
}
