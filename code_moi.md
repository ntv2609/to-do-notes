# MANIFEST: 11
# File: gradle/libs.versions.toml
[versions]
agp = "9.4.1"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
appcompat = "1.8.0"
material = "1.14.0"
activityKtx = "1.13.0"
constraintlayout = "2.2.2"
recyclerview = "1.4.0"
lifecycle = "2.8.7"
room = "2.6.1"
archCoreTesting = "2.2.0"

[libraries]
junit = { group = "junit", name = "junit", version.ref = "junit" }
ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
activity-ktx = { group = "androidx.activity", name = "activity-ktx", version.ref = "activityKtx" }
constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
recyclerview = { group = "androidx.recyclerview", name = "recyclerview", version.ref = "recyclerview" }
lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel", version.ref = "lifecycle" }
lifecycle-livedata = { group = "androidx.lifecycle", name = "lifecycle-livedata", version.ref = "lifecycle" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
arch-core-testing = { group = "androidx.arch.core", name = "core-testing", version.ref = "archCoreTesting" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }

# File: app/build.gradle.kts
plugins {
alias(libs.plugins.android.application)
}

android {
namespace = "com.vinhnt.todonotes"
compileSdk {
version = release(37)
}

    defaultConfig {
        applicationId = "com.vinhnt.todonotes"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

dependencies {
implementation(libs.activity.ktx)
implementation(libs.appcompat)
implementation(libs.constraintlayout)
implementation(libs.material)
implementation(libs.recyclerview)
implementation(libs.lifecycle.viewmodel)
implementation(libs.lifecycle.livedata)
implementation(libs.room.runtime)
annotationProcessor(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.arch.core.testing)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.arch.core.testing)
}

# File: app/src/main/keepRules/rules.keep
# Giữ nguyên các lớp Room Entity, DAO và Database khi tối ưu hoá R8/ProGuard
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class com.vinhnt.todonotes.data.** { *; }

# File: app/src/main/java/com/vinhnt/todonotes/data/model/Priority.java
package com.vinhnt.todonotes.data.model;

/**
* Định nghĩa các mức độ ưu tiên của công việc (0 = Thấp, 1 = Trung bình, 2 = Cao).
  */
  public enum Priority {
  LOW(0),
  MEDIUM(1),
  HIGH(2);

  private final int value;

  Priority(int value) {
  this.value = value;
  }

  public int getValue() {
  return value;
  }

  public static Priority fromValue(int value) {
  for (Priority priority : values()) {
  if (priority.value == value) {
  return priority;
  }
  }
  return MEDIUM;
  }
  }

# File: app/src/main/java/com/vinhnt/todonotes/data/model/TaskCategory.java
package com.vinhnt.todonotes.data.model;

/**
* Định nghĩa các nhãn phân loại (Category) của công việc.
  */
  public enum TaskCategory {
  PERSONAL(0),
  STUDY(1),
  WORK(2),
  SHOPPING(3),
  OTHER(4);

  private final int value;

  TaskCategory(int value) {
  this.value = value;
  }

  public int getValue() {
  return value;
  }

  public static TaskCategory fromValue(int value) {
  for (TaskCategory category : values()) {
  if (category.value == value) {
  return category;
  }
  }
  return PERSONAL;
  }
  }

# File: app/src/main/java/com/vinhnt/todonotes/data/model/SubtaskItem.java
package com.vinhnt.todonotes.data.model;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
* Mô hình dữ liệu cho một công việc con (Subtask / Checklist item) bên trong công việc chính.
  */
  public class SubtaskItem {
  @NonNull
  private String title;
  private boolean done;

  public SubtaskItem(@NonNull String title, boolean done) {
  this.title = title.trim();
  this.done = done;
  }

  @NonNull
  public String getTitle() {
  return title;
  }

  public void setTitle(@NonNull String title) {
  this.title = title.trim();
  }

  public boolean isDone() {
  return done;
  }

  public void setDone(boolean done) {
  this.done = done;
  }

  @Override
  public boolean equals(Object o) {
  if (this == o) return true;
  if (o == null || getClass() != o.getClass()) return false;
  SubtaskItem that = (SubtaskItem) o;
  return done == that.done && Objects.equals(title, that.title);
  }

  @Override
  public int hashCode() {
  return Objects.hash(title, done);
  }
  }

# File: app/src/main/java/com/vinhnt/todonotes/util/AppExecutors.java
package com.vinhnt.todonotes.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
* Quản lý hồ luồng (Thread Pool) toàn cục cho truy xuất cơ sở dữ liệu Room và cập nhật giao diện.
  */
  public class AppExecutors {
  private static final Object LOCK = new Object();
  private static volatile AppExecutors instance;

  private final Executor diskIO;
  private final Executor mainThread;

  public AppExecutors(@NonNull Executor diskIO, @NonNull Executor mainThread) {
  this.diskIO = diskIO;
  this.mainThread = mainThread;
  }

  public static AppExecutors getInstance() {
  if (instance == null) {
  synchronized (LOCK) {
  if (instance == null) {
  instance = new AppExecutors(
  Executors.newFixedThreadPool(4),
  new MainThreadExecutor()
  );
  }
  }
  }
  return instance;
  }

  @NonNull
  public Executor diskIO() {
  return diskIO;
  }

  @NonNull
  public Executor mainThread() {
  return mainThread;
  }

  private static class MainThreadExecutor implements Executor {
  private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

       @Override
       public void execute(@NonNull Runnable command) {
           mainThreadHandler.post(command);
       }
  }
  }

# File: app/src/main/java/com/vinhnt/todonotes/util/DateTimeUtils.java
package com.vinhnt.todonotes.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
* Tiện ích xử lý thời gian Epoch milliseconds, định dạng ngày giờ và kiểm tra trạng thái Quá hạn / Hôm nay.
  */
  public final class DateTimeUtils {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
  DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());
  private static final DateTimeFormatter DATE_FORMATTER =
  DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());

  private DateTimeUtils() {
  // Không cho phép khởi tạo trực tiếp
  }

  /**
    * Định dạng mốc thời gian Epoch ms thành chuỗi "dd/MM/yyyy HH:mm" theo múi giờ hiện tại của thiết bị.
      */
      @NonNull
      public static String formatDateTime(@Nullable Long epochMillis) {
      if (epochMillis == null || epochMillis <= 0L) {
      return "";
      }
      return formatDateTime(epochMillis, ZoneId.systemDefault());
      }

  @NonNull
  public static String formatDateTime(long epochMillis, @NonNull ZoneId zoneId) {
  ZonedDateTime zdt = Instant.ofEpochMilli(epochMillis).atZone(zoneId);
  return DATE_TIME_FORMATTER.format(zdt);
  }

  @NonNull
  public static String formatDate(long epochMillis, @NonNull ZoneId zoneId) {
  ZonedDateTime zdt = Instant.ofEpochMilli(epochMillis).atZone(zoneId);
  return DATE_FORMATTER.format(zdt);
  }

  /**
    * Kết hợp mốc ngày UTC (từ MaterialDatePicker) với giờ và phút (từ MaterialTimePicker)
    * thành mốc thời gian Epoch ms theo múi giờ người dùng.
      */
      public static long combineUtcDateAndLocalTime(long utcDateMillis, int hour, int minute, @NonNull ZoneId zoneId) {
      LocalDate pickedDate = Instant.ofEpochMilli(utcDateMillis)
      .atZone(ZoneId.of("UTC"))
      .toLocalDate();
      LocalTime pickedTime = LocalTime.of(hour, minute, 0, 0);
      LocalDateTime localDateTime = LocalDateTime.of(pickedDate, pickedTime);
      return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
      }

  /**
    * Kiểm tra một công việc có đang bị quá hạn hay không (BR-03).
      */
      public static boolean isOverdue(boolean isCompleted, @Nullable Long dueDateMillis, long nowMillis) {
      return !isCompleted && dueDateMillis != null && dueDateMillis < nowMillis;
      }

  /**
    * Kiểm tra một mốc thời gian có thuộc ngày hôm nay theo múi giờ được chỉ định hay không.
      */
      public static boolean isSameDay(long targetMillis, long referenceNowMillis, @NonNull ZoneId zoneId) {
      LocalDate targetDate = Instant.ofEpochMilli(targetMillis).atZone(zoneId).toLocalDate();
      LocalDate refDate = Instant.ofEpochMilli(referenceNowMillis).atZone(zoneId).toLocalDate();
      return targetDate.equals(refDate);
      }

  /**
    * Tính mốc bắt đầu của ngày (00:00:00.000) từ mốc thời gian hiện tại.
      */
      public static long getStartOfDayMillis(long referenceNowMillis, @NonNull ZoneId zoneId) {
      LocalDate date = Instant.ofEpochMilli(referenceNowMillis).atZone(zoneId).toLocalDate();
      return date.atStartOfDay(zoneId).toInstant().toEpochMilli();
      }

  /**
    * Tính mốc kết thúc của ngày (23:59:59.999) từ mốc thời gian hiện tại.
      */
      public static long getEndOfDayMillis(long referenceNowMillis, @NonNull ZoneId zoneId) {
      LocalDate date = Instant.ofEpochMilli(referenceNowMillis).atZone(zoneId).toLocalDate();
      return date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1L;
      }
      }

# File: app/src/main/java/com/vinhnt/todonotes/util/ValidationUtils.java
package com.vinhnt.todonotes.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
* Tiện ích kiểm tra tính hợp lệ dữ liệu đầu vào của Công việc và Ghi chú theo đúng BR-01, BR-02, BR-06.
  */
  public final class ValidationUtils {

  public static final int MAX_TITLE_LENGTH = 200;
  public static final int MAX_TASK_DESCRIPTION_LENGTH = 5000;
  public static final int MAX_NOTE_CONTENT_LENGTH = 20000;
  public static final int CONVERTED_TASK_TITLE_MAX_LENGTH = 100;

  public enum TaskValidationError {
  NONE,
  EMPTY_TITLE,
  TITLE_TOO_LONG,
  DESCRIPTION_TOO_LONG,
  REMINDER_IN_PAST
  }

  public enum NoteValidationError {
  NONE,
  EMPTY_NOTE,
  TITLE_TOO_LONG,
  CONTENT_TOO_LONG
  }

  private ValidationUtils() {
  // Không cho phép khởi tạo trực tiếp
  }

  /**
    * Kiểm tra dữ liệu công việc trước khi lưu (UC 3.2, UC 3.3, UC 3.13).
      */
      @NonNull
      public static TaskValidationError validateTaskInput(
      @Nullable String title,
      @Nullable String description,
      @Nullable Long reminderTimeMillis,
      boolean validateReminderFuture,
      long nowMillis
      ) {
      String trimmedTitle = title == null ? "" : title.trim();
      if (trimmedTitle.isEmpty()) {
      return TaskValidationError.EMPTY_TITLE;
      }
      if (trimmedTitle.length() > MAX_TITLE_LENGTH) {
      return TaskValidationError.TITLE_TOO_LONG;
      }
      if (description != null && description.length() > MAX_TASK_DESCRIPTION_LENGTH) {
      return TaskValidationError.DESCRIPTION_TOO_LONG;
      }
      if (validateReminderFuture && reminderTimeMillis != null && reminderTimeMillis <= nowMillis) {
      return TaskValidationError.REMINDER_IN_PAST;
      }
      return TaskValidationError.NONE;
      }

  /**
    * Kiểm tra dữ liệu ghi chú nhanh trước khi lưu (UC 3.6, UC 3.8).
      */
      @NonNull
      public static NoteValidationError validateNoteInput(
      @Nullable String title,
      @Nullable String content
      ) {
      String trimmedTitle = title == null ? "" : title.trim();
      String trimmedContent = content == null ? "" : content.trim();

      if (trimmedTitle.isEmpty() && trimmedContent.isEmpty()) {
      return NoteValidationError.EMPTY_NOTE;
      }
      if (trimmedTitle.length() > MAX_TITLE_LENGTH) {
      return NoteValidationError.TITLE_TOO_LONG;
      }
      if (content != null && content.length() > MAX_NOTE_CONTENT_LENGTH) {
      return NoteValidationError.CONTENT_TOO_LONG;
      }
      return NoteValidationError.NONE;
      }

  /**
    * Trích xuất tiêu đề công việc từ một ghi chú khi người dùng chọn "Chuyển thành công việc" (BR-07).
      */
      @NonNull
      public static String deriveTaskTitleFromNote(@Nullable String noteTitle, @Nullable String noteContent) {
      String trimmedTitle = noteTitle == null ? "" : noteTitle.trim();
      if (!trimmedTitle.isEmpty()) {
      return trimmedTitle.length() > MAX_TITLE_LENGTH
      ? trimmedTitle.substring(0, MAX_TITLE_LENGTH)
      : trimmedTitle;
      }

      String trimmedContent = noteContent == null ? "" : noteContent.trim();
      if (trimmedContent.isEmpty()) {
      return "";
      }

      int newlineIndex = trimmedContent.indexOf('\n');
      String firstLine = newlineIndex >= 0
      ? trimmedContent.substring(0, newlineIndex).trim()
      : trimmedContent;

      if (firstLine.length() > CONVERTED_TASK_TITLE_MAX_LENGTH) {
      return firstLine.substring(0, CONVERTED_TASK_TITLE_MAX_LENGTH).trim();
      }
      return firstLine;
      }
      }

