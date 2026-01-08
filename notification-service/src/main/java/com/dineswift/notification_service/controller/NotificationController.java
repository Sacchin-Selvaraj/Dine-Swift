package com.dineswift.notification_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @GetMapping("/greet")
    public ResponseEntity<String> greet(){

        return ResponseEntity.ok("Notification Service is Working");

    }

}
