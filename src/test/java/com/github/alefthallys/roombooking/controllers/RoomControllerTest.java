package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.RoomResponseDTO;
import com.github.alefthallys.roombooking.exceptions.EntityRoomAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.security.jwt.JwtAuthenticationFilter;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import com.github.alefthallys.roombooking.services.RoomService;
import com.github.alefthallys.roombooking.testBuilders.RoomTestBuilder;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {
	
	private static final String URL_PREFIX = TestConstants.API_V1_ROOMS;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	
	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@MockitoBean
	private RoomService roomService;
	
	private RoomRequestDTO roomRequestDTO;
	private RoomResponseDTO roomResponseDTO;
	
	@BeforeEach
	void setUp() {
		
		roomRequestDTO = RoomTestBuilder.aRoom().buildRequestDTO();
		roomResponseDTO = RoomTestBuilder.aRoom().buildResponseDTO();
	}
	
	private void assertRoomResponseDTO(ResultActions resultActions, RoomResponseDTO roomResponseDTO) throws Exception {
		resultActions.andExpect(jsonPath("$.id").value(roomResponseDTO.id()))
				.andExpect(jsonPath("$.name").value(roomResponseDTO.name()))
				.andExpect(jsonPath("$.description").value(roomResponseDTO.description()))
				.andExpect(jsonPath("$.capacity").value(roomResponseDTO.capacity()))
				.andExpect(jsonPath("$.available").value(roomResponseDTO.available()))
				.andExpect(jsonPath("$.location").value(roomResponseDTO.location()));
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX)
	class FindAllRooms {
		
		@Test
		@DisplayName("should return all rooms")
		void shouldReturnAllRooms() throws Exception {
			when(roomService.findAll()).thenReturn(List.of(roomResponseDTO));
			
			mockMvc.perform(get(URL_PREFIX))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].id").value(roomResponseDTO.id()))
					.andExpect(jsonPath("$[0].name").value(roomResponseDTO.name()))
					.andExpect(jsonPath("$[0].description").value(roomResponseDTO.description()))
					.andExpect(jsonPath("$[0].capacity").value(roomResponseDTO.capacity()))
					.andExpect(jsonPath("$[0].available").value(roomResponseDTO.available()))
					.andExpect(jsonPath("$[0].location").value(roomResponseDTO.location()));
		}
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX + "/{id}")
	class FindRoomById {
		
		@Test
		@DisplayName("should return room by id")
		void shouldReturnRoomById() throws Exception {
			when(roomService.findById(1L)).thenReturn(roomResponseDTO);
			
			assertRoomResponseDTO(
					mockMvc.perform(get(URL_PREFIX + "/{id}", 1L)).andExpect(status().isOk()),
					roomResponseDTO
			);
		}
		
		@Test
		@DisplayName("should return 404 when room not found")
		void shouldThrowEntityRoomNotFoundException() throws Exception {
			when(roomService.findById(1L)).thenThrow(new EntityRoomNotFoundException(1L));
			
			mockMvc.perform(get(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("Room not found with id: " + 1L));
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
	class CreateRoom {
		
		private static Stream<Arguments> invalidRoomRequestDTOs() {
			return Stream.of(
					Arguments.of(RoomTestBuilder.aRoom().withName(null).buildRequestDTO()),
					Arguments.of(RoomTestBuilder.aRoom().withCapacity(-1).buildRequestDTO()),
					Arguments.of(RoomTestBuilder.aRoom().withLocation("").buildRequestDTO())
			);
		}
		
		@Test
		@DisplayName("should create a room")
		void shouldCreateRoom() throws Exception {
			when(roomService.create(roomRequestDTO)).thenReturn(roomResponseDTO);
			
			assertRoomResponseDTO(
					mockMvc.perform(post(URL_PREFIX)
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(roomRequestDTO)))
							.andExpect(status().isCreated()),
					roomResponseDTO
			);
		}
		
		@ParameterizedTest(name = "should return 400 when request body is invalid: {0}")
		@MethodSource("invalidRoomRequestDTOs")
		@DisplayName("should return 400 when request is invalid")
		void shouldReturnBadRequestWhenCreateRoomWithInvalidData(RoomRequestDTO invalidDto) throws Exception {
			mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidDto)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
		
		@Test
		@DisplayName("should return 409 when room already exists")
		void shouldThrowEntityRoomAlreadyExistsExceptionOnCreate() throws Exception {
			when(roomService.create(roomRequestDTO)).thenThrow(new EntityRoomAlreadyExistsException(roomRequestDTO.name()));
			
			mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(roomRequestDTO)))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.message").value("Room already exists with name: " + roomRequestDTO.name()));
		}
	}
	
	@Nested
	@DisplayName("PUT " + URL_PREFIX + "/{id}")
	class UpdateRoom {
		
		private static Stream<Arguments> invalidRoomRequestDTOs() {
			return Stream.of(
					Arguments.of(RoomTestBuilder.aRoom().withName(null).buildRequestDTO()),
					Arguments.of(RoomTestBuilder.aRoom().withCapacity(-1).buildRequestDTO()),
					Arguments.of(RoomTestBuilder.aRoom().withLocation("").buildRequestDTO())
			);
		}
		
		@Test
		@DisplayName("should update a room")
		void shouldUpdateRoom() throws Exception {
			when(roomService.update(1L, roomRequestDTO)).thenReturn(roomResponseDTO);
			
			assertRoomResponseDTO(
					mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(roomRequestDTO)))
							.andExpect(status().isOk()),
					roomResponseDTO
			);
		}
		
		@Test
		@DisplayName("should return 404 when room not found")
		void shouldThrowEntityRoomNotFoundExceptionOnUpdate() throws Exception {
			when(roomService.update(1L, roomRequestDTO)).thenThrow(new EntityRoomNotFoundException(1L));
			
			mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(roomRequestDTO)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("Room not found with id: " + 1L));
		}
		
		@ParameterizedTest(name = "should return 400 when request body is invalid: {0}")
		@MethodSource("invalidRoomRequestDTOs")
		@DisplayName("should return 400 when request is invalid")
		void shouldReturnBadRequestWhenUpdateRoomWithInvalidData(RoomRequestDTO invalidDto) throws Exception {
			mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidDto)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
	
	@Nested
	@DisplayName("DELETE " + URL_PREFIX + "/{id}")
	class DeleteRoom {
		
		@Test
		@DisplayName("should delete a room")
		void shouldDeleteRoom() throws Exception {
			mockMvc.perform(delete(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isNoContent());
		}
		
		@Test
		@DisplayName("should return 404 when room not found")
		void shouldThrowEntityRoomNotFoundExceptionOnDelete() throws Exception {
			doThrow(new EntityRoomNotFoundException(1L))
					.when(roomService)
					.delete(1L);
			
			mockMvc.perform(delete(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("Room not found with id: " + 1L));
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