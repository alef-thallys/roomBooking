package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.dtos.UserUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityUserNotFoundException;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
	
	private static final String URL_PREFIX = "/api/v1/users";
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	
	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@MockitoBean
	private UserService userService;
	
	private UserRequestDTO userRequestDTO;
	private UserResponseDTO userResponseDTO;
	private UserUpdateRequestDTO userUpdateRequestDTO;
	
	@BeforeEach
	void setUp() {
		userRequestDTO = new UserRequestDTO(
				"John Doe",
				"john@gmail.com",
				"password",
				"12997665045"
		);
		
		userUpdateRequestDTO = new UserUpdateRequestDTO(
				"John Doe",
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
	}
	
	private void assertUserResponseDTO(ResultActions resultActions, UserResponseDTO userResponseDTO) throws Exception {
		resultActions.andExpect(jsonPath("$.id").value(userResponseDTO.id()))
				.andExpect(jsonPath("$.name").value(userResponseDTO.name()))
				.andExpect(jsonPath("$.email").value(userResponseDTO.email()))
				.andExpect(jsonPath("$.phone").value(userResponseDTO.phone()))
				.andExpect(jsonPath("$.role").value(userResponseDTO.role().name()));
	}
	
	@Nested
	@DisplayName("GET /api/v1/users")
	class FindAllUsers {
		
		@Test
		@DisplayName("should return all users")
		void shouldReturnAllUsers() throws Exception {
			when(userService.findAll()).thenReturn(List.of(userResponseDTO));
			
			mockMvc.perform(get(URL_PREFIX))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].id").value(userResponseDTO.id()))
					.andExpect(jsonPath("$[0].name").value(userResponseDTO.name()))
					.andExpect(jsonPath("$[0].email").value(userResponseDTO.email()))
					.andExpect(jsonPath("$[0].phone").value(userResponseDTO.phone()))
					.andExpect(jsonPath("$[0].role").value(userResponseDTO.role().name()));
		}
	}
	
	@Nested
	@DisplayName("GET /api/v1/users/{id}")
	class FindUserById {
		
		@Test
		@DisplayName("should return user by id")
		void shouldReturnUserById() throws Exception {
			when(userService.findById(1L)).thenReturn(userResponseDTO);
			
			assertUserResponseDTO(
					mockMvc.perform(get(URL_PREFIX + "/{id}", 1L)).andExpect(status().isOk()),
					userResponseDTO
			);
		}
		
		@Test
		@DisplayName("should return 404 when user not found")
		void shouldThrowEntityUserNotFoundException() throws Exception {
			when(userService.findById(1L)).thenThrow(new EntityUserNotFoundException(1L));
			
			mockMvc.perform(get(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("User not found with id: " + 1L));
		}
		
		@Test
		@DisplayName("should return 400 when id is invalid")
		void shouldThrowMethodArgumentNotValidExceptionOnDelete() throws Exception {
			mockMvc.perform(delete(URL_PREFIX + "/{id}", "invalid-id"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
	
	@Nested
	@DisplayName("POST /api/v1/users")
	class CreateUser {
		
		@Test
		@DisplayName("should create user")
		void shouldCreateUser() throws Exception {
			when(userService.create(userRequestDTO)).thenReturn(userResponseDTO);
			
			assertUserResponseDTO(
					mockMvc.perform(post(URL_PREFIX)
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(userRequestDTO)))
							.andExpect(status().isCreated()),
					userResponseDTO
			);
		}
		
		@Test
		@DisplayName("should return 409 when user already exists")
		void shouldThrowEntityUserAlreadyExistsExceptionOnCreate() throws Exception {
			when(userService.create(userRequestDTO)).thenThrow(new EntityUserAlreadyExistsException(userRequestDTO.email()));
			
			mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(userRequestDTO)))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.message").value("User already exists with email: " + userRequestDTO.email()));
		}
		
		@Test
		@DisplayName("should return 400 when request body is invalid")
		void shouldThrowMethodArgumentNotValidExceptionOnCreate() throws Exception {
			userRequestDTO = new UserRequestDTO(
					"John Doe",
					"invalid-email",
					"password",
					"12997665045"
			);
			
			mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(userRequestDTO)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
	
	@Nested
	@DisplayName("PUT /api/v1/users/{id}")
	class UpdateUser {
		
		@Test
		@DisplayName("should update user")
		void shouldUpdateUser() throws Exception {
			when(userService.update(1L, userUpdateRequestDTO)).thenReturn(userResponseDTO);
			
			assertUserResponseDTO(
					mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(userRequestDTO)))
							.andExpect(status().isOk()),
					userResponseDTO
			);
		}
		
		@Test
		@DisplayName("should return 404 when updating non-existent user")
		void shouldThrowEntityUserNotFoundExceptionOnUpdate() throws Exception {
			when(userService.update(1L, userUpdateRequestDTO)).thenThrow(new EntityUserNotFoundException(1L));
			
			mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(userRequestDTO)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("User not found with id: " + 1L));
		}
		
		@Test
		@DisplayName("should return 400 when id is invalid")
		void shouldThrowMethodArgumentNotValidExceptionOnDelete() throws Exception {
			mockMvc.perform(delete(URL_PREFIX + "/{id}", "invalid-id"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
	
	@Nested
	@DisplayName("DELETE /api/v1/users/{id}")
	class DeleteUser {
		
		@Test
		@DisplayName("should delete user by id")
		void shouldDeleteUserById() throws Exception {
			doNothing().when(userService).delete(1L);
			
			mockMvc.perform(delete(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isNoContent());
		}
		
		@Test
		@DisplayName("should return 404 when deleting non-existent user")
		void shouldThrowEntityUserNotFoundExceptionOnDelete() throws Exception {
			doThrow(new EntityUserNotFoundException(1L))
					.when(userService)
					.delete(1L);
			
			mockMvc.perform(delete(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("User not found with id: " + 1L));
		}
		
		@Test
		@DisplayName("should return 400 when id is invalid")
		void shouldThrowMethodArgumentNotValidExceptionOnDelete() throws Exception {
			mockMvc.perform(delete(URL_PREFIX + "/{id}", "invalid-id"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
}