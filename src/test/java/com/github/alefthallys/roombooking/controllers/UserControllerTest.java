package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.assemblers.UserModelAssembler;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
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
	
	@MockitoBean
	private UserModelAssembler userModelAssembler;
	
	private UserRequestDTO userRequestDTO;
	private UserResponseDTO userResponseDTO;
	private UserUpdateRequestDTO userUpdateRequestDTO;
	private EntityModel<UserResponseDTO> userEntityModel;
	private CollectionModel<EntityModel<UserResponseDTO>> userCollectionModel;
	
	@BeforeEach
	void setUp() {
		userRequestDTO = UserTestBuilder.anUser().buildRequestDTO();
		userUpdateRequestDTO = UserTestBuilder.anUser().buildUpdateRequestDTO();
		userResponseDTO = UserTestBuilder.anUser().buildResponseDTO();
		
		userEntityModel = EntityModel.of(userResponseDTO,
				linkTo(methodOn(UserController.class).findById(userResponseDTO.id())).withSelfRel(),
				linkTo(methodOn(UserController.class).update(userResponseDTO.id(), null)).withRel("update"),
				linkTo(methodOn(UserController.class).delete(userResponseDTO.id())).withRel("delete")
		);
		
		userCollectionModel = CollectionModel.of(Collections.singletonList(userEntityModel),
				linkTo(methodOn(UserController.class).findAll()).withSelfRel()
		);
	}
	
	private void assertUserEntityModel(ResultActions resultActions, UserResponseDTO expectedDto) throws Exception {
		resultActions
				.andExpect(jsonPath("$.id").value(expectedDto.id()))
				.andExpect(jsonPath("$.name").value(expectedDto.name()))
				.andExpect(jsonPath("$.email").value(expectedDto.email()))
				.andExpect(jsonPath("$.phone").value(expectedDto.phone()))
				.andExpect(jsonPath("$.role").value(expectedDto.role().name()))
				.andExpect(jsonPath("$._links.self.href").exists())
				.andExpect(jsonPath("$._links.update.href").exists())
				.andExpect(jsonPath("$._links.delete.href").exists());
	}
	
	private void assertUserCollectionModel(ResultActions resultActions, List<UserResponseDTO> expectedDtos) throws Exception {
		resultActions.andExpect(jsonPath("$._embedded.userResponseDTOList.length()").value(expectedDtos.size()));
		for (int i = 0; i < expectedDtos.size(); i++) {
			UserResponseDTO expectedDto = expectedDtos.get(i);
			resultActions
					.andExpect(jsonPath("$._embedded.userResponseDTOList[" + i + "].id").value(expectedDto.id()))
					.andExpect(jsonPath("$._embedded.userResponseDTOList[" + i + "].name").value(expectedDto.name()))
					.andExpect(jsonPath("$._embedded.userResponseDTOList[" + i + "].email").value(expectedDto.email()))
					.andExpect(jsonPath("$._embedded.userResponseDTOList[" + i + "].phone").value(expectedDto.phone()))
					.andExpect(jsonPath("$._embedded.userResponseDTOList[" + i + "].role").value(expectedDto.role().name()))
					.andExpect(jsonPath("$._embedded.userResponseDTOList[" + i + "]._links.self.href").exists())
					.andExpect(jsonPath("$._embedded.userResponseDTOList[" + i + "]._links.update.href").exists())
					.andExpect(jsonPath("$._embedded.userResponseDTOList[" + i + "]._links.delete.href").exists());
		}
		resultActions.andExpect(jsonPath("$._links.self.href").exists());
	}
	
	
	@Nested
	@DisplayName("GET " + URL_PREFIX)
	class FindAllUsers {
		
		@Test
		@DisplayName("should return all users with HATEOAS links")
		void shouldReturnAllUsersWithHateoasLinks() throws Exception {
			List<UserResponseDTO> responseList = List.of(userResponseDTO);
			when(userService.findAll()).thenReturn(responseList);
			
			doReturn(userCollectionModel).when(userModelAssembler).toCollectionModel(responseList);
			
			ResultActions resultActions = mockMvc.perform(get(URL_PREFIX))
					.andExpect(status().isOk());
			assertUserCollectionModel(resultActions, responseList);
		}
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX + "/{id}")
	class FindUserById {
		
		@Test
		@DisplayName("should return user by id with HATEOAS links")
		void shouldReturnUserByIdWithHateoasLinks() throws Exception {
			when(userService.findById(1L)).thenReturn(userResponseDTO);
			doReturn(userEntityModel).when(userModelAssembler).toModel(userResponseDTO);
			
			ResultActions resultActions = mockMvc.perform(get(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isOk());
			assertUserEntityModel(resultActions, userResponseDTO);
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
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
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
		@DisplayName("should create a new user with HATEOAS links")
		void shouldCreateNewUserWithHateoasLinks() throws Exception {
			when(userService.create(userRequestDTO)).thenReturn(userResponseDTO);
			doReturn(userEntityModel).when(userModelAssembler).toModel(userResponseDTO);
			
			ResultActions resultActions = mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(userRequestDTO)))
					.andExpect(status().isCreated());
			assertUserEntityModel(resultActions, userResponseDTO);
		}
		
		@Test
		@DisplayName("should return 409 when email already exists")
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
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
	}
	
	@Nested
	@DisplayName("PUT " + URL_PREFIX + "/{id}")
	class UpdateUser {
		
		private static Stream<Arguments> invalidUserUpdateRequestDTOs() {
			return Stream.of(
					Arguments.of(UserTestBuilder.anUser().withName(null).buildUpdateRequestDTO()),
					Arguments.of(UserTestBuilder.anUser().withPhone("").buildUpdateRequestDTO())
			);
		}
		
		@Test
		@DisplayName("should update user with HATEOAS links")
		void shouldUpdateUserWithHateoasLinks() throws Exception {
			when(userService.update(1L, userUpdateRequestDTO)).thenReturn(userResponseDTO);
			doReturn(userEntityModel).when(userModelAssembler).toModel(userResponseDTO);
			
			ResultActions resultActions = mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(userUpdateRequestDTO)))
					.andExpect(status().isOk());
			assertUserEntityModel(resultActions, userResponseDTO);
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
		
		@ParameterizedTest(name = "should return 400 when request body is invalid for update: {0}")
		@MethodSource("invalidUserUpdateRequestDTOs")
		@DisplayName("should return 400 when request body is invalid for update")
		void shouldReturnBadRequestWhenUpdateUserWithInvalidData(UserUpdateRequestDTO invalidDto) throws Exception {
			mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidDto)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
		
		@ParameterizedTest(name = "should return 400 when id is invalid: {0}")
		@ValueSource(strings = {"invalid-id", "abc"})
		@DisplayName("should return 400 when id is invalid")
		void shouldReturnBadRequestWhenIdIsInvalid(String invalidId) throws Exception {
			mockMvc.perform(put(URL_PREFIX + "/{id}", invalidId)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(userUpdateRequestDTO)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
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
			
			verify(userService, times(1)).delete(1L);
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
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
	}
}