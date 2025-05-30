package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.User.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserResponseDTO;
import com.github.alefthallys.roombooking.dtos.User.UserUpdateRequestDTO;
import com.github.alefthallys.roombooking.exceptions.User.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.User.EntityUserNotFoundException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import com.github.alefthallys.roombooking.testBuilders.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
	
	private User user;
	private UserRequestDTO userRequestDTO;
	private UserUpdateRequestDTO userUpdateRequestDTO;
	private UserResponseDTO userResponseDTO;
	
	@BeforeEach
	void setUp() {
		user = UserTestBuilder.anUser().build();
		userRequestDTO = UserTestBuilder.anUser().buildRequestDTO();
		userUpdateRequestDTO = UserTestBuilder.anUser().buildUpdateRequestDTO();
		userResponseDTO = UserTestBuilder.anUser().buildResponseDTO();
	}
	
	private void assertEqualsResponseDTO(User expectedUser, UserResponseDTO actualResponseDTO) {
		assertEquals(expectedUser.getId(), actualResponseDTO.id());
		assertEquals(expectedUser.getName(), actualResponseDTO.name());
		assertEquals(expectedUser.getEmail(), actualResponseDTO.email());
		assertEquals(expectedUser.getPhone(), actualResponseDTO.phone());
		assertEquals(expectedUser.getRole(), actualResponseDTO.role());
	}
	
	@Nested
	@DisplayName("Find All Users")
	class FindAllUsers {
		
		@Test
		@DisplayName("Should return a list of users")
		void shouldReturnAListOfUsers() {
			when(userRepository.findAll()).thenReturn(List.of(user));
			List<UserResponseDTO> userResponseDTOList = userService.findAll();
			assertEquals(1, userResponseDTOList.size());
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
		
		@ParameterizedTest(name = "Should throw IllegalArgumentException when user id is invalid: {0}")
		@NullSource
		@ValueSource(longs = {0L, -1L})
		@DisplayName("Should throw IllegalArgumentException when user id is invalid")
		void shouldThrowIllegalArgumentExceptionWhenUserIdIsInvalid(Long invalidId) {
			assertThrows(IllegalArgumentException.class, () -> userService.findById(invalidId));
			verify(userRepository, never()).findById(anyLong());
		}
	}
	
	@Nested
	@DisplayName("Create User")
	class CreateUser {
		
		@Test
		@DisplayName("Should create a user")
		void shouldCreateAUser() {
			when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(false);
			when(passwordEncoder.encode(userRequestDTO.password())).thenReturn("encodedPassword");
			when(userRepository.save(any(User.class))).thenReturn(user);
			
			UserResponseDTO userResponseDTO = userService.create(userRequestDTO);
			
			assertEqualsResponseDTO(user, userResponseDTO);
			verify(passwordEncoder, times(1)).encode(userRequestDTO.password());
			verify(userRepository, times(1)).save(any(User.class));
		}
		
		@Test
		@DisplayName("Should throw exception when user already exists")
		void shouldThrowExceptionWhenUserAlreadyExists() {
			when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(true);
			assertThrows(EntityUserAlreadyExistsException.class, () -> userService.create(userRequestDTO));
			verify(passwordEncoder, never()).encode(anyString());
			verify(userRepository, never()).save(any(User.class));
		}
	}
	
	@Nested
	@DisplayName("Update User")
	class UpdateUser {
		
		@Test
		@DisplayName("Should update a user")
		void shouldUpdateAUser() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			when(passwordEncoder.encode(userUpdateRequestDTO.password())).thenReturn("encodedPassword");
			when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
				User userToSave = invocation.getArgument(0);
				userToSave.setName(userUpdateRequestDTO.name());
				userToSave.setPhone(userUpdateRequestDTO.phone());
				userToSave.setPassword("encodedPassword");
				return userToSave;
			});
			
			UserResponseDTO result = userService.update(1L, userUpdateRequestDTO);
			
			assertEquals(user.getId(), result.id());
			assertEquals(userUpdateRequestDTO.name(), result.name());
			assertEquals(user.getEmail(), result.email());
			assertEquals(userUpdateRequestDTO.phone(), result.phone());
			
			verify(passwordEncoder, times(1)).encode(userUpdateRequestDTO.password());
			verify(userRepository, times(1)).save(any(User.class));
		}
		
		@Test
		@DisplayName("Should throw EntityUserNotFoundException when user is not found")
		void shouldThrowEntityUserNotFoundExceptionWhenUserIsNotFound() {
			when(userRepository.findById(1L)).thenReturn(Optional.empty());
			assertThrows(EntityUserNotFoundException.class, () -> userService.update(1L, userUpdateRequestDTO));
			verify(authService, never()).validateUserOwnership(any(User.class));
			verify(userRepository, never()).save(any(User.class));
		}
		
		@ParameterizedTest(name = "Should throw IllegalArgumentException when user id is invalid: {0}")
		@NullSource
		@ValueSource(longs = {0L, -1L})
		@DisplayName("Should throw IllegalArgumentException when user id is invalid")
		void shouldThrowIllegalArgumentExceptionWhenUserIdIsInvalid(Long invalidId) {
			assertThrows(IllegalArgumentException.class, () -> userService.findById(invalidId));
			verify(userRepository, never()).findById(anyLong());
		}
	}
	
	@Nested
	@DisplayName("Delete User")
	class DeleteUser {
		
		@Test
		@DisplayName("Should delete a user")
		void shouldDeleteAUser() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			doNothing().when(userRepository).delete(any(User.class));
			
			userService.delete(1L);
			verify(userRepository, times(1)).delete(user);
		}
		
		@Test
		@DisplayName("Should throw EntityUserNotFoundException when user is not found")
		void shouldThrowEntityUserNotFoundExceptionWhenUserIsNotFound() {
			when(userRepository.findById(1L)).thenReturn(Optional.empty());
			assertThrows(EntityUserNotFoundException.class, () -> userService.delete(1L));
			verify(authService, never()).validateUserOwnership(any(User.class));
			verify(userRepository, never()).delete(any(User.class));
		}
		
		@ParameterizedTest(name = "Should throw IllegalArgumentException when user id is invalid: {0}")
		@NullSource
		@ValueSource(longs = {0L, -1L})
		@DisplayName("Should throw IllegalArgumentException when user id is invalid")
		void shouldThrowIllegalArgumentExceptionWhenUserIdIsInvalid(Long invalidId) {
			assertThrows(IllegalArgumentException.class, () -> userService.findById(invalidId));
			verify(userRepository, never()).findById(anyLong());
		}
	}
}