package com.dineswift.restaurant_service.payment.repository;

import com.dineswift.restaurant_service.model.PaymentRefund;
import com.dineswift.restaurant_service.payment.payload.response.PaymentRefundDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, UUID> {

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.tableBooking.tableBookingId = :tableBookingId")
    List<PaymentRefund> findAllByTableBookingId(UUID tableBookingId);
}
