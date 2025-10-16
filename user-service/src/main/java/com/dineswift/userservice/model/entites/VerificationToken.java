package com.dineswift.userservice.model.entites;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Data
@RequiredArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "verification_id", updatable = false, nullable = false)
    private UUID verificationId;

    @NotBlank(message = "Token is required")
    @Size(min = 6, max = 20, message = "Token must be between 6 and 20 characters")
    @Column(name = "token", nullable = false,unique = true)
    private String token;

    @NotNull(message = "Token type is required")
    @Column(name = "token_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @NotNull(message = "Token Status is required")
    @Column(name = "token_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenStatus tokenStatus;


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

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH},fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;
}
