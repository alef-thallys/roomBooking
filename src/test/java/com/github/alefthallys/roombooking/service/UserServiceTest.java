package com.github.alefthallys.roombooking.service;

import com.github.alefthallys.roombooking.model.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	
	@InjectMocks
	private UserService userService;
	
	@Mock
	private UserRepository userRepository;
	
	private User user;
	
	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setName("John Doe");
		user.setEmail("john@gmail.com");
		user.setPassword("password");
		user.setPhone("123456789");
		user.setRole(User.Role.USER);
	}
	
	@Test
	void findAll() {
		List<User> users = List.of(user);
		when(userRepository.findAll()).thenReturn(users);
		List<User> result = userService.findAll();
		
		assertEquals(1, result.size());
		assertEquals(user, result.get(0));
	}
	
	@Test
	void findById() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		User result = userService.findById(1L);
		
		assertEquals(user, result);
	}
	
	@Test
	void create() {
		when(userRepository.save(user)).thenReturn(user);
		User result = userService.create(user);
		
		verify(userRepository, times(1)).save(user);
		verifyNoMoreInteractions(userRepository);
	}
	
	@Test
	void update() {
		when(userRepository.save(user)).thenReturn(user);
		User result = userService.update(1L, user);
		
		verify(userRepository, times(1)).save(user);
		verifyNoMoreInteractions(userRepository);
	}
	
	@Test
	void delete() {
		doNothing().when(userRepository).deleteById(1L);
		userService.delete(1L);
		
		verify(userRepository, times(1)).deleteById(1L);
		verifyNoMoreInteractions(userRepository);
	}
}