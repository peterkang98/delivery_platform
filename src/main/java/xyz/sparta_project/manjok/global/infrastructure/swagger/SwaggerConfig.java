package xyz.sparta_project.manjok.global.infrastructure.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
	@Bean
	public GroupedOpenApi userApi() {
		return GroupedOpenApi.builder()
							 .group("user-api")
							 .displayName("회원 API")
							 .pathsToMatch(
									 "/v1/users", "/v1/users/**", "/v1/auth/**",
									 "/v1/role-promotion-requests", "/v1/admin/role-promotion-requests"
							 )
							 .build();
	}

	@Bean
	public GroupedOpenApi reviewApi() {
		return GroupedOpenApi.builder()
							 .group("review-api")
							 .displayName("리뷰 API")
							 .pathsToMatch("/v1/reviews", "/v1/reviews/**")
							 .build();
	}

	@Bean
	public GroupedOpenApi aiPromptApi() {
		return GroupedOpenApi.builder()
							 .group("AI-prompt-api")
							 .displayName("AI 프롬프트 API")
							 .pathsToMatch("/v1/aiprompt/**", "/v1/owners/aiprompt/**", "/v1/customers/aiprompt/**")
							 .build();
	}

	@Bean
	public GroupedOpenApi restaurantCommonApi() {
		return GroupedOpenApi.builder()
							 .group("restaurant-api")
							 .displayName("가게 API")
							 .pathsToMatch(
									 "/v1/common/restaurants/**", "/v1/owners/restaurants/**",
									 "/v1/customers/restaurants/**", "/v1/admin/restaurants/**"
							 )
							 .build();
	}

	@Bean
	public GroupedOpenApi paymentCustomerApi() {
		return GroupedOpenApi.builder()
							 .group("payment-api")
							 .displayName("결제 API")
							 .pathsToMatch("/v1/customers/payments/**", "/api/v1/owner/payments/**", "/v1/admin/payments/**")
							 .build();
	}

	@Bean
	public GroupedOpenApi favoriteApi() {
		return GroupedOpenApi.builder()
							 .group("favorites-api")
							 .displayName("찜하기 API")
							 .pathsToMatch(
									 "/api/v1/favorites/**",
									 "/v1/common/favorites/**",
									 "/v1/customers/favorites/**"
							 )
							 .build();
	}

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI().info(new Info().title("배달의 만족 REST API")
											.description("""
													  '배달의 만족'은 고객과 음식점을 연결하는 배달 플랫폼입니다.\n
													  회원가입, 가게 조회, 주문, 결제, 리뷰 관리 등 다양한 기능을 제공합니다. \n
													  본 API 문서는 클라이언트 및 서버 개발자를 위한 엔드포인트 명세를 포함합니다.
													  """)
											.version("1.0"))
							.components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
							.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}
}
