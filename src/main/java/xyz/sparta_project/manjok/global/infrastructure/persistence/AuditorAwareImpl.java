package xyz.sparta_project.manjok.global.infrastructure.persistence;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import xyz.sparta_project.manjok.user.infrastructure.security.userdetails.CustomUserDetails;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
			return Optional.of("SYSTEM");
		}
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		return Optional.of(userDetails.getUsername());
	}
}
