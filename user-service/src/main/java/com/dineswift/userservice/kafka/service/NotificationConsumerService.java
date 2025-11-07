package com.dineswift.userservice.kafka.service;

import com.dineswift.notification_service.model.BookingStatusUpdateDetail;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.notification.service.EmailService;
import com.dineswift.userservice.service.UserCommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumerService {

    private final EmailService emailService;
    private final UserCommonService userCommonService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 2),
            dltTopicSuffix = "-booking-status-update-dlt"
    )
    @KafkaListener(topics = "${app.kafka.topic.email-notification-topic}", groupId = "user-service-group")
    public void listenEmailVerification(@Payload BookingStatusUpdateDetail message) {
        if (message==null){
            log.error("Invalid message received in email notification topic: {}", message);
            return;
        }
        User userDetail = userCommonService.findValidUser(message.getUserId());

        Map<String,Object> modal = Map.of(
                "userName", userDetail.getFirstName(),
                "
                "status", message.getStatus(),
                "updateTime", message.getUpdateTime()
        );

        emailService.sendMail(
                userDetail.getEmail(),
                "Booking Status Update",
                message.getTemplateType(),

        );
        log.info("Booking status update email sent to: {}", userDetail.getEmail());

    }


}
