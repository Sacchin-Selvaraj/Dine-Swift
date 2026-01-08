package com.dineswift.userservice.kafka.service;

import com.dineswift.notification_service.model.BookingStatusPayload;
import com.dineswift.userservice.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String,Object> kafkaTemplate;


    @Value("${app.kafka.topic.booking-status}")
    private String bookingStatusTopic;


    public void sendBookingStatusNotification(String email,
                                              String bookingStatusUpdate,
                                              String templateType,
                                              Map<String, Object> model) {

        BookingStatusPayload bookingStatusPayload = new BookingStatusPayload();

        bookingStatusPayload.setBookingStatusUpdate(bookingStatusUpdate);
        bookingStatusPayload.setEmail(email);
        bookingStatusPayload.setModel(model);
        bookingStatusPayload.setTemplateType(templateType);

        kafkaTemplate.send(bookingStatusTopic,bookingStatusPayload)
                .thenAccept(res-> {
                    log.info("Message Published Successfully....");
                    log.info("Topic Name: "+res.getRecordMetadata().topic());
                }).exceptionally(throwable -> {
                    log.error("Exception occurred while sending email verifications: " + throwable.getMessage());
                    throw new NotificationException("Failed to send Booking Status Email");
                });
    }
}
