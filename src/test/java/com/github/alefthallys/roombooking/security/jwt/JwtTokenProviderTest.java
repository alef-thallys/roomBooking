package com.github.alefthallys.roombooking.security.jwt;

import com.github.alefthallys.roombooking.exceptions.InvalidJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
	
	private JwtTokenProvider jwtTokenProvider;
	private UserDetails userDetails;
	
	@BeforeEach
	void setUp() {
		String testSecretKey = Base64.getEncoder().encodeToString(
				Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded()
		);
		
		userDetails = User.withUsername("userTest@gmail.com")
				.password("passwordTest")
				.roles("USER")
				.build();
		
		JwtProperties jwtProperties = new JwtProperties();
		jwtProperties.setSecret(testSecretKey);
		jwtProperties.setExpiration(1000 * 60); // 1 minute
		jwtProperties.setIssuer("testIssuer");
		jwtProperties.setAudience("testAudience");
		
		jwtTokenProvider = new JwtTokenProvider(jwtProperties);
		jwtTokenProvider.init();
	}
	
	@Nested
	@DisplayName("Token Generation and Validation")
	class TokenGenerationAndValidation {
		
		@Test
		@DisplayName("Should generate a valid JWT and extract username")
		void testGenerateAndValidateToken() {
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			
			String token = jwtTokenProvider.generateToken(auth);
			assertNotNull(token, "Token should not be null");
			
			assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token), "Token should be valid");
			assertEquals("userTest@gmail.com", jwtTokenProvider.getUsernameFromToken(token), "Username should match");
		}
		
		@Test
		@DisplayName("Should throw InvalidJwtException for malformed token")
		void testInvalidToken() {
			String invalidToken = "invalidTokenString";
			assertThrows(InvalidJwtException.class, () -> jwtTokenProvider.validateToken(invalidToken));
		}
		
		@Test
		@DisplayName("Should throw InvalidJwtException for expired token")
		void testExpiredToken() throws InterruptedException {
			JwtProperties shortExpiryProps = new JwtProperties();
			String shortSecret = Base64.getEncoder().encodeToString(
					Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded()
			);
			shortExpiryProps.setSecret(shortSecret);
			shortExpiryProps.setExpiration(100);
			shortExpiryProps.setIssuer("testIssuer");
			shortExpiryProps.setAudience("testAudience");
			
			JwtTokenProvider shortExpiryProvider = new JwtTokenProvider(shortExpiryProps);
			shortExpiryProvider.init();
			
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			String token = shortExpiryProvider.generateToken(auth);
			
			Thread.sleep(200);
			
			assertThrows(InvalidJwtException.class, () -> shortExpiryProvider.validateToken(token));
		}
	}
	
	@Nested
	@DisplayName("Token Claims and Roles")
	class TokenClaimsAndRoles {
		
		@Test
		@DisplayName("Should include user role in token claims")
		void testRoleClaimInToken() throws Exception {
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			String token = jwtTokenProvider.generateToken(auth);
			
			var field = jwtTokenProvider.getClass().getDeclaredField("secretKey");
			field.setAccessible(true);
			var secretKey = (java.security.Key) field.get(jwtTokenProvider);
			
			var claims = io.jsonwebtoken.Jwts.parserBuilder()
					.setSigningKey(secretKey)
					.build()
					.parseClaimsJws(token)
					.getBody();
			
			assertEquals("ROLE_USER", claims.get("role"));
		}

		
		@Test
		@DisplayName("Should return UserDetails for authenticated context")
		void testGetAuthenticationWhenAuthenticated() {
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
			
			UserDetails details = jwtTokenProvider.getAuthentication();
			assertNotNull(details);
			assertEquals(userDetails.getUsername(), details.getUsername());
		}
	}
}