package com.dineswift.restaurant_service.payload.response.guestInformation;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class GuestInformationResponse {

    private UUID userId;
    private String username;
    private String email;
    private String gender;
    private String phoneNumber;
    private String firstName;
    private String lastName;
}
