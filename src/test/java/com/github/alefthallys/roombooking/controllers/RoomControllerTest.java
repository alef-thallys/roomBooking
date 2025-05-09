package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.RoomResponseDTO;
import com.github.alefthallys.roombooking.exceptions.EntityRoomAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.services.RoomService;
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

@WebMvcTest(RoomController.class)
class RoomControllerTest {
	
	private final String urlPrefix = "/api/v1/rooms";
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private RoomService roomService;
	private RoomRequestDTO roomRequestDTO;
	private RoomResponseDTO roomResponseDTO;
	
	@BeforeEach
	void setUp() {
		roomRequestDTO = new RoomRequestDTO(
				"Room 101",
				"Conference Room",
				10,
				true,
				"1st Floor"
		);
		
		roomResponseDTO = new RoomResponseDTO(
				1L,
				"Room 101",
				"Conference Room",
				10,
				true,
				"1st Floor"
		);
	}
	
	@Test
	void shouldReturnAllRooms() throws Exception {
		when(roomService.findAll()).thenReturn(List.of(roomResponseDTO));
		
		mockMvc.perform(get(urlPrefix))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(roomResponseDTO.id()))
				.andExpect(jsonPath("$[0].name").value(roomResponseDTO.name()))
				.andExpect(jsonPath("$[0].description").value(roomResponseDTO.description()))
				.andExpect(jsonPath("$[0].capacity").value(roomResponseDTO.capacity()))
				.andExpect(jsonPath("$[0].available").value(roomResponseDTO.available()))
				.andExpect(jsonPath("$[0].location").value(roomResponseDTO.location()));
	}
	
	@Test
	void shouldReturnRoomById() throws Exception {
		when(roomService.findById(1L)).thenReturn(roomResponseDTO);
		
		mockMvc.perform(get(urlPrefix + "/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(roomResponseDTO.id()))
				.andExpect(jsonPath("$.name").value(roomResponseDTO.name()))
				.andExpect(jsonPath("$.description").value(roomResponseDTO.description()))
				.andExpect(jsonPath("$.capacity").value(roomResponseDTO.capacity()))
				.andExpect(jsonPath("$.available").value(roomResponseDTO.available()))
				.andExpect(jsonPath("$.location").value(roomResponseDTO.location()));
	}
	
	@Test
	void shouldThrowEntityRoomNotFoundException() throws Exception {
		when(roomService.findById(1L)).thenThrow(new EntityRoomNotFoundException(1L));
		
		mockMvc.perform(get(urlPrefix + "/{id}", 1L))
				.andExpect(status().isNotFound());
	}
	
	@Test
	void shouldCreateRoom() throws Exception {
		when(roomService.create(roomRequestDTO)).thenReturn(roomResponseDTO);
		
		mockMvc.perform(post(urlPrefix)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(roomRequestDTO)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(roomResponseDTO.id()))
				.andExpect(jsonPath("$.name").value(roomResponseDTO.name()))
				.andExpect(jsonPath("$.description").value(roomResponseDTO.description()))
				.andExpect(jsonPath("$.capacity").value(roomResponseDTO.capacity()))
				.andExpect(jsonPath("$.available").value(roomResponseDTO.available()))
				.andExpect(jsonPath("$.location").value(roomResponseDTO.location()));
	}
	
	@Test
	void shouldThrowEntityRoomAlreadyExistsExceptionOnCreate() throws Exception {
		when(roomService.create(roomRequestDTO)).thenThrow(new EntityRoomAlreadyExistsException(roomRequestDTO.description()));
		
		mockMvc.perform(post(urlPrefix)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(roomRequestDTO)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Room already exists with description: " + roomRequestDTO.description()));
	}
	
	@Test
	void shouldUpdateRoom() throws Exception {
		when(roomService.update(1L, roomRequestDTO)).thenReturn(roomResponseDTO);
		
		mockMvc.perform(put(urlPrefix + "/{id}", 1L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(roomRequestDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(roomResponseDTO.id()))
				.andExpect(jsonPath("$.name").value(roomResponseDTO.name()))
				.andExpect(jsonPath("$.description").value(roomResponseDTO.description()))
				.andExpect(jsonPath("$.capacity").value(roomResponseDTO.capacity()))
				.andExpect(jsonPath("$.available").value(roomResponseDTO.available()))
				.andExpect(jsonPath("$.location").value(roomResponseDTO.location()));
	}
	
	@Test
	void shouldThrowEntityRoomNotFoundExceptionOnUpdate() throws Exception {
		when(roomService.update(1L, roomRequestDTO)).thenThrow(new EntityRoomNotFoundException(1L));
		
		mockMvc.perform(put(urlPrefix + "/{id}", 1L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(roomRequestDTO)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Room not found with id: " + 1L));
	}
	
	@Test
	void shouldDeleteRoomById() throws Exception {
		doNothing().when(roomService).delete(1L);
		
		mockMvc.perform(delete(urlPrefix + "/{id}", 1L))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void shouldThrowEntityRoomNotFoundExceptionOnDelete() throws Exception {
		doThrow(new EntityRoomNotFoundException(1L))
				.when(roomService)
				.delete(1L);
		
		mockMvc.perform(delete(urlPrefix + "/{id}", 1L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Room not found with id: " + 1L));
	}
}