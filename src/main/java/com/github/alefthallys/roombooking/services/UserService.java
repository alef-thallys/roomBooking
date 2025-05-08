package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.exceptions.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityUserNotFoundException;
import com.github.alefthallys.roombooking.mappers.UserMapper;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public List<UserResponseDTO> findAll() {
		return userRepository.findAll().stream()
				.map(UserMapper::toDto)
				.toList();
	}
	
	public UserResponseDTO findById(Long id) {
		User user = userRepository.findById(id).orElseThrow(() -> new EntityUserNotFoundException(id));
		return UserMapper.toDto(user);
	}
	
	public UserResponseDTO create(UserRequestDTO userDTO) {
		if (userRepository.existsByEmail((userDTO.email()))) {
			throw new EntityUserAlreadyExistsException(userDTO.email());
		}
		
		User userToSave = UserMapper.toEntity(userDTO);
		return UserMapper.toDto(userRepository.save(userToSave));
	}
	
	public UserResponseDTO update(Long id, UserRequestDTO userDTO) {
		User userToUpdate = userRepository.findById(id).orElseThrow(
				() -> new EntityUserNotFoundException(id));
		
		userToUpdate.setName(userDTO.name());
		userToUpdate.setEmail(userDTO.email());
		userToUpdate.setPassword(userDTO.password());
		userToUpdate.setPhone(userDTO.phone());
		
		User savedUser = userRepository.save(userToUpdate);
		return UserMapper.toDto(savedUser);
	}
	
	public void delete(Long id) {
		User userById = userRepository.findById(id).orElseThrow(
				() -> new EntityUserNotFoundException(id));
		userRepository.delete(userById);
	}
}
