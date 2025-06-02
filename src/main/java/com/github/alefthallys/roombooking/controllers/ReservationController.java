package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.dtos.Reservation.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationResponseDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationUpdateRequestDTO;
import com.github.alefthallys.roombooking.services.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
	
	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}
	
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Find all reservations")
	public ResponseEntity<List<ReservationResponseDTO>> findAll() {
		return ResponseEntity.ok(reservationService.findAll());
	}
	
	@GetMapping("/me")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Find reservations for the current user")
	public ResponseEntity<List<ReservationResponseDTO>> getMyReservations() {
		return ResponseEntity.ok(reservationService.findByUser());
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Find reservation by ID")
	public ResponseEntity<ReservationResponseDTO> findById(@PathVariable Long id) {
		return ResponseEntity.ok(reservationService.findById(id));
	}
	
	@GetMapping("/me/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Find reservation by ID for the current user")
	public ResponseEntity<ReservationResponseDTO> findByIdForUser(@PathVariable Long id) {
		return ResponseEntity.ok(reservationService.findByIdForUser(id));
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Create a new reservation")
	public ResponseEntity<ReservationResponseDTO> create(@RequestBody @Valid ReservationRequestDTO reservationDTO) {
		ReservationResponseDTO createdReservation = reservationService.create(reservationDTO);
		return new ResponseEntity<>(createdReservation, HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Update an existing reservation")
	public ResponseEntity<ReservationResponseDTO> update(@PathVariable Long id, @RequestBody @Valid ReservationUpdateRequestDTO reservationDTO) {
		ReservationResponseDTO updatedReservation = reservationService.update(id, reservationDTO);
		return ResponseEntity.ok(updatedReservation);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete a reservation")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		reservationService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
