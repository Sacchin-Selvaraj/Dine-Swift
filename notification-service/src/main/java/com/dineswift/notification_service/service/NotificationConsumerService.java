package com.dineswift.userservice.kafka.service;

import com.dineswift.notification_service.model.BookingStatusUpdateDetail;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.notification.service.EmailService;
import com.dineswift.userservice.service.BookingService;
import com.dineswift.userservice.service.UserCommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
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
    private final BookingService bookingService;
    private final KafkaProducerService kafkaProducerService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 2),
            dltTopicSuffix = "-booking-status-update-dlt"
    )
    @KafkaListener(topics = "${app.kafka.topic.email-notification-topic}", groupId = "user-service-group-v1")
    public void listenEmailVerification(@Payload BookingStatusUpdateDetail message) {
        if (message==null){
            log.error("Invalid message received in email notification topic: {}", message);
            return;
        }
        User userDetail = userCommonService.findValidUser(message.getUserId());

        Map<String,Object> model = Map.of(
                "userName", userDetail.getFirstName(),
                "status", message.getStatus(),
                "dineInTime", message.getDineInTime(),
                "noOfGuest", message.getNoOfGuest(),
                "bookingDate", message.getBookingDate(),
                "grandTotal", message.getGrandTotal()
        );

        if (message.isBookingStatusUpdated()){
            bookingService.updateBookingStatus(message.getTableBookingId(), message.getStatus());
        }

        kafkaProducerService.sendBookingStatusNotification(userDetail.getEmail(),
                "Booking Status Update",
                message.getTemplateType(),
                model
                );

        emailService.sendMailThroughResend(
                userDetail.getEmail(),
                "Booking Status Update",
                message.getTemplateType(),
                model
        );
        log.info("Booking status update email sent to: {}", userDetail.getEmail());
    }

    @KafkaListener(topics = "${app.kafka.topic.email-notification-topic}-booking-status-update-dlt", groupId = "user-service-group-dlt")
    public void listenEmailVerificationDlt(@Payload BookingStatusUpdateDetail message,
                                           @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE) String dltExceptionMessage,
                                           @Header(value = KafkaHeaders.ORIGINAL_TOPIC) String originalTopic,
                                           @Header(value = KafkaHeaders.ORIGINAL_PARTITION) Integer originalPartition
    ) {
        log.error("DLT Exception Message: {}", dltExceptionMessage);
        log.error("Original Topic: {}, Original Partition: {}", originalTopic, originalPartition);
        log.error("Message moved to DLT in email notification topic: {}", message);
    }

}
