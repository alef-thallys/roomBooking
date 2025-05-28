package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.dtos.Reservation.ReservationRequestDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationResponseDTO;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationUpdateRequestDTO;
import com.github.alefthallys.roombooking.services.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {
	
	private final ReservationService reservationService;
	
	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}
	
	@GetMapping
	@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
	public ResponseEntity<List<ReservationResponseDTO>> findAll() {
		return ResponseEntity.ok(reservationService.findAll());
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
	public ResponseEntity<ReservationResponseDTO> findById(@PathVariable Long id) {
		return ResponseEntity.ok(reservationService.findById(id));
	}
	
	@PostMapping
	@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
	public ResponseEntity<ReservationResponseDTO> create(@RequestBody @Valid ReservationRequestDTO reservationDTO) {
		ReservationResponseDTO createdReservation = reservationService.create(reservationDTO);
		return new ResponseEntity<>(createdReservation, HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
	public ResponseEntity<ReservationResponseDTO> update(@PathVariable Long id, @RequestBody @Valid ReservationUpdateRequestDTO reservationDTO) {
		ReservationResponseDTO updatedReservation = reservationService.update(id, reservationDTO);
		return ResponseEntity.ok(updatedReservation);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		reservationService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
