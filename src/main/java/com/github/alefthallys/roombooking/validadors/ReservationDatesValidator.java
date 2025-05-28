package com.github.alefthallys.roombooking.validadors;

import com.github.alefthallys.roombooking.annotations.ValidReservationDates;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationUpdateRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class ReservationDatesValidator implements ConstraintValidator<ValidReservationDates, Object> {
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value instanceof ReservationRequestDTO dto) {
			return isValidDates(dto.startDate(), dto.endDate());
		}
		
		if (value instanceof ReservationUpdateRequestDTO dto) {
			return isValidDates(dto.startDate(), dto.endDate());
		}
		return true;
	}
	
	private boolean isValidDates(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null) return true;
		return start.isBefore(end);
	}
	
}
