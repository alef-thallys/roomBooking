package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.RoomRequestDTO;
import com.github.alefthallys.roombooking.dtos.RoomResponseDTO;
import com.github.alefthallys.roombooking.exceptions.EntityRoomAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.repositories.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
	
	@InjectMocks
	private RoomService roomService;
	
	@Mock
	private RoomRepository roomRepository;
	
	private Room room;
	private RoomRequestDTO roomRequestDTO;
	
	@BeforeEach
	void setUp() {
		room = new Room();
		room.setId(1L);
		room.setName("Room 101");
		room.setDescription("A small room for meetings");
		room.setCapacity(10);
		room.setAvailable(true);
		room.setLocation("1st Floor");
		
		roomRequestDTO = new RoomRequestDTO("Room 101", "A small room for meetings", 10, true, "1st Floor");
	}
	
	private void assertEqualsResponseDTO(Room room, RoomResponseDTO roomResponseDTO) {
		assertEquals(room.getId(), roomResponseDTO.id());
		assertEquals(room.getName(), roomResponseDTO.name());
		assertEquals(room.getDescription(), roomResponseDTO.description());
		assertEquals(room.getCapacity(), roomResponseDTO.capacity());
		assertEquals(room.isAvailable(), roomResponseDTO.available());
		assertEquals(room.getLocation(), roomResponseDTO.location());
	}
	
	@Nested
	@DisplayName("Find All Rooms")
	class FindAllRooms {
		
		@Test
		@DisplayName("should return a list of rooms")
		void shouldReturnAllRooms() {
			when(roomRepository.findAll()).thenReturn(List.of(room));
			List<RoomResponseDTO> result = roomService.findAll();
			assertEqualsResponseDTO(room, result.get(0));
		}
		
		@Test
		@DisplayName("should return an empty list when no rooms are found")
		void shouldReturnEmptyList() {
			when(roomRepository.findAll()).thenReturn(List.of());
			List<RoomResponseDTO> result = roomService.findAll();
			assertEquals(0, result.size());
		}
	}
	
	@Nested
	@DisplayName("Find Room By ID")
	class FindRoomById {
		
		@Test
		@DisplayName("should return room by id")
		void shouldReturnRoomById() {
			when(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room));
			RoomResponseDTO result = roomService.findById(1L);
			assertEqualsResponseDTO(room, result);
		}
		
		@Test
		@DisplayName("should throw exception when room not found")
		void shouldThrowExceptionWhenRoomNotFound() {
			when(roomRepository.findById(1L)).thenReturn(java.util.Optional.empty());
			assertThrows(EntityRoomNotFoundException.class, () -> roomService.findById(1L));
		}
		
		@Test
		@DisplayName("should throw IllegalArgumentException when room id is null")
		void shouldThrowIllegalArgumentExceptionWhenRoomIdIsNull() {
			assertThrows(IllegalArgumentException.class, () -> roomService.findById(null));
		}
	}
	
	@Nested
	@DisplayName("Create Room")
	class CreateRoom {
		
		@Test
		@DisplayName("should create a new room")
		void shouldCreateRoom() {
			when(roomRepository.save(any(Room.class))).thenReturn(room);
			RoomResponseDTO result = roomService.create(roomRequestDTO);
			assertEqualsResponseDTO(room, result);
		}
		
		@Test
		@DisplayName("should throw exception when room already exists")
		void shouldThrowExceptionWhenRoomAlreadyExists() {
			when(roomRepository.existsByName(roomRequestDTO.name())).thenReturn(true);
			assertThrows(EntityRoomAlreadyExistsException.class, () -> roomService.create(roomRequestDTO));
		}
	}
	
	@Nested
	@DisplayName("Update Room")
	class UpdateRoom {
		
		@Test
		@DisplayName("should update room")
		void shouldUpdateRoom() {
			when(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room));
			when(roomRepository.save(any(Room.class))).thenReturn(room);
			RoomResponseDTO result = roomService.update(1L, roomRequestDTO);
			assertEqualsResponseDTO(room, result);
		}
		
		@Test
		@DisplayName("should throw exception when room not found")
		void shouldThrowExceptionWhenRoomNotFound() {
			when(roomRepository.findById(1L)).thenReturn(java.util.Optional.empty());
			assertThrows(EntityRoomNotFoundException.class, () -> roomService.update(1L, roomRequestDTO));
		}
		
		@Test
		@DisplayName("should throw IllegalArgumentException when room id is null")
		void shouldThrowIllegalArgumentExceptionWhenRoomIdIsNull() {
			assertThrows(IllegalArgumentException.class, () -> roomService.update(null, roomRequestDTO));
		}
	}
	
	@Nested
	@DisplayName("Delete Room")
	class DeleteRoom {
		
		@Test
		@DisplayName("should delete room")
		void shouldDeleteRoom() {
			when(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room));
			roomService.delete(1L);
			when(roomRepository.findById(1L)).thenReturn(java.util.Optional.empty());
			assertThrows(EntityRoomNotFoundException.class, () -> roomService.findById(1L));
		}
		
		@Test
		@DisplayName("should throw exception when room not found")
		void shouldThrowExceptionWhenRoomNotFound() {
			when(roomRepository.findById(1L)).thenReturn(java.util.Optional.empty());
			assertThrows(EntityRoomNotFoundException.class, () -> roomService.delete(1L));
		}
		
		@Test
		@DisplayName("should throw IllegalArgumentException when room id is null")
		void shouldThrowIllegalArgumentExceptionWhenRoomIdIsNull() {
			assertThrows(IllegalArgumentException.class, () -> roomService.delete(null));
		}
	}
}