package com.dineswift.userservice.service;

import com.dineswift.userservice.model.entites.Booking;
import com.dineswift.userservice.model.entites.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class BookingSpecification {


    public Specification<Booking> hasBookingStatus(BookingStatus bookingStatus) {
        if (bookingStatus == null){
            Specification.allOf();
        }
        return (root, query, builder) -> builder.equal(root.get("bookingStatus"), bookingStatus);
    }
}
