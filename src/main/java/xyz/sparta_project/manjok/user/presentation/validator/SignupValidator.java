package xyz.sparta_project.manjok.user.presentation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.user.domain.repository.UserRepository;
import xyz.sparta_project.manjok.user.exception.UserException;
import xyz.sparta_project.manjok.user.presentation.dto.SignupRequest;

import static xyz.sparta_project.manjok.user.exception.UserErrorCode.*;

@Component
@RequiredArgsConstructor
public class SignupValidator implements UsernameValidator, PasswordValidator{

	private final UserRepository userRepository;

	public void validate(SignupRequest req) {
		String email = req.email();
		String username = req.username();
		String password = req.password();
		String confirmPassword = req.confirmPassword();

		if (!password.equals(confirmPassword)) {
			throw new UserException(INVALID_SIGNUP_CONFIRM_PASSWORD);
		}

		if (!validateUsername(username)) {
			throw new UserException(INVALID_SIGNUP_USERNAME);
		}

		if (!validatePassword(password)) {
			throw new UserException(INVALID_SIGNUP_PASSWORD);
		}

		if (userRepository.existsByEmail(email)) {
			throw new UserException(DUPLICATE_SIGNUP_EMAIL);
		}

		if (userRepository.existsByUsername(username)) {
			throw new UserException(DUPLICATE_SIGNUP_USERNAME);
		}
	}
}
