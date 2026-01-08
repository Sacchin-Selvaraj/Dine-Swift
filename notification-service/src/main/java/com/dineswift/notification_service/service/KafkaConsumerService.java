package com.dineswift.notification_service.service;

import com.dineswift.notification_service.model.EmailVerificationDetail;
import com.dineswift.notification_service.model.SmsVerificationDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final EmailService emailService;
    private final SmsService smsService;


    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 2),
            exclude = {RuntimeException.class},
            dltTopicSuffix = "-email-verification-dlt"
    )
    @KafkaListener(topics = "${app.kafka.topic.email-verification-topic}", groupId = "user-service-group-v1",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenEmailVerification(@Payload EmailVerificationDetail message) {

        try {
            if (message==null || message.getEmail()==null || message.getToken()==null || message.getUserName()==null){
                log.error("Invalid message received in email verification topic: {}", message);
                return;
            }

            log.info("Message Received in Kafka Consumer");

            Map<String,Object> modal = new HashMap<>();
            modal.put("userName", message.getUserName());
            modal.put("companyName", "DineSwift");
            modal.put("verificationCode", message.getToken());
            modal.put("expiryTime", 10);

            emailService.sendMail(
                    message.getEmail(),
                    "Verification Code from DineSwift",
                    message.getTemplateType(),
                    modal
            );
            log.info("Email verification sent to: {}", message.getEmail());
        } catch (Exception e) {
            log.error("Error processing email verification message: {}", e.getMessage());
        }

    }
    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 2),
            dltTopicSuffix = "-sms-verification-dlt"
    )
    @KafkaListener(topics = "${app.kafka.topic.email-verification-topic}-email-verification-dlt",
            groupId = "dlt-listener-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void dltListenerEmailVerification(@Payload EmailVerificationDetail message,
                                             @Header(value = KafkaHeaders.ORIGINAL_TOPIC,required = false) String originalTopic,
                                             @Header(value = KafkaHeaders.ORIGINAL_PARTITION,required = false) Integer originalPartition,
                                             @Header(value = KafkaHeaders.ORIGINAL_OFFSET,required = false) Long originalOffset
    ) {
        log.error("DLT reached for email verification message: {}", message);
        log.error("Original Topics: {}, Partition: {}, Offset: {}", originalTopic, originalPartition, originalOffset);
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 2),
            dltTopicSuffix = "-sms-verification-dlt"
    )
    @KafkaListener(topics = "${app.kafka.topic.sms-verification-topic}", groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenSmsVerification(@Payload SmsVerificationDetail message) {
        if (message==null || message.getPhoneNumber()==null || message.getToken()==null || message.getUserName()==null){
            log.error("Invalid message received in SMS verification topic: {}", message);
            return;
        }

        log.info("Message Received in Kafka Consumer at SMS");

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
        }
    }

    @KafkaListener(topics = "${app.kafka.topic.sms-verification-topic}-sms-verification-dlt",
            groupId = "dlt-listener-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void dltListenerSmsVerification(@Payload SmsVerificationDetail message,
                                           @Header(value = KafkaHeaders.ORIGINAL_TOPIC,required = false) String originalTopic,
                                           @Header(value = KafkaHeaders.ORIGINAL_PARTITION,required = false) Integer originalPartition,
                                           @Header(value = KafkaHeaders.ORIGINAL_OFFSET, required = false) Long originalOffset
    ) {
        log.error("DLT reached for SMS verification message: {}", message);
        log.error("Original Topic: {}, Partition: {}, Offset: {}", originalTopic, originalPartition, originalOffset);
    }
}
