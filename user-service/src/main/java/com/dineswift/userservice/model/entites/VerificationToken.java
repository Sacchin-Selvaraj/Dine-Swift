package com.dineswift.userservice.model.entites;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "verification_id", updatable = false, nullable = false)
    private UUID verificationId;

    @NotBlank(message = "Token is required")
    @Size(min = 6, max = 20, message = "Token must be between 6 and 20 characters")
    @Column(name = "token", nullable = false, length = 20,unique = true)
    private String token;

    @NotBlank(message = "Token type is required")
    @Size(min = 3, max = 50, message = "Token type must be between 3 and 50 characters")
    @Column(name = "token_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @Future(message = "Token expiry date must be in the future")
    @Column(name = "token_expiry_date", nullable = false)
    private LocalDateTime tokenExpiryDate;

    @Email(message = "New email must be a valid email address")
    @Column(name = "new_email")
    private String newEmail;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "New phone number must be a valid")
    @Column(name = "new_phonenumber")
    private String newPhonenumber;

    @Column(name = "was_used", nullable = false)
    private Boolean wasUsed = false;

    @NotNull(message = "Created date is required")
    @PastOrPresent(message = "Created date must be in the past or present")
    @Column(name = "created_at", nullable = false, updatable = false)
    @UpdateTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;
}
