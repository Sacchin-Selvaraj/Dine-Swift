package com.dineswift.userservice.repository;

import com.dineswift.userservice.model.entites.VerificationToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationToken, UUID> {


    @EntityGraph(attributePaths = "user")
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.token=:token AND vt.wasUsed=false AND vt.tokenStatus='SENT'")
    Optional<VerificationToken> findByToken(@Param("token") String token);
    

}
