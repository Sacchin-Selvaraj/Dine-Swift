package com.dineswift.notification_service.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioInitializer {

    private final String accountSid;
    private final String authToken;

    public TwilioInitializer(@Value("${twilio.account.sid}") String accountSid,
                             @Value("${twilio.auth.token}") String authToken) {
        this.accountSid = accountSid;
        this.authToken = authToken;
    }

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }
}