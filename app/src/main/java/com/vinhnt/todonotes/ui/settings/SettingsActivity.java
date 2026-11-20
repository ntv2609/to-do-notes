package com.vinhnt.todonotes.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.TodoNotesApp;
import com.vinhnt.todonotes.data.repository.BackupRepository;
import com.vinhnt.todonotes.databinding.ActivitySettingsBinding;
import com.vinhnt.todonotes.util.PreferencesManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
* Màn hình Cài đặt & Dữ liệu: Chuyển Giao diện Sáng/Tối, Ngôn ngữ Việt/Anh,
* Sao lưu / Khôi phục tệp JSON qua Storage Access Framework (SAF) và mở nhanh cài đặt quyền.
  */
  public class SettingsActivity extends AppCompatActivity {

  private ActivitySettingsBinding binding;
  private PreferencesManager preferencesManager;
  private BackupRepository backupRepository;
  private boolean isInitializingControls = false;

  private final ActivityResultLauncher<String> exportDocumentLauncher =
  registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
  if (uri != null) {
  performExportBackup(uri);
  }
  });

  private final ActivityResultLauncher<String[]> importDocumentLauncher =
  registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
  if (uri != null) {
  performImportBackup(uri);
  }
  });

  @NonNull
  public static Intent newIntent(@NonNull Context context) {
  return new Intent(context, SettingsActivity.class);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  binding = ActivitySettingsBinding.inflate(getLayoutInflater());
  setContentView(binding.getRoot());

       preferencesManager = PreferencesManager.getInstance(this);
       backupRepository = new BackupRepository(this);

       setupToolbar();
       setupAppearanceControls();
       setupBackupControls();
       setupPermissionShortcuts();
  }

  private void setupToolbar() {
  binding.toolbarSettings.setNavigationOnClickListener(v -> finish());
  }

  private void setupAppearanceControls() {
  isInitializingControls = true;

       int savedTheme = preferencesManager.getThemeMode();
       if (savedTheme == PreferencesManager.THEME_LIGHT) {
           binding.rbThemeLight.setChecked(true);
       } else if (savedTheme == PreferencesManager.THEME_DARK) {
           binding.rbThemeDark.setChecked(true);
       } else {
           binding.rbThemeSystem.setChecked(true);
       }

       String savedLang = preferencesManager.getLanguageTag();
       if (PreferencesManager.LANG_EN.equals(savedLang)) {
           binding.rbLangEn.setChecked(true);
       } else {
           binding.rbLangVi.setChecked(true);
       }

       isInitializingControls = false;

       binding.rgThemeMode.setOnCheckedChangeListener((group, checkedId) -> {
           if (isInitializingControls) {
               return;
           }
           int chosenMode = PreferencesManager.THEME_SYSTEM;
           if (checkedId == R.id.rb_theme_light) {
               chosenMode = PreferencesManager.THEME_LIGHT;
           } else if (checkedId == R.id.rb_theme_dark) {
               chosenMode = PreferencesManager.THEME_DARK;
           }
           preferencesManager.setThemeMode(chosenMode);
           TodoNotesApp.applyThemeMode(chosenMode);
       });

       binding.rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
           if (isInitializingControls) {
               return;
           }
           String chosenLang = checkedId == R.id.rb_lang_en
                   ? PreferencesManager.LANG_EN
                   : PreferencesManager.LANG_VI;
           preferencesManager.setLanguageTag(chosenLang);
           TodoNotesApp.applyAppLanguage(chosenLang);
       });
  }

  private void setupBackupControls() {
  binding.btnExportJson.setOnClickListener(v -> {
  String dateSuffix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
  String suggestedFileName = "todonotes_backup_" + dateSuffix + ".json";
  exportDocumentLauncher.launch(suggestedFileName);
  });

       binding.btnImportJson.setOnClickListener(v ->
               importDocumentLauncher.launch(new String[]{"application/json", "text/plain", "*/*"}));
  }

  private void setupPermissionShortcuts() {
  binding.btnOpenNotificationSettings.setOnClickListener(v -> openAppNotificationSettings());

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
           binding.btnOpenExactAlarmSettings.setVisibility(View.VISIBLE);
           binding.btnOpenExactAlarmSettings.setOnClickListener(v -> openExactAlarmSettings());
       } else {
           binding.btnOpenExactAlarmSettings.setVisibility(View.GONE);
       }
  }

  private void performExportBackup(@NonNull Uri destinationUri) {
  setBackupButtonsEnabled(false);
  backupRepository.exportBackupToUri(destinationUri, new BackupRepository.ExportCallback() {
  @Override
  public void onSuccess() {
  setBackupButtonsEnabled(true);
  Toast.makeText(SettingsActivity.this, R.string.msg_export_success, Toast.LENGTH_SHORT).show();
  }

           @Override
           public void onError(@NonNull Throwable error) {
               setBackupButtonsEnabled(true);
               Toast.makeText(SettingsActivity.this, R.string.error_storage_generic, Toast.LENGTH_SHORT).show();
           }
       });
  }

  private void performImportBackup(@NonNull Uri sourceUri) {
  setBackupButtonsEnabled(false);
  backupRepository.importBackupFromUri(sourceUri, new BackupRepository.ImportCallback() {
  @Override
  public void onSuccess(int taskCount, int noteCount) {
  setBackupButtonsEnabled(true);
  String msg = getString(R.string.msg_import_success, taskCount, noteCount);
  Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_LONG).show();
  }

           @Override
           public void onInvalidFormat(@NonNull Throwable error) {
               setBackupButtonsEnabled(true);
               Toast.makeText(SettingsActivity.this, R.string.error_backup_invalid, Toast.LENGTH_LONG).show();
           }

           @Override
           public void onError(@NonNull Throwable error) {
               setBackupButtonsEnabled(true);
               Toast.makeText(SettingsActivity.this, R.string.error_storage_generic, Toast.LENGTH_SHORT).show();
           }
       });
  }

  private void setBackupButtonsEnabled(boolean enabled) {
  binding.btnExportJson.setEnabled(enabled);
  binding.btnImportJson.setEnabled(enabled);
  }

  private void openAppNotificationSettings() {
  Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
  intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
  try {
  startActivity(intent);
  } catch (Exception e) {
  openAppDetailsFallback();
  }
  }

  private void openExactAlarmSettings() {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
  Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
  intent.setData(Uri.parse("package:" + getPackageName()));
  try {
  startActivity(intent);
  } catch (Exception e) {
  openAppDetailsFallback();
  }
  }
  }

  private void openAppDetailsFallback() {
  Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
  fallback.setData(Uri.parse("package:" + getPackageName()));
  startActivity(fallback);
  }
  }
