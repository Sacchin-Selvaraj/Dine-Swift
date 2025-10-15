package com.dineswift.userservice.kafka.service;


import com.dineswift.notification_service.model.EmailVerificationDetail;
import com.dineswift.notification_service.model.SmsVerificationDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaService {

    private final KafkaTemplate<String,Object> kafkaTemplate;


    @Value("${app.kafka.topic.email-verification-topic}")
    private String emailVerificationTopic;

    @Value("${app.kafka.topic.sms-verification-topic}")
    private String smsVerificationTopic;

    @Value("${app.kafka.topic.email-forgot-password-topic}")
    private String emailForgotPasswordTopic;

    @Value("${app.kafka.topic.sms-forgot-password-topic}")
    private String smsForgotPasswordTopic;

    public CompletableFuture<Boolean> sendEmailVerification(String toEmail, String token, String userName) {

        try {
            EmailVerificationDetail message = new EmailVerificationDetail();
            message.setEmail(toEmail);
            message.setToken(token);
            message.setUserName(userName);

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
            SmsVerificationDetail smsVerificationDetail=new SmsVerificationDetail();
            smsVerificationDetail.setPhoneNumber(toPhoneNumber);
            smsVerificationDetail.setToken(token);
            smsVerificationDetail.setUserName(userName);

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

    public CompletionStage<Boolean> sendEmailForForgotPassword(String toEmail, String token, String username) {
        try {
            EmailVerificationDetail message = new EmailVerificationDetail();
            message.setEmail(toEmail);
            message.setToken(token);
            message.setUserName(username);

            CompletableFuture<SendResult<String, Object>> result = kafkaTemplate.send(emailForgotPasswordTopic, message);

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

    public CompletableFuture<Boolean> sendSmsForForgotPassword(String phoneNumber, String token,  String username) {

        try {
            if (phoneNumber == null || token == null || username == null) {
                return CompletableFuture.completedFuture(false);
            }
            SmsVerificationDetail smsVerificationDetail=new SmsVerificationDetail();
            smsVerificationDetail.setPhoneNumber(phoneNumber);
            smsVerificationDetail.setToken(token);
            smsVerificationDetail.setUserName(username);

            return kafkaTemplate.send(smsForgotPasswordTopic,smsVerificationDetail).thenApply(res->{
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
