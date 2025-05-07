package com.github.alefthallys.roombooking.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String name;
	private String description;
	private int capacity;
	private boolean available;
	private String location;
	private String imageUrl;
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Room room = (Room) o;
		return getCapacity() == room.getCapacity() && isAvailable() == room.isAvailable() && Objects.equals(getId(), room.getId()) && Objects.equals(getName(), room.getName()) && Objects.equals(getDescription(), room.getDescription()) && Objects.equals(getLocation(), room.getLocation()) && Objects.equals(getImageUrl(), room.getImageUrl());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getId(), getName(), getDescription(), getCapacity(), isAvailable(), getLocation(), getImageUrl());
	}
}
