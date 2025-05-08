package com.github.alefthallys.roombooking.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rooms")
public class Room implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	private int capacity;
	private boolean available;
	private String location;
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Room room = (Room) o;
		return getCapacity() == room.getCapacity() && isAvailable() == room.isAvailable() && Objects.equals(getId(), room.getId()) && Objects.equals(getName(), room.getName()) && Objects.equals(getDescription(), room.getDescription()) && Objects.equals(getLocation(), room.getLocation());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getId(), getName(), getDescription(), getCapacity(), isAvailable(), getLocation());
	}
}
