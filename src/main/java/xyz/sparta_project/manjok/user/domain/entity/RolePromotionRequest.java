package xyz.sparta_project.manjok.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.user.domain.service.RoleCheck;
import xyz.sparta_project.manjok.user.domain.vo.PromotionRequestStatus;
import xyz.sparta_project.manjok.user.domain.vo.Role;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
		name = "p_role_promotion_request",
		indexes = @Index(name = "idx_requester_id", columnList = "requester_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RolePromotionRequest extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requester_id")
	private User requester;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
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

	// 승급 요청 사유
	private String reason;

	// 승인, 거절 사유
	private String reviewComment;

	@Column(name = "business_registration_number")
	private String businessRegistrationNum;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by", length = 36)
	private String deletedBy;

	@Column(name = "created_by", length = 36)
	private String createdBy;

	@Builder
	public RolePromotionRequest(User currentUser, Role currentRole, Role requestedRole,
								String reason, String businessRegistrationNum) {
		setRequester(currentUser);
		this.currentRole = currentRole;
		this.requestedRole = requestedRole;
		this.reason = reason;
		this.businessRegistrationNum = businessRegistrationNum;
	}

	public void setRequester(User requester) {
		this.requester = requester;
		requester.getSentRequests().add(this);
	}

	public void review(User reviewer, String reviewComment, String action) {
		this.reviewer = reviewer;
		this.reviewComment = reviewComment;

		if (action.equals("ACCEPTED")) {
			this.status = PromotionRequestStatus.ACCEPTED;
		} else {
			this.status = PromotionRequestStatus.REJECTED;
		}
	}

}
