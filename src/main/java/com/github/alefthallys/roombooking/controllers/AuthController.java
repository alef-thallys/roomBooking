package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.dtos.JwtResponseDTO;
import com.github.alefthallys.roombooking.dtos.LoginRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import com.github.alefthallys.roombooking.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	
	public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.userService = userService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid UserRequestDTO userRequestDTO) {
		UserResponseDTO userResponseDTO = userService.create(userRequestDTO);
		return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
	}
	
	@PostMapping("/login")
	public ResponseEntity<JwtResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
		String token = jwtTokenProvider.generateToken(authentication);
		return ResponseEntity.ok(new JwtResponseDTO(token));
	}
	
	// TODO: This endpoint should be deleted in production
	@GetMapping("/me")
	public ResponseEntity<UserDetails> getCurrentUser() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return ResponseEntity.ok(userDetails);
	}
}
