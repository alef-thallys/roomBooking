package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.Reservation.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationResponseDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.Reservation.EntityReservationNotFoundException;
import com.github.alefthallys.roombooking.exceptions.Room.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.mappers.ReservationMapper;
import com.github.alefthallys.roombooking.models.Reservation;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.ReservationRepository;
import com.github.alefthallys.roombooking.repositories.RoomRepository;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {
	
	private final ReservationRepository reservationRepository;
	private final RoomRepository roomRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthService authService;
	private final UserRepository userRepository;
	
	public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository, JwtTokenProvider jwtTokenProvider, AuthService authService, UserRepository userRepository) {
		this.reservationRepository = reservationRepository;
		this.roomRepository = roomRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.authService = authService;
		this.userRepository = userRepository;
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
	
	@Transactional
	public ReservationResponseDTO create(ReservationRequestDTO reservationDTO) {
		if (!roomRepository.existsById(reservationDTO.roomId())) {
			throw new EntityRoomNotFoundException(reservationDTO.roomId());
		}
		
		User currentUser = jwtTokenProvider.getCurrentUser();
		Room roomById = roomRepository.findById(reservationDTO.roomId()).orElseThrow(() -> new EntityRoomNotFoundException(reservationDTO.roomId()));
		
		Reservation reservationToSave = new Reservation();
		reservationToSave.setRoom(roomById);
		reservationToSave.setUser(currentUser);
		reservationToSave.setStartDate(reservationDTO.startDate());
		reservationToSave.setEndDate(reservationDTO.endDate());
		
		return ReservationMapper.toDto(reservationRepository.save(reservationToSave));
	}
	
	@Transactional
	public ReservationResponseDTO update(Long id, ReservationUpdateRequestDTO reservationDTO) {
		validateIdOrThrowException(id);
		
		Reservation reservationById = reservationRepository.findById(id).orElseThrow(
				() -> new EntityReservationNotFoundException(id));
		
		authService.validateUserOwnership(reservationById.getUser());
		
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
}
