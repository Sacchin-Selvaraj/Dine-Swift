package com.dineswift.restaurant_service.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification")
@Data
@RequiredArgsConstructor
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "verification_id", nullable = false, updatable = false)
    private UUID verificationId;

    @NotBlank(message = "Token is required")
    @Size(min = 1, max = 255, message = "Token must be between 1 and 255 characters")
    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @NotNull(message = "Token type is required")
    @Column(name = "token_type", nullable = false, length = 50)
    private TokenType tokenType;

    @NotNull(message = "Token expiry date is required")
    @Column(name = "token_expiry_date", nullable = false)
    private LocalDateTime tokenExpiryDate;

    @Pattern(
            regexp = "^[\\+]?[0-9\\s\\-\\(\\)]{10,20}$",
            message = "New phone number must be valid 10 digits"
    )
    @Column(name = "new_phonenumber", length = 20)
    private String newPhone_number;

    @Email(message = "New email should be valid")
    @Size(max = 255, message = "New email cannot exceed 255 characters")
    @Column(name = "new_email")
    private String newEmail;

    @NotNull(message = "Was used status is required")
    @Column(name = "was_used", nullable = false)
    private Boolean wasUsed = false;

    @NotNull(message = "Created at timestamp is required")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull(message = "Employee is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
}