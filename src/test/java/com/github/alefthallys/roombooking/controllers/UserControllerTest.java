package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.exceptions.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityUserNotFoundException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {
	
	private final String urlPrefix = "/api/v1/users";
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private UserService userService;
	private UserRequestDTO userRequestDTO;
	private UserResponseDTO userResponseDTO;
	
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
				User.Role.USER
		);
	}
	
	@Test
	void shouldReturnAllUsers() throws Exception {
		when(userService.findAll()).thenReturn(List.of(userResponseDTO));
		
		mockMvc.perform(get(urlPrefix))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(userResponseDTO.id()))
				.andExpect(jsonPath("$[0].name").value(userResponseDTO.name()))
				.andExpect(jsonPath("$[0].email").value(userResponseDTO.email()))
				.andExpect(jsonPath("$[0].phone").value(userResponseDTO.phone()))
				.andExpect(jsonPath("$[0].role").value(userResponseDTO.role().name()));
	}
	
	@Test
	void shouldReturnUserById() throws Exception {
		when(userService.findById(1L)).thenReturn(userResponseDTO);
		
		mockMvc.perform(get(urlPrefix + "/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userResponseDTO.id()))
				.andExpect(jsonPath("$.name").value(userResponseDTO.name()))
				.andExpect(jsonPath("$.email").value(userResponseDTO.email()))
				.andExpect(jsonPath("$.phone").value(userResponseDTO.phone()));
	}
	
	@Test
	void shouldThrowEntityUserNotFoundException() throws Exception {
		when(userService.findById(1L)).thenThrow(new EntityUserNotFoundException(1L));
		
		mockMvc.perform(get(urlPrefix + "/{id}", 1L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("User not found with id: " + 1L));
	}
	
	@Test
	void shouldCreateUser() throws Exception {
		when(userService.create(userRequestDTO)).thenReturn(userResponseDTO);
		
		mockMvc.perform(post(urlPrefix)
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
	void shouldThrowEntityUserAlreadyExistsExceptionOnCreate() throws Exception {
		when(userService.create(userRequestDTO)).thenThrow(new EntityUserAlreadyExistsException(userRequestDTO.email()));
		
		mockMvc.perform(post(urlPrefix)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequestDTO)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("User already exists with email: " + userRequestDTO.email()));
	}
	
	@Test
	void shouldUpdateUser() throws Exception {
		when(userService.update(1L, userRequestDTO)).thenReturn(userResponseDTO);
		
		mockMvc.perform(put(urlPrefix + "/{id}", 1L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequestDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userResponseDTO.id()))
				.andExpect(jsonPath("$.name").value(userResponseDTO.name()))
				.andExpect(jsonPath("$.email").value(userResponseDTO.email()))
				.andExpect(jsonPath("$.phone").value(userResponseDTO.phone()))
				.andExpect(jsonPath("$.role").value(userResponseDTO.role().name()));
	}
	
	@Test
	void shouldThrowEntityUserNotFoundExceptionOnUpdate() throws Exception {
		when(userService.update(1L, userRequestDTO)).thenThrow(new EntityUserNotFoundException(1L));
		
		mockMvc.perform(put(urlPrefix + "/{id}", 1L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequestDTO)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("User not found with id: " + 1L));
	}
	
	@Test
	void shouldDeleteUserById() throws Exception {
		doNothing().when(userService).delete(1L);
		
		mockMvc.perform(delete(urlPrefix + "/{id}", 1L))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void shouldThrowEntityUserNotFoundExceptionOnDelete() throws Exception {
		doThrow(new EntityUserNotFoundException(1L))
				.when(userService)
				.delete(1L);
		
		mockMvc.perform(delete(urlPrefix + "/{id}", 1L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("User not found with id: " + 1L));
	}
}