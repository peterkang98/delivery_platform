package xyz.sparta_project.manjok.user.infrastructure.security;

import org.springframework.stereotype.Service;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.user.domain.service.RoleCheck;

import java.util.List;

@Service
public class SecurityRoleCheck implements RoleCheck {
	@Override
	public boolean check() {
		return SecurityUtils.getCurrentUserDetails()
							.map(userDetails -> userDetails.getAuthorities().stream().anyMatch(a -> List.of("ROLE_MASTER", "ROLE_MANAGER").contains(a.getAuthority())))
							.orElse(false);
	}
}
