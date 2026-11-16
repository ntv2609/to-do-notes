package com.vinhnt.todonotes;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.vinhnt.todonotes.util.PreferencesManager;

/**
* Lớp Application khởi tạo kênh thông báo nhắc việc và áp dụng cấu hình Giao diện Sáng/Tối + Ngôn ngữ.
  */
  public class TodoNotesApp extends Application {

  public static final String REMINDER_CHANNEL_ID = "todonotes_task_reminders";

  @Override
  public void onCreate() {
  super.onCreate();
  PreferencesManager prefs = PreferencesManager.getInstance(this);
  applyThemeMode(prefs.getThemeMode());
  applyAppLanguage(prefs.getLanguageTag());
  createReminderNotificationChannel();
  }

  public static void applyThemeMode(int themeMode) {
  int nightMode = PreferencesManager.resolveNightModeFlag(themeMode);
  AppCompatDelegate.setDefaultNightMode(nightMode);
  }

  public static void applyAppLanguage(String languageTag) {
  String safeTag = PreferencesManager.normalizeLanguageTag(languageTag);
  LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(safeTag);
  AppCompatDelegate.setApplicationLocales(appLocale);
  }

  private void createReminderNotificationChannel() {
  NotificationManager manager = getSystemService(NotificationManager.class);
  if (manager == null) {
  return;
  }
  String channelName = getString(R.string.notification_channel_name);
  String channelDesc = getString(R.string.notification_channel_desc);
  NotificationChannel channel = new NotificationChannel(
  REMINDER_CHANNEL_ID,
  channelName,
  NotificationManager.IMPORTANCE_HIGH
  );
  channel.setDescription(channelDesc);
  channel.enableVibration(true);
  manager.createNotificationChannel(channel);
  }
  }
