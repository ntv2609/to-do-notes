package com.vinhnt.todonotes;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.vinhnt.todonotes.util.PreferencesManager;

/**
* Lớp Application khởi tạo kênh thông báo nhắc việc, áp dụng Giao diện Sáng/Tối + Ngôn ngữ
* và tự động xử lý vùng đệm Edge-to-Edge (Status Bar / Display Cutout / Navigation Bar) trên Android 15+.
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
  registerEdgeToEdgeInsetsHandler();
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

  /**
  * Tự động áp dụng khoảng đệm an toàn tránh Status Bar, Camera Cutout và Navigation Bar
  * cho toàn bộ các Activity trong ứng dụng khi chạy trên Android 15+ (targetSdk 35–37).
    */
    private void registerEdgeToEdgeInsetsHandler() {
    registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

         @Override
         public void onActivityStarted(@NonNull Activity activity) {
             applySystemBarInsets(activity);
         }

         @Override
         public void onActivityResumed(@NonNull Activity activity) {
         }

         @Override
         public void onActivityPaused(@NonNull Activity activity) {
         }

         @Override
         public void onActivityStopped(@NonNull Activity activity) {
         }

         @Override
         public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
         }

         @Override
         public void onActivityDestroyed(@NonNull Activity activity) {
         }
    });
    }

  private void applySystemBarInsets(@NonNull Activity activity) {
  Window window = activity.getWindow();
  if (window == null) {
  return;
  }

       View decorView = window.getDecorView();
       decorView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_surface));

       boolean isNightMode = (activity.getResources().getConfiguration().uiMode
               & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
       WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);
       controller.setAppearanceLightStatusBars(!isNightMode);
       controller.setAppearanceLightNavigationBars(!isNightMode);

       View contentView = activity.findViewById(android.R.id.content);
       if (contentView == null) {
           return;
       }

       ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
           Insets systemBars = windowInsets.getInsets(
                   WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
           );
           Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
           int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
           v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
           return WindowInsetsCompat.CONSUMED;
       });
       ViewCompat.requestApplyInsets(contentView);
  }
  }
