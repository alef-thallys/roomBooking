package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.RoomResponseDTO;
import com.github.alefthallys.roombooking.exceptions.EntityRoomAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.mappers.RoomMapper;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
	
	private final RoomRepository roomRepository;
	
	public RoomService(RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}
	
	public List<RoomResponseDTO> findAll() {
		return roomRepository.findAll().stream()
				.map(RoomMapper::toDto)
				.toList();
	}
	
	public RoomResponseDTO findById(Long id) {
		Room room = roomRepository.findById(id).orElseThrow(() -> new EntityRoomNotFoundException(id));
		return RoomMapper.toDto(room);
	}
	
	public RoomResponseDTO create(RoomRequestDTO room) {
		if (roomRepository.existsByName(room.name())) {
			throw new EntityRoomAlreadyExistsException(room.name());
		}
		
		Room roomToSave = RoomMapper.toEntity(room);
		return RoomMapper.toDto(roomRepository.save(roomToSave));
	}
	
	public RoomResponseDTO update(Long id, RoomRequestDTO room) {
		Room roomToUpdate = roomRepository.findById(id).orElseThrow(
				() -> new EntityRoomNotFoundException(id));
		
		roomToUpdate.setName(room.name());
		roomToUpdate.setDescription(room.description());
		roomToUpdate.setCapacity(room.capacity());
		roomToUpdate.setAvailable(room.available());
		roomToUpdate.setLocation(room.location());
		
		return RoomMapper.toDto(roomRepository.save(roomToUpdate));
	}
	
	public void delete(Long id) {
		Room roomById = roomRepository.findById(id).orElseThrow(
				() -> new EntityRoomNotFoundException(id));
		roomRepository.delete(roomById);
	}
}
