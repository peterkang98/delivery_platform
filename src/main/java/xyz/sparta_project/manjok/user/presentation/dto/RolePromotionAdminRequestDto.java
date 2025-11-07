package xyz.sparta_project.manjok.user.presentation.dto;

public record RolePromotionAdminRequestDto (
		String requestId,
		String requestUserId,
		String action,
		String reviewComment
){ }
