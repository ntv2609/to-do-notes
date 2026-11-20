package com.vinhnt.todonotes.receiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
* Kiểm thử đơn vị (JUnit 4) cho quy tắc đồng bộ thời điểm nhắc nhở khi thay đổi hạn chót (BR-02 & UC 3.15 rẽ nhánh 2).
  */
  public class ReminderSyncLogicTest {

  private static final long NOW = 1_763_600_000_000L;

  @Test
  public void resolveReminderAfterDueDateChange_noReminder_returnsNull() {
  assertNull(ReminderScheduler.resolveReminderAfterDueDateChange(null, NOW + 3_600_000L, NOW));
  }

  @Test
  public void resolveReminderAfterDueDateChange_newDueDateAfterReminder_keepsOriginalReminder() {
  long currentReminder = NOW + 3_600_000L;
  long newDueDate = NOW + 7_200_000L;
  assertEquals(
  Long.valueOf(currentReminder),
  ReminderScheduler.resolveReminderAfterDueDateChange(currentReminder, newDueDate, NOW)
  );
  }

  @Test
  public void resolveReminderAfterDueDateChange_newDueDateEarlierButInFuture_syncsReminderToNewDueDate() {
  long currentReminder = NOW + 7_200_000L;
  long newDueDate = NOW + 1_800_000L;
  assertEquals(
  Long.valueOf(newDueDate),
  ReminderScheduler.resolveReminderAfterDueDateChange(currentReminder, newDueDate, NOW)
  );
  }

  @Test
  public void resolveReminderAfterDueDateChange_newDueDateInPast_clearsReminder() {
  long currentReminder = NOW + 3_600_000L;
  long newDueDateInPast = NOW - 60_000L;
  assertNull(ReminderScheduler.resolveReminderAfterDueDateChange(currentReminder, newDueDateInPast, NOW));
  }
  }
