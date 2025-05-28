package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.Reservation.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationResponseDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.Reservation.EntityReservationNotFoundException;
import com.github.alefthallys.roombooking.exceptions.Room.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.models.Reservation;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.ReservationRepository;
import com.github.alefthallys.roombooking.repositories.RoomRepository;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import com.github.alefthallys.roombooking.testBuilders.ReservationTestBuilder;
import com.github.alefthallys.roombooking.testBuilders.RoomTestBuilder;
import com.github.alefthallys.roombooking.testBuilders.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
	
	@InjectMocks
	private ReservationService reservationService;
	
	@Mock
	private ReservationRepository reservationRepository;
	
	@Mock
	private RoomRepository roomRepository;
	
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	
	@Mock
	private AuthService authService;
	
	@Mock
	private UserRepository userRepository;
	
	private Reservation reservation;
	private ReservationRequestDTO reservationRequestDTO;
	private ReservationUpdateRequestDTO reservationUpdateRequestDTO;
	private ReservationResponseDTO reservationResponseDTO;
	private User user;
	private Room room;
	
	@BeforeEach
	void setUp() {
		user = UserTestBuilder.anUser().build();
		room = RoomTestBuilder.aRoom().build();
		reservation = ReservationTestBuilder.aReservation().withUser(user).withRoom(room).build();
		reservationRequestDTO = ReservationTestBuilder.aReservation().withRoom(room).buildRequestDTO();
		reservationUpdateRequestDTO = ReservationTestBuilder.aReservation().buildUpdateRequestDTO();
		reservationResponseDTO = ReservationTestBuilder.aReservation().withUser(user).withRoom(room).buildResponseDTO();
	}
	
	private void assertEqualsResponseDTO(Reservation expectedReservation, ReservationResponseDTO actualResponseDTO) {
		assertEquals(expectedReservation.getId(), actualResponseDTO.id());
		assertEquals(expectedReservation.getStartDate().toLocalDate(), actualResponseDTO.startDate().toLocalDate());
		assertEquals(expectedReservation.getEndDate().toLocalDate(), actualResponseDTO.endDate().toLocalDate());
		assertEquals(expectedReservation.getUser().getId(), actualResponseDTO.user().id());
		assertEquals(expectedReservation.getRoom().getId(), actualResponseDTO.room().id());
	}
	
	@Nested
	@DisplayName("Find All Reservations")
	class FindAllReservations {
		
		@Test
		@DisplayName("Should return a list of reservations")
		void shouldReturnAListOfReservations() {
			when(reservationRepository.findAll()).thenReturn(List.of(reservation));
			List<ReservationResponseDTO> result = reservationService.findAll();
			assertEquals(1, result.size());
			assertEqualsResponseDTO(reservation, result.get(0));
		}
		
		@Test
		@DisplayName("Should return an empty list when no reservations are found")
		void shouldReturnAnEmptyList() {
			when(reservationRepository.findAll()).thenReturn(List.of());
			List<ReservationResponseDTO> result = reservationService.findAll();
			assertEquals(0, result.size());
		}
	}
	
	@Nested
	@DisplayName("Find Reservation By ID")
	class FindReservationById {
		
		@Test
		@DisplayName("Should return reservation by id")
		void shouldReturnReservationById() {
			when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
			ReservationResponseDTO result = reservationService.findById(1L);
			assertEqualsResponseDTO(reservation, result);
		}
		
		@Test
		@DisplayName("Should throw EntityReservationNotFoundException when reservation is not found")
		void shouldThrowEntityReservationNotFoundException() {
			when(reservationRepository.findById(1L)).thenReturn(Optional.empty());
			assertThrows(EntityReservationNotFoundException.class, () -> reservationService.findById(1L));
		}
		
		@ParameterizedTest(name = "Should throw IllegalArgumentException when user id is invalid: {0}")
		@NullSource
		@ValueSource(longs = {0L, -1L})
		@DisplayName("Should throw IllegalArgumentException when user id is invalid")
		void shouldThrowIllegalArgumentExceptionWhenUserIdIsInvalid(Long invalidId) {
			assertThrows(IllegalArgumentException.class, () -> reservationService.findById(invalidId));
			verify(userRepository, never()).findById(anyLong());
		}
	}
	
	@Nested
	@DisplayName("Create Reservation")
	class CreateReservation {
		
		@Test
		@DisplayName("Should create a new reservation")
		void shouldCreateNewReservation() {
			when(roomRepository.existsById(reservationRequestDTO.roomId())).thenReturn(true);
			when(jwtTokenProvider.getCurrentUser()).thenReturn(user);
			when(roomRepository.findById(reservationRequestDTO.roomId())).thenReturn(Optional.of(room));
			when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
			
			ReservationResponseDTO result = reservationService.create(reservationRequestDTO);
			
			assertEqualsResponseDTO(reservation, result);
			verify(reservationRepository, times(1)).save(any(Reservation.class));
		}
		
		@Test
		@DisplayName("Should throw EntityRoomNotFoundException when room does not exist")
		void shouldThrowEntityRoomNotFoundExceptionWhenRoomDoesNotExist() {
			when(roomRepository.existsById(reservationRequestDTO.roomId())).thenReturn(false);
			assertThrows(EntityRoomNotFoundException.class, () -> reservationService.create(reservationRequestDTO));
			verify(reservationRepository, never()).save(any(Reservation.class));
		}
	}
	
	@Nested
	@DisplayName("Update Reservation")
	class UpdateReservation {
		
		@Test
		@DisplayName("Should update reservation")
		void shouldUpdateReservation() {
			when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
			doNothing().when(authService).validateUserOwnership(any(User.class));
			when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
			
			ReservationResponseDTO result = reservationService.update(1L, reservationUpdateRequestDTO);
			
			assertEqualsResponseDTO(reservation, result);
			verify(authService, times(1)).validateUserOwnership(any(User.class));
			verify(reservationRepository, times(1)).save(any(Reservation.class));
		}
		
		@Test
		@DisplayName("Should throw EntityReservationNotFoundException when reservation is not found")
		void shouldThrowEntityReservationNotFoundExceptionWhenReservationIsNotFound() {
			when(reservationRepository.findById(1L)).thenReturn(Optional.empty());
			assertThrows(EntityReservationNotFoundException.class, () -> reservationService.update(1L, reservationUpdateRequestDTO));
			verify(authService, never()).validateUserOwnership(any(User.class));
			verify(reservationRepository, never()).save(any(Reservation.class));
		}
		
		@ParameterizedTest(name = "Should throw IllegalArgumentException when user id is invalid: {0}")
		@NullSource
		@ValueSource(longs = {0L, -1L})
		@DisplayName("Should throw IllegalArgumentException when user id is invalid")
		void shouldThrowIllegalArgumentExceptionWhenUserIdIsInvalid(Long invalidId) {
			assertThrows(IllegalArgumentException.class, () -> reservationService.findById(invalidId));
			verify(userRepository, never()).findById(anyLong());
		}
	}
	
	@Nested
	@DisplayName("Delete Reservation")
	class DeleteReservation {
		
		@Test
		@DisplayName("Should delete reservation")
		void shouldDeleteReservation() {
			when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
			doNothing().when(authService).validateUserOwnership(any(User.class));
			doNothing().when(reservationRepository).delete(any(Reservation.class));
			
			reservationService.delete(1L);
			
			verify(authService, times(1)).validateUserOwnership(any(User.class));
			verify(reservationRepository, times(1)).delete(any(Reservation.class));
		}
		
		@Test
		@DisplayName("Should throw EntityReservationNotFoundException when reservation is not found")
		void shouldThrowEntityReservationNotFoundExceptionWhenReservationIsNotFound() {
			when(reservationRepository.findById(1L)).thenReturn(Optional.empty());
			assertThrows(EntityReservationNotFoundException.class, () -> reservationService.delete(1L));
			verify(authService, never()).validateUserOwnership(any(User.class));
			verify(reservationRepository, never()).delete(any(Reservation.class));
		}
		
		@ParameterizedTest(name = "Should throw IllegalArgumentException when user id is invalid: {0}")
		@NullSource
		@ValueSource(longs = {0L, -1L})
		@DisplayName("Should throw IllegalArgumentException when user id is invalid")
		void shouldThrowIllegalArgumentExceptionWhenUserIdIsInvalid(Long invalidId) {
			assertThrows(IllegalArgumentException.class, () -> reservationService.findById(invalidId));
			verify(userRepository, never()).findById(anyLong());
		}
	}
}