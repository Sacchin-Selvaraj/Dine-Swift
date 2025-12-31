//package com.dineswift.userservice.kafka.service;
//
//import com.dineswift.notification_service.model.SmsVerificationDetail;
//import com.dineswift.userservice.notification.service.EmailService;
//import com.dineswift.notification_service.service.SmsService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.annotation.RetryableTopic;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.stereotype.Service;
//
//import java.util.Locale;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class KafkaConsumerService {
//
//    private final EmailService emailService;
//    private final SmsService smsService;
//
//
//    @RetryableTopic(
//            attempts = "4",
//            backoff = @Backoff(delay = 2000, multiplier = 2),
//            dltTopicSuffix = "-sms-verification-dlt"
//    )
//    @KafkaListener(topics = "${app.kafka.topic.sms-verification-topic}", groupId = "user-service-group",
//            containerFactory = "kafkaListenerContainerFactory")
//    public void listenSmsVerification(@Payload SmsVerificationDetail message) {
//        if (message==null || message.getPhoneNumber()==null || message.getToken()==null || message.getUserName()==null){
//            log.error("Invalid message received in SMS verification topic: {}", message);
//            return;
//        }
//
//        log.info("Message Received in Kafka Consumer at SMS");
//
//        try {
//            String smsContent = String.format(
//                    Locale.ENGLISH,
//                    "Hello %s, your verification code is %s. It will expire in 10 minutes. - DineSwift",
//                    message.getUserName(),
//                    message.getToken()
//            );
//
//            smsService.sendSms(message.getPhoneNumber(), smsContent);
//            log.info("SMS verification sent to: {}", message.getPhoneNumber());
//        } catch (Exception e) {
//            log.error("Error processing SMS verification message: {}", e.getMessage());
//        }
//    }
//
//
//    @KafkaListener(topics = "${app.kafka.topic.sms-verification-topic}-sms-verification-dlt",
//            groupId = "dlt-listener-group",
//            containerFactory = "kafkaListenerContainerFactory")
//    public void dltListenerSmsVerification(@Payload SmsVerificationDetail message,
//                                           @Header(value = KafkaHeaders.ORIGINAL_TOPIC,required = false) String originalTopic,
//                                           @Header(value = KafkaHeaders.ORIGINAL_PARTITION,required = false) Integer originalPartition,
//                                           @Header(value = KafkaHeaders.ORIGINAL_OFFSET, required = false) Long originalOffset
//    ) {
//        log.error("DLT reached for SMS verification message: {}", message);
//        log.error("Original Topic: {}, Partition: {}, Offset: {}", originalTopic, originalPartition, originalOffset);
//    }
//}
