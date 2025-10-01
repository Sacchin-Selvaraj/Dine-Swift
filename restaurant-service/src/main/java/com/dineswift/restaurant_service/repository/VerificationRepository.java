package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationToken, UUID> {

    @Query("SELECT vt FROM VerificationToken vt WHERE vt.token=:token AND vt.wasUsed=false AND vt.tokenStatus='SENT'")
    Optional<VerificationToken> findByToken(String token);
}
