package com.dineswift.notification_service.service;

import com.dineswift.notification_service.model.BookingStatusPayload;
import com.dineswift.notification_service.model.BookingStatusUpdateDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumerService {

    private final EmailService emailService;


    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 2),
            dltTopicSuffix = "-booking-status-update-dlt"
    )
    @KafkaListener(topics = "${app.kafka.topic.booking-status}", groupId = "user-service-group-v1")
    public void listenEmailVerification(@Payload BookingStatusPayload payload) {
        if (payload==null){
            log.error("Invalid message received in Booking Status notification topic");
            return;
        }

        Map<String,Object> model = null;
        try {
            List<Integer> dateParts = (List<Integer>) payload.getModel().get("bookingDate");

            LocalDate bookingDate = LocalDate.of(
                    dateParts.get(0),
                    dateParts.get(1),
                    dateParts.get(2)
            );

            List<Integer> timeParts = (List<Integer>) payload.getModel().get("dineInTime");

            LocalTime dineInTime = LocalTime.of(
                    timeParts.get(0),
                    timeParts.get(1)
            );

            model = payload.getModel();

            model.put("bookingDate",bookingDate);
            model.put("dineInTime",dineInTime);
        } catch (Exception e) {
            throw new RuntimeException("Class Cast Exception: "+ e.getMessage());
        }

        emailService.sendMail(
                payload.getEmail(),
                payload.getBookingStatusUpdate(),
                payload.getTemplateType(),
                model
        );

        log.info("Booking status update email sent to: {}", payload.getEmail());
    }

    @KafkaListener(topics = "${app.kafka.topic.booking-status}-booking-status-update-dlt", groupId = "user-service-group-dlt")
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
