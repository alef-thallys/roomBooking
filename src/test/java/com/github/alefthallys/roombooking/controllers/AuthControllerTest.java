package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.Auth.LoginRequestDTO;
import com.github.alefthallys.roombooking.dtos.Auth.RefreshTokenRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserResponseDTO;
import com.github.alefthallys.roombooking.exceptions.Auth.InvalidJwtException;
import com.github.alefthallys.roombooking.exceptions.User.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import com.github.alefthallys.roombooking.security.CustomUserDetailsService;
import com.github.alefthallys.roombooking.security.jwt.JwtAuthenticationFilter;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import com.github.alefthallys.roombooking.services.UserService;
import com.github.alefthallys.roombooking.testBuilders.UserTestBuilder;
import com.github.alefthallys.roombooking.testUtils.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
	
	private final String URL_PREFIX = TestConstants.API_V1_AUTH;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	
	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@MockitoBean
	private AuthenticationManager authenticationManager;
	
	@MockitoBean
	private UserService userService;
	
	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;
	
	@MockitoBean
	private UserRepository userRepository;
	
	private LoginRequestDTO loginRequestDTO;
	private UserRequestDTO userRequestDTO;
	private UserResponseDTO userResponseDTO;
	private String accessToken;
	private String refreshToken;
	
	@BeforeEach
	void setUp() {
		loginRequestDTO = UserTestBuilder.anUser().buildLoginRequestDTO();
		userRequestDTO = UserTestBuilder.anUser().buildRequestDTO();
		userResponseDTO = UserTestBuilder.anUser().buildResponseDTO();
		
		accessToken = "my-jwt-access-token";
		refreshToken = "my-jwt-refresh-token";
	}
	
	private void assertUserResponseDTO(ResultActions resultActions, UserResponseDTO userResponseDTO) throws Exception {
		resultActions
				.andExpect(jsonPath("$.id").value(userResponseDTO.id()))
				.andExpect(jsonPath("$.name").value(userResponseDTO.name()))
				.andExpect(jsonPath("$.email").value(userResponseDTO.email()))
				.andExpect(jsonPath("$.phone").value(userResponseDTO.phone()))
				.andExpect(jsonPath("$.role").value(userResponseDTO.role().name()));
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX + "/me")
	class GetCurrentUser {
		
		@Test
		@DisplayName("should return current user")
		void shouldReturnCurrentUserDetails() throws Exception {
			User user = UserTestBuilder.anUser().build();
			
			when(jwtTokenProvider.getCurrentUser()).thenReturn(user);
			
			mockMvc.perform(get(URL_PREFIX + "/me"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(user.getId()))
					.andExpect(jsonPath("$.name").value(user.getName()))
					.andExpect(jsonPath("$.email").value(user.getEmail()))
					.andExpect(jsonPath("$.phone").value(user.getPhone()))
					.andExpect(jsonPath("$.role").value(user.getRole().name()));
		}
		
		@Test
		@DisplayName("should return 401 when user is not authenticated")
		void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
			when(jwtTokenProvider.getCurrentUser()).thenThrow(new InvalidJwtException("No authenticated user found"));
			
			mockMvc.perform(get(URL_PREFIX + "/me"))
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.message").value("No authenticated user found"));
		}
	}
	
	@Nested
	@DisplayName("POST " + URL_PREFIX + "/register")
	class RegisterUser {
		
		private static Stream<Arguments> invalidUserRequestDTOs() {
			return Stream.of(
					Arguments.of(UserTestBuilder.anUser().withEmail("invalid-email").buildRequestDTO()),
					Arguments.of(UserTestBuilder.anUser().withName(null).buildRequestDTO()),
					Arguments.of(UserTestBuilder.anUser().withPassword(null).buildRequestDTO()),
					Arguments.of(UserTestBuilder.anUser().withPhone("").buildRequestDTO())
			);
		}
		
		@Test
		@DisplayName("should register a user")
		void shouldRegisterUser() throws Exception {
			when(userService.create(userRequestDTO)).thenReturn(userResponseDTO);
			
			assertUserResponseDTO(
					mockMvc.perform(post(URL_PREFIX + "/register")
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(userRequestDTO)))
							.andExpect(status().isCreated()),
					userResponseDTO
			);
		}
		
		@Test
		@DisplayName("should return 409 when email already exists")
		void shouldReturnBadRequestWhenRegisteringUserWithExistingEmail() throws Exception {
			when(userService.create(userRequestDTO)).thenThrow(new EntityUserAlreadyExistsException(userRequestDTO.email()));
			
			mockMvc.perform(post(URL_PREFIX + "/register")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(userRequestDTO)))
					.andExpect(status().isConflict());
		}
		
		@ParameterizedTest(name = "should return 400 when request body is invalid: {0}")
		@MethodSource("invalidUserRequestDTOs")
		@DisplayName("should return 400 when request body is invalid")
		void shouldReturnBadRequestWhenRegisteringUserWithInvalidData(UserRequestDTO invalidDto) throws Exception {
			mockMvc.perform(post(URL_PREFIX + "/register")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidDto)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
	}
	
	@Nested
	@DisplayName("POST " + URL_PREFIX + "/login")
	class LoginUser {
		
		private static Stream<Arguments> invalidLoginRequestDTOs() {
			return Stream.of(
					Arguments.of(UserTestBuilder.anUser().withEmail("invalid-email").buildLoginRequestDTO()),
					Arguments.of(UserTestBuilder.anUser().withPassword(null).buildLoginRequestDTO()),
					Arguments.of(new LoginRequestDTO(null, "password"))
			);
		}
		
		@Test
		@DisplayName("should login and return JWT and refresh token")
		void shouldLoginAndReturnJwtAndRefreshToken() throws Exception {
			Authentication authMock = mock(Authentication.class);
			when(authenticationManager.authenticate(any())).thenReturn(authMock);
			when(jwtTokenProvider.generateToken(authMock)).thenReturn(accessToken);
			when(jwtTokenProvider.generateRefreshToken(authMock)).thenReturn(refreshToken);
			
			mockMvc.perform(post(URL_PREFIX + "/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(loginRequestDTO)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.token").value(accessToken))
					.andExpect(jsonPath("$.refreshToken").value(refreshToken));
		}
		
		@ParameterizedTest(name = "should return 400 when request body is invalid: {0}")
		@MethodSource("invalidLoginRequestDTOs")
		@DisplayName("should return 400 when request body is invalid")
		void shouldReturnBadRequestWhenLoginWithInvalidData(LoginRequestDTO invalidDto) throws Exception {
			mockMvc.perform(post(URL_PREFIX + "/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidDto)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
		
		@Test
		@DisplayName("should return 401 when credentials are invalid")
		void shouldReturnUnauthorizedWhenLoginWithInvalidCredentials() throws Exception {
			when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));
			
			mockMvc.perform(post(URL_PREFIX + "/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(loginRequestDTO)))
					.andExpect(status().isUnauthorized());
		}
	}
	
	@Nested
	@DisplayName("POST " + URL_PREFIX + "/refresh-token")
	class RefreshToken {
		
		@Test
		@DisplayName("should return new JWT and refresh token when refresh token is valid")
		void shouldReturnNewJwtAndRefreshTokenWhenRefreshTokenIsValid() throws Exception {
			String userEmail = loginRequestDTO.email();
			UserDetails userDetailsMock = org.springframework.security.core.userdetails.User.withUsername(userEmail)
					.password("encodedPassword")
					.roles("USER")
					.build();
			
			when(jwtTokenProvider.getUsernameFromRefreshToken(refreshToken)).thenReturn(userEmail);
			when(customUserDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetailsMock);
			when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("new-access-token");
			when(jwtTokenProvider.generateRefreshToken(any(Authentication.class))).thenReturn("new-refresh-token");
			
			mockMvc.perform(post(URL_PREFIX + "/refresh-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO(refreshToken))))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.token").value("new-access-token"))
					.andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
		}
		
		@Test
		@DisplayName("should return 401 when refresh token is invalid")
		void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
			String invalidRefreshToken = "invalid-refresh-token";
			
			doThrow(new InvalidJwtException("Invalid refresh token")).when(jwtTokenProvider).validateRefreshToken(invalidRefreshToken);
			
			mockMvc.perform(post(URL_PREFIX + "/refresh-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO(invalidRefreshToken))))
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.message").value("Invalid refresh token"));
		}
		
		@Test
		@DisplayName("should return 400 when refresh token field is null")
		void shouldReturnBadRequestWhenRefreshTokenFieldIsNull() throws Exception {
			mockMvc.perform(post(URL_PREFIX + "/refresh-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO(null))))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
		
		@Test
		@DisplayName("should return 400 when refresh token request body is missing or malformed")
		void shouldReturnBadRequestWhenRefreshTokenRequestBodyIsMissingOrMalformed() throws Exception {
			mockMvc.perform(post(URL_PREFIX + "/refresh-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content(""))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
	}
}