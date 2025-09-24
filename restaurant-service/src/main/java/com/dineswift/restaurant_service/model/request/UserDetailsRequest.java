package com.dineswift.restaurant_service.model.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import jakarta.validation.constraints.*;

@Data
@RequiredArgsConstructor
public class UserDetailsRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private Date dob;

    @Size(min = 2, max = 255, message = "Address must be between 2 and 250 characters")
    private String address;

    @Size(min = 2 ,max = 100, message = "Area must be between 2 and 100 characters")
    private String area;

    @Size(min = 2, max = 100, message = "City must be between 2 and 50 characters")
    private String city;

    @Size(min = 3, max = 50, message = "District must be between 3 and 50 characters")
    private String district;

    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    private String state;

    @Size(min = 2, max = 50, message = "Country must be between 5 and 50 characters")
    private String country;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Pincode must be a valid 6-digit number")
    private String pincode;

}
