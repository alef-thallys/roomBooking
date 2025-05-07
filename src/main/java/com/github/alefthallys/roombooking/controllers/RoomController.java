package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.dtos.RoomDTO;
import com.github.alefthallys.roombooking.services.RoomService;
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
	public List<RoomDTO> findAll() {
		return roomService.findAll();
	}
	
	@GetMapping("/{id}")
	public RoomDTO findById(@PathVariable Long id) {
		return roomService.findById(id);
	}
	
	@PostMapping
	public RoomDTO create(@RequestBody RoomDTO room) {
		return roomService.create(room);
	}

	@PutMapping("/{id}")
	public RoomDTO update(@PathVariable Long id, @RequestBody RoomDTO room) {
		return roomService.update(id, room);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		roomService.delete(id);
	}
}
