package com.github.alefthallys.roombooking.testBuilders;

import com.github.alefthallys.roombooking.dtos.Room.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.Room.RoomResponseDTO;
import com.github.alefthallys.roombooking.models.Room;

public class RoomTestBuilder {
	
	private Long id = 1L;
	private String name = "Room 101";
	private String description = "Conference Room";
	private int capacity = 10;
	private boolean available = true;
	private String location = "1st Floor";
	
	public static RoomTestBuilder aRoom() {
		return new RoomTestBuilder();
	}
	
	public RoomTestBuilder withId(Long id) {
		this.id = id;
		return this;
	}
	
	public RoomTestBuilder withName(String name) {
		this.name = name;
		return this;
	}
	
	public RoomTestBuilder withDescription(String description) {
		this.description = description;
		return this;
	}
	
	public RoomTestBuilder withCapacity(int capacity) {
		this.capacity = capacity;
		return this;
	}
	
	public RoomTestBuilder withAvailable(boolean available) {
		this.available = available;
		return this;
	}
	
	public RoomTestBuilder withLocation(String location) {
		this.location = location;
		return this;
	}
	
	public Room build() {
		Room room = new Room();
		room.setId(id);
		room.setName(name);
		room.setDescription(description);
		room.setCapacity(capacity);
		room.setLocation(location);
		return room;
	}
	
	public RoomRequestDTO buildRequestDTO() {
		return new RoomRequestDTO(name, description, capacity, location);
	}
	
	public RoomResponseDTO buildResponseDTO() {
		return new RoomResponseDTO(id, name, description, capacity, location);
	}
}