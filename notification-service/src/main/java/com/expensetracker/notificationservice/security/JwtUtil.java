package com.expensetracker.notificationservice.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	@Value("${jwt.secret}")
	private String SECRET;

	public SecretKey getSingingKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
	}

	public String extractUsername(String token) {
		return Jwts.parser().verifyWith(getSingingKey()).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public boolean isTokenValid(String token) {
		try {
			Jwts.parser().verifyWith(getSingingKey()).build().parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public String extractRole(String token) {
	    return Jwts.parser()
	            .verifyWith(getSingingKey())
	            .build()
	            .parseSignedClaims(token)
	            .getPayload()
	            .get("role", String.class);
	}
}
