package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.UserDTO;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private UserService userService;
	
	private UserDTO userDTO;
	private final String urlPrefix = "/api/v1/users";
	
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
		
		mockMvc.perform(get(urlPrefix + "/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userDTO.id()))
				.andExpect(jsonPath("$.name").value(userDTO.name()))
				.andExpect(jsonPath("$.email").value(userDTO.email()))
				.andExpect(jsonPath("$.phone").value(userDTO.phone()));
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
	void shouldUpdateUser() throws Exception {
		when(userService.update(1L, userDTO)).thenReturn(userDTO);
		
		mockMvc.perform(put(urlPrefix + "/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userDTO.id()))
				.andExpect(jsonPath("$.name").value(userDTO.name()))
				.andExpect(jsonPath("$.email").value(userDTO.email()))
				.andExpect(jsonPath("$.phone").value(userDTO.phone()));
	}
	
	@Test
	void shouldDeleteUserById() throws Exception {
		doNothing().when(userService).delete(1L);
		
		mockMvc.perform(delete(urlPrefix + "/1"))
				.andExpect(status().isNoContent());
	}
}