package com.dineswift.restaurant_service.kafka.service;

import com.dineswift.userservice.exception.NotificationException;
import com.dineswift.userservice.kafka.payload.EmailVerificationDetail;
import com.dineswift.userservice.kafka.payload.SmsVerificationDetail;
import com.dineswift.userservice.notification.service.EmailService;
import com.dineswift.userservice.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${app.kafka.topic.email-verification-topic}")
    public String emailVerificationTopic;

    @Value("${app.kafka.topic.sms-verification-topic}")
    public String smsVerificationTopic;

    @KafkaListener(topics = "${app.kafka.topic.email-verification-topic}", groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenEmailVerification(@Payload EmailVerificationDetail message) {

        try {
            if (message==null || message.getEmail()==null || message.getToken()==null || message.getUserName()==null){
                log.error("Invalid message received in email verification topic: {}", message);
                return;
            }

            Map<String,Object> modal = new HashMap<>();
            modal.put("userName", message.getUserName());
            modal.put("companyName", "DineSwift");
            modal.put("verificationCode", message.getToken());
            modal.put("expiryTime", 10);

            emailService.sendMail(
                    message.getEmail(),
                    "Email Verification Code from DineSwift",
                    "email-verification",
                    modal
            );
            log.info("Email verification sent to: {}", message.getEmail());
        } catch (Exception e) {
            log.error("Error processing email verification message: {}", e.getMessage());
            throw new NotificationException("Failed to process email verification message: " + e.getMessage());
        }

    }
    
    @KafkaListener(topics = "${app.kafka.topic.sms-verification-topic}", groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenSmsVerification(@Payload SmsVerificationDetail message) {
        if (message==null || message.getPhoneNumber()==null || message.getToken()==null || message.getUserName()==null){
            log.error("Invalid message received in SMS verification topic: {}", message);
            return;
        }
        try {
            String smsContent = String.format(
                    Locale.ENGLISH,
                    "Hello %s, your verification code is %s. It will expire in 10 minutes. - DineSwift",
                    message.getUserName(),
                    message.getToken()
            );

            smsService.sendSms(message.getPhoneNumber(), smsContent);
            log.info("SMS verification sent to: {}", message.getPhoneNumber());
        } catch (Exception e) {
            log.error("Error processing SMS verification message: {}", e.getMessage());
            throw new NotificationException("Failed to process SMS verification message: " + e.getMessage());
        }
    }
}
