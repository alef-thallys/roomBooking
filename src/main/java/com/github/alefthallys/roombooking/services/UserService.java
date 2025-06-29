package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.User.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserResponseDTO;
import com.github.alefthallys.roombooking.dtos.User.UserUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.User.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.User.EntityUserNotFoundException;
import com.github.alefthallys.roombooking.mappers.UserMapper;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	private static void validateIdOrThrowException(Long id) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Invalid user ID: " + id);
		}
	}
	
	@Transactional(readOnly = true)
	public List<UserResponseDTO> findAll() {
		return userRepository.findAll()
				.stream()
				.map(UserMapper::toDto)
				.collect(Collectors.toList());
	}
	
	@Transactional(readOnly = true)
	public UserResponseDTO findById(Long id) {
		validateIdOrThrowException(id);
		return userRepository.findById(id)
				.map(UserMapper::toDto)
				.orElseThrow(() -> new EntityUserNotFoundException(id));
	}
	
	@Transactional
	public UserResponseDTO create(UserRequestDTO userRequestDTO) {
		if (userRepository.existsByEmail(userRequestDTO.email())) {
			throw new EntityUserAlreadyExistsException(userRequestDTO.email());
		}
		User user = UserMapper.toEntity(userRequestDTO);
		user.setPassword(passwordEncoder.encode(userRequestDTO.password()));
		user = userRepository.save(user);
		return UserMapper.toDto(user);
	}
	
	@Transactional
	public UserResponseDTO update(Long id, UserUpdateRequestDTO userUpdateRequestDTO) {
		validateIdOrThrowException(id);
		User user = userRepository.findById(id)
				.orElseThrow(() -> new EntityUserNotFoundException(id));
		
		user.setName(userUpdateRequestDTO.name());
		user.setPhone(userUpdateRequestDTO.phone());
		user.setPassword(passwordEncoder.encode(userUpdateRequestDTO.password()));
		
		user = userRepository.save(user);
		return UserMapper.toDto(user);
	}
	
	@Transactional
	public void delete(Long id) {
		validateIdOrThrowException(id);
		User user = userRepository.findById(id)
				.orElseThrow(() -> new EntityUserNotFoundException(id));
		userRepository.delete(user);
	}
}
