package com.tickr.tickr.notification;

import com.tickr.tickr.domain.notification.Notification;
import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.dto.EmailNotification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends email notifications via JavaMail (SMTP). Configure in application.yml
 * under spring.mail (host, port, username, password).
 */
@Component
public class EmailNotificationSender implements NotificationSender {

    private final JavaMailSender mailSender;

    public EmailNotificationSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean supports(Notification notification) {
        return notification != null && notification.getChannel() == Reminder.Channel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        if (!supports(notification)) {
            throw new IllegalArgumentException("EmailNotificationSender does not support " + notification.getChannel());
        }
        EmailNotification email = (EmailNotification) notification;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email.getTo());
        message.setSubject(email.getSubject());
        message.setText(email.getBody());
        mailSender.send(message);
    }
}
