package com.github.alefthallys.roombooking.repositories;

import com.github.alefthallys.roombooking.models.Reservation;
import com.github.alefthallys.roombooking.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	Collection<Reservation> findByUser(User currentUser);
	
	List<Reservation> findByRoomIdAndStartDateBeforeAndEndDateAfter(Long roomId, LocalDateTime newEndDate, LocalDateTime newStartDate);
}
