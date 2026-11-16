package com.vinhnt.todonotes.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.appcompat.app.AppCompatDelegate;

import com.vinhnt.todonotes.data.model.Priority;
import com.vinhnt.todonotes.data.model.SubtaskItem;
import com.vinhnt.todonotes.data.model.TaskCategory;
import com.vinhnt.todonotes.util.ValidationUtils.NoteValidationError;
import com.vinhnt.todonotes.util.ValidationUtils.TaskValidationError;

import org.junit.Test;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
* Bộ kiểm thử đơn vị (Local Unit Test) cho ValidationUtils, SubtaskJsonHelper, DateTimeUtils, PreferencesManager và các Enum mô hình.
  */
  public class ValidationAndHelperTest {

  @Test
  public void validateTaskInput_emptyOrWhitespaceTitle_returnsEmptyTitleError() {
  long now = 1_700_000_000_000L;
  assertEquals(
  TaskValidationError.EMPTY_TITLE,
  ValidationUtils.validateTaskInput("", "Mô tả", null, true, now)
  );
  assertEquals(
  TaskValidationError.EMPTY_TITLE,
  ValidationUtils.validateTaskInput("   \n\t ", "Mô tả", null, true, now)
  );
  }

  @Test
  public void validateTaskInput_reminderInPast_returnsReminderInPastError() {
  long now = 1_700_000_000_000L;
  long pastReminder = now - 60_000L;
  assertEquals(
  TaskValidationError.REMINDER_IN_PAST,
  ValidationUtils.validateTaskInput("Học Android Java", "", pastReminder, true, now)
  );
  assertEquals(
  TaskValidationError.REMINDER_IN_PAST,
  ValidationUtils.validateTaskInput("Học Android Java", "", now, true, now)
  );
  }

  @Test
  public void validateTaskInput_validTaskWithFutureReminder_returnsNone() {
  long now = 1_700_000_000_000L;
  long futureReminder = now + 3_600_000L;
  assertEquals(
  TaskValidationError.NONE,
  ValidationUtils.validateTaskInput("Hoàn thành bài tập lớn", "Chương 3 Use Case", futureReminder, true, now)
  );
  }

  @Test
  public void validateNoteInput_bothEmpty_returnsEmptyNoteError() {
  assertEquals(
  NoteValidationError.EMPTY_NOTE,
  ValidationUtils.validateNoteInput("", "   ")
  );
  }

  @Test
  public void validateNoteInput_onlyContentProvided_returnsNone() {
  assertEquals(
  NoteValidationError.NONE,
  ValidationUtils.validateNoteInput("", "Ghi nhanh ý tưởng đồ án Android")
  );
  }

  @Test
  public void deriveTaskTitleFromNote_extractsFirstLineWhenTitleEmpty() {
  String noteContent = "Mua đồ chuẩn bị thuyết trình\n- Cáp HDMI\n- Chuột không dây";
  String derivedTitle = ValidationUtils.deriveTaskTitleFromNote("   ", noteContent);
  assertEquals("Mua đồ chuẩn bị thuyết trình", derivedTitle);
  }

  @Test
  public void subtaskJsonHelper_roundTripPreservesQuotesAndSpecialCharacters() {
  List<SubtaskItem> original = Arrays.asList(
  new SubtaskItem("Viết tài liệu \"SPEC.md\" & kiểm tra", true),
  new SubtaskItem("Chạy lệnh .\\gradlew.bat testDebugUnitTest", false)
  );

       String json = SubtaskJsonHelper.toJson(original);
       List<SubtaskItem> restored = SubtaskJsonHelper.fromJson(json);

       assertEquals(2, restored.size());
       assertEquals(original, restored);
       assertEquals(1, SubtaskJsonHelper.countCompleted(restored));
  }

  @Test
  public void subtaskJsonHelper_emptyOrNull_returnsEmptyList() {
  assertEquals("[]", SubtaskJsonHelper.toJson(Collections.emptyList()));
  assertTrue(SubtaskJsonHelper.fromJson(null).isEmpty());
  assertTrue(SubtaskJsonHelper.fromJson("[]").isEmpty());
  }

  @Test
  public void dateTimeUtils_isOverdueAndSameDay_worksCorrectly() {
  ZoneId hanoiZone = ZoneId.of("Asia/Ho_Chi_Minh");
  long now = 1_763_215_200_000L; // 2025-11-15
  long pastDue = now - 10_000L;
  long futureDue = now + 10_000L;

       assertTrue(DateTimeUtils.isOverdue(false, pastDue, now));
       assertFalse(DateTimeUtils.isOverdue(true, pastDue, now));
       assertFalse(DateTimeUtils.isOverdue(false, futureDue, now));
       assertFalse(DateTimeUtils.isOverdue(false, null, now));

       long startOfDay = DateTimeUtils.getStartOfDayMillis(now, hanoiZone);
       long endOfDay = DateTimeUtils.getEndOfDayMillis(now, hanoiZone);
       assertTrue(DateTimeUtils.isSameDay(startOfDay, now, hanoiZone));
       assertTrue(DateTimeUtils.isSameDay(endOfDay, now, hanoiZone));
       assertFalse(DateTimeUtils.isSameDay(endOfDay + 1000L, now, hanoiZone));
  }

  @Test
  public void enums_fromValueFallbackWorks() {
  assertEquals(Priority.HIGH, Priority.fromValue(2));
  assertEquals(Priority.MEDIUM, Priority.fromValue(99));
  assertEquals(TaskCategory.STUDY, TaskCategory.fromValue(1));
  assertEquals(TaskCategory.PERSONAL, TaskCategory.fromValue(-5));
  }

  @Test
  public void preferencesManager_resolveNightModeAndLanguageTag_worksCorrectly() {
  assertEquals(
  AppCompatDelegate.MODE_NIGHT_NO,
  PreferencesManager.resolveNightModeFlag(PreferencesManager.THEME_LIGHT)
  );
  assertEquals(
  AppCompatDelegate.MODE_NIGHT_YES,
  PreferencesManager.resolveNightModeFlag(PreferencesManager.THEME_DARK)
  );
  assertEquals(
  AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
  PreferencesManager.resolveNightModeFlag(PreferencesManager.THEME_SYSTEM)
  );

       assertEquals("en", PreferencesManager.normalizeLanguageTag("en-US"));
       assertEquals("vi", PreferencesManager.normalizeLanguageTag("vi-VN"));
       assertEquals("vi", PreferencesManager.normalizeLanguageTag(null));
  }
  }
