package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.RoomResponseDTO;
import com.github.alefthallys.roombooking.exceptions.EntityRoomAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.mappers.RoomMapper;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.repositories.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
	
	private final RoomRepository roomRepository;
	
	public RoomService(RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}
	
	private static void validateIdOrThrowException(Long id) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Invalid user ID: " + id);
		}
	}
	
	@Transactional(readOnly = true)
	public List<RoomResponseDTO> findAll() {
		return roomRepository.findAll()
				.stream()
				.map(RoomMapper::toDto)
				.collect(Collectors.toList());
	}
	
	@Transactional(readOnly = true)
	public RoomResponseDTO findById(Long id) {
		validateIdOrThrowException(id);
		return roomRepository.findById(id)
				.map(RoomMapper::toDto)
				.orElseThrow(() -> new EntityRoomNotFoundException(id));
	}
	
	@Transactional
	public RoomResponseDTO create(RoomRequestDTO roomRequestDTO) {
		if (roomRepository.existsByName(roomRequestDTO.name())) {
			throw new EntityRoomAlreadyExistsException(roomRequestDTO.name());
		}
		Room room = RoomMapper.toEntity(roomRequestDTO);
		room = roomRepository.save(room);
		return RoomMapper.toDto(room);
	}
	
	@Transactional
	public RoomResponseDTO update(Long id, RoomRequestDTO roomRequestDTO) {
		validateIdOrThrowException(id);
		
		Room room = roomRepository.findById(id)
				.orElseThrow(() -> new EntityRoomNotFoundException(id));
		
		room.setName(roomRequestDTO.name());
		room.setDescription(roomRequestDTO.description());
		room.setCapacity(roomRequestDTO.capacity());
		room.setAvailable(roomRequestDTO.available());
		room.setLocation(roomRequestDTO.location());
		
		if (roomRepository.existsByName(room.getName())) {
			throw new EntityRoomAlreadyExistsException(room.getName());
		}
		
		room = roomRepository.save(room);
		return RoomMapper.toDto(room);
	}
	
	@Transactional
	public void delete(Long id) {
		validateIdOrThrowException(id);
		Room room = roomRepository.findById(id)
				.orElseThrow(() -> new EntityRoomNotFoundException(id));
		roomRepository.delete(room);
	}
}
