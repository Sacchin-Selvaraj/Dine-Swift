//package com.dineswift.userservice.notification.service;
//
//import com.dineswift.userservice.exception.NotificationException;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//import org.thymeleaf.context.Context;
//import org.thymeleaf.spring6.SpringTemplateEngine;
//
//import java.util.Map;
//
//@Slf4j
//@Service
//public class EmailService {
//
//
//    private final JavaMailSender javaMailSender;
//    private final SpringTemplateEngine templateEngine;
//
//    public EmailService(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine) {
//        this.javaMailSender = javaMailSender;
//        this.templateEngine = templateEngine;
//    }
//
//    @Value("${spring.mail.username}")
//    public String fromMail;
//
//
//    public void sendMail(String toMail, String subject,String templateType, Map<String, Object> model) {
//
//        try {
//            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
//
//            Context context = new Context();
//            context.setVariables(model);
//            String htmlContent = templateEngine.process(templateType, context);
//
//            helper.setFrom(fromMail);
//            helper.setTo(toMail);
//            helper.setSubject(subject);
//            helper.setText(htmlContent, true);
//
//            javaMailSender.send(mimeMessage);
//
//        } catch (MessagingException e) {
//            throw new NotificationException("Failed to send email");
//        }
//    }
//}
//
