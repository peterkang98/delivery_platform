package xyz.sparta_project.manjok.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.user.domain.entity.User;
import xyz.sparta_project.manjok.user.domain.entity.VerificationToken;
import xyz.sparta_project.manjok.user.domain.repository.UserRepository;
import xyz.sparta_project.manjok.user.domain.repository.VerificationRepository;
import xyz.sparta_project.manjok.user.domain.vo.TokenType;
import xyz.sparta_project.manjok.user.exception.UserException;
import xyz.sparta_project.manjok.user.infrastructure.email.EmailService;
import xyz.sparta_project.manjok.user.infrastructure.security.jwt.JwtTokenProvider;
import xyz.sparta_project.manjok.user.presentation.dto.LoginRequest;
import xyz.sparta_project.manjok.user.presentation.dto.ConfirmPasswordResetRequest;
import xyz.sparta_project.manjok.user.presentation.dto.SignupRequest;

import static xyz.sparta_project.manjok.user.exception.UserErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
	private final JwtTokenProvider tokenProvider;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final EmailService emailService;
	private final VerificationRepository verificationRepository;

	public ApiResponse<?> signUp(SignupRequest request) {
		User user = User.builder()
						.email(request.email())
						.password(passwordEncoder.encode(request.password()))
						.username(request.username())
						.build();

		VerificationToken token = VerificationToken.builder()
												   .tokenType(TokenType.EMAIL_VERIFICATION)
												   .user(user)
												   .build();
		userRepository.save(user);
		verificationRepository.save(token);
		emailService.sendVerificationEmail(request.email(), token.getId());

		return ApiResponse.success(null, "회원가입 성공! 본인 확인용 이메일을 보냈습니다.");
	}

	@Transactional(readOnly = true)
	public ApiResponse<String> logIn(LoginRequest request) {
		User foundUser = getUser(request.email());

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(foundUser.getId(), request.password());
		authenticationManager.authenticate(authToken);

		String token = tokenProvider.generateToken(foundUser.getId());
		return ApiResponse.success(token);
	}

	public ApiResponse<?> verifyEmailToken(String tokenValue) {
		VerificationToken token = verifyToken(tokenValue, TokenType.EMAIL_VERIFICATION);
		User user = token.getUser();
		user.verify();

		token.markAsUsed();

		userRepository.save(user);

		return ApiResponse.success(null, "이메일 인증 성공");
	}

	public ApiResponse<?> createPasswordResetToken(SignupRequest request) {
		User foundUser = getUser(request.email());
		VerificationToken token = VerificationToken.builder()
												   .tokenType(TokenType.PASSWORD_RESET)
												   .user(foundUser)
												   .build();
		verificationRepository.save(token);
		emailService.sendPasswordResetEmail(request.email(), token.getId());

		return ApiResponse.success(null, "비밀번호 초기화 이메일 전송 완료. 1시간 이내에 초기화 부탁드립니다.");
	}

	public ApiResponse<?> verifyResetPasswordToken(ConfirmPasswordResetRequest request) {
		VerificationToken token = verifyToken(request.token(), TokenType.PASSWORD_RESET);

		User user = token.getUser();
		user.updatePassword(request.newPassword());

		token.markAsUsed();

		return ApiResponse.success(null, "비밀번호 초기화 성공");
	}

	private User getUser(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new UserException(INVALID_LOGIN_EMAIL));
	}

	private VerificationToken verifyToken(String tokenValue, TokenType type) {
		VerificationToken token = verificationRepository.findByIdAndTokenType(tokenValue, type)
														.orElseThrow(() -> new UserException(VERIFICATION_TOKEN_ERROR));

		if (token.isUsed()) {
			throw new UserException(USED_VERIFICATION_TOKEN_ERROR);
		}

		if (token.isExpired()) {
			throw new UserException(EXPIRED_VERIFICATION_TOKEN_ERROR);
		}

		return token;
	}
}
