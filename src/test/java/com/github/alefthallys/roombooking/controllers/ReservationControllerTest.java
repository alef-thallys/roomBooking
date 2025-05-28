package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
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
	
	private ReservationRequestDTO reservationRequestDTO;
	private ReservationResponseDTO reservationResponseDTO;
	private ReservationUpdateRequestDTO reservationUpdateRequestDTO;
	private Reservation mockReservation;
	
	@BeforeEach
	void setUp() {
		reservationRequestDTO = ReservationTestBuilder.aReservation().buildRequestDTO();
		reservationUpdateRequestDTO = ReservationTestBuilder.aReservation().buildUpdateRequestDTO();
		reservationResponseDTO = ReservationTestBuilder.aReservation().buildResponseDTO();
		mockReservation = ReservationTestBuilder.aReservation().build();
	}
	
	private void assertReservationResponseDTO(ResultActions resultActions, ReservationResponseDTO response) throws Exception {
		resultActions
				.andExpect(jsonPath("$.id").value(response.id()))
				.andExpect(jsonPath("$.startDate").value(response.startDate().truncatedTo(ChronoUnit.SECONDS).toString()))
				.andExpect(jsonPath("$.endDate").value(response.endDate().truncatedTo(ChronoUnit.SECONDS).toString()))
				.andExpect(jsonPath("$.user.id").value(response.user().id()))
				.andExpect(jsonPath("$.user.name").value(response.user().name()))
				.andExpect(jsonPath("$.room.id").value(response.room().id()))
				.andExpect(jsonPath("$.room.name").value(response.room().name()));
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX)
	class FindAllReservations {
		
		@Test
		@DisplayName("should return a list of reservations")
		void shouldReturnListOfReservations() throws Exception {
			when(reservationService.findAll()).thenReturn(List.of(reservationResponseDTO));
			
			mockMvc.perform(get(URL_PREFIX))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].id").value(reservationResponseDTO.id()))
					.andExpect(jsonPath("$[0].startDate").value(reservationResponseDTO.startDate().truncatedTo(ChronoUnit.SECONDS).toString()))
					.andExpect(jsonPath("$[0].endDate").value(reservationResponseDTO.endDate().truncatedTo(ChronoUnit.SECONDS).toString()))
					.andExpect(jsonPath("$[0].user.id").value(reservationResponseDTO.user().id()))
					.andExpect(jsonPath("$[0].user.name").value(reservationResponseDTO.user().name()))
					.andExpect(jsonPath("$[0].room.id").value(reservationResponseDTO.room().id()))
					.andExpect(jsonPath("$[0].room.name").value(reservationResponseDTO.room().name()));
		}
	}
	
	@Nested
	@DisplayName("GET " + URL_PREFIX + "/{id}")
	class FindReservationById {
		
		@Test
		@DisplayName("should return a reservation by ID")
		void shouldReturnReservationById() throws Exception {
			when(reservationService.findById(1L)).thenReturn(reservationResponseDTO);
			
			assertReservationResponseDTO(mockMvc.perform(get(URL_PREFIX + "/{id}", 1L)), reservationResponseDTO);
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
		@DisplayName("should create a new reservation")
		void shouldCreateNewReservation() throws Exception {
			when(reservationService.create(reservationRequestDTO)).thenReturn(reservationResponseDTO);
			
			assertReservationResponseDTO(mockMvc.perform(post(URL_PREFIX)
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(reservationRequestDTO)))
							.andExpect(status().isCreated()),
					reservationResponseDTO);
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
		@DisplayName("should update reservation")
		void shouldUpdateReservation() throws Exception {
			when(reservationService.update(1L, reservationUpdateRequestDTO)).thenReturn(reservationResponseDTO);
			
			assertReservationResponseDTO(
					mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(reservationUpdateRequestDTO)))
							.andExpect(status().isOk()),
					reservationResponseDTO
			);
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
