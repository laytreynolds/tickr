package com.tickr.tickr.scheduler;

import com.tickr.tickr.domain.reminder.ReminderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReminderScheduler")
class ReminderSchedulerTest {

    @Mock
    private ReminderService reminderService;

    @InjectMocks
    private ReminderScheduler reminderScheduler;

    @Nested
    @DisplayName("init")
    class Init {

        @Test
        @DisplayName("should log poll rate on initialization")
        void shouldLogPollRateOnInit() {
            ReflectionTestUtils.setField(reminderScheduler, "pollRateMs", 10000L);

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> reminderScheduler.init()
            );
        }
    }

    @Nested
    @DisplayName("run")
    class Run {

        @Test
        @DisplayName("should delegate to ReminderService.sendDueReminders")
        void shouldDelegateToReminderService() {
            reminderScheduler.run();

            then(reminderService).should().sendDueReminders();
        }

        @Test
        @DisplayName("should not propagate exception when sendDueReminders fails")
        void shouldNotPropagateException() {
            doThrow(new RuntimeException("DB connection failed"))
                    .when(reminderService).sendDueReminders();

            // Should not throw - exception is caught inside run()
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> reminderScheduler.run()
            );
        }
    }
}
