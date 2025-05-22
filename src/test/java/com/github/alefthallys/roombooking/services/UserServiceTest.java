package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.dtos.UserUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityUserNotFoundException;
import com.github.alefthallys.roombooking.exceptions.ForbiddenException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
	private AuthService authService;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private PasswordEncoder passwordEncoder;
	
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	
	private User user;
	private UserRequestDTO userRequestDTO;
	private UserUpdateRequestDTO userUpdateRequestDTO;
	
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
		
		userUpdateRequestDTO = new UserUpdateRequestDTO(
				"John Doe",
				"123456789",
				"password"
		);
	}
	
	
	private void assertEqualsResponseDTO(User user, UserResponseDTO userResponseDTO) {
		assertEquals(user.getId(), userResponseDTO.id());
		assertEquals(user.getName(), userResponseDTO.name());
		assertEquals(user.getEmail(), userResponseDTO.email());
		assertEquals(user.getPhone(), userResponseDTO.phone());
		assertEquals(user.getRole(), userResponseDTO.role());
	}
	
	@Nested
	@DisplayName("Find All Users")
	class FindAllUsers {
		
		@Test
		@DisplayName("Should return a list of users")
		void shouldReturnAListOfUsers() {
			when(userRepository.findAll()).thenReturn(List.of(user));
			List<UserResponseDTO> userResponseDTOList = userService.findAll();
			assertEqualsResponseDTO(user, userResponseDTOList.get(0));
		}
		
		@Test
		@DisplayName("Should return an empty list when no users are found")
		void shouldReturnAnEmptyList() {
			when(userRepository.findAll()).thenReturn(List.of());
			List<UserResponseDTO> userResponseDTOList = userService.findAll();
			assertEquals(0, userResponseDTOList.size());
		}
	}
	
	@Nested
	@DisplayName("Find User By ID")
	class FindUserById {
		
		@Test
		@DisplayName("Should return user by id")
		void shouldReturnUserById() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			UserResponseDTO userResponseDTO = userService.findById(1L);
			assertEqualsResponseDTO(user, userResponseDTO);
		}
		
		@Test
		@DisplayName("Should throw EntityUserNotFoundException when user is not found")
		void shouldThrowEntityUserNotFoundException() {
			when(userRepository.findById(1L)).thenReturn(Optional.empty());
			assertThrows(EntityUserNotFoundException.class, () -> userService.findById(1L));
		}
		
		@Test
		@DisplayName("Should throw EntityUserNotFoundException when user id is null")
		void shouldThrowEntityUserNotFoundExceptionWhenUserIdIsNull() {
			assertThrows(EntityUserNotFoundException.class, () -> userService.findById(null));
		}
	}
	
	@Nested
	@DisplayName("Create User")
	class CreateUser {
		
		@Test
		@DisplayName("Should create a user")
		void shouldCreateAUser() {
			when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(false);
			when(userRepository.save(any(User.class))).thenReturn(user);
			when(passwordEncoder.encode(userRequestDTO.password())).thenReturn("encodedPassword");
			UserResponseDTO userResponseDTO = userService.create(userRequestDTO);
			assertEqualsResponseDTO(user, userResponseDTO);
		}
		
		@Test
		@DisplayName("Should throw exception when user already exists")
		void shouldThrowExceptionWhenUserAlreadyExists() {
			when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(true);
			assertThrows(EntityUserAlreadyExistsException.class, () -> userService.create(userRequestDTO));
		}
	}
	
	@Nested
	@DisplayName("Update User")
	class UpdateUser {
		
		@Test
		@DisplayName("Should update a user")
		void shouldUpdateAUser() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			doNothing().when(authService).validateUserOwnership(user);
			when(passwordEncoder.encode(userUpdateRequestDTO.password())).thenReturn("encodedPassword");
			when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
			
			UserResponseDTO userResponseDTO = userService.update(1L, userUpdateRequestDTO);
			assertEquals(user.getId(), userResponseDTO.id());
			assertEquals(userUpdateRequestDTO.name(), userResponseDTO.name());
			assertEquals(user.getEmail(), userResponseDTO.email());
			assertEquals(userUpdateRequestDTO.phone(), userResponseDTO.phone());
		}
		
		@Test
		@DisplayName("Should throw ForbiddenException when user is not the owner")
		void shouldThrowForbiddenExceptionWhenUserIsNotTheOwner() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			doThrow(new ForbiddenException()).when(authService).validateUserOwnership(user);
			assertThrows(ForbiddenException.class, () -> userService.update(1L, userUpdateRequestDTO));
		}
		
		@Test
		@DisplayName("Should throw EntityUserNotFoundException when user is not found")
		void shouldThrowEntityUserNotFoundExceptionWhenUserIsNotFound() {
			when(userRepository.findById(1L)).thenReturn(Optional.empty());
			assertThrows(EntityUserNotFoundException.class, () -> userService.update(1L, userUpdateRequestDTO));
		}
		
		@Test
		@DisplayName("Should throw EntityUserNotFoundException when user id is null")
		void shouldThrowEntityUserNotFoundExceptionWhenUserIdIsNull() {
			assertThrows(EntityUserNotFoundException.class, () -> userService.update(null, userUpdateRequestDTO));
		}
	}
	
	@Nested
	@DisplayName("Delete User")
	class DeleteUser {
		
		@Test
		@DisplayName("Should delete a user")
		void shouldDeleteAUser() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			doNothing().when(authService).validateUserOwnership(user);
			
			userService.delete(1L);
			verify(userRepository).delete(user);
		}
		
		@Test
		@DisplayName("Should throw ForbiddenException when user is not the owner")
		void shouldThrowForbiddenExceptionWhenUserIsNotTheOwner() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			doThrow(new ForbiddenException()).when(authService).validateUserOwnership(user);
			assertThrows(ForbiddenException.class, () -> userService.delete(1L));
		}
		
		@Test
		@DisplayName("Should throw EntityUserNotFoundException when user is not found")
		void shouldThrowEntityUserNotFoundExceptionWhenUserIsNotFound() {
			when(userRepository.findById(1L)).thenReturn(Optional.empty());
			assertThrows(EntityUserNotFoundException.class, () -> userService.delete(1L));
		}
		
		@Test
		@DisplayName("Should throw EntityUserNotFoundException when user id is null")
		void shouldThrowEntityUserNotFoundExceptionWhenUserIdIsNull() {
			assertThrows(EntityUserNotFoundException.class, () -> userService.delete(null));
		}
	}
}