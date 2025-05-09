package com.github.alefthallys.roombooking.security.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
	
	private JwtTokenProvider jwtTokenProvider;
	
	@BeforeEach
	void setUp() {
		String testSecretKey = Base64.getEncoder().encodeToString(
				Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded()
		);
		
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
		Authentication auth = new UsernamePasswordAuthenticationToken("usuarioTeste", null, Collections.emptyList());
		
		String token = jwtTokenProvider.generateToken(auth);
		assertNotNull(token);
		
		assertTrue(jwtTokenProvider.validateToken(token));
		assertEquals("usuarioTeste", jwtTokenProvider.getUsernameFromToken(token));
	}
	
	@Test
	void testInvalidToken() {
		String invalidToken = "isso.nao.e.um.token.valido";
		
		assertFalse(jwtTokenProvider.validateToken(invalidToken));
	}
	
	@Test
	void testExpiredToken() throws InterruptedException {
		JwtProperties shortExpiryProps = new JwtProperties();
		shortExpiryProps.setSecret("supersecret12345678910");
		shortExpiryProps.setExpiration(100);
		
		JwtTokenProvider shortExpiryProvider = new JwtTokenProvider(shortExpiryProps);
		shortExpiryProvider.init();
		
		Authentication auth = new UsernamePasswordAuthenticationToken("userExpired", null, Collections.emptyList());
		String token = shortExpiryProvider.generateToken(auth);
		
		Thread.sleep(200);
		
		assertFalse(shortExpiryProvider.validateToken(token));
	}
}