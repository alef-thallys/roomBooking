package com.github.alefthallys.roombooking.testBuilders;

import com.github.alefthallys.roombooking.dtos.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.ReservationResponseDTO;
import com.github.alefthallys.roombooking.dtos.ReservationUpdateRequestDTO;
import com.github.alefthallys.roombooking.mappers.RoomMapper;
import com.github.alefthallys.roombooking.mappers.UserMapper;
import com.github.alefthallys.roombooking.models.Reservation;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.models.User;

import java.time.LocalDateTime;

public class ReservationTestBuilder {
	
	private Long id = 1L;
	private LocalDateTime startDate = LocalDateTime.now().plusDays(1);
	private LocalDateTime endDate = LocalDateTime.now().plusDays(2);
	private User user = UserTestBuilder.anUser().build();
	private Room room = RoomTestBuilder.aRoom().build();
	
	public static ReservationTestBuilder aReservation() {
		return new ReservationTestBuilder();
	}
	
	public ReservationTestBuilder withId(Long id) {
		this.id = id;
		return this;
	}
	
	public ReservationTestBuilder withStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
		return this;
	}
	
	public ReservationTestBuilder withEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
		return this;
	}
	
	public ReservationTestBuilder withUser(User user) {
		this.user = user;
		return this;
	}
	
	public ReservationTestBuilder withRoom(Room room) {
		this.room = room;
		return this;
	}
	
	public Reservation build() {
		Reservation reservation = new Reservation();
		reservation.setId(id);
		reservation.setStartDate(startDate);
		reservation.setEndDate(endDate);
		reservation.setUser(user);
		reservation.setRoom(room);
		return reservation;
	}
	
	public ReservationRequestDTO buildRequestDTO() {
		return new ReservationRequestDTO(room.getId(), startDate, endDate);
	}
	
	public ReservationUpdateRequestDTO buildUpdateRequestDTO() {
		return new ReservationUpdateRequestDTO(startDate, endDate);
	}
	
	public ReservationResponseDTO buildResponseDTO() {
		return new ReservationResponseDTO(id, startDate, endDate, UserMapper.toDto(user), RoomMapper.toDto(room));
	}
}