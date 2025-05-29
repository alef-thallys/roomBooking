package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.Email.ReservationConfirmationEmailDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationResponseDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.EntityReservationConflictException;
import com.github.alefthallys.roombooking.exceptions.Reservation.EntityReservationNotFoundException;
import com.github.alefthallys.roombooking.exceptions.Room.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.mappers.ReservationMapper;
import com.github.alefthallys.roombooking.models.Reservation;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.ReservationRepository;
import com.github.alefthallys.roombooking.repositories.RoomRepository;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {
	
	private final ReservationRepository reservationRepository;
	private final RoomRepository roomRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthService authService;
	private final EmailNotificationService emailNotificationService;
	
	
	public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository, JwtTokenProvider jwtTokenProvider, AuthService authService, EmailNotificationService emailNotificationService) {
		this.reservationRepository = reservationRepository;
		this.roomRepository = roomRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.authService = authService;
		this.emailNotificationService = emailNotificationService;
	}
	
	private static void validateIdOrThrowException(Long id) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Invalid user ID: " + id);
		}
	}
	
	@Transactional(readOnly = true)
	public List<ReservationResponseDTO> findAll() {
		return reservationRepository.findAll()
				.stream()
				.map(ReservationMapper::toDto)
				.collect(Collectors.toList());
	}
	
	@Transactional(readOnly = true)
	public ReservationResponseDTO findById(Long id) {
		validateIdOrThrowException(id);
		return reservationRepository.findById(id)
				.map(ReservationMapper::toDto)
				.orElseThrow(() -> new EntityReservationNotFoundException(id));
	}
	
	@Transactional(readOnly = true)
	public List<ReservationResponseDTO> findByUser() {
		User currentUser = jwtTokenProvider.getCurrentUser();
		return reservationRepository.findByUser(currentUser)
				.stream()
				.map(ReservationMapper::toDto)
				.collect(Collectors.toList());
	}
	
	@Transactional
	public ReservationResponseDTO create(ReservationRequestDTO reservationDTO) {
		validateRoomExists(reservationDTO.roomId());
		
		User currentUser = jwtTokenProvider.getCurrentUser();
		Room roomById = roomRepository.findById(reservationDTO.roomId()).orElseThrow(() -> new EntityRoomNotFoundException(reservationDTO.roomId()));
		
		checkReservationConflict(reservationDTO.roomId(), reservationDTO.startDate(), reservationDTO.endDate(), null);
		
		Reservation reservationToSave = new Reservation();
		reservationToSave.setRoom(roomById);
		reservationToSave.setUser(currentUser);
		reservationToSave.setStartDate(reservationDTO.startDate());
		reservationToSave.setEndDate(reservationDTO.endDate());
		
		Reservation savedReservation = reservationRepository.save(reservationToSave);
		ReservationResponseDTO responseDTO = ReservationMapper.toDto(savedReservation);
		
		emailNotificationService.sendReservationConfirmationEmail(
				new ReservationConfirmationEmailDTO(
						currentUser.getEmail(),
						currentUser.getName(),
						roomById.getName(),
						savedReservation.getStartDate(),
						savedReservation.getEndDate(),
						savedReservation.getId()
				)
		);
		
		return responseDTO;
	}
	
	@Transactional
	public ReservationResponseDTO update(Long id, ReservationUpdateRequestDTO reservationDTO) {
		validateIdOrThrowException(id);
		
		Reservation reservationById = reservationRepository.findById(id).orElseThrow(
				() -> new EntityReservationNotFoundException(id));
		
		authService.validateUserOwnership(reservationById.getUser());
		
		checkReservationConflict(reservationById.getRoom().getId(), reservationDTO.startDate(), reservationDTO.endDate(), id);
		
		if (reservationDTO.startDate() != null) {
			reservationById.setStartDate(reservationDTO.startDate());
		}
		
		if (reservationDTO.endDate() != null) {
			reservationById.setEndDate(reservationDTO.endDate());
		}
		
		Reservation savedReservation = reservationRepository.save(reservationById);
		return ReservationMapper.toDto(savedReservation);
	}
	
	@Transactional
	public void delete(Long id) {
		validateIdOrThrowException(id);
		
		Reservation reservationById = reservationRepository.findById(id).orElseThrow(
				() -> new EntityReservationNotFoundException(id));
		
		authService.validateUserOwnership(reservationById.getUser());
		reservationRepository.delete(reservationById);
	}
	
	private void validateRoomExists(Long roomId) {
		if (!roomRepository.existsById(roomId)) {
			throw new EntityRoomNotFoundException(roomId);
		}
	}
	
	@Transactional(readOnly = true)
	public void checkReservationConflict(Long roomId, LocalDateTime newStartDate, LocalDateTime newEndDate, Long currentReservationId) {
		List<Reservation> existingReservations = reservationRepository.findByRoomIdAndStartDateBeforeAndEndDateAfter(
				roomId, newEndDate, newStartDate);
		
		for (Reservation existingReservation : existingReservations) {
			// Skip the current reservation if it's an update operation
			if (currentReservationId != null && existingReservation.getId().equals(currentReservationId)) {
				continue;
			}
			throw new EntityReservationConflictException(
					existingReservation.getStartDate(),
					existingReservation.getEndDate(),
					existingReservation.getRoom().getName()
			);
		}
	}
}
