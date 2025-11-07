package xyz.sparta_project.manjok.user.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@Slf4j
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

	private static final Map<String, Role> AUTHORITY_MAP = Arrays.stream(values()).collect(Collectors.toMap(Role::getAuthority, r -> r));

	public static Role fromAuthority(String authority) {
		Role role = AUTHORITY_MAP.get(authority);
		if (role == null) {
			log.error("DB에서 유효하지 않은 권한이 발견됨");
		}
		return role;
	}
}
