package xyz.sparta_project.manjok.user.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.user.application.service.AuthService;
import xyz.sparta_project.manjok.user.presentation.dto.LoginRequest;
import xyz.sparta_project.manjok.user.presentation.dto.ConfirmPasswordResetRequest;
import xyz.sparta_project.manjok.user.presentation.dto.PasswordResetRequest;
import xyz.sparta_project.manjok.user.presentation.dto.SignupRequest;
import xyz.sparta_project.manjok.user.presentation.validator.PasswordResetValidator;
import xyz.sparta_project.manjok.user.presentation.validator.SignupValidator;

@Tag(name = "회원 인증 API", description = "회원 정보 조회, 등록, 수정, 삭제 기능 제공")
@RequestMapping("/v1/auth")
@Controller
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final SignupValidator signupValidator;
	private final PasswordResetValidator resetValidator;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<?>> signUp(@Valid @RequestBody SignupRequest request) {
		signupValidator.validate(request);
		return ResponseEntity.ok(authService.signUp(request));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<String>> logIn(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.logIn(request));
	}

	@GetMapping("/verify-email")
	public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestParam String token) {
		return ResponseEntity.ok(authService.verifyEmailToken(token));
	}

	@PostMapping("/confirm-password-reset")
	public ResponseEntity<ApiResponse<?>> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequest request) {
		resetValidator.validate(request);
		return ResponseEntity.ok(authService.verifyResetPasswordToken(request));
	}

	@PostMapping("/password-reset")
	public ResponseEntity<ApiResponse<?>> passwordReset(@Valid @RequestBody PasswordResetRequest request) {
		return ResponseEntity.ok(authService.createPasswordResetToken(request));
	}
}
