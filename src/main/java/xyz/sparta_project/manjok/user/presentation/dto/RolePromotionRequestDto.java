package xyz.sparta_project.manjok.user.presentation.dto;

import jakarta.validation.constraints.NotNull;
import xyz.sparta_project.manjok.user.domain.vo.Role;

public record RolePromotionRequestDto(
		@NotNull
		Role requestedRole,
		String reason,
		String businessRegistrationNum
) { }
