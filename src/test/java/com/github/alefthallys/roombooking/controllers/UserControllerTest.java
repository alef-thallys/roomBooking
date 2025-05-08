package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.UserDTO;
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
	private UserDTO userDTO;
	
	@BeforeEach
	void setUp() {
		userDTO = new UserDTO(
				1L,
				"John Doe",
				"john@gmail.com",
				"password",
				"123456789",
				User.Role.USER
		);
	}
	
	@Test
	void shouldReturnAllUsers() throws Exception {
		when(userService.findAll()).thenReturn(List.of(userDTO));
		
		mockMvc.perform(get(urlPrefix))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(userDTO.id()))
				.andExpect(jsonPath("$[0].name").value(userDTO.name()))
				.andExpect(jsonPath("$[0].email").value(userDTO.email()))
				.andExpect(jsonPath("$[0].phone").value(userDTO.phone()));
	}
	
	@Test
	void shouldReturnUserById() throws Exception {
		when(userService.findById(1L)).thenReturn(userDTO);
		
		mockMvc.perform(get(urlPrefix + "/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userDTO.id()))
				.andExpect(jsonPath("$.name").value(userDTO.name()))
				.andExpect(jsonPath("$.email").value(userDTO.email()))
				.andExpect(jsonPath("$.phone").value(userDTO.phone()));
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
		when(userService.create(userDTO)).thenReturn(userDTO);
		
		mockMvc.perform(post(urlPrefix)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDTO)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(userDTO.id()))
				.andExpect(jsonPath("$.name").value(userDTO.name()))
				.andExpect(jsonPath("$.email").value(userDTO.email()))
				.andExpect(jsonPath("$.phone").value(userDTO.phone()));
	}
	
	@Test
	void shouldThrowEntityUserAlreadyExistsExceptionOnCreate() throws Exception {
		when(userService.create(userDTO)).thenThrow(new EntityUserAlreadyExistsException(userDTO.email()));
		
		mockMvc.perform(post(urlPrefix)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDTO)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("User already exists with email: " + userDTO.email()));
	}
	
	@Test
	void shouldUpdateUser() throws Exception {
		when(userService.update(1L, userDTO)).thenReturn(userDTO);
		
		mockMvc.perform(put(urlPrefix + "/{id}", 1L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userDTO.id()))
				.andExpect(jsonPath("$.name").value(userDTO.name()))
				.andExpect(jsonPath("$.email").value(userDTO.email()))
				.andExpect(jsonPath("$.phone").value(userDTO.phone()));
	}
	
	@Test
	void shouldThrowEntityUserNotFoundExceptionOnUpdate() throws Exception {
		when(userService.update(1L, userDTO)).thenThrow(new EntityUserNotFoundException(1L));
		
		mockMvc.perform(put(urlPrefix + "/{id}", 1L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDTO)))
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