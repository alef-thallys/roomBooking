package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.UserDTO;
import com.github.alefthallys.roombooking.mappers.UserMapper;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public List<UserDTO> findAll() {
		return userRepository.findAll().stream()
				.map(UserMapper::toDto)
				.toList();
	}
	
	public UserDTO findById(Long id) {
		User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		return UserMapper.toDto(user);
	}
	
	public UserDTO create(UserDTO userDTO) {
		User userToSave = UserMapper.toEntity(userDTO);
		return UserMapper.toDto(userRepository.save(userToSave));
	}
	
	public UserDTO update(Long id, UserDTO userDTO) {
		User userToUpdate = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		
		userToUpdate.setName(userDTO.name());
		userToUpdate.setEmail(userDTO.email());
		userToUpdate.setPassword(userDTO.password());
		userToUpdate.setPhone(userDTO.phone());
		
		User savedUser = userRepository.save(userToUpdate);
		return UserMapper.toDto(savedUser);
	}
	
	public void delete(Long id) {
		User userById = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		userRepository.delete(userById);
	}
}
