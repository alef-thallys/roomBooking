package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.dtos.RoomDTO;
import com.github.alefthallys.roombooking.exceptions.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.models.Room;
import com.github.alefthallys.roombooking.repositories.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
	
	@InjectMocks
	private RoomService roomService;
	
	@Mock
	private RoomRepository roomRepository;
	
	private Room room;
	private RoomDTO roomDTO;
	
	@BeforeEach
	void setUp() {
		room = new Room();
		room.setId(1L);
		room.setName("Room 101");
		room.setDescription("A small room for meetings");
		room.setCapacity(10);
		room.setAvailable(true);
		room.setLocation("1st Floor");
		
		roomDTO = new RoomDTO(
				1L,
				"Room 101",
				"A small room for meetings",
				10,
				true,
				"1st Floor"
		);
	}
	
	@Test
	void findAll() {
		when(roomRepository.findAll()).thenReturn(List.of(room));
		List<RoomDTO> result = roomService.findAll();
		
		assertEquals(1, result.size());
		assertEquals(room.getId(), result.get(0).id());
		assertEquals(room.getName(), result.get(0).name());
		assertEquals(room.getDescription(), result.get(0).description());
		assertEquals(room.getCapacity(), result.get(0).capacity());
		assertEquals(room.isAvailable(), result.get(0).available());
		assertEquals(room.getLocation(), result.get(0).location());
		
		verify(roomRepository).findAll();
	}
	
	@Test
	void findById() {
		when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
		RoomDTO result = roomService.findById(1L);
		
		assertEquals(room.getId(), result.id());
		assertEquals(room.getName(), result.name());
		assertEquals(room.getDescription(), result.description());
		assertEquals(room.getCapacity(), result.capacity());
		assertEquals(room.isAvailable(), result.available());
		assertEquals(room.getLocation(), result.location());
		
		verify(roomRepository).findById(1L);
	}
	
	@Test
	void findById_shouldThrowException_whenRoomNotFound() {
		when(roomRepository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(EntityRoomNotFoundException.class, () -> roomService.findById(1L));
		
		verify(roomRepository).findById(1L);
	}
	
	@Test
	void create() {
		when(roomRepository.save(room)).thenReturn(room);
		RoomDTO result = roomService.create(roomDTO);
		
		assertEquals(room.getId(), result.id());
		assertEquals(room.getName(), result.name());
		assertEquals(room.getDescription(), result.description());
		assertEquals(room.getCapacity(), result.capacity());
		assertEquals(room.isAvailable(), result.available());
		assertEquals(room.getLocation(), result.location());
		
		verify(roomRepository).save(any(Room.class));
	}
	
	@Test
	void update() {
		RoomDTO updated = new RoomDTO(
				1L,
				"Room 205",
				"A huge room for meetings",
				40,
				false,
				"12st Floor"
		);
		
		when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
		when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
		RoomDTO result = roomService.update(1L, updated);
		
		assertEquals("Room 205", result.name());
		assertEquals("A huge room for meetings", result.description());
		assertEquals(40, result.capacity());
		
		verify(roomRepository).save(any(Room.class));
		verify(roomRepository).findById(1L);
	}
	
	@Test
	void update_shouldThrowException_whenRoomNotFound() {
		when(roomRepository.findById(1L)).thenReturn(Optional.empty());
		
		assertThrows(EntityRoomNotFoundException.class, () -> roomService.update(1L, roomDTO));
		
		verify(roomRepository).findById(1L);
	}
	
	
	@Test
	void delete() {
		when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
		doNothing().when(roomRepository).delete(room);
		roomService.delete(1L);
		
		verify(roomRepository).findById(1L);
		verify(roomRepository).delete(room);
	}
	
	@Test
	void delete_shouldThrowException_whenRoomNotFound() {
		when(roomRepository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(EntityRoomNotFoundException.class, () -> roomService.delete(1L));
		
		verify(roomRepository).findById(1L);
	}
}