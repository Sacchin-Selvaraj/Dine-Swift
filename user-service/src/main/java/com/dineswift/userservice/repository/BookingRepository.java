package com.dineswift.userservice.repository;

import com.dineswift.userservice.model.entites.Booking;
import com.dineswift.userservice.model.entites.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    Page<Booking> findByUser_UserId(UUID userId, Pageable pageable);

    @Query("select b from Booking b where b.user.userId=:userId AND b.bookingStatus=:bookingStatus")
    Page<Booking> findByUser_UserIdAndBookingStatus( @Param("userId") UUID userId,@Param("bookingStatus") BookingStatus bookingStatus, Pageable pageable);

    Optional<Booking> findByTableBookingId(UUID tableBookingId);
}
