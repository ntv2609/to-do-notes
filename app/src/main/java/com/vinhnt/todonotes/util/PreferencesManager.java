package com.vinhnt.todonotes.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

/**
* Quản lý các tùy chọn người dùng trong SharedPreferences:
* - Chế độ giao diện (Hệ thống / Sáng / Tối)
* - Ngôn ngữ ứng dụng (vi / en)
* - Tiêu chí sắp xếp công việc & chiều sắp xếp
* - Chế độ hiển thị ghi chú (Danh sách 1 cột / Lưới 2 cột)
    */
    public class PreferencesManager {

private static final String PREF_NAME = "todonotes_preferences";

private static final String KEY_THEME_MODE = "key_theme_mode";
private static final String KEY_LANGUAGE_TAG = "key_language_tag";
private static final String KEY_TASK_SORT_OPTION = "key_task_sort_option";
private static final String KEY_TASK_SORT_ASCENDING = "key_task_sort_ascending";
private static final String KEY_NOTE_GRID_MODE = "key_note_grid_mode";

public static final int THEME_SYSTEM = 0;
public static final int THEME_LIGHT = 1;
public static final int THEME_DARK = 2;

public static final String LANG_VI = "vi";
public static final String LANG_EN = "en";

public static final int SORT_CREATED_DATE = 0;
public static final int SORT_DUE_DATE = 1;
public static final int SORT_PRIORITY = 2;

private static volatile PreferencesManager instance;
private final SharedPreferences prefs;

private PreferencesManager(@NonNull Context context) {
this.prefs = context.getApplicationContext()
.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
}

@NonNull
public static PreferencesManager getInstance(@NonNull Context context) {
if (instance == null) {
synchronized (PreferencesManager.class) {
if (instance == null) {
instance = new PreferencesManager(context);
}
}
}
return instance;
}

public int getThemeMode() {
return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM);
}

public void setThemeMode(int themeMode) {
int validMode = (themeMode == THEME_LIGHT || themeMode == THEME_DARK)
? themeMode
: THEME_SYSTEM;
prefs.edit().putInt(KEY_THEME_MODE, validMode).apply();
}

@NonNull
public String getLanguageTag() {
String raw = prefs.getString(KEY_LANGUAGE_TAG, LANG_VI);
return normalizeLanguageTag(raw);
}

public void setLanguageTag(@NonNull String languageTag) {
prefs.edit().putString(KEY_LANGUAGE_TAG, normalizeLanguageTag(languageTag)).apply();
}

public int getTaskSortOption() {
return prefs.getInt(KEY_TASK_SORT_OPTION, SORT_CREATED_DATE);
}

public void setTaskSortOption(int sortOption) {
int validSort = (sortOption == SORT_DUE_DATE || sortOption == SORT_PRIORITY)
? sortOption
: SORT_CREATED_DATE;
prefs.edit().putInt(KEY_TASK_SORT_OPTION, validSort).apply();
}

public boolean isTaskSortAscending() {
return prefs.getBoolean(KEY_TASK_SORT_ASCENDING, false);
}

public void setTaskSortAscending(boolean ascending) {
prefs.edit().putBoolean(KEY_TASK_SORT_ASCENDING, ascending).apply();
}

public boolean isNoteGridMode() {
return prefs.getBoolean(KEY_NOTE_GRID_MODE, true);
}

public void setNoteGridMode(boolean isGrid) {
prefs.edit().putBoolean(KEY_NOTE_GRID_MODE, isGrid).apply();
}

/**
    * Chuyển đổi mã theme nội bộ (0, 1, 2) sang hằng số của AppCompatDelegate.
      */
      public static int resolveNightModeFlag(int themeMode) {
      switch (themeMode) {
      case THEME_LIGHT:
      return AppCompatDelegate.MODE_NIGHT_NO;
      case THEME_DARK:
      return AppCompatDelegate.MODE_NIGHT_YES;
      case THEME_SYSTEM:
      default:
      return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
      }
      }

/**
    * Chuẩn hóa mã ngôn ngữ về "vi" hoặc "en".
      */
      @NonNull
      public static String normalizeLanguageTag(String languageTag) {
      if (languageTag != null && languageTag.trim().toLowerCase().startsWith(LANG_EN)) {
      return LANG_EN;
      }
      return LANG_VI;
      }
      }
