package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.dtos.User.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserResponseDTO;
import com.github.alefthallys.roombooking.dtos.User.UserUpdateRequestDTO;
import com.github.alefthallys.roombooking.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
	
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<UserResponseDTO>> findAll() {
		return ResponseEntity.ok(userService.findAll());
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
		return ResponseEntity.ok(userService.findById(id));
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<UserResponseDTO> create(@RequestBody @Valid UserRequestDTO userDTO) {
		return new ResponseEntity<>(userService.create(userDTO), HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<UserResponseDTO> update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequestDTO userDTO) {
		return ResponseEntity.ok(userService.update(id, userDTO));
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		userService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
