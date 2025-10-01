package com.dineswift.userservice.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
public class EmailService {


    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Value("${spring.mail.username}")
    public String fromMail;


    public void sendMail(String toMail, String subject,String templateType, Map<String, Object> model) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            Context context = new Context();
            context.setVariables(model);
            String htmlContent = templateEngine.process(templateType, context);

            helper.setFrom(fromMail);
            helper.setTo(toMail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email");
        }
    }
}

//Context context = new Context();
//            context.setVariable("userName", userName);
//            context.setVariable("companyName", companyName);
//            context.setVariable("companyDomain", companyDomain);
//            context.setVariable("verificationCode", verificationCode);
//            context.setVariable("expiryTime", 30); // 30 minutes expiry
//            context.setVariable("currentYear", LocalDate.now().getYear());


// forgot-password
// context.setVariable("userName", userName);
//            context.setVariable("companyName", companyName);
//            context.setVariable("companyDomain", companyDomain);
//            context.setVariable("resetLink", resetLink);
//            context.setVariable("resetCode", resetCode);
//            context.setVariable("expiryTime", expiryMinutes);
//            context.setVariable("currentYear", LocalDate.now().getYear());
