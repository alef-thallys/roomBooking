package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.assemblers.ReservationModelAssembler;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationResponseDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationUpdateRequestDTO;
import com.github.alefthallys.roombooking.services.ReservationService;
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
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservation Management")
public class ReservationController {
	
	private final ReservationService reservationService;
	private final ReservationModelAssembler reservationModelAssembler;
	
	public ReservationController(ReservationService reservationService, ReservationModelAssembler reservationModelAssembler) {
		this.reservationService = reservationService;
		this.reservationModelAssembler = reservationModelAssembler;
	}
	
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Find all reservations")
	public ResponseEntity<CollectionModel<EntityModel<ReservationResponseDTO>>> findAll() {
		List<ReservationResponseDTO> reservations = reservationService.findAll();
		CollectionModel<EntityModel<ReservationResponseDTO>> collectionModel = reservationModelAssembler.toCollectionModel(reservations);
		return ResponseEntity.ok(collectionModel);
	}
	
	@GetMapping("/me")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Find reservations for the current user")
	public ResponseEntity<CollectionModel<EntityModel<ReservationResponseDTO>>> getMyReservations() {
		List<ReservationResponseDTO> reservationResponseDTOList = reservationService.findByUser();
		CollectionModel<EntityModel<ReservationResponseDTO>> collectionModel = reservationModelAssembler.toCollectionModel(reservationResponseDTOList);
		return ResponseEntity.ok(collectionModel);
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Find reservation by ID")
	public ResponseEntity<EntityModel<ReservationResponseDTO>> findById(@PathVariable Long id) {
		ReservationResponseDTO reservationResponseDTO = reservationService.findById(id);
		EntityModel<ReservationResponseDTO> model = reservationModelAssembler.toModel(reservationResponseDTO);
		return ResponseEntity.ok(model);
	}
	
	@GetMapping("/me/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Find reservation by ID for the current user")
	public ResponseEntity<EntityModel<ReservationResponseDTO>> findByIdForUser(@PathVariable Long id) {
		ReservationResponseDTO reservationResponseDTO = reservationService.findByIdForUser(id);
		EntityModel<ReservationResponseDTO> model = reservationModelAssembler.toModel(reservationResponseDTO);
		return ResponseEntity.ok(model);
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Create a new reservation")
	public ResponseEntity<EntityModel<ReservationResponseDTO>> create(@RequestBody @Valid ReservationRequestDTO reservationDTO) {
		ReservationResponseDTO reservationResponseDTO = reservationService.create(reservationDTO);
		EntityModel<ReservationResponseDTO> model = reservationModelAssembler.toModel(reservationResponseDTO);
		return new ResponseEntity<>(model, HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Update an existing reservation")
	public ResponseEntity<EntityModel<ReservationResponseDTO>> update(@PathVariable Long id, @RequestBody @Valid ReservationUpdateRequestDTO reservationDTO) {
		ReservationResponseDTO reservationResponseDTO = reservationService.update(id, reservationDTO);
		EntityModel<ReservationResponseDTO> model = reservationModelAssembler.toModel(reservationResponseDTO);
		return ResponseEntity.ok(model);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete a reservation")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		reservationService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
