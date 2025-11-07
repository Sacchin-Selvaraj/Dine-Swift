package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.response.guestInformation.GuestInformationResponse;
import com.dineswift.restaurant_service.service.GuestInformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/restaurant/guest-information")
public class GuestInformationController {

    private final GuestInformationService guestInformationService;

    @PreAuthorize(("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER','ROLE_CHEF', 'ROLE_WAITER')"))
    @GetMapping("/info/{guestInformationId}")
    public ResponseEntity<GuestInformationResponse> getGuestInformationById(@PathVariable UUID guestInformationId) {
        log.info("Fetching guest information for ID: {}", guestInformationId);
        GuestInformationResponse response = guestInformationService.getGuestInformationById(guestInformationId);
        return ResponseEntity.ok(response);
    }
}
