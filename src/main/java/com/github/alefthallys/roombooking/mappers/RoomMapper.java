package com.github.alefthallys.roombooking.mappers;

import com.github.alefthallys.roombooking.dtos.RoomDTO;
import com.github.alefthallys.roombooking.models.Room;

public class RoomMapper {
	
	public static RoomDTO toDto(Room room) {
		return new RoomDTO(
				room.getId(),
				room.getName(),
				room.getDescription(),
				room.getCapacity(),
				room.isAvailable(),
				room.getLocation()
		);
	}
	
	public static Room toEntity(RoomDTO roomDTO) {
		return new Room(
				roomDTO.id(),
				roomDTO.name(),
				roomDTO.description(),
				roomDTO.capacity(),
				roomDTO.available(),
				roomDTO.location()
		);
	}
}
