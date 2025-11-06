package xyz.sparta_project.manjok.global.infrastructure.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import xyz.sparta_project.manjok.user.domain.vo.Role;
import xyz.sparta_project.manjok.user.infrastructure.security.userdetails.CustomUserDetails;

import java.util.Optional;

public class SecurityUtils {
	public static Optional<CustomUserDetails> getCurrentUserDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
			return Optional.empty();
		}
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		return Optional.of(userDetails);
	}

	public static Optional<String> getCurrentUserId() {
		return getCurrentUserDetails().map(CustomUserDetails::getUsername);
	}

	public static Optional<Role> getCurrentRole() {
		return getCurrentUserDetails().flatMap(userDetails -> userDetails.getAuthorities()
																		 .stream()
																		 .findFirst()
																		 .map(GrantedAuthority::getAuthority)
																		 .map(Role::fromAuthority));
	}
}
