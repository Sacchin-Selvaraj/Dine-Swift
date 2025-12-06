package com.dineswift.restaurant_service.payment.repository;

import com.dineswift.restaurant_service.model.Payment;
import com.dineswift.restaurant_service.model.TableBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(String orderId);

    @Query("SELECT p FROM Payment p WHERE p.tableBooking = :existingBooking")
    List<Payment> findAllByTableBooking(TableBooking existingBooking);


    Page<Payment> findAllByTableBooking_TableBookingId(UUID tableBookingId, Pageable pageable);
}
