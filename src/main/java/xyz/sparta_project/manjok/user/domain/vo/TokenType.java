package xyz.sparta_project.manjok.user.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
	EMAIL_VERIFICATION(24),      // 24시간
	PASSWORD_RESET(1);           // 1시간

	private final int expiryHours;
}
