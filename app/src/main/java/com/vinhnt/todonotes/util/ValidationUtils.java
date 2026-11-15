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
