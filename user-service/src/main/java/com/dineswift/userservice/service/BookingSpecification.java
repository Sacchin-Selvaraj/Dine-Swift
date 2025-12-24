package com.dineswift.userservice.service;

import com.dineswift.userservice.model.entites.Booking;
import com.dineswift.userservice.model.entites.BookingStatus;
import com.dineswift.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingSpecification {

    private final UserRepository userRepository;

    public Specification<Booking> hasBookingStatus(BookingStatus bookingStatus) {
        if (bookingStatus == null){
           return Specification.allOf();
        }
        return (root, query, builder) -> builder.equal(root.get("bookingStatus"), bookingStatus);
    }

    public Specification<Booking> belongsToUser(UUID userId) {
        if (userId==null){
           return Specification.allOf();
        }
        return (root, query, builder) -> builder.equal(root.get("user").get("userId"), userId);
    }

    public Specification<Booking> hasBookingDate(LocalDate bookingDate) {
        if (bookingDate == null){
           return Specification.allOf();
        }
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("bookingDate"), bookingDate);
    }
}
