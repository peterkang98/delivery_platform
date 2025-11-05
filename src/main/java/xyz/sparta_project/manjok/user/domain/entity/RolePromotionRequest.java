package xyz.sparta_project.manjok.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.user.domain.vo.PromotionRequestStatus;
import xyz.sparta_project.manjok.user.domain.vo.Role;

import java.util.List;

@Entity
@Table(name = "p_role_promotion_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RolePromotionRequest extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requester_id")
	private User requester;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer_id")
	private User reviewer;

	@Enumerated(EnumType.STRING)
	@Column(name = "current_role", nullable = false)
	private Role currentRole;

	@Enumerated(EnumType.STRING)
	@Column(name = "requested_role", nullable = false)
	private Role requestedRole;

	@Enumerated(EnumType.STRING)
	@Column(name = "request_status", nullable = false)
	private PromotionRequestStatus status = PromotionRequestStatus.PENDING;

	// 승인, 거절 사유
	private String reviewComment;

	@Column(nullable = true)
	private String businessRegistrationNum;
}
