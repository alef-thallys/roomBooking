package com.github.alefthallys.roombooking.controller;

import com.github.alefthallys.roombooking.model.Room;
import com.github.alefthallys.roombooking.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
	
	private final RoomService roomService;
	
	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}
	
	@GetMapping
	public List<Room> findAll() {
		return roomService.findAll();
	}
	
	@GetMapping("/{id}")
	public Room findById(@PathVariable Long id) {
		return roomService.findById(id);
	}
	
	@PostMapping
	public Room create(@RequestBody Room room) {
		return roomService.create(room);
	}
	
	@PutMapping("/{id}")
	public Room update(@PathVariable Long id, @RequestBody Room room) {
		return roomService.update(id, room);
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		roomService.delete(id);
	}
}
