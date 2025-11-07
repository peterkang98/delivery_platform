package xyz.sparta_project.manjok.user;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import xyz.sparta_project.manjok.user.domain.entity.User;
import xyz.sparta_project.manjok.user.domain.vo.Role;
import xyz.sparta_project.manjok.user.infrastructure.security.userdetails.CustomUserDetails;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class WithMockUserSecurityContextFactory implements WithSecurityContextFactory<MockUser> {
	@Override
	public SecurityContext createSecurityContext(MockUser annotation) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();

		// Mock User 엔티티 생성
		User user = createMockUser(annotation);

		// CustomUserDetails 생성
		CustomUserDetails principal = new CustomUserDetails(user);

		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(
						principal,
						principal.getPassword(),
						principal.getAuthorities()
				);

		context.setAuthentication(authentication);
		return context;
	}

	private User createMockUser(MockUser annotation) {
		// Builder로 기본 필드 설정
		User user = User.builder()
						.username(annotation.username())
						.email(annotation.email())
						.password(annotation.password())
						.build();

		// Reflection을 사용해서 private 필드들 설정
		try {
			// ID 설정
			setField(user, "id", annotation.id());

			// Role 설정
			setField(user, "role", Role.fromAuthority(annotation.role()));

			// isVerified 설정
			setField(user, "isVerified", annotation.isVerified());

			// isPublic 설정
			setField(user, "isPublic", annotation.isPublic());

			// isDeleted 설정 (deletedAt으로 판단)
			if (annotation.isDeleted()) {
				setField(user, "deletedAt", LocalDateTime.now());
				setField(user, "deletedBy", "test-system");
			}

			// createdAt 설정
			setField(user, "createdAt", LocalDateTime.now());

		} catch (Exception e) {
			throw new RuntimeException("Failed to create mock user", e);
		}

		return user;
	}

	private void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = findField(target.getClass(), fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}

	private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			if (clazz.getSuperclass() != null) {
				return findField(clazz.getSuperclass(), fieldName);
			}
			throw e;
		}
	}
}
