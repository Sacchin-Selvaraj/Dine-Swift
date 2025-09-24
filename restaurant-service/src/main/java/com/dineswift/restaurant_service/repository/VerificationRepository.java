package com.dineswift.restaurant_service.repository;

import com.dineswift.userservice.model.entites.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);
}
