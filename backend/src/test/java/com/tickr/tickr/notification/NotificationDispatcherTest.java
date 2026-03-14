package com.tickr.tickr.notification;

import com.tickr.tickr.domain.notification.Notification;
import com.tickr.tickr.domain.reminder.Reminder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDispatcher")
class NotificationDispatcherTest {

    @Test
    @DisplayName("should dispatch notification to the first supporting sender")
    void shouldDispatchToSupportingSender() {
        Notification notification = mock(Notification.class);
        NotificationSender sender = mock(NotificationSender.class);
        given(sender.supports(notification)).willReturn(true);

        NotificationDispatcher dispatcher = new NotificationDispatcher(List.of(sender));
        dispatcher.send(notification);

        then(sender).should().send(notification);
    }

    @Test
    @DisplayName("should skip non-supporting senders and use the matching one")
    void shouldSkipNonSupportingSender() {
        Notification notification = mock(Notification.class);
        NotificationSender nonSupporting = mock(NotificationSender.class);
        NotificationSender supporting = mock(NotificationSender.class);

        given(nonSupporting.supports(notification)).willReturn(false);
        given(supporting.supports(notification)).willReturn(true);

        NotificationDispatcher dispatcher = new NotificationDispatcher(List.of(nonSupporting, supporting));
        dispatcher.send(notification);

        then(nonSupporting).should(never()).send(notification);
        then(supporting).should().send(notification);
    }

    @Test
    @DisplayName("should throw when no sender supports the notification")
    void shouldThrowWhenNoSenderSupports() {
        Notification notification = mock(Notification.class);
        given(notification.getChannel()).willReturn(Reminder.Channel.PHONE);

        NotificationSender sender = mock(NotificationSender.class);
        given(sender.supports(notification)).willReturn(false);

        NotificationDispatcher dispatcher = new NotificationDispatcher(List.of(sender));

        assertThatThrownBy(() -> dispatcher.send(notification))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No sender found for notification channel");
    }

    @Test
    @DisplayName("should throw when sender list is empty")
    void shouldThrowWhenNoSenders() {
        Notification notification = mock(Notification.class);
        given(notification.getChannel()).willReturn(Reminder.Channel.EMAIL);

        NotificationDispatcher dispatcher = new NotificationDispatcher(Collections.emptyList());

        assertThatThrownBy(() -> dispatcher.send(notification))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No sender found for notification channel");
    }
}
