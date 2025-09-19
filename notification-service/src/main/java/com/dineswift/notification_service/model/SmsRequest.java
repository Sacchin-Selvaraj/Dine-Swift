package com.dineswift.notification_service.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SmsRequest {
    private String toNumber;
    private String message;
}