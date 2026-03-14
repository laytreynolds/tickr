package com.tickr.tickr.notification;

import com.tickr.tickr.domain.notification.Notification;
import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.dto.EmailNotification;
import com.tickr.tickr.dto.SmsNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailNotificationSender")
class EmailNotificationSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationSender sender;

    @Nested
    @DisplayName("supports")
    class Supports {

        @Test
        @DisplayName("should return true for EMAIL channel")
        void shouldSupportEmailChannel() {
            EmailNotification notification = new EmailNotification("test@test.com", "Subject", "Body");
            assertThat(sender.supports(notification)).isTrue();
        }

        @Test
        @DisplayName("should return false for SMS channel")
        void shouldNotSupportSmsChannel() {
            SmsNotification notification = new SmsNotification("+1234", "Body");
            assertThat(sender.supports(notification)).isFalse();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(sender.supports(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for non-EMAIL channel notification")
        void shouldReturnFalseForNonEmailChannel() {
            Notification notification = mock(Notification.class);
            when(notification.getChannel()).thenReturn(Reminder.Channel.PHONE);
            assertThat(sender.supports(notification)).isFalse();
        }
    }

    @Nested
    @DisplayName("send")
    class Send {

        @Test
        @DisplayName("should send email via JavaMailSender")
        void shouldSendEmail() {
            EmailNotification notification = new EmailNotification("user@example.com", "Reminder", "Your event is soon");

            sender.send(notification);

            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            then(mailSender).should().send(messageCaptor.capture());

            SimpleMailMessage sent = messageCaptor.getValue();
            assertThat(sent.getTo()).containsExactly("user@example.com");
            assertThat(sent.getSubject()).isEqualTo("Reminder");
            assertThat(sent.getText()).isEqualTo("Your event is soon");
        }

        @Test
        @DisplayName("should throw for unsupported notification")
        void shouldThrowForUnsupportedNotification() {
            SmsNotification smsNotification = new SmsNotification("+1234", "Text");

            assertThatThrownBy(() -> sender.send(smsNotification))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not support");
        }
    }
}
