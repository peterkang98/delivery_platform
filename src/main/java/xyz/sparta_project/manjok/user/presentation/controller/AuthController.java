package xyz.sparta_project.manjok.user.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "회원 인증 API", description = "회원 로그인, 로그아웃, 회원가입, 비밀번호 초기화 등의 인증 기능을 제공합니다.")
@RequestMapping("/v1/auth")
@Controller
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final SignupValidator signupValidator;
	private final PasswordResetValidator resetValidator;

	@Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<?>> signUp(@Valid @RequestBody SignupRequest request) {
		signupValidator.validate(request);
		return ResponseEntity.ok(authService.signUp(request));
	}

	@Operation(summary = "로그인", description = "새로운 JWT 토큰을 발급합니다")
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<String>> logIn(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.logIn(request));
	}

	@Operation(summary = "이메일 인증", description = "회원가입 후 전송된 인증 메일의 링크를 클릭하면, 해당 링크에 포함된 토큰이 이 엔드포인트로 전달되어 이메일 인증이 완료됩니다.")
	@GetMapping("/verify-email")
	public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestParam String token) {
		return ResponseEntity.ok(authService.verifyEmailToken(token));
	}

	@Operation(
			summary = "비밀번호 초기화 검증",
			description = """
					    사용자가 '비밀번호 초기화' 이메일의 링크를 클릭하면 프론트엔드 페이지로 이동합니다. \n
					    해당 페이지에서 새 비밀번호와 비밀번호 확인 값을 입력한 뒤 제출하면, \n
					    이 API가 토큰의 유효성을 검증하고 비밀번호를 최종적으로 초기화합니다.
					    """
	)
	@PostMapping("/confirm-password-reset")
	public ResponseEntity<ApiResponse<?>> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequest request) {
		resetValidator.validate(request);
		return ResponseEntity.ok(authService.verifyResetPasswordToken(request));
	}

	@Operation(summary = "비밀번호 초기화 요청", description = "회원이 이메일을 입력하면, 해당 이메일로 비밀번호 초기화 링크를 전송합니다.")
	@PostMapping("/password-reset")
	public ResponseEntity<ApiResponse<?>> passwordReset(@Valid @RequestBody PasswordResetRequest request) {
		return ResponseEntity.ok(authService.createPasswordResetToken(request));
	}
}
