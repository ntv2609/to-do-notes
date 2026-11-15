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
