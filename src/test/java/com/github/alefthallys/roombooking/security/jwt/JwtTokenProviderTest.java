package com.github.alefthallys.roombooking.security.jwt;

import com.github.alefthallys.roombooking.exceptions.InvalidJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;
import java.util.Collections;

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
	
	@Test
	void testGenerateAndValidateToken() {
		Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());
		
		String token = jwtTokenProvider.generateToken(auth);
		assertNotNull(token);
		
		assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token));
		assertEquals("userTest@gmail.com", jwtTokenProvider.getUsernameFromToken(token));
	}
	
	@Test
	void testInvalidToken() {
		String invalidToken = "invalidTokenString";
		assertThrows(InvalidJwtException.class, () -> jwtTokenProvider.validateToken(invalidToken));
	}
	
	@Test
	void testExpiredToken() throws InterruptedException {
		JwtProperties shortExpiryProps = new JwtProperties();
		shortExpiryProps.setSecret("supersecret12345678910");
		shortExpiryProps.setExpiration(100);
		
		JwtTokenProvider shortExpiryProvider = new JwtTokenProvider(shortExpiryProps);
		shortExpiryProvider.init();
		
		Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());
		String token = shortExpiryProvider.generateToken(auth);
		
		Thread.sleep(200);
		
		assertThrows(InvalidJwtException.class, () -> jwtTokenProvider.validateToken(token));
	}
}