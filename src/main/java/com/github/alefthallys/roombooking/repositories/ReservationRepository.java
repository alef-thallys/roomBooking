package com.github.alefthallys.roombooking.repositories;

import com.github.alefthallys.roombooking.models.Reservation;
import com.github.alefthallys.roombooking.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	Collection<Reservation> findByUser(User currentUser);
}
