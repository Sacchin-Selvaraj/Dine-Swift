package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.EmployeeException;
import com.dineswift.restaurant_service.model.GuestInformation;
import com.dineswift.restaurant_service.payload.response.guestInformation.GuestInformationResponse;
import com.dineswift.restaurant_service.repository.GuestInformationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GuestInformationService {

    private final GuestInformationRepository guestInformationRepository;
    private final RestClient restClient;

    public GuestInformationResponse getGuestInformationById(UUID guestInformationId) {
        log.info("Service layer: Retrieving guest information for ID: {}", guestInformationId);
        GuestInformation guestInformation = guestInformationRepository.findById(guestInformationId)
                .orElseThrow(() -> new EmployeeException("Guest Information not found for ID: " + guestInformationId));

        UUID userId = guestInformation.getUserId();
        if (userId==null)
            throw new EmployeeException("User ID is null for Guest Information ID: " + guestInformationId);

        GuestInformationResponse response = fetchUserDetails(userId);
        log.info("Successfully retrieved guest information for ID: {}", guestInformationId);
        return response;
    }

    private GuestInformationResponse fetchUserDetails(UUID userId) {
        log.info("Fetching user details from User Service for User ID: {}", userId);
        ResponseEntity<GuestInformationResponse> responseEntity =
                restClient.get()
                        .uri("/get-info/{userId}", userId)
                        .retrieve()
                        .toEntity(GuestInformationResponse.class);

        return responseEntity.getBody();
    }
}
