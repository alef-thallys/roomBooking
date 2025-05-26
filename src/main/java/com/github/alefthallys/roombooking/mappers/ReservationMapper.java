package com.github.alefthallys.roombooking.mappers;

import com.github.alefthallys.roombooking.dtos.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.ReservationResponseDTO;
import com.github.alefthallys.roombooking.models.Reservation;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.models.User;

public class ReservationMapper {
	
	public static ReservationResponseDTO toDto(Reservation reservation) {
		return new ReservationResponseDTO(
				reservation.getId(),
				reservation.getStartDate(),
				reservation.getEndDate(),
				UserMapper.toDto(reservation.getUser()),
				RoomMapper.toDto(reservation.getRoom())
		);
	}
	
	public static Reservation toEntity(ReservationRequestDTO dto, User user, Room room) {
		Reservation reservation = new Reservation();
		reservation.setStartDate(dto.startDate());
		reservation.setEndDate(dto.endDate());
		reservation.setUser(user);
		reservation.setRoom(room);
		return reservation;
	}
}
