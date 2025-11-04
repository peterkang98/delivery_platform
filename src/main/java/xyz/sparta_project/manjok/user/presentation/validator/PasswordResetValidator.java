package xyz.sparta_project.manjok.user.presentation.validator;

import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.user.exception.UserException;
import xyz.sparta_project.manjok.user.presentation.dto.PasswordResetRequest;

import static xyz.sparta_project.manjok.user.exception.UserErrorCode.INVALID_SIGNUP_CONFIRM_PASSWORD;
import static xyz.sparta_project.manjok.user.exception.UserErrorCode.INVALID_SIGNUP_PASSWORD;

@Component
public class PasswordResetValidator implements PasswordValidator{
	public void validate(PasswordResetRequest request) {
		String password = request.newPassword();
		String confirmPassword = request.confirmPassword();

		if (!password.equals(confirmPassword)) {
			throw new UserException(INVALID_SIGNUP_CONFIRM_PASSWORD);
		}
		if (!validatePassword(password)) {
			throw new UserException(INVALID_SIGNUP_PASSWORD);
		}
	}
}
