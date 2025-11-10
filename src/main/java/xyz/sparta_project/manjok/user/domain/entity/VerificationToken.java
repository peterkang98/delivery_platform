package xyz.sparta_project.manjok.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.user.domain.vo.TokenType;

import java.time.LocalDateTime;

@Entity
@Table(name = "p_verification_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationToken extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "token_type", nullable = false, length = 30)
	private TokenType tokenType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private boolean used = false;

	@Column(name = "expiry_date", nullable = false)
	private LocalDateTime expiryDate;

	@Builder
	public VerificationToken(TokenType tokenType, User user){
		this.tokenType = tokenType;
		this.user = user;
		this.expiryDate = LocalDateTime.now().plusHours(tokenType.getExpiryHours());
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiryDate);
	}

	public void markAsUsed() {
		this.used = true;
	}
}
