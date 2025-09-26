//package com.dineswift.restaurant_service.repository;
//
//import com.dineswift.restaurant_service.model.entites.TableBooking;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.UUID;
//
//@Repository
//public interface TableBookingRepository extends JpaRepository<TableBooking, UUID> {
//
//    Page<TableBooking> findByUser_UserId(UUID userId, Pageable pageable);
//
//    @Query("select b from Booking b where b.user.userId=:userId AND b.bookingStatus=:bookingStatus")
//    Page<TableBooking> findByUser_UserIdAndBookingStatus( @Param("userId") UUID userId,@Param("bookingStatus") BookingStatus bookingStatus, Pageable pageable);
//}
