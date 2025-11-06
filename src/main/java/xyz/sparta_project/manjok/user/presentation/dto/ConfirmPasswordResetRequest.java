package xyz.sparta_project.manjok.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmPasswordResetRequest(
		String token,

		@NotBlank(message = "비밀번호를 입력해주세요")
		@Size(min=8, max = 15, message = "비밀번호는 8~15자여야 합니다.")
		String newPassword,

		@NotBlank(message = "비밀번호를 다시 입력해주세요")
		String confirmPassword
){}
