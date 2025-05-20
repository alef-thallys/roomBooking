package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.LoginRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.exceptions.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.security.jwt.JwtAuthenticationFilter;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import com.github.alefthallys.roombooking.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
	
	private final String URL_PREFIX = "/api/v1/auth";
	
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
	
	private LoginRequestDTO loginRequestDTO;
	private UserRequestDTO userRequestDTO;
	private UserResponseDTO userResponseDTO;
	private String token;
	
	@BeforeEach
	void setUp() {
		loginRequestDTO = new LoginRequestDTO(
				"john@gmail.com",
				"12997665045"
		);
		
		userRequestDTO = new UserRequestDTO(
				"John Doe",
				"john@gmail.com",
				"password",
				"12997665045"
		);
		
		userResponseDTO = new UserResponseDTO(
				1L,
				"John Doe",
				"john@gmail.com",
				"12997665045",
				User.Role.ROLE_USER
		);
		
		token = "my-jwt-token";
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
	@DisplayName("POST /api/v1/auth/register")
	class RegisterUser {
		
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
		
		@Test
		@DisplayName("should return 400 when request body is invalid")
		void shouldReturnBadRequestWhenRegisteringUserWithInvalidData() throws Exception {
			userRequestDTO = new UserRequestDTO(
					"John Doe",
					"invalid-email",
					"password",
					"12997665045"
			);
			
			mockMvc.perform(post(URL_PREFIX + "/register")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(userRequestDTO)))
					.andExpect(status().isBadRequest());
		}
	}
	
	@Nested
	@DisplayName("POST /api/v1/auth/login")
	class LoginUser {
		
		@Test
		@DisplayName("should login and return JWT token")
		void shouldLoginAndReturnJwtToken() throws Exception {
			when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
			when(jwtTokenProvider.generateToken(any())).thenReturn(token);
			
			mockMvc.perform(post(URL_PREFIX + "/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(loginRequestDTO)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.token").value(token));
		}
		
		@Test
		@DisplayName("should return 400 when request body is invalid")
		void shouldReturnBadRequestWhenLoginWithInvalidData() throws Exception {
			mockMvc.perform(post(URL_PREFIX + "/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(new LoginRequestDTO(
									"invalid-email",
									"password"
							))))
					.andExpect(status().isBadRequest());
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
}