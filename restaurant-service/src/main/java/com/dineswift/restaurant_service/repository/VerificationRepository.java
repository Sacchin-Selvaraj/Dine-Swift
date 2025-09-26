package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.entites.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, UUID> {

    Optional<Verification> findByToken(String token);
}
