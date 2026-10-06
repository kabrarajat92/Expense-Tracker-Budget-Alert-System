package com.expensetracker.userservice.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	
	@Value("${jwt.secret}")
	private String SECRET;
//	private final String SECRET = "gTeNw43tYY30lsYUR9/5FtzrjQBw1mqpXtYZKPU7M90=";
    private final long EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours
    
    public SecretKey getSingingKey() {
    	return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }
    
    public String generateToken(String username, String role, String email) {
    	return Jwts.builder()
    				.subject(username)
    				.claim("role", role)
    				.claim("email", email)
    				.issuedAt(new Date())
    				.expiration(new Date(System.currentTimeMillis() + EXPIRATION))
    				.signWith(getSingingKey(),SignatureAlgorithm.HS256)
    				.compact();
    }
    
    public String extractUsername(String token) {
    	return Jwts.parser()
    				.verifyWith(getSingingKey())
    				.build()
    				.parseSignedClaims(token)
    				.getPayload()
    				.getSubject();
    }
    
    public boolean isTokenValid(String token) {
    	try {
    		Jwts.parser().verifyWith(getSingingKey()).build().parseSignedClaims(token);
    		return true;
    	}catch(Exception e) {
    		return false;
    	}
    }
}
