package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.dtos.Room.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.Room.RoomResponseDTO;
import com.github.alefthallys.roombooking.services.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
	public ResponseEntity<List<RoomResponseDTO>> findAll() {
		return ResponseEntity.ok(roomService.findAll());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<RoomResponseDTO> findById(@PathVariable Long id) {
		return ResponseEntity.ok(roomService.findById(id));
	}
	
	@PostMapping
	public ResponseEntity<RoomResponseDTO> create(@RequestBody @Valid RoomRequestDTO room) {
		return new ResponseEntity<>(roomService.create(room), HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<RoomResponseDTO> update(@PathVariable Long id, @RequestBody @Valid RoomRequestDTO room) {
		return ResponseEntity.ok(roomService.update(id, room));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		roomService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
