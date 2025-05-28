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
	
	@Column(nullable = false, unique = true)
	private String name;
	
	@Column
	private String description;
	
	@Column(nullable = false)
	private int capacity;
	
	@Column(nullable = false)
	private String location;
	
	public Room(String name, String description, int capacity, String location) {
		this.name = name;
		this.description = description;
		this.capacity = capacity;
		this.location = location;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Room room = (Room) o;
		return Objects.equals(getId(), room.getId());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}
}
