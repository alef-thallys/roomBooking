package com.github.alefthallys.roombooking.service;

import com.github.alefthallys.roombooking.model.Room;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
	
	@InjectMocks
	private RoomService roomService;
	
	@Mock
	private RoomRepository roomRepository;
	
	private Room room;
	
	@BeforeEach
	void setUp() {
		room = new Room();
		room.setId(1L);
		room.setName("Room 101");
		room.setDescription("A small room for meetings");
		room.setCapacity(10);
		room.setAvailable(true);
		room.setLocation("1st Floor");
	}
	
	@Test
	void findAll() {
		List<Room> rooms = List.of(room);
		when(roomRepository.findAll()).thenReturn(rooms);
		List<Room> result = roomService.findAll();
		
		assertEquals(1, result.size());
		assertEquals(room, result.get(0));
	}
	
	@Test
	void findById() {
		when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
		Room result = roomService.findById(1L);
		
		assertEquals(room, result);
	}
	
	@Test
	void create() {
		when(roomRepository.save(room)).thenReturn(room);
		Room result = roomService.create(room);
		
		verify(roomRepository, times(1)).save(room);
		verifyNoMoreInteractions(roomRepository);
	}
	
	@Test
	void update() {
		when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
		when(roomRepository.save(any(Room.class))).thenReturn(room);
		
		Room result = roomService.update(1L, room);
		
		assertEquals(room, result);
		
		verify(roomRepository, times(1)).save(any(Room.class));
		verify(roomRepository, times(1)).findById(1L);
		verifyNoMoreInteractions(roomRepository);
	}
	
	
	@Test
	void delete() {
		doNothing().when(roomRepository).deleteById(1L);
		roomService.delete(1L);
		
		verify(roomRepository, times(1)).deleteById(1L);
		verifyNoMoreInteractions(roomRepository);
	}
}