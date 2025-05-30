package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.exceptions.Auth.ForbiddenException;
import com.github.alefthallys.roombooking.exceptions.Auth.InvalidJwtException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import com.github.alefthallys.roombooking.testBuilders.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
	
	@InjectMocks
	private AuthService authService;
	
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	
	private User ownerUser;
	private User otherUser;
	private UserDetails ownerUserDetails;
	private UserDetails otherUserDetails;
	
	@BeforeEach
	void setUp() {
		ownerUser = UserTestBuilder.anUser().withEmail("owner@example.com").withId(1L).build();
		otherUser = UserTestBuilder.anUser().withEmail("other@example.com").withId(2L).build();
		
		ownerUserDetails = org.springframework.security.core.userdetails.User.withUsername(ownerUser.getEmail())
				.password(ownerUser.getPassword())
				.roles(ownerUser.getRole().toString())
				.build();
		
		otherUserDetails = org.springframework.security.core.userdetails.User.withUsername(otherUser.getEmail())
				.password(otherUser.getPassword())
				.roles(otherUser.getRole().toString())
				.build();
	}
	
	@Nested
	@DisplayName("Validate User Ownership")
	class ValidateUserOwnership {
		
		@Test
		@DisplayName("Should not throw ForbiddenException when current user is the owner")
		void shouldNotThrowForbiddenExceptionWhenCurrentUserIsOwner() {
			when(jwtTokenProvider.getAuthentication()).thenReturn(ownerUserDetails);
			assertDoesNotThrow(() -> authService.validateUserOwnership(ownerUser));
		}
		
		@Test
		@DisplayName("Should throw ForbiddenException when current user is not the owner")
		void shouldThrowForbiddenExceptionWhenCurrentUserIsNotOwner() {
			when(jwtTokenProvider.getAuthentication()).thenReturn(otherUserDetails);
			assertThrows(ForbiddenException.class, () -> authService.validateUserOwnership(ownerUser));
		}
		
		@Test
		@DisplayName("Should throw ForbiddenException when no authenticated user is found")
		void shouldThrowForbiddenExceptionWhenNoAuthenticatedUser() {
			when(jwtTokenProvider.getAuthentication()).thenReturn(null);
			assertThrows(ForbiddenException.class, () -> authService.validateUserOwnership(ownerUser));
		}
		
		@Test
		@DisplayName("Should throw ForbiddenException when JwtTokenProvider throws InvalidJwtException")
		void shouldThrowForbiddenExceptionWhenJwtTokenProviderThrowsInvalidJwtException() {
			when(jwtTokenProvider.getAuthentication()).thenThrow(new InvalidJwtException("Invalid JWT"));
			assertThrows(ForbiddenException.class, () -> authService.validateUserOwnership(ownerUser));
		}
	}
}
