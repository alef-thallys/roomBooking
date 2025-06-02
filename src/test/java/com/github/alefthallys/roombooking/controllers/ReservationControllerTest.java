package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.assemblers.ReservationModelAssembler;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationResponseDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.Reservation.EntityReservationAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.Reservation.EntityReservationNotFoundException;
import com.github.alefthallys.roombooking.models.Reservation;
import com.github.alefthallys.roombooking.security.jwt.JwtAuthenticationFilter;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import com.github.alefthallys.roombooking.services.ReservationService;
import com.github.alefthallys.roombooking.testBuilders.ReservationTestBuilder;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReservationControllerTest {
	
	private static final String URL_PREFIX = TestConstants.API_V1_RESERVATIONS;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	
	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@MockitoBean
	private ReservationService reservationService;
	
	@MockitoBean
	private ReservationModelAssembler reservationModelAssembler;
	
	private ReservationRequestDTO reservationRequestDTO;
	private ReservationResponseDTO reservationResponseDTO;
	private ReservationUpdateRequestDTO reservationUpdateRequestDTO;
	private Reservation mockReservation;
	private EntityModel<ReservationResponseDTO> reservationEntityModel;
	private CollectionModel<EntityModel<ReservationResponseDTO>> reservationCollectionModel;
	
	@BeforeEach
	void setUp() {
		reservationRequestDTO = ReservationTestBuilder.aReservation().buildRequestDTO();
		reservationUpdateRequestDTO = ReservationTestBuilder.aReservation().buildUpdateRequestDTO();
		reservationResponseDTO = ReservationTestBuilder.aReservation().buildResponseDTO();
		mockReservation = ReservationTestBuilder.aReservation().build();
		
		reservationEntityModel = EntityModel.of(reservationResponseDTO,
				linkTo(methodOn(ReservationController.class).findById(reservationResponseDTO.id())).withSelfRel(),
				linkTo(methodOn(ReservationController.class).update(reservationResponseDTO.id(), null)).withRel("update"),
				linkTo(methodOn(ReservationController.class).delete(reservationResponseDTO.id())).withRel("delete")
		);
		
		reservationCollectionModel = CollectionModel.of(Collections.singletonList(reservationEntityModel),
				linkTo(methodOn(ReservationController.class).findAll()).withSelfRel()
		);
	}
	
	private void assertReservationEntityModel(ResultActions resultActions, ReservationResponseDTO response) throws Exception {
		resultActions
				.andExpect(jsonPath("$.id").value(response.id()))
				.andExpect(jsonPath("$.startDate").value(response.startDate().truncatedTo(ChronoUnit.SECONDS).toString()))
				.andExpect(jsonPath("$.endDate").value(response.endDate().truncatedTo(ChronoUnit.SECONDS).toString()))
				.andExpect(jsonPath("$.user.id").value(response.user().id()))
				.andExpect(jsonPath("$.user.name").value(response.user().name()))
				.andExpect(jsonPath("$.room.id").value(response.room().id()))
				.andExpect(jsonPath("$.room.name").value(response.room().name()))
				.andExpect(jsonPath("$._links.self.href").exists())
				.andExpect(jsonPath("$._links.update.href").exists())
				.andExpect(jsonPath("$._links.delete.href").exists());
	}
	
	private void assertReservationCollectionModel(ResultActions resultActions, List<ReservationResponseDTO> responses, String collectionKey) throws Exception {
		resultActions.andExpect(jsonPath("$._embedded." + collectionKey + ".length()").value(responses.size()));
		for (int i = 0; i < responses.size(); i++) {
			ReservationResponseDTO response = responses.get(i);
			resultActions
					.andExpect(jsonPath("$._embedded." + collectionKey + "[" + i + "].id").value(response.id()))
					.andExpect(jsonPath("$._embedded." + collectionKey + "[" + i + "].startDate").value(response.startDate().truncatedTo(ChronoUnit.SECONDS).toString()))
					.andExpect(jsonPath("$._embedded." + collectionKey + "[" + i + "].endDate").value(response.endDate().truncatedTo(ChronoUnit.SECONDS).toString()))
					.andExpect(jsonPath("$._embedded." + collectionKey + "[" + i + "].user.id").value(response.user().id()))
					.andExpect(jsonPath("$._embedded." + collectionKey + "[" + i + "].user.name").value(response.user().name()))
					.andExpect(jsonPath("$._embedded." + collectionKey + "[" + i + "].room.id").value(response.room().id()))
					.andExpect(jsonPath("$._embedded." + collectionKey + "[" + i + "].room.name").value(response.room().name()))
					.andExpect(jsonPath("$._embedded." + collectionKey + "[" + i + "]._links.self.href").exists())
					.andExpect(jsonPath("$._embedded." + collectionKey + "[" + i + "]._links.update.href").exists())
					.andExpect(jsonPath("$._embedded." + collectionKey + "[" + i + "]._links.delete.href").exists());
		}
		resultActions.andExpect(jsonPath("$._links.self.href").exists());
	}
	
	
	@Nested
	@DisplayName("GET " + URL_PREFIX)
	class FindAllReservations {
		
		@Test
		@DisplayName("should return a list of reservations with HATEOAS links")
		void shouldReturnListOfReservationsWithHateoasLinks() throws Exception {
			List<ReservationResponseDTO> responseList = List.of(reservationResponseDTO);
			when(reservationService.findAll()).thenReturn(responseList);
			
			doReturn(reservationCollectionModel).when(reservationModelAssembler).toCollectionModel(responseList);
			
			ResultActions resultActions = mockMvc.perform(get(URL_PREFIX))
					.andExpect(status().isOk());
			
			assertReservationCollectionModel(resultActions, responseList, "reservationResponseDTOList");
		}
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX + "/{id}")
	class FindReservationById {
		
		@Test
		@DisplayName("should return a reservation by ID with HATEOAS links")
		void shouldReturnReservationByIdWithHateoasLinks() throws Exception {
			when(reservationService.findById(1L)).thenReturn(reservationResponseDTO);
			
			doReturn(reservationEntityModel).when(reservationModelAssembler).toModel(reservationResponseDTO);
			
			ResultActions resultActions = mockMvc.perform(get(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isOk());
			assertReservationEntityModel(resultActions, reservationResponseDTO);
		}
		
		@Test
		@DisplayName("should return 404 when reservation not found")
		void shouldThrowMethodNotFoundIfReservationNotFound() throws Exception {
			when(reservationService.findById(1L)).thenThrow(new EntityReservationNotFoundException(1L));
			
			mockMvc.perform(get(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("Reservation not found with id: " + 1L));
		}
		
		@ParameterizedTest(name = "should return 400 if ID is invalid: {0}")
		@ValueSource(strings = {"invalid-id", "abc"})
		@DisplayName("should return 400 if ID is invalid")
		void shouldReturnBadRequestIfIdIsInvalid(String invalidId) throws Exception {
			mockMvc.perform(get(URL_PREFIX + "/{id}", invalidId))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX + "/me")
	class FindMyReservations {
		
		@Test
		@DisplayName("should return a list of reservations for the authenticated user with HATEOAS links")
		void shouldReturnMyReservationsWithHateoasLinks() throws Exception {
			List<ReservationResponseDTO> responseList = List.of(reservationResponseDTO);
			when(reservationService.findByUser()).thenReturn(responseList);
			
			CollectionModel<EntityModel<ReservationResponseDTO>> myReservationsCollectionModel = CollectionModel.of(Collections.singletonList(reservationEntityModel),
					linkTo(methodOn(ReservationController.class).getMyReservations()).withSelfRel()
			);
			doReturn(myReservationsCollectionModel).when(reservationModelAssembler).toCollectionModel(responseList);
			
			
			ResultActions resultActions = mockMvc.perform(get(URL_PREFIX + "/me"))
					.andExpect(status().isOk());
			
			assertReservationCollectionModel(resultActions, responseList, "reservationResponseDTOList");
		}
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX + "/me/{id}")
	class FindMyReservationById {
		
		@Test
		@DisplayName("should return a reservation by ID for the current user with HATEOAS links")
		void shouldReturnMyReservationByIdWithHateoasLinks() throws Exception {
			when(reservationService.findByIdForUser(1L)).thenReturn(reservationResponseDTO);
			doReturn(reservationEntityModel).when(reservationModelAssembler).toModel(reservationResponseDTO);
			
			ResultActions resultActions = mockMvc.perform(get(URL_PREFIX + "/me/{id}", 1L))
					.andExpect(status().isOk());
			assertReservationEntityModel(resultActions, reservationResponseDTO);
		}
		
		@Test
		@DisplayName("should return 404 when reservation not found for current user")
		void shouldThrowMethodNotFoundIfReservationNotFoundForUser() throws Exception {
			when(reservationService.findByIdForUser(1L)).thenThrow(new EntityReservationNotFoundException(1L));
			
			mockMvc.perform(get(URL_PREFIX + "/me/{id}", 1L))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("Reservation not found with id: " + 1L));
		}
		
		@ParameterizedTest(name = "should return 400 if ID is invalid for current user: {0}")
		@ValueSource(strings = {"invalid-id", "abc"})
		@DisplayName("should return 400 if ID is invalid for current user")
		void shouldReturnBadRequestIfIdIsInvalidForUser(String invalidId) throws Exception {
			mockMvc.perform(get(URL_PREFIX + "/me/{id}", invalidId))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
	}
	
	@Nested
	@DisplayName("POST " + URL_PREFIX)
	class CreateReservation {
		
		private static Stream<Arguments> invalidReservationRequestDTOs() {
			return Stream.of(
					Arguments.of(ReservationTestBuilder.aReservation()
							.withStartDate(LocalDateTime.now().plusDays(2))
							.withEndDate(LocalDateTime.now().plusDays(1))
							.buildRequestDTO()),
					Arguments.of(new ReservationRequestDTO(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))),
					Arguments.of(new ReservationRequestDTO(1L, null, LocalDateTime.now().plusDays(2))),
					Arguments.of(new ReservationRequestDTO(1L, LocalDateTime.now().plusDays(1), null))
			);
		}
		
		@Test
		@DisplayName("should create a new reservation with HATEOAS links")
		void shouldCreateNewReservationWithHateoasLinks() throws Exception {
			when(reservationService.create(reservationRequestDTO)).thenReturn(reservationResponseDTO);
			doReturn(reservationEntityModel).when(reservationModelAssembler).toModel(reservationResponseDTO);
			
			ResultActions resultActions = mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(reservationRequestDTO)))
					.andExpect(status().isCreated());
			assertReservationEntityModel(resultActions, reservationResponseDTO);
		}
		
		@ParameterizedTest(name = "should return 400 when reservation request is invalid: {0}")
		@MethodSource("invalidReservationRequestDTOs")
		@DisplayName("should return 400 when reservation date has invalid format or request body is invalid")
		void shouldReturnBadRequestIfReservationDateHasInvalidFormatOrRequestBodyIsInvalid(ReservationRequestDTO invalidDto) throws Exception {
			mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidDto)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
		
		@Test
		@DisplayName("should return 409 when reservation already exists")
		void shouldReturnConflictIfReservationAlreadyExists() throws Exception {
			when(reservationService.create(reservationRequestDTO)).thenThrow(new EntityReservationAlreadyExistsException(1L));
			
			mockMvc.perform(post(URL_PREFIX)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(reservationRequestDTO)))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.message").value("Reservation with ID " + 1L + " already exists."));
		}
	}
	
	@Nested
	@DisplayName("PUT " + URL_PREFIX + "/{id}")
	class UpdateReservation {
		
		private static Stream<Arguments> invalidReservationUpdateRequestDTOs() {
			return Stream.of(
					Arguments.of(ReservationTestBuilder.aReservation()
							.withStartDate(LocalDateTime.now().plusDays(2))
							.withEndDate(LocalDateTime.now().plusDays(1))
							.buildUpdateRequestDTO()),
					Arguments.of(new ReservationUpdateRequestDTO(null, LocalDateTime.now().plusDays(2))),
					Arguments.of(new ReservationUpdateRequestDTO(LocalDateTime.now().plusDays(1), null))
			);
		}
		
		@Test
		@DisplayName("should update reservation with HATEOAS links")
		void shouldUpdateReservationWithHateoasLinks() throws Exception {
			when(reservationService.update(1L, reservationUpdateRequestDTO)).thenReturn(reservationResponseDTO);
			doReturn(reservationEntityModel).when(reservationModelAssembler).toModel(reservationResponseDTO);
			
			ResultActions resultActions = mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(reservationUpdateRequestDTO)))
					.andExpect(status().isOk());
			assertReservationEntityModel(resultActions, reservationResponseDTO);
		}
		
		@Test
		@DisplayName("should return 404 if reservation not found")
		void shouldReturnNotFoundIfReservationNotFound() throws Exception {
			when(reservationService.update(1L, reservationUpdateRequestDTO)).thenThrow(new EntityReservationNotFoundException(1L));
			
			mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(reservationUpdateRequestDTO)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("Reservation not found with id: " + 1L));
		}
		
		@ParameterizedTest(name = "should return 400 when id is invalid: {0}")
		@ValueSource(strings = {"invalid-id", "abc"})
		@DisplayName("should return 400 when id is invalid")
		void shouldReturnBadRequestIfIdIsInvalid(String invalidId) throws Exception {
			mockMvc.perform(put(URL_PREFIX + "/{id}", invalidId)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(reservationUpdateRequestDTO)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
		
		@ParameterizedTest(name = "should return 400 when update request is invalid: {0}")
		@MethodSource("invalidReservationUpdateRequestDTOs")
		@DisplayName("should return 400 when update request is invalid")
		void shouldReturnBadRequestWhenUpdateWithInvalidData(ReservationUpdateRequestDTO invalidDto) throws Exception {
			mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidDto)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
	}
	
	@Nested
	@DisplayName("DELETE " + URL_PREFIX + "/{id}")
	class DeleteReservation {
		
		@Test
		@DisplayName("should delete reservation by id")
		void shouldDeleteReservation() throws Exception {
			doNothing().when(reservationService).delete(1L);
			
			mockMvc.perform(delete(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isNoContent());
		}
		
		@Test
		@DisplayName("should return 404 when deleting non-existent reservation")
		void shouldReturnNotFoundIfReservationNotFound() throws Exception {
			doThrow(new EntityReservationNotFoundException(1L))
					.when(reservationService)
					.delete(1L);
			
			mockMvc.perform(delete(URL_PREFIX + "/{id}", 1L))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("Reservation not found with id: " + 1L));
		}
		
		@ParameterizedTest(name = "should return 400 if ID is invalid: {0}")
		@ValueSource(strings = {"invalid-id", "abc"})
		@DisplayName("should return 400 if ID is invalid")
		void shouldReturnBadRequestIfIdIsInvalid(String invalidId) throws Exception {
			mockMvc.perform(delete(URL_PREFIX + "/{id}", invalidId))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid request body format or missing content"));
		}
	}
}