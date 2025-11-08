package com.dineswift.userservice.service;

import com.dineswift.userservice.model.entites.Booking;
import com.dineswift.userservice.model.entites.BookingStatus;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public final class BookingSpecification {

    private final UserRepository userRepository;

    public Specification<Booking> hasBookingStatus(BookingStatus bookingStatus) {
        if (bookingStatus == null){
            Specification.allOf();
        }
        return (root, query, builder) -> builder.equal(root.get("bookingStatus"), bookingStatus);
    }

    public Specification<Booking> belongsToUser(UUID userId) {
        if (userId==null){
            Specification.allOf();
        }
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return (root, query, builder) -> builder.equal(root.get("user"), existingUser);
    }
}
