package com.github.alefthallys.roombooking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alefthallys.roombooking.dtos.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.ReservationResponseDTO;
import com.github.alefthallys.roombooking.dtos.ReservationUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.EntityReservationAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityReservationNotFoundException;
import com.github.alefthallys.roombooking.mappers.RoomMapper;
import com.github.alefthallys.roombooking.mappers.UserMapper;
import com.github.alefthallys.roombooking.models.Reservation;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.security.jwt.JwtAuthenticationFilter;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import com.github.alefthallys.roombooking.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReservationControllerTest {
	
	private static final String URL_PREFIX = "/api/v1/reservations";
	
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
	
	@BeforeEach
	void setUp() {
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setEmail("test@gmail.com");
		user.setPassword("password");
		user.setRole(User.Role.ROLE_USER);
		
		Room room = new Room();
		room.setId(1L);
		room.setName("Test Room");
		room.setCapacity(10);
		room.setDescription("A test room for reservations");
		room.setLocation("Test Location");
		room.setAvailable(true);
		
		Reservation reservation = new Reservation();
		reservation.setId(1L);
		reservation.setStartDate(LocalDateTime.now());
		reservation.setEndDate(LocalDateTime.now().plusHours(24));
		reservation.setUser(user);
		reservation.setRoom(room);
		
		reservationRequestDTO = new ReservationRequestDTO(
				reservation.getRoom().getId(),
				reservation.getStartDate(),
				reservation.getEndDate());
		
		reservationResponseDTO = new ReservationResponseDTO(
				reservation.getRoom().getId(),
				reservation.getStartDate(),
				reservation.getEndDate(),
				UserMapper.toDto(user),
				RoomMapper.toDto(room));
		
		reservationUpdateRequestDTO = new ReservationUpdateRequestDTO(
				reservation.getStartDate(),
				reservation.getEndDate());
	}
	
	private void assertReservationResponseDTO(ResultActions resultActions, ReservationResponseDTO response) throws Exception {
		resultActions
				.andExpect(jsonPath("$.id").value(response.id()))
				.andExpect(jsonPath("$.startDate").value(String.valueOf(response.startDate())))
				.andExpect(jsonPath("$.endDate").value(String.valueOf(response.endDate())))
				.andExpect(jsonPath("$.user.id").value(response.user().id()))
				.andExpect(jsonPath("$.user.name").value(response.user().name()))
				.andExpect(jsonPath("$.room.id").value(response.room().id()))
				.andExpect(jsonPath("$.room.name").value(response.room().name()));
	}
	
	@Nested
	@DisplayName("GET /api/v1/reservations")
	class FindAllReservations {
		
		@Test
		@DisplayName("should return a list of reservations")
		void shouldReturnListOfReservations() throws Exception {
			when(reservationService.findAll()).thenReturn(List.of(reservationResponseDTO));
			
			mockMvc.perform(get(URL_PREFIX))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].id").value(reservationResponseDTO.id()))
					.andExpect(jsonPath("$[0].startDate").value(String.valueOf(reservationResponseDTO.startDate())))
					.andExpect(jsonPath("$[0].endDate").value(String.valueOf(reservationResponseDTO.endDate())))
					.andExpect(jsonPath("$[0].user.id").value(reservationResponseDTO.user().id()))
					.andExpect(jsonPath("$[0].user.name").value(reservationResponseDTO.user().name()))
					.andExpect(jsonPath("$[0].room.id").value(reservationResponseDTO.room().id()))
					.andExpect(jsonPath("$[0].room.name").value(reservationResponseDTO.room().name()));
		}
	}
	
	@Nested
	@DisplayName("GET /api/v1/reservations/{id}")
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
			
			mockMvc.perform(get(URL_PREFIX + "/{id}", 1L)).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Reservation not found with id: " + 1L));
		}
		
		@Test
		@DisplayName("should return 400 if ID is invalid")
		void shouldThrowMethodArgumentNotValidException() throws Exception {
			mockMvc.perform(get(URL_PREFIX + "/{id}", "invalid-id")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
	
	@Nested
	@DisplayName("POST /api/v1/reservations")
	class CreateReservation {
		
		@Test
		@DisplayName("should create a new reservation")
		void shouldCreateNewReservation() throws Exception {
			when(reservationService.create(reservationRequestDTO)).thenReturn(reservationResponseDTO);
			
			assertReservationResponseDTO(mockMvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reservationRequestDTO))).andExpect(status().isCreated()),
					
					reservationResponseDTO);
		}
		
		@Test
		@DisplayName("should return 400 when reservation date has invalid format")
		void shouldReturnBadRequestIfReservationDateHasInvalidFormat() throws Exception {
			reservationRequestDTO = new ReservationRequestDTO(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now());
			mockMvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reservationRequestDTO))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"));
		}
		
		@Test
		@DisplayName("should return 409 when reservation already exists")
		void shouldReturnConflictIfReservationAlreadyExists() throws Exception {
			when(reservationService.create(reservationRequestDTO)).thenThrow(new EntityReservationAlreadyExistsException(1L));
			
			mockMvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reservationRequestDTO))).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("Reservation with ID " + 1L + " already exists."));
		}
		
		@Test
		@DisplayName("should return 400 if request body is invalid")
		void shouldReturnBadRequestIfRequestBodyIsInvalid() throws Exception {
			reservationRequestDTO = new ReservationRequestDTO(null, null, null);
			
			mockMvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reservationRequestDTO))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
	
	@Nested
	@DisplayName("PUT /api/v1/reservations/{id}")
	class UpdateReservation {
		
		@Test
		@DisplayName("should update reservation")
		void shouldUpdateReservation() throws Exception {
			when(reservationService.update(1L, reservationUpdateRequestDTO)).thenReturn(reservationResponseDTO);
			
			assertReservationResponseDTO(
					mockMvc.perform(put(URL_PREFIX + "/{id}", 1L)
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(reservationRequestDTO)))
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
							.content(objectMapper.writeValueAsString(reservationRequestDTO)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("Reservation not found with id: " + 1L));
		}
		
		@Test
		@DisplayName("should return 400 when id is invalid")
		void shouldThrowMethodArgumentNotValidException() throws Exception {
			mockMvc.perform(put(URL_PREFIX + "/{id}", "invalid-id")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(reservationRequestDTO)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
	
	@Nested
	@DisplayName("DELETE /api/v1/reservations/{id}")
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
		
		@Test
		@DisplayName("should return 400 if ID is invalid")
		void shouldReturnBadRequestIfIdIsInvalid() throws Exception {
			mockMvc.perform(delete(URL_PREFIX + "/{id}", "invalid-id"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Validation failed"));
		}
	}
}
