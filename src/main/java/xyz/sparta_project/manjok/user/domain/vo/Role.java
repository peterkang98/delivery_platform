package xyz.sparta_project.manjok.user.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
	CUSTOMER("ROLE_CUSTOMER", "고객"),
	OWNER("ROLE_OWNER", "점주"),
	MANAGER("ROLE_MANAGER", "서비스 담당자"),
	MASTER("ROLE_MASTER", "최종관리자");

	private final String authority;
	private final String description;

	public boolean hasHigherAuthorityThan(Role other) {
		return this.ordinal() > other.ordinal();
	}

	public boolean canApproveRoleUpgrade() {
		return this == MANAGER || this == MASTER;
	}
}
