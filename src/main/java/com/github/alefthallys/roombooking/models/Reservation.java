package com.github.alefthallys.roombooking.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private User userId;
	private Room roomId;
	private String startDate;
	private String endDate;
	private Status status;
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Reservation that = (Reservation) o;
		return Objects.equals(getId(), that.getId()) && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getRoomId(), that.getRoomId()) && Objects.equals(getStartDate(), that.getStartDate()) && Objects.equals(getEndDate(), that.getEndDate()) && getStatus() == that.getStatus();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getId(), getUserId(), getRoomId(), getStartDate(), getEndDate(), getStatus());
	}
	
	private enum Status {
		PENDING,
		CONFIRMED,
		CANCELLED
	}
}
