package xyz.sparta_project.manjok.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
		@Email(message = "올바른 이메일을 입력해주세요")
		@NotBlank(message = "이메일을 입력해주세요")
		String email
) { }
