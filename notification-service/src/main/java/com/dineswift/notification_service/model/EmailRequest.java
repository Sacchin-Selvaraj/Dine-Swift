package com.dineswift.notification_service.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EmailRequest {

    private String to;
    private String subject;
    private String body;
}
