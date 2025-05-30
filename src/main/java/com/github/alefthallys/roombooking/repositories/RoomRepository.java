package com.github.alefthallys.roombooking.repositories;

import com.github.alefthallys.roombooking.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
	boolean existsByName(String name);
}
