package com.github.alefthallys.roombooking.service;

import com.github.alefthallys.roombooking.model.Room;
import org.springframework.stereotype.Service;

@Service
public class RoomService {
	
	public String findAll() {
		return "List of rooms";
	}
	
	public String findById(Long id) {
		return "Room by id";
	}
	
	public String create(Room room) {
		return "Room created";
	}
	
	public String update(Long id, Room room) {
		return "Room updated";
	}
	
	public String delete(Long id) {
		return "Room deleted";
	}
}
