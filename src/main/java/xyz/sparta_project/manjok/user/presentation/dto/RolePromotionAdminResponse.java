package xyz.sparta_project.manjok.user.presentation.dto;

import xyz.sparta_project.manjok.user.domain.entity.RolePromotionRequest;
import xyz.sparta_project.manjok.user.domain.entity.User;

import java.time.LocalDateTime;

public record RolePromotionAdminResponse(
		String id,
		String requesterUsername,
		String requesterEmail,
		String currentRole,
		String requestedRole,
		String status,
		String reason,
		String businessRegistrationNum,
		String reviewComment,
		LocalDateTime createdAt
) {
	public static RolePromotionAdminResponse from(RolePromotionRequest entity) {
		User user = entity.getRequester();
		return new RolePromotionAdminResponse(
				entity.getId(),
				user.getUsername(),
				user.getEmail(),
				entity.getCurrentRole().getDescription(),
				entity.getRequestedRole().getDescription(),
				entity.getStatus().getDescription(),
				entity.getReason(),
				entity.getBusinessRegistrationNum(),
				entity.getReviewComment(),
				entity.getCreatedAt()
		);
	}
}

