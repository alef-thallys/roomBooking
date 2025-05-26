package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.ReservationResponseDTO;
import com.github.alefthallys.roombooking.dtos.ReservationUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.EntityReservationNotFoundException;
import com.github.alefthallys.roombooking.exceptions.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.exceptions.RoomNotAvailableException;
import com.github.alefthallys.roombooking.mappers.ReservationMapper;
import com.github.alefthallys.roombooking.models.Reservation;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.ReservationRepository;
import com.github.alefthallys.roombooking.repositories.RoomRepository;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.util.List;

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
	
	public List<ReservationResponseDTO> findAll() {
		return reservationRepository.findAll().stream()
				.map(ReservationMapper::toDto)
				.toList();
	}
	
	public ReservationResponseDTO findById(Long id) {
		Reservation reservationById = reservationRepository.findById(id).orElseThrow(() -> new EntityReservationNotFoundException(id));
		return ReservationMapper.toDto(reservationById);
	}
	
	public ReservationResponseDTO create(ReservationRequestDTO reservationDTO) {
		if (!roomRepository.existsById(reservationDTO.roomId())) {
			throw new EntityRoomNotFoundException(reservationDTO.roomId());
		}
		
		if (!roomRepository.isRoomAvailable(reservationDTO.roomId())) {
			throw new RoomNotAvailableException(reservationDTO.roomId());
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
	
	public ReservationResponseDTO update(Long id, ReservationUpdateRequestDTO reservationDTO) {
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
	
	public void delete(Long id) {
		Reservation reservationById = reservationRepository.findById(id).orElseThrow(
				() -> new EntityReservationNotFoundException(id));
		
		authService.validateUserOwnership(reservationById.getUser());
		reservationRepository.delete(reservationById);
	}
}
