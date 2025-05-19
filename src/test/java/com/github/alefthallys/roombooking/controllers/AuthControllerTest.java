package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.LoginRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.security.jwt.JwtAuthenticationFilter;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import com.github.alefthallys.roombooking.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
	
	private final String urlPrefix = "/api/v1/auth";
	
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
	
	private UserRequestDTO userRequestDTO;
	private UserResponseDTO userResponseDTO;
	private User user;
	
	@BeforeEach
	void setUp() {
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
		
		user = new User();
		user.setId(1L);
		user.setName("John Doe");
		user.setEmail("john@gmail.com");
		user.setPhone("12997665045");
		user.setRole(User.Role.ROLE_USER);
	}
	
	@Test
	void shouldRegisterUser() throws Exception {
		when(userService.create(userRequestDTO)).thenReturn(userResponseDTO);
		
		mockMvc.perform(post(urlPrefix + "/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequestDTO)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(userResponseDTO.id()))
				.andExpect(jsonPath("$.name").value(userResponseDTO.name()))
				.andExpect(jsonPath("$.email").value(userResponseDTO.email()))
				.andExpect(jsonPath("$.phone").value(userResponseDTO.phone()))
				.andExpect(jsonPath("$.role").value(userResponseDTO.role().name()));
	}
	
	@Test
	void shouldLoginAndReturnJwtToken() throws Exception {
		String token = "mocked-jwt-token";
		LoginRequestDTO loginRequest = new LoginRequestDTO("john@gmail.com", "password");
		
		when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
		when(jwtTokenProvider.generateToken(any())).thenReturn(token);
		
		mockMvc.perform(post(urlPrefix + "/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value(token));
	}
	
	@Test
	void getCurrentUser() throws Exception {
		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(user);
		
		var securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);
		
		mockMvc.perform(get(urlPrefix + "/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(user.getId()))
				.andExpect(jsonPath("$.name").value(user.getName()))
				.andExpect(jsonPath("$.email").value(user.getEmail()))
				.andExpect(jsonPath("$.phone").value(user.getPhone()))
				.andExpect(jsonPath("$.role").value(user.getRole().name()));
	}
}