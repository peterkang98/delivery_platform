package xyz.sparta_project.manjok.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest (
	@NotBlank(message = "닉네임을 입력해주세요")
	@Size(min=4, max = 10, message = "아이디는 4~10자여야 합니다.")
	String username,

	@NotBlank(message = "비밀번호를 입력해주세요")
	@Size(min=8, max = 15, message = "비밀번호는 8~15자여야 합니다.")
	String password,

	@NotBlank(message = "비밀번호를 다시 입력해주세요")
	String confirmPassword,

	@Email(message = "올바른 이메일을 입력해주세요")
	@NotBlank(message = "이메일을 입력해주세요")
	String email
) {}
