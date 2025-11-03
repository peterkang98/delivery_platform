package xyz.sparta_project.manjok.global.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import static xyz.sparta_project.manjok.global.infrastructure.security.jwt.JwtErrorCode.*;

@Component
public class JwtTokenProvider {

	private final SecretKey key;
	private final long accessTokenValidityInMs;
	private final long refreshTokenValidityInMs;

	public JwtTokenProvider(@Value("${jwt.secret}") String secret,
							@Value("${jwt.access-token-exp}") long accessTokenValidityInMs,
							@Value("${jwt.refresh-token-exp}") long refreshTokenValidityInMs) {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.accessTokenValidityInMs = accessTokenValidityInMs;
		this.refreshTokenValidityInMs = refreshTokenValidityInMs;
	}

	public String generateToken(String email, String type) {
		Date now = new Date();
		long tokenExpiration = type.equals("access") ? accessTokenValidityInMs : refreshTokenValidityInMs;
		Date validity = new Date(now.getTime() + tokenExpiration);

		return Jwts.builder()
				   .subject(email)
				   .issuedAt(now)
				   .expiration(validity)
				   .signWith(key)
				   .compact();
	}

	public String getEmail(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			throw new JwtTokenException(INVALID_TOKEN);
		}
	}

	public Authentication getAuthentication(String token) {
		Claims claims = parseClaims(token);

		Collection<? extends GrantedAuthority> authorities;
		if (claims.get("auth") != null) {

		} else {
			authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
		}

		UserDetails principal = new User(claims.getSubject(), "", authorities);
		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				   .verifyWith(key)
				   .build()
				   .parseSignedClaims(token)
				   .getPayload();
	}
}
