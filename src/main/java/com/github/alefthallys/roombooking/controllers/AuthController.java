package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.dtos.JwtResponseDTO;
import com.github.alefthallys.roombooking.dtos.LoginRequestDTO;
import com.github.alefthallys.roombooking.dtos.RefreshTokenRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserResponseDTO;
import com.github.alefthallys.roombooking.exceptions.InvalidJwtException;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import com.github.alefthallys.roombooking.security.services.CustomUserDetailsService;
import com.github.alefthallys.roombooking.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	private final CustomUserDetailsService customUserDetailsService;
	
	public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService, CustomUserDetailsService customUserDetailsService) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.userService = userService;
		this.customUserDetailsService = customUserDetailsService;
	}
	
	@PostMapping("/register")
	@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
	public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid UserRequestDTO userRequestDTO) {
		UserResponseDTO userResponseDTO = userService.create(userRequestDTO);
		return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
	}
	
	@PostMapping("/login")
	@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
	public ResponseEntity<JwtResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
		String token = jwtTokenProvider.generateToken(authentication);
		String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
		return ResponseEntity.ok(new JwtResponseDTO(token, refreshToken));
	}
	
	@GetMapping("/me")
	public ResponseEntity<UserDetails> getCurrentUser() {
		UserDetails authentication = jwtTokenProvider.getAuthentication();
		return ResponseEntity.ok(authentication);
	}
	
	@PostMapping("/refresh-token")
	public ResponseEntity<JwtResponseDTO> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO request) {
		String refreshToken = request.refreshToken();
		
		try {
			jwtTokenProvider.validateRefreshToken(refreshToken);
			String username = jwtTokenProvider.getUsernameFromRefreshToken(refreshToken);
			UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
			
			Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			
			String newAccessToken = jwtTokenProvider.generateToken(authentication);
			String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);
			
			return ResponseEntity.ok(new JwtResponseDTO(newAccessToken, newRefreshToken));
		} catch (InvalidJwtException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", e);
		}
	}
}
