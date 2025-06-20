package com.github.alefthallys.roombooking.security.jwt;

import com.github.alefthallys.roombooking.exceptions.Auth.InvalidJwtException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Clock;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
	
	private final JwtProperties jwtProperties;
	private final Clock clock;
	private final UserRepository userRepository;
	private Key secretKey;
	private Key refreshSecretKey;
	
	@Autowired
	public JwtTokenProvider(JwtProperties jwtProperties, UserRepository userRepository) {
		this(jwtProperties, userRepository, Clock.systemUTC());
	}
	
	public JwtTokenProvider(JwtProperties jwtProperties, UserRepository userRepository, Clock clock) {
		this.jwtProperties = jwtProperties;
		this.userRepository = userRepository;
		this.clock = clock;
	}
	
	@PostConstruct
	protected void init() {
		try {
			byte[] decodedKeyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
			this.secretKey = Keys.hmacShaKeyFor(decodedKeyBytes);
			byte[] decodedRefreshKeyBytes = Base64.getDecoder().decode(jwtProperties.getRefreshSecret());
			this.refreshSecretKey = Keys.hmacShaKeyFor(decodedRefreshKeyBytes);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Failed to decode JWT secret or refresh secret from Base64. " +
					"Please check 'jwt.secret' and 'jwt.refreshSecret' in application.yml.", e);
		}
	}
	
	public String generateToken(Authentication authentication) {
		String email = authentication.getName();
		Date now = Date.from(clock.instant());
		Date expiry = Date.from(clock.instant().plusMillis(jwtProperties.getExpiration()));
		
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		
		return Jwts.builder()
				.setSubject(email)
				.claim("role", userDetails.getAuthorities().stream()
						.map(auth -> auth.getAuthority().replace("ROLE_", "")) // remove prefixo
						.findFirst()
						.orElse("USER"))
				.setIssuer(jwtProperties.getIssuer())
				.setAudience(jwtProperties.getAudience())
				.setIssuedAt(now)
				.setExpiration(expiry)
				.signWith(secretKey, SignatureAlgorithm.HS256)
				.compact();
	}
	
	public String generateRefreshToken(Authentication authentication) {
		String email = authentication.getName();
		Date now = Date.from(clock.instant());
		Date expiry = Date.from(clock.instant().plusMillis(jwtProperties.getRefreshExpiration()));
		
		return Jwts.builder()
				.setSubject(email)
				.setId(UUID.randomUUID().toString())
				.claim("type", "refresh")
				.setIssuer(jwtProperties.getIssuer())
				.setAudience(jwtProperties.getAudience())
				.setIssuedAt(now)
				.setExpiration(expiry)
				.signWith(refreshSecretKey, SignatureAlgorithm.HS256)
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
	
	public String getUsernameFromRefreshToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(refreshSecretKey)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}
	
	public UserDetails getAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}
		
		boolean isRoleAnonymous = authentication.getAuthorities().stream()
				.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ANONYMOUS"));
		
		if (!isRoleAnonymous) {
			return (UserDetails) authentication.getPrincipal();
		}
		return null;
	}
	
	public void validateToken(String token) {
		try {
			Jwts.parserBuilder()
					.setSigningKey(secretKey)
					.build()
					.parseClaimsJws(token);
		} catch (Exception e) {
			throw new InvalidJwtException("JWT token is invalid", e);
		}
	}
	
	public void validateRefreshToken(String token) {
		try {
			Jwts.parserBuilder()
					.setSigningKey(refreshSecretKey)
					.build()
					.parseClaimsJws(token);
		} catch (Exception e) {
			throw new InvalidJwtException("Refresh token is invalid", e);
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