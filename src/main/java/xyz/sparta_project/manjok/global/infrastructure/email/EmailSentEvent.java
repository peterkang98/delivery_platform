package xyz.sparta_project.manjok.global.infrastructure.email;

import lombok.Getter;

@Getter
public class EmailSentEvent {
	private final String to;
	private final String token;
	private final EmailType emailType;

	private EmailSentEvent(String to, String token, EmailType emailType) {
		this.to = to;
		this.token = token;
		this.emailType = emailType;
	}

	/**
	 * 이메일 인증 이벤트 생성
	 */
	public static EmailSentEvent createVerificationEvent(String to, String token) {
		return new EmailSentEvent(to, token, EmailType.VERIFICATION);
	}

	/**
	 * 비밀번호 재설정 이벤트 생성
	 */
	public static EmailSentEvent createPasswordResetEvent(String to, String token) {
		return new EmailSentEvent(to, token, EmailType.PASSWORD_RESET);
	}

	/**
	 * 이메일 타입
	 */
	public enum EmailType {
		VERIFICATION,      // 이메일 인증
		PASSWORD_RESET     // 비밀번호 재설정
	}
}
