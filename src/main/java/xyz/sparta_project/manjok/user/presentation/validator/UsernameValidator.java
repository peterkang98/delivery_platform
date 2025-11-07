package xyz.sparta_project.manjok.user.presentation.validator;

public interface UsernameValidator {
	default boolean validateUsername(String username) {
		return username.matches("^[a-z0-9]{4,10}$");
	}
}