# File: app/src/main/java/com/vinhnt/todonotes/util/SubtaskJsonHelper.java
package com.vinhnt.todonotes.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vinhnt.todonotes.data.model.SubtaskItem;

import java.util.ArrayList;
import java.util.List;

/**
* Trình chuyển đổi danh sách {@link SubtaskItem} sang chuỗi JSON và ngược lại.
* Sử dụng bộ phân tích thuần Java không phụ thuộc android.jar stub để chạy mượt cả trên JVM Unit Test lẫn Android.
  */
  public final class SubtaskJsonHelper {

  private SubtaskJsonHelper() {
  // Không cho phép khởi tạo trực tiếp
  }

  /**
    * Chuyển danh sách SubtaskItem thành chuỗi JSON dạng: [{"title":"...","done":true},...]
      */
      @NonNull
      public static String toJson(@Nullable List<SubtaskItem> items) {
      if (items == null || items.isEmpty()) {
      return "[]";
      }
      StringBuilder sb = new StringBuilder("[");
      boolean first = true;
      for (SubtaskItem item : items) {
      if (item == null || item.getTitle().trim().isEmpty()) {
      continue;
      }
      if (!first) {
      sb.append(",");
      }
      first = false;
      sb.append("{\"title\":\"")
      .append(escapeJson(item.getTitle().trim()))
      .append("\",\"done\":")
      .append(item.isDone())
      .append("}");
      }
      sb.append("]");
      return sb.toString();
      }

  /**
    * Khôi phục danh sách SubtaskItem từ chuỗi JSON lưu trong cột subtasks_json của Room DB.
      */
      @NonNull
      public static List<SubtaskItem> fromJson(@Nullable String json) {
      List<SubtaskItem> result = new ArrayList<>();
      if (json == null) {
      return result;
      }
      String trimmed = json.trim();
      if (trimmed.isEmpty() || trimmed.equals("[]")) {
      return result;
      }

      int index = 0;
      int length = trimmed.length();
      while (index < length) {
      int objStart = findNextObjectStart(trimmed, index);
      if (objStart < 0) {
      break;
      }
      int objEnd = findObjectEnd(trimmed, objStart);
      if (objEnd < 0) {
      break;
      }
      String objectSlice = trimmed.substring(objStart + 1, objEnd);
      SubtaskItem parsed = parseSingleObject(objectSlice);
      if (parsed != null && !parsed.getTitle().isEmpty()) {
      result.add(parsed);
      }
      index = objEnd + 1;
      }
      return result;
      }

  /**
    * Đếm số lượng công việc con đã hoàn thành trong danh sách.
      */
      public static int countCompleted(@Nullable List<SubtaskItem> items) {
      if (items == null || items.isEmpty()) {
      return 0;
      }
      int count = 0;
      for (SubtaskItem item : items) {
      if (item != null && item.isDone()) {
      count++;
      }
      }
      return count;
      }

  private static int findNextObjectStart(String text, int fromIndex) {
  boolean inQuotes = false;
  for (int i = fromIndex; i < text.length(); i++) {
  char c = text.charAt(i);
  if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
  inQuotes = !inQuotes;
  } else if (!inQuotes && c == '{') {
  return i;
  }
  }
  return -1;
  }

  private static int findObjectEnd(String text, int startBraceIndex) {
  boolean inQuotes = false;
  for (int i = startBraceIndex + 1; i < text.length(); i++) {
  char c = text.charAt(i);
  if (c == '"' && text.charAt(i - 1) != '\\') {
  inQuotes = !inQuotes;
  } else if (!inQuotes && c == '}') {
  return i;
  }
  }
  return -1;
  }

  @Nullable
  private static SubtaskItem parseSingleObject(String objectSlice) {
  String title = extractJsonStringField(objectSlice, "title");
  if (title == null) {
  return null;
  }
  boolean done = extractJsonBooleanField(objectSlice, "done");
  return new SubtaskItem(title, done);
  }

  @Nullable
  private static String extractJsonStringField(String objectSlice, String fieldName) {
  String keyPattern = "\"" + fieldName + "\"";
  int keyIndex = objectSlice.indexOf(keyPattern);
  if (keyIndex < 0) {
  return null;
  }
  int colonIndex = objectSlice.indexOf(':', keyIndex + keyPattern.length());
  if (colonIndex < 0) {
  return null;
  }
  int firstQuote = objectSlice.indexOf('"', colonIndex + 1);
  if (firstQuote < 0) {
  return null;
  }

       StringBuilder unescaped = new StringBuilder();
       for (int i = firstQuote + 1; i < objectSlice.length(); i++) {
           char c = objectSlice.charAt(i);
           if (c == '\\' && i + 1 < objectSlice.length()) {
               char next = objectSlice.charAt(++i);
               switch (next) {
                   case '"':
                       unescaped.append('"');
                       break;
                   case '\\':
                       unescaped.append('\\');
                       break;
                   case 'n':
                       unescaped.append('\n');
                       break;
                   case 'r':
                       unescaped.append('\r');
                       break;
                   case 't':
                       unescaped.append('\t');
                       break;
                   default:
                       unescaped.append(next);
                       break;
               }
           } else if (c == '"') {
               return unescaped.toString();
           } else {
               unescaped.append(c);
           }
       }
       return null;
  }

  private static boolean extractJsonBooleanField(String objectSlice, String fieldName) {
  String keyPattern = "\"" + fieldName + "\"";
  int keyIndex = objectSlice.indexOf(keyPattern);
  if (keyIndex < 0) {
  return false;
  }
  int colonIndex = objectSlice.indexOf(':', keyIndex + keyPattern.length());
  if (colonIndex < 0) {
  return false;
  }
  String remainder = objectSlice.substring(colonIndex + 1).trim();
  return remainder.startsWith("true");
  }

  @NonNull
  private static String escapeJson(@NonNull String raw) {
  StringBuilder sb = new StringBuilder(raw.length() + 8);
  for (int i = 0; i < raw.length(); i++) {
  char c = raw.charAt(i);
  switch (c) {
  case '"':
  sb.append("\\\"");
  break;
  case '\\':
  sb.append("\\\\");
  break;
  case '\n':
  sb.append("\\n");
  break;
  case '\r':
  sb.append("\\r");
  break;
  case '\t':
  sb.append("\\t");
  break;
  default:
  sb.append(c);
  break;
  }
  }
  return sb.toString();
  }
  }

# File: app/src/test/java/com/vinhnt/todonotes/util/ValidationAndHelperTest.java
package com.vinhnt.todonotes.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
* Bộ kiểm thử đơn vị (Local Unit Test) cho ValidationUtils, SubtaskJsonHelper, DateTimeUtils và các Enum mô hình.
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
  }
# END_OF_BATCH