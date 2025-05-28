package com.github.alefthallys.roombooking.repositories;

import com.github.alefthallys.roombooking.models.Room;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
	boolean existsByName(@Size(max = 50, message = "Name must be less than 50 characters") @NotBlank(message = "Name is required") String name);
}
