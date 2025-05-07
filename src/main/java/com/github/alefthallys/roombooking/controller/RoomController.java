package com.github.alefthallys.roombooking.controller;

import com.github.alefthallys.roombooking.model.Room;
import com.github.alefthallys.roombooking.service.RoomService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
	
	private final RoomService roomService;
	
	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}
	
	@GetMapping
	public String findAll() {
		return roomService.findAll();
	}
	
	@GetMapping("/{id}")
	public String findById(@PathVariable Long id) {
		return roomService.findById(id);
	}
	
	@PostMapping
	public String create(@RequestBody Room room) {
		return roomService.create(room);
	}
	
	@PutMapping("/{id}")
	public String update(@PathVariable Long id, @RequestBody Room room) {
		return roomService.update(id, room);
	}
	
	@DeleteMapping("/{id}")
	public String delete(@PathVariable Long id) {
		return roomService.delete(id);
	}
}
