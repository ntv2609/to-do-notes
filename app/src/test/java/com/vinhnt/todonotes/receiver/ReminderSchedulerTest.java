package com.vinhnt.todonotes.receiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.model.Priority;
import com.vinhnt.todonotes.data.model.TaskCategory;

import org.junit.Test;

/**
* Kiểm thử đơn vị (JUnit 4) cho điều kiện lập lịch nhắc nhở và sinh mã requestCode của ReminderScheduler.
  */
  public class ReminderSchedulerTest {

  private static final long NOW = 1_763_600_000_000L;

  @Test
  public void shouldScheduleReminder_activeTaskWithFutureTime_returnsTrue() {
  TaskEntity task = createTask(10L, false, NOW + 60_000L);
  assertTrue(ReminderScheduler.shouldScheduleReminder(task, NOW));
  }

  @Test
  public void shouldScheduleReminder_completedTaskWithFutureTime_returnsFalse() {
  TaskEntity task = createTask(10L, true, NOW + 60_000L);
  assertFalse(ReminderScheduler.shouldScheduleReminder(task, NOW));
  }

  @Test
  public void shouldScheduleReminder_pastOrNullReminder_returnsFalse() {
  TaskEntity pastTask = createTask(11L, false, NOW - 1_000L);
  TaskEntity exactNowTask = createTask(12L, false, NOW);
  TaskEntity nullReminderTask = createTask(13L, false, null);

       assertFalse(ReminderScheduler.shouldScheduleReminder(pastTask, NOW));
       assertFalse(ReminderScheduler.shouldScheduleReminder(exactNowTask, NOW));
       assertFalse(ReminderScheduler.shouldScheduleReminder(nullReminderTask, NOW));
  }

  @Test
  public void shouldScheduleReminder_invalidIdOrNullTask_returnsFalse() {
  TaskEntity unsavedTask = createTask(0L, false, NOW + 60_000L);
  assertFalse(ReminderScheduler.shouldScheduleReminder(unsavedTask, NOW));
  assertFalse(ReminderScheduler.shouldScheduleReminder(null, NOW));
  }

  @Test
  public void buildRequestCode_returnsStableNonNegativeInt() {
  assertEquals(42, ReminderScheduler.buildRequestCode(42L));
  assertTrue(ReminderScheduler.buildRequestCode(Long.MAX_VALUE) >= 0);
  assertEquals(
  ReminderScheduler.buildRequestCode(12345L),
  ReminderScheduler.buildRequestCode(12345L)
  );
  }

  private static TaskEntity createTask(long id, boolean completed, Long reminderTime) {
  TaskEntity task = new TaskEntity(
  "Nhắc nộp bài tập Android",
  "Kiểm tra lại báo cáo",
  null,
  reminderTime,
  Priority.HIGH.getValue(),
  TaskCategory.STUDY.getValue(),
  "[]",
  completed,
  NOW - 10_000L,
  NOW - 10_000L
  );
  task.setId(id);
  return task;
  }
  }
