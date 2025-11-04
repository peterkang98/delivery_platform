package xyz.sparta_project.manjok.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.user.domain.entity.User;
import xyz.sparta_project.manjok.user.domain.repository.UserRepository;
import xyz.sparta_project.manjok.user.exception.UserException;
import xyz.sparta_project.manjok.user.infrastructure.security.jwt.JwtTokenProvider;
import xyz.sparta_project.manjok.user.presentation.dto.LoginRequest;
import xyz.sparta_project.manjok.user.presentation.dto.SignupRequest;

import static xyz.sparta_project.manjok.user.exception.UserErrorCode.INVALID_LOGIN_EMAIL;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
	private final JwtTokenProvider tokenProvider;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;

	public ApiResponse<?> signUp(SignupRequest request) {
		User user = User.builder()
						.email(request.email())
						.password(passwordEncoder.encode(request.password()))
						.username(request.username())
						.build();

		userRepository.save(user);
		return ApiResponse.success(null, "회원가입 성공");
	}

	@Transactional(readOnly = true)
	public ApiResponse<String> logIn(LoginRequest request) {
		User foundUser = userRepository.findByEmail(request.email()).orElseThrow(() -> new UserException(INVALID_LOGIN_EMAIL));

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(foundUser.getId(), request.password());
		authenticationManager.authenticate(authToken);

		String token = tokenProvider.generateToken(foundUser.getId());
		return ApiResponse.success(token);
	}
}
