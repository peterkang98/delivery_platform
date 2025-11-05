package xyz.sparta_project.manjok.user;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserSecurityContextFactory.class)
public @interface MockUser {
	String id() default "a2166e2e-0c94-4624-b3a1-e8cf15b6ef05";

	String email() default "test@example.com";

	String username() default "testuser";

	String password() default "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"; // "password"

	String role() default "ROLE_CUSTOMER";

	boolean isVerified() default true;

	boolean isPublic() default true;

	boolean isDeleted() default false;
}
