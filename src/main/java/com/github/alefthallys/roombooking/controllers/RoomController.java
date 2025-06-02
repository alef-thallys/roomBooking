package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.dtos.Room.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.Room.RoomResponseDTO;
import com.github.alefthallys.roombooking.services.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@Tag(name = "Room Management")
public class RoomController {
	
	private final RoomService roomService;
	
	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}
	
	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Find all rooms")
	public ResponseEntity<List<RoomResponseDTO>> findAll() {
		return ResponseEntity.ok(roomService.findAll());
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Find room by ID")
	public ResponseEntity<RoomResponseDTO> findById(@PathVariable Long id) {
		return ResponseEntity.ok(roomService.findById(id));
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a new room")
	public ResponseEntity<RoomResponseDTO> create(@RequestBody @Valid RoomRequestDTO room) {
		return new ResponseEntity<>(roomService.create(room), HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update an existing room")
	public ResponseEntity<RoomResponseDTO> update(@PathVariable Long id, @RequestBody @Valid RoomRequestDTO room) {
		return ResponseEntity.ok(roomService.update(id, room));
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete a room")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		roomService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
