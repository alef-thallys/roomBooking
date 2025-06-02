package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.assemblers.UserModelAssembler;
import com.github.alefthallys.roombooking.dtos.User.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserResponseDTO;
import com.github.alefthallys.roombooking.dtos.User.UserUpdateRequestDTO;
import com.github.alefthallys.roombooking.services.UserService;
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
@Tag(name = "User Management")
@RequestMapping("/api/v1/users")
public class UserController {
	
	private final UserService userService;
	private final UserModelAssembler userModelAssembler;
	
	public UserController(UserService userService, UserModelAssembler userModelAssembler) {
		this.userService = userService;
		this.userModelAssembler = userModelAssembler;
	}
	
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Find all users")
	public ResponseEntity<CollectionModel<EntityModel<UserResponseDTO>>> findAll() {
		List<UserResponseDTO> userResponseDTOList = userService.findAll();
		CollectionModel<EntityModel<UserResponseDTO>> collectionModel = userModelAssembler.toCollectionModel(userResponseDTOList);
		return ResponseEntity.ok(collectionModel);
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or @userSecurity.hasUserId(#id)")
	@Operation(summary = "Find user by ID")
	public ResponseEntity<EntityModel<UserResponseDTO>> findById(@PathVariable Long id) {
		UserResponseDTO userResponseDTO = userService.findById(id);
		EntityModel<UserResponseDTO> model = userModelAssembler.toModel(userResponseDTO);
		return ResponseEntity.ok(model);
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a new user")
	public ResponseEntity<EntityModel<UserResponseDTO>> create(@RequestBody @Valid UserRequestDTO userDTO) {
		UserResponseDTO userResponseDTO = userService.create(userDTO);
		EntityModel<UserResponseDTO> model = userModelAssembler.toModel(userResponseDTO);
		return new ResponseEntity<>(model, HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or @userSecurity.hasUserId(#id)")
	@Operation(summary = "Update an existing user")
	public ResponseEntity<EntityModel<UserResponseDTO>> update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequestDTO userDTO) {
		UserResponseDTO userResponseDTO = userService.update(id, userDTO);
		EntityModel<UserResponseDTO> model = userModelAssembler.toModel(userResponseDTO);
		return ResponseEntity.ok(model);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or @userSecurity.hasUserId(#id)")
	@Operation(summary = "Delete a user")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		userService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
