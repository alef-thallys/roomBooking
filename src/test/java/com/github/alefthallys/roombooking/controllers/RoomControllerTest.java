package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.assemblers.RoomModelAssembler;
import com.github.alefthallys.roombooking.dtos.Room.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.Room.RoomResponseDTO;
import com.github.alefthallys.roombooking.exceptions.Room.EntityRoomAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.Room.EntityRoomNotFoundException;
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
	
	@MockitoBean
	private RoomModelAssembler roomModelAssembler;
	
	private RoomRequestDTO roomRequestDTO;
	private RoomResponseDTO roomResponseDTO;
	private EntityModel<RoomResponseDTO> roomEntityModel;
	private CollectionModel<EntityModel<RoomResponseDTO>> roomCollectionModel;
	
	@BeforeEach
	void setUp() {
		
		roomRequestDTO = RoomTestBuilder.aRoom().buildRequestDTO();
		roomResponseDTO = RoomTestBuilder.aRoom().buildResponseDTO();
		
		roomEntityModel = EntityModel.of(roomResponseDTO,
				linkTo(methodOn(RoomController.class).findById(roomResponseDTO.id())).withSelfRel(),
				linkTo(methodOn(RoomController.class).update(roomResponseDTO.id(), null)).withRel("update"),
				linkTo(methodOn(RoomController.class).delete(roomResponseDTO.id())).withRel("delete")
		);
		
		roomCollectionModel = CollectionModel.of(Collections.singletonList(roomEntityModel),
				linkTo(methodOn(RoomController.class).findAll()).withSelfRel()
		);
	}
	
	private void assertRoomEntityModel(ResultActions resultActions, RoomResponseDTO expectedDto) throws Exception {
		resultActions
				.andExpect(jsonPath("$.id").value(expectedDto.id()))
				.andExpect(jsonPath("$.name").value(expectedDto.name()))
				.andExpect(jsonPath("$.description").value(expectedDto.description()))
				.andExpect(jsonPath("$.capacity").value(expectedDto.capacity()))
				.andExpect(jsonPath("$.location").value(expectedDto.location()))
				.andExpect(jsonPath("$._links.self.href").exists())
				.andExpect(jsonPath("$._links.update.href").exists())
				.andExpect(jsonPath("$._links.delete.href").exists());
	}
	
	private void assertRoomCollectionModel(ResultActions resultActions, List<RoomResponseDTO> expectedDtos) throws Exception {
		resultActions.andExpect(jsonPath("$._embedded.roomResponseDTOList.length()").value(expectedDtos.size()));
		for (int i = 0; i < expectedDtos.size(); i++) {
			RoomResponseDTO expectedDto = expectedDtos.get(i);
			resultActions
					.andExpect(jsonPath("$._embedded.roomResponseDTOList[" + i + "].id").value(expectedDto.id()))
					.andExpect(jsonPath("$._embedded.roomResponseDTOList[" + i + "].name").value(expectedDto.name()))
					.andExpect(jsonPath("$._embedded.roomResponseDTOList[" + i + "].description").value(expectedDto.description()))
					.andExpect(jsonPath("$._embedded.roomResponseDTOList[" + i + "].capacity").value(expectedDto.capacity()))
					.andExpect(jsonPath("$._embedded.roomResponseDTOList[" + i + "].location").value(expectedDto.location()))
					.andExpect(jsonPath("$._embedded.roomResponseDTOList[" + i + "]._links.self.href").exists())
					.andExpect(jsonPath("$._embedded.roomResponseDTOList[" + i + "]._links.update.href").exists())
					.andExpect(jsonPath("$._embedded.roomResponseDTOList[" + i + "]._links.delete.href").exists());
		}
		resultActions.andExpect(jsonPath("$._links.self.href").exists());
	}
	
	
	@Nested
	@DisplayName("GET " + URL_PREFIX)
	class FindAllRooms {
		
		@Test
		@DisplayName("should return all rooms with HATEOAS links")
		void shouldReturnAllRoomsWithHateoasLinks() throws Exception {
			List<RoomResponseDTO> responseList = List.of(roomResponseDTO);
			when(roomService.findAll()).thenReturn(responseList);
			
			doReturn(roomCollectionModel).when(roomModelAssembler).toCollectionModel(responseList);
			
			ResultActions resultActions = mockMvc.perform(get(URL_PREFIX))
					.andExpect(status().isOk());
			
			assertRoomCollectionModel(resultActions, responseList);
		}
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX + "/{id}")
	class FindRoomById {
		
		@Test
		@DisplayName("should return room by id with HATEOAS links")
		void shouldReturnRoomByIdWithHateoasLinks() throws Exception {
			when(roomService.findById(1L)).thenReturn(roomResponseDTO);
			doReturn(roomEntityModel).when(roomModelAssembler).toModel(roomResponseDTO);
			
			ResultActions resultActions = mockMvc.perform(get(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isOk());
			assertRoomEntityModel(resultActions, roomResponseDTO);
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
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
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
		@DisplayName("should create a room with HATEOAS links")
		void shouldCreateRoomWithHateoasLinks() throws Exception {
			when(roomService.create(roomRequestDTO)).thenReturn(roomResponseDTO);
			doReturn(roomEntityModel).when(roomModelAssembler).toModel(roomResponseDTO);
			
			ResultActions resultActions = mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(roomRequestDTO)))
					.andExpect(status().isCreated());
			assertRoomEntityModel(resultActions, roomResponseDTO);
		}
		
		@ParameterizedTest(name = "should return 400 when request body is invalid: {0}")
		@MethodSource("invalidRoomRequestDTOs")
		@DisplayName("should return 400 when request is invalid")
		void shouldReturnBadRequestWhenCreateRoomWithInvalidData(RoomRequestDTO invalidDto) throws Exception {
			mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidDto)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
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
		@DisplayName("should update a room with HATEOAS links")
		void shouldUpdateRoomWithHateoasLinks() throws Exception {
			when(roomService.update(1L, roomRequestDTO)).thenReturn(roomResponseDTO);
			doReturn(roomEntityModel).when(roomModelAssembler).toModel(roomResponseDTO);
			
			ResultActions resultActions = mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(roomRequestDTO)))
					.andExpect(status().isOk());
			assertRoomEntityModel(resultActions, roomResponseDTO);
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
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
		
		@ParameterizedTest(name = "should return 400 when id is invalid: {0}")
		@ValueSource(strings = {"invalid-id", "abc"})
		@DisplayName("should return 400 when id is invalid for update")
		void shouldReturnBadRequestWhenIdIsInvalidForUpdate(String invalidId) throws Exception {
			mockMvc.perform(put(URL_PREFIX + "/{id}", invalidId)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(roomRequestDTO)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
	}
	
	@Nested
	@DisplayName("DELETE " + URL_PREFIX + "/{id}")
	class DeleteRoom {
		
		@Test
		@DisplayName("should delete a room")
		void shouldDeleteRoom() throws Exception {
			doNothing().when(roomService).delete(1L);
			
			mockMvc.perform(delete(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isNoContent());
			
			verify(roomService, times(1)).delete(1L);
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
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
	}
}