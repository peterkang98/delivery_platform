package xyz.sparta_project.manjok.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
	@NotBlank(message = "이메일을 입력해주세요")
	String email,

	@NotBlank(message = "비밀번호를 입력해주세요")
	String password
) {}
