package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.assemblers.RoomModelAssembler;
import com.github.alefthallys.roombooking.dtos.Room.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.Room.RoomResponseDTO;
import com.github.alefthallys.roombooking.services.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
	private final RoomModelAssembler roomModelAssembler;
	
	public RoomController(RoomService roomService, RoomModelAssembler roomModelAssembler) {
		this.roomService = roomService;
		this.roomModelAssembler = roomModelAssembler;
	}
	
	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Find all rooms")
	public ResponseEntity<CollectionModel<EntityModel<RoomResponseDTO>>> findAll() {
		List<RoomResponseDTO> roomResponseDTOList = roomService.findAll();
		CollectionModel<EntityModel<RoomResponseDTO>> collectionModel = roomModelAssembler.toCollectionModel(roomResponseDTOList);
		return ResponseEntity.ok(collectionModel);
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Find room by ID")
	public ResponseEntity<EntityModel<RoomResponseDTO>> findById(@PathVariable Long id) {
		RoomResponseDTO roomResponseDTO = roomService.findById(id);
		EntityModel<RoomResponseDTO> model = roomModelAssembler.toModel(roomResponseDTO);
		return ResponseEntity.ok(model);
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a new room")
	public ResponseEntity<EntityModel<RoomResponseDTO>> create(@RequestBody @Valid RoomRequestDTO room) {
		RoomResponseDTO roomResponseDTO = roomService.create(room);
		EntityModel<RoomResponseDTO> model = roomModelAssembler.toModel(roomResponseDTO);
		return new ResponseEntity<>(model, HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update an existing room")
	public ResponseEntity<EntityModel<RoomResponseDTO>> update(@PathVariable Long id, @RequestBody @Valid RoomRequestDTO room) {
		RoomResponseDTO roomResponseDTO = roomService.update(id, room);
		EntityModel<RoomResponseDTO> model = roomModelAssembler.toModel(roomResponseDTO);
		return ResponseEntity.ok(model);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete a room")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		roomService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
