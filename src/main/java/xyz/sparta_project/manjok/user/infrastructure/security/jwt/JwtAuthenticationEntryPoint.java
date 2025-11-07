package xyz.sparta_project.manjok.user.infrastructure.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.global.presentation.dto.ErrorResponse;

import java.io.IOException;

import static xyz.sparta_project.manjok.user.infrastructure.security.exception.JwtErrorCode.*;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

		// Filter에서 저장한 예외 가져오기
		JwtException jwtException = (JwtException) request.getAttribute("exception");

		ErrorResponse errorResponse;

		if (jwtException != null) {
			errorResponse = ErrorResponse.of(INVALID_TOKEN, request.getRequestURI());
			response.setStatus(INVALID_TOKEN.getStatus());
		} else {
			// 일반 인증 예외
			errorResponse = ErrorResponse.of(
					"UNAUTHORIZED",
					"인증이 필요합니다.",
					HttpStatus.UNAUTHORIZED.value(),
					request.getRequestURI()
			);
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
		}

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}

