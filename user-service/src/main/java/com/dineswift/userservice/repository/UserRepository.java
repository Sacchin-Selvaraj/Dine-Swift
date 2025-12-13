package com.dineswift.userservice.repository;

import com.dineswift.userservice.model.entites.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.isActive = true")
    Optional<User> findByIdAndIsActive(UUID userId);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findByEmailAndIsActive(String email);

    Optional<User> findByPhoneNumber(String userPhoneNumber);

    @EntityGraph(attributePaths = {"cart","roles"})
    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByIdWithCart(UUID userId);
}
