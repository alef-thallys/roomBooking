package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.exceptions.EntityUserNotFoundException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	
	@InjectMocks
	private UserService userService;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private PasswordEncoder passwordEncoder;
	
	private User user;
	private UserRequestDTO userRequestDTO;
	
	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setName("John Doe");
		user.setEmail("john@gmail.com");
		user.setPassword("password");
		user.setPhone("123456789");
		user.setRole(User.Role.ROLE_USER);
		
		userRequestDTO = new UserRequestDTO(
				"John Doe",
				"john@gmail.com",
				"123456789",
				"password"
		);
	}
	
	@Test
	void findAll() {
		when(userRepository.findAll()).thenReturn(List.of(user));
		List<UserResponseDTO> result = userService.findAll();
		
		assertEquals(1, result.size());
		assertEquals(user.getId(), result.get(0).id());
		assertEquals(user.getName(), result.get(0).name());
		assertEquals(user.getEmail(), result.get(0).email());
		assertEquals(user.getPhone(), result.get(0).phone());
		assertEquals(user.getRole(), result.get(0).role());
		
		verify(userRepository).findAll();
	}
	
	@Test
	void findById() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		UserResponseDTO result = userService.findById(1L);
		
		assertEquals(user.getId(), result.id());
		assertEquals(user.getName(), result.name());
		assertEquals(user.getEmail(), result.email());
		assertEquals(user.getPhone(), result.phone());
		assertEquals(user.getRole(), result.role());
		
		verify(userRepository).findById(1L);
	}
	
	@Test
	void findById_shouldThrowException_whenUserNotFound() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(EntityUserNotFoundException.class, () -> userService.findById(1L));
		
		verify(userRepository).findById(1L);
	}
	
	@Test
	void create() {
		when(userRepository.save(any(User.class))).thenReturn(user);
		UserResponseDTO result = userService.create(userRequestDTO);
		
		assertEquals(user.getId(), result.id());
		assertEquals(user.getName(), result.name());
		assertEquals(user.getEmail(), result.email());
		assertEquals(user.getPhone(), result.phone());
		assertEquals(user.getRole(), result.role());
		
		verify(userRepository).save(any(User.class));
	}
	
	@Test
	void update() {
		UserRequestDTO updated = new UserRequestDTO(
				"Mary Doe",
				"mary@gmail.com",
				"password",
				"123456789"
		);
		
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserResponseDTO result = userService.update(1L, updated);
		
		assertEquals("Mary Doe", result.name());
		assertEquals("mary@gmail.com", result.email());
		assertEquals("123456789", result.phone());
		assertEquals(User.Role.ROLE_USER, result.role());
		
		verify(userRepository).findById(1L);
		verify(userRepository).save(any(User.class));
	}
	
	@Test
	void update_shouldThrowException_whenUserNotFound() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		
		assertThrows(EntityUserNotFoundException.class, () -> userService.update(1L, userRequestDTO));
		
		verify(userRepository).findById(1L);
	}
	
	
	@Test
	void delete() {
		when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
		doNothing().when(userRepository).delete(user);
		userService.delete(1L);
		
		verify(userRepository).findById(1L);
		verify(userRepository).delete(user);
	}
	
	@Test
	void delete_shouldThrowException_whenUserNotFound() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(EntityUserNotFoundException.class, () -> userService.delete(1L));
		
		verify(userRepository).findById(1L);
	}
}