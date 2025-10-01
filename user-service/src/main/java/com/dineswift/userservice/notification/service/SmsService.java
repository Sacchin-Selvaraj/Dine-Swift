package com.dineswift.userservice.notification.service;


import com.dineswift.userservice.exception.UserException;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SmsService {

    @Value("${twilio.phone.number}")
    private String fromNumber;

    @Async
    public CompletableFuture<Boolean> sendSms(String toNumber, String messageBody) {
        try {
            Message message = Message.creator(
                            new PhoneNumber(toNumber),
                            new PhoneNumber(fromNumber),
                            messageBody)
                    .create();

            return message.getSid() != null ?
                    CompletableFuture.completedFuture(true) :
                    CompletableFuture.completedFuture(false);
        } catch (ApiException e) {
            throw new UserException(e.getMessage());
        }
    }

}
