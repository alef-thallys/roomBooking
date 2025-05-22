package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.dtos.UserUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityUserNotFoundException;
import com.github.alefthallys.roombooking.mappers.UserMapper;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthService authService;
	
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthService authService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authService = authService;
	}
	
	public List<UserResponseDTO> findAll() {
		return userRepository.findAll().stream()
				.map(UserMapper::toDto)
				.toList();
	}
	
	public UserResponseDTO findById(Long id) {
		User userById = userRepository.findById(id).orElseThrow(() -> new EntityUserNotFoundException(id));
		return UserMapper.toDto(userById);
	}
	
	public UserResponseDTO create(UserRequestDTO userDTO) {
		if (userRepository.existsByEmail((userDTO.email()))) {
			throw new EntityUserAlreadyExistsException(userDTO.email());
		}
		
		User userToSave = UserMapper.toEntity(userDTO);
		userToSave.setPassword(passwordEncoder.encode(userToSave.getPassword()));
		
		return UserMapper.toDto(userRepository.save(userToSave));
	}
	
	public UserResponseDTO update(Long id, UserUpdateRequestDTO userDTO) {
		User userById = userRepository.findById(id).orElseThrow(
				() -> new EntityUserNotFoundException(id));
		
		authService.validateUserOwnership(userById);
		
		if (!userDTO.name().isBlank()) {
			userById.setName(userDTO.name());
		}
		
		if (!userDTO.password().isBlank()) {
			userById.setPassword(passwordEncoder.encode(userDTO.password()));
		}
		
		if (!userDTO.phone().isBlank()) {
			userById.setPhone(userDTO.phone());
		}
		
		User savedUser = userRepository.save(userById);
		return UserMapper.toDto(savedUser);
	}
	
	public void delete(Long id) {
		User userById = userRepository.findById(id).orElseThrow(
				() -> new EntityUserNotFoundException(id));
		
		authService.validateUserOwnership(userById);
		userRepository.delete(userById);
	}
}
