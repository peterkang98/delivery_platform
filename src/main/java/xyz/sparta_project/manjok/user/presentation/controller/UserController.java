package xyz.sparta_project.manjok.user.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.user.application.service.AuthService;
import xyz.sparta_project.manjok.user.presentation.dto.LoginRequest;
import xyz.sparta_project.manjok.user.presentation.dto.SignupRequest;
import xyz.sparta_project.manjok.user.presentation.validator.SignupValidator;

@RequestMapping("/v1/auth")
@Controller
@RequiredArgsConstructor
public class UserController {
	private final AuthService authService;
	private final SignupValidator signupValidator;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<?>> signUp(@Valid @RequestBody SignupRequest request) {
		signupValidator.validate(request);
		return ResponseEntity.ok(authService.signUp(request));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<String>> logIn(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.logIn(request));
	}
}
