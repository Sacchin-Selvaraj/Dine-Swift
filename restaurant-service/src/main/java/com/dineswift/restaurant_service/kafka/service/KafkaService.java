package com.dineswift.restaurant_service.kafka.service;

import com.dineswift.notification_service.model.BookingStatusUpdateDetail;
import com.dineswift.notification_service.model.EmailVerificationDetail;
import com.dineswift.notification_service.model.SmsVerificationDetail;
import com.dineswift.restaurant_service.model.TableBooking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
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

    @Value("${app.kafka.topic.email-notification-topic}")
    private String emailNotificationTopic;


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

    public CompletableFuture<Boolean> sendEmailNotification(UUID userId,
                                                            String status,
                                                            String templateType,
                                                            TableBooking existingBooking,
                                                            boolean isBookingStatusUpdated) {

        try {
            if (userId == null || status == null || templateType == null) {
                return CompletableFuture.completedFuture(false);
            }
            BookingStatusUpdateDetail bookingStatusUpdateDetail = BookingStatusUpdateDetail.builder()
                    .userId(userId)
                    .status(status)
                    .templateType(templateType)
                    .dineInTime(existingBooking.getDineInTime())
                    .noOfGuest(existingBooking.getNoOfGuest())
                    .bookingDate(existingBooking.getBookingDate())
                    .grandTotal(existingBooking.getGrandTotal())
                    .isBookingStatusUpdated(isBookingStatusUpdated)
                    .tableBookingId(existingBooking.getTableBookingId())
                    .build();
            return kafkaTemplate.send(emailNotificationTopic,bookingStatusUpdateDetail).thenApply(res->{
                log.info("Email Notification Message Published Successfully....");
                log.info("Topic Name: "+res.getRecordMetadata().topic());
                return true;
            }).exceptionally(throwable -> {
                log.error("Exception occurred while sending Email notification: " + throwable.getMessage());
                return false;
            });
        } catch (Exception e) {
            log.error("Exception occurred while sending Email notification: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }

    }

}
