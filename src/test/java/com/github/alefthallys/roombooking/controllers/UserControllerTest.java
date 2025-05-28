package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.User.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserResponseDTO;
import com.github.alefthallys.roombooking.dtos.User.UserUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.User.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.User.EntityUserNotFoundException;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
	
	private static final String URL_PREFIX = TestConstants.API_V1_USERS;
	
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
		userRequestDTO = UserTestBuilder.anUser().buildRequestDTO();
		userUpdateRequestDTO = UserTestBuilder.anUser().buildUpdateRequestDTO();
		userResponseDTO = UserTestBuilder.anUser().buildResponseDTO();
	}
	
	private void assertUserResponseDTO(ResultActions resultActions, UserResponseDTO userResponseDTO) throws Exception {
		resultActions.andExpect(jsonPath("$.id").value(userResponseDTO.id()))
				.andExpect(jsonPath("$.name").value(userResponseDTO.name()))
				.andExpect(jsonPath("$.email").value(userResponseDTO.email()))
				.andExpect(jsonPath("$.phone").value(userResponseDTO.phone()))
				.andExpect(jsonPath("$.role").value(userResponseDTO.role().name()));
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX)
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
	@DisplayName("GET " + URL_PREFIX + "/{id}")
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
		
		@ParameterizedTest(name = "should return 400 when id is invalid: {0}")
		@ValueSource(strings = {"invalid-id", "abc"})
		@DisplayName("should return 400 when id is invalid")
		void shouldReturnBadRequestWhenIdIsInvalid(String invalidId) throws Exception {
			mockMvc.perform(get(URL_PREFIX + "/{id}", invalidId))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
	
	@Nested
	@DisplayName("POST " + URL_PREFIX)
	class CreateUser {
		
		private static Stream<Arguments> invalidUserRequestDTOs() {
			return Stream.of(
					Arguments.of(UserTestBuilder.anUser().withEmail("invalid-email").buildRequestDTO()),
					Arguments.of(UserTestBuilder.anUser().withName(null).buildRequestDTO()),
					Arguments.of(UserTestBuilder.anUser().withPassword(null).buildRequestDTO()),
					Arguments.of(UserTestBuilder.anUser().withPhone("").buildRequestDTO())
			);
		}
		
		@Test
		@DisplayName("should create a new user")
		void shouldCreateNewUser() throws Exception {
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
		
		@ParameterizedTest(name = "should return 400 when request body is invalid: {0}")
		@MethodSource("invalidUserRequestDTOs")
		@DisplayName("should return 400 when request body is invalid")
		void shouldReturnBadRequestWhenRegisteringUserWithInvalidData(UserRequestDTO invalidDto) throws Exception {
			mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidDto)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
	
	@Nested
	@DisplayName("PUT " + URL_PREFIX + "/{id}")
	class UpdateUser {
		
		@Test
		@DisplayName("should update user")
		void shouldUpdateUser() throws Exception {
			when(userService.update(1L, userUpdateRequestDTO)).thenReturn(userResponseDTO);
			
			assertUserResponseDTO(
					mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(userUpdateRequestDTO)))
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
							.content(objectMapper.writeValueAsString(userUpdateRequestDTO)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("User not found with id: " + 1L));
		}
		
		@ParameterizedTest(name = "should return 400 when id is invalid: {0}")
		@ValueSource(strings = {"invalid-id", "abc"})
		@DisplayName("should return 400 when id is invalid")
		void shouldReturnBadRequestWhenIdIsInvalid(String invalidId) throws Exception {
			mockMvc.perform(put(URL_PREFIX + "/{id}", invalidId)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(userUpdateRequestDTO)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
	
	@Nested
	@DisplayName("DELETE " + URL_PREFIX + "/{id}")
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
		
		@ParameterizedTest(name = "should return 400 when id is invalid: {0}")
		@ValueSource(strings = {"invalid-id", "abc"})
		@DisplayName("should return 400 when id is invalid")
		void shouldReturnBadRequestWhenIdIsInvalid(String invalidId) throws Exception {
			mockMvc.perform(delete(URL_PREFIX + "/{id}", invalidId))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
}