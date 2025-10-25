package com.dineswift.restaurant_service.kafka.service;

import com.dineswift.notification_service.model.EmailVerificationDetail;
import com.dineswift.notification_service.model.SmsVerificationDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaService {

    private final KafkaTemplate<String,Object> kafkaTemplate;


    @Value("${app.kafka.topic.email-verification-topic}")
    private String emailVerificationTopic;

    @Value("${app.kafka.topic.sms-verification-topic}")
    private String smsVerificationTopic;

    public CompletableFuture<Boolean> sendEmailVerification(String toEmail, String token, String userName,String templateType) {

        try {
            EmailVerificationDetail message = EmailVerificationDetail.builder()
                    .email(toEmail)
                    .token(token)
                    .userName(userName)
                    .templateType(templateType)
                    .build();

            CompletableFuture<SendResult<String, Object>> result = kafkaTemplate.send(emailVerificationTopic, message);

            return result.thenApply(res-> {
                    log.info("Message Published Successfully....");
                    log.info("Topic Name: "+res.getRecordMetadata().topic());
                    return true;
            }).exceptionally(throwable -> {
                log.error("Exception occurred while sending email verification: " + throwable.getMessage());
                return false;
            });

        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }

    }

    public CompletableFuture<Boolean> sendSmsVerification(String toPhoneNumber, String token, String userName) {

        try {
            if (toPhoneNumber == null || token == null || userName == null) {
                return CompletableFuture.completedFuture(false);
            }
            SmsVerificationDetail smsVerificationDetail= SmsVerificationDetail.builder()
                    .phoneNumber(toPhoneNumber)
                    .token(token)
                    .userName(userName)
                    .build();

            return kafkaTemplate.send(smsVerificationTopic,smsVerificationDetail).thenApply(res->{
                log.info("SMS Message Published Successfully....");
                log.info("Topic Name: "+res.getRecordMetadata().topic());
                return true;
            }).exceptionally(throwable -> {
                log.error("Exception occurred while sending SMS verification: " + throwable.getMessage());
                return false;
            });
        } catch (Exception e) {
            log.error("Exception occurred while sending SMS verification: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }

    }

}
