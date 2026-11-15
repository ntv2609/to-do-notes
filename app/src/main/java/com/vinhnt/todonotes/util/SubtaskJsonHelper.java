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
