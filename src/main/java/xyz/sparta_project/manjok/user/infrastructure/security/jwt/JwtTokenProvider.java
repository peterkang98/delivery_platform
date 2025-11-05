package xyz.sparta_project.manjok.user.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

	private final SecretKey key;
	private final long accessTokenValidityInMs;

	public JwtTokenProvider(@Value("${jwt.secret}") String secret,
							@Value("${jwt.access-token-exp}") long accessTokenValidityInMs) {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.accessTokenValidityInMs = accessTokenValidityInMs;
	}

	public String generateToken(String id) {
		Date now = new Date();
		Date validity = new Date(now.getTime() + accessTokenValidityInMs);

		return Jwts.builder()
				   .subject(id)
				   .issuedAt(now)
				   .expiration(validity)
				   .signWith(key)
				   .compact();
	}

	public String getId(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean validateToken(String token) {
		parseClaims(token);
		return true;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				   .verifyWith(key)
				   .build()
				   .parseSignedClaims(token)
				   .getPayload();
	}
}
