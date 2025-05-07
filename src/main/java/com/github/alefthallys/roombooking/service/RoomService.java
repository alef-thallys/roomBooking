package com.github.alefthallys.roombooking.service;

import com.github.alefthallys.roombooking.model.Room;
import com.github.alefthallys.roombooking.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
	
	private final RoomRepository roomRepository;
	
	public RoomService(RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}
	
	public List<Room> findAll() {
		return roomRepository.findAll();
	}
	
	public Room findById(Long id) {
		return roomRepository.findById(id).orElse(null);
	}
	
	public Room create(Room room) {
		return roomRepository.save(room);
	}
	
	public Room update(Long id, Room room) {
		Room roomToUpdate = roomRepository.findById(id).orElse(null);
		return roomRepository.save(roomToUpdate);
	}
	
	public void delete(Long id) {
		roomRepository.deleteById(id);
	}
}
