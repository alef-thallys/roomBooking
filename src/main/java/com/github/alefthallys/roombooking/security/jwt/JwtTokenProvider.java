package com.github.alefthallys.roombooking.security.jwt;

import com.github.alefthallys.roombooking.exceptions.InvalidJwtException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {
	
	private final JwtProperties jwtProperties;
	private Key secretKey;
	
	@Autowired
	private UserRepository userRepository;
	
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
	
	public UserDetails getAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean isRoleAnonymous = authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ANONYMOUS"));
		
		if (authentication.isAuthenticated() && !isRoleAnonymous) {
			return (UserDetails) authentication.getPrincipal();
		}
		return null;
	}
	
	public void validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
		} catch (Exception e) {
			throw new InvalidJwtException("JWT token is invalid");
		}
	}
	
	public User getCurrentUser() {
		UserDetails userDetails = getAuthentication();
		if (userDetails != null) {
			String username = userDetails.getUsername();
			return userRepository.findByEmail(username)
					.orElseThrow(() -> new InvalidJwtException("User not found with email: " + username));
		}
		throw new InvalidJwtException("No authenticated user found");
	}
}