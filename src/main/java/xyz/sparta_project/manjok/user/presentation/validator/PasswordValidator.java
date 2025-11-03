package xyz.sparta_project.manjok.user.presentation.validator;

public interface PasswordValidator {
	default boolean validatePassword(String password) {
		return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,15}$");
	}
}
