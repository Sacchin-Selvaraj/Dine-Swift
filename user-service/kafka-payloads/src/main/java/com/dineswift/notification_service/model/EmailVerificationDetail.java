package com.dineswift.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailVerificationDetail {

    private String email;
    private String token;
    private String userName;
    private String templateType;
}
