package com.dineswift.userservice.repository;

import com.dineswift.userservice.model.entites.Cart;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cart c WHERE c.id = :cartId AND c.isActive = true")
    boolean existsByIdAndIsActive(UUID cartId);

    @Query("SELECT c FROM Cart c WHERE c.cartId = :cartId AND c.isActive = true")
    Optional<Cart> findByIdAndIsActive(UUID cartId);
}
