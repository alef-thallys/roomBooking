package com.github.alefthallys.roombooking.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {
	
	private final JwtProperties jwtProperties;
	private Key secretKey;
	
	public JwtTokenProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}
	
	@PostConstruct
	protected void init() {
		byte[] decodedKey = Base64.getEncoder().encode(jwtProperties.getSecret().getBytes());
		this.secretKey = Keys.hmacShaKeyFor(decodedKey);
	}
	
	public String generateToken(Authentication authentication) {
		String email = authentication.getName();
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());
		
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		
		return Jwts.builder()
				.setSubject(email)
				.claim("role", userDetails.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority)
						.findFirst()
						.orElse("ROLE_USER"))
				.setIssuer(jwtProperties.getIssuer())
				.setAudience(jwtProperties.getAudience())
				.setIssuedAt(now)
				.setExpiration(expiry)
				.signWith(secretKey)
				.compact();
	}
	
	public String getUsernameFromToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}
	
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}