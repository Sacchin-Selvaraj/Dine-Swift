package com.dineswift.userservice.model.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Data
@RequiredArgsConstructor
public class UserDTO {

    private UUID userId;

    private String username;

    private String email;

    private String gender;

    private String phoneNumber;

    private String firstName;

    private String lastName;

    private Date dob;

    private String address;

    private String area;

    private String city;

    private String district;

    private String state;

    private String country;

    private String pincode;

    private LocalDateTime registrationDate;

    private LocalDateTime UpdatedDate;

    private Boolean isActive;

    private CartDTO cart;

}
