package com.github.alefthallys.roombooking.mappers;

import com.github.alefthallys.roombooking.dtos.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.RoomResponseDTO;
import com.github.alefthallys.roombooking.models.Room;

public class RoomMapper {
	
	public static RoomResponseDTO toDto(Room room) {
		return new RoomResponseDTO(
				room.getId(),
				room.getName(),
				room.getDescription(),
				room.getCapacity(),
				room.isAvailable(),
				room.getLocation()
		);
	}
	
	public static Room toEntity(RoomRequestDTO roomDTO) {
		return new Room(
				roomDTO.name(),
				roomDTO.description(),
				roomDTO.capacity(),
				roomDTO.available(),
				roomDTO.location()
		);
	}
}
