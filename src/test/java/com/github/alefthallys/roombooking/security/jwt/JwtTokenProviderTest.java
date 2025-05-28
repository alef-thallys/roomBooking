package com.github.alefthallys.roombooking.security.jwt;

import com.github.alefthallys.roombooking.exceptions.InvalidJwtException;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
	
	private static final long DEFAULT_EXPIRATION_MILLIS = 1000 * 60; // 1 minute
	@InjectMocks
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private UserRepository userRepository;
	@Mock
	private Clock clock;
	@Mock
	private JwtProperties jwtProperties;
	private UserDetails userDetails;
	private Key testSecretKey;
	
	@BeforeEach
	void setUp() {
		testSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		String secretString = Base64.getEncoder().encodeToString(testSecretKey.getEncoded());
		
		Mockito.lenient().when(jwtProperties.getSecret()).thenReturn(secretString);
		Mockito.lenient().when(jwtProperties.getRefreshSecret()).thenReturn(secretString);
		Mockito.lenient().when(jwtProperties.getExpiration()).thenReturn(DEFAULT_EXPIRATION_MILLIS);
		Mockito.lenient().when(jwtProperties.getIssuer()).thenReturn("testIssuer");
		Mockito.lenient().when(jwtProperties.getAudience()).thenReturn("testAudience");
		
		userDetails = org.springframework.security.core.userdetails.User.withUsername("userTest@gmail.com")
				.password("passwordTest")
				.roles("USER")
				.build();
		
		Mockito.lenient().when(clock.instant()).thenReturn(Instant.now());
		Mockito.lenient().when(clock.millis()).thenReturn(Instant.now().toEpochMilli());
		
		jwtTokenProvider.init();
		
		SecurityContextHolder.clearContext();
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
		void testExpiredToken() {
			Instant fixedInstant = Instant.parse("2023-01-01T10:00:00Z");
			
			JwtProperties shortExpiryProps = new JwtProperties();
			Key localTestSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
			shortExpiryProps.setSecret(Base64.getEncoder().encodeToString(localTestSecretKey.getEncoded()));
			shortExpiryProps.setRefreshSecret(Base64.getEncoder().encodeToString(localTestSecretKey.getEncoded()));
			shortExpiryProps.setExpiration(100);
			shortExpiryProps.setIssuer("testIssuer");
			shortExpiryProps.setAudience("testAudience");
			
			JwtTokenProvider tokenGeneratorProvider = new JwtTokenProvider(shortExpiryProps, userRepository, Clock.fixed(fixedInstant, ZoneId.of("UTC")));
			tokenGeneratorProvider.init();
			
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			String token = tokenGeneratorProvider.generateToken(auth);
			
			Clock movedClock = Clock.fixed(fixedInstant.plusMillis(200), ZoneId.of("UTC"));
			
			JwtTokenProvider validationProvider = new JwtTokenProvider(shortExpiryProps, userRepository, movedClock);
			validationProvider.init();
			
			assertThrows(InvalidJwtException.class, () -> validationProvider.validateToken(token), "Token should be expired");
		}
	}
	
	@Nested
	@DisplayName("Token Claims and Roles")
	class TokenClaimsAndRoles {
		
		@Test
		@DisplayName("Should include user role in token claims")
		void testRoleClaimInToken() {
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			String token = jwtTokenProvider.generateToken(auth);
			
			var claims = io.jsonwebtoken.Jwts.parserBuilder()
					.setSigningKey(testSecretKey)
					.build()
					.parseClaimsJws(token)
					.getBody();
			
			assertEquals("ROLE_USER", claims.get("role"));
		}
		
		@Test
		@DisplayName("Should include multiple user roles in token claims (if applicable)")
		void testMultipleRoleClaimsInToken() {
			Collection<? extends GrantedAuthority> authorities = Arrays.asList(
					new SimpleGrantedAuthority("ROLE_ADMIN"),
					new SimpleGrantedAuthority("ROLE_USER")
			);
			UserDetails adminUserDetails = org.springframework.security.core.userdetails.User.withUsername("admin@gmail.com")
					.password("password")
					.authorities(authorities)
					.build();
			
			Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminUserDetails, null, adminUserDetails.getAuthorities());
			String token = jwtTokenProvider.generateToken(adminAuth);
			
			var claims = io.jsonwebtoken.Jwts.parserBuilder()
					.setSigningKey(testSecretKey)
					.build()
					.parseClaimsJws(token)
					.getBody();
			
			assertEquals("ROLE_ADMIN", claims.get("role"));
		}
		
		@Test
		@DisplayName("Should return UserDetails for authenticated context")
		void testGetAuthenticationWhenAuthenticated() {
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
			
			UserDetails details = jwtTokenProvider.getAuthentication();
			assertNotNull(details);
			assertEquals(userDetails.getUsername(), details.getUsername());
			
			SecurityContextHolder.clearContext();
		}
		
		@Test
		@DisplayName("Should return null when no authenticated user in context")
		void testGetAuthenticationWhenNotAuthenticated() {
			SecurityContextHolder.clearContext();
			UserDetails details = jwtTokenProvider.getAuthentication();
			assertNull(details);
		}
		
		@Test
		@DisplayName("Should return null when authenticated user has ROLE_ANONYMOUS")
		void testGetAuthenticationWhenAnonymous() {
			Collection<? extends GrantedAuthority> authorities = List.of(
					new SimpleGrantedAuthority("ROLE_ANONYMOUS")
			);
			UserDetails anonymousUserDetails = org.springframework.security.core.userdetails.User.withUsername("anonymous@example.com")
					.password("N/A")
					.authorities(authorities)
					.build();
			Authentication anonymousAuth = new UsernamePasswordAuthenticationToken(anonymousUserDetails, null, anonymousUserDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
			
			UserDetails details = jwtTokenProvider.getAuthentication();
			assertNull(details);
			
			SecurityContextHolder.clearContext();
		}
	}
	
	@Nested
	@DisplayName("Current User Retrieval")
	class CurrentUserRetrieval {
		
		@BeforeEach
		void setupCurrentUserTests() {
			SecurityContextHolder.clearContext();
		}
		
		@Test
		@DisplayName("Should return current user when authenticated")
		void shouldReturnCurrentUserWhenAuthenticated() {
			com.github.alefthallys.roombooking.models.User mockUser = new com.github.alefthallys.roombooking.models.User();
			mockUser.setEmail("userTest@gmail.com");
			mockUser.setId(1L);
			mockUser.setName("Test User");
			mockUser.setRole(com.github.alefthallys.roombooking.models.User.Role.ROLE_USER);
			
			UserDetails authenticatedUserDetails = org.springframework.security.core.userdetails.User.withUsername("userTest@gmail.com")
					.password("passwordTest")
					.roles("USER")
					.build();
			
			Authentication auth = new UsernamePasswordAuthenticationToken(authenticatedUserDetails, null, authenticatedUserDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
			
			when(userRepository.findByEmail("userTest@gmail.com")).thenReturn(Optional.of(mockUser));
			
			com.github.alefthallys.roombooking.models.User currentUser = jwtTokenProvider.getCurrentUser();
			assertNotNull(currentUser);
			assertEquals("userTest@gmail.com", currentUser.getEmail());
			assertEquals(1L, currentUser.getId());
		}
		
		@Test
		@DisplayName("Should throw InvalidJwtException when no authenticated user")
		void shouldThrowExceptionWhenNoAuthenticatedUser() {
			SecurityContextHolder.clearContext();
			assertThrows(InvalidJwtException.class, () -> jwtTokenProvider.getCurrentUser());
		}
		
		@Test
		@DisplayName("Should throw InvalidJwtException when user not found in repository")
		void shouldThrowExceptionWhenUserNotFoundInRepository() {
			UserDetails authenticatedUserDetails = org.springframework.security.core.userdetails.User.withUsername("nonexistent@gmail.com")
					.password("passwordTest")
					.roles("USER")
					.build();
			
			Authentication auth = new UsernamePasswordAuthenticationToken(authenticatedUserDetails, null, authenticatedUserDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
			
			when(userRepository.findByEmail("nonexistent@gmail.com")).thenReturn(Optional.empty());
			
			assertThrows(InvalidJwtException.class, () -> jwtTokenProvider.getCurrentUser());
		}
	}
}