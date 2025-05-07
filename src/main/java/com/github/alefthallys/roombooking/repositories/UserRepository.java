package com.github.alefthallys.roombooking.repositories;

import com.github.alefthallys.roombooking.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
