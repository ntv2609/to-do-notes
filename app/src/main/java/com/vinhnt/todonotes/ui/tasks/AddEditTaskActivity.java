package com.vinhnt.todonotes.ui.tasks;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.model.Priority;
import com.vinhnt.todonotes.data.model.SubtaskItem;
import com.vinhnt.todonotes.data.model.TaskCategory;
import com.vinhnt.todonotes.databinding.ActivityAddEditTaskBinding;
import com.vinhnt.todonotes.receiver.ReminderScheduler;
import com.vinhnt.todonotes.util.DateTimeUtils;
import com.vinhnt.todonotes.util.SubtaskJsonHelper;
import com.vinhnt.todonotes.util.ValidationUtils;
import com.vinhnt.todonotes.util.ValidationUtils.TaskValidationError;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
* Màn hình thống nhất cho Thêm mới (UC 3.2), Xem chi tiết (UC 3.1 bước 3) và Chỉnh sửa công việc (UC 3.3),
* đồng thời hỗ trợ Thiết lập / Thay đổi / Hủy nhắc nhở (UC 3.13, UC 3.14), xin quyền Android 12/13+
* và nhận dữ liệu chuyển từ Ghi chú (BR-07).
  */
  public class AddEditTaskActivity extends AppCompatActivity {

  public static final String EXTRA_TASK_ID = "extra_task_id";
  public static final String EXTRA_PREFILL_TITLE = "extra_prefill_title";
  public static final String EXTRA_PREFILL_DESCRIPTION = "extra_prefill_description";

  private static final String STATE_DUE_DATE = "state_due_date";
  private static final String STATE_REMINDER_TIME = "state_reminder_time";
  private static final String STATE_SUBTASKS_JSON = "state_subtasks_json";
  private static final String STATE_HAS_EDITED = "state_has_edited";
  private static final String STATE_PROMPTED_EXACT_ALARM = "state_prompted_exact_alarm";

  private ActivityAddEditTaskBinding binding;
  private AddEditTaskViewModel viewModel;
  private SubtaskAdapter subtaskAdapter;

  private long taskId = -1L;
  @Nullable
  private TaskEntity existingTask;
  @Nullable
  private Long selectedDueDateMillis;
  @Nullable
  private Long selectedReminderMillis;

  private boolean isPopulatingData = false;
  private boolean hasUnsavedEdits = false;
  private boolean hasPromptedExactAlarm = false;

  private final ActivityResultLauncher<String> notificationPermissionLauncher =
  registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
  if (Boolean.TRUE.equals(isGranted)) {
  proceedToReminderPicker();
  } else {
  showNotificationSettingsDialog();
  }
  });

  @NonNull
  public static Intent newIntentForAdd(@NonNull Context context) {
  return new Intent(context, AddEditTaskActivity.class);
  }

  @NonNull
  public static Intent newIntentForEdit(@NonNull Context context, long taskId) {
  Intent intent = new Intent(context, AddEditTaskActivity.class);
  intent.putExtra(EXTRA_TASK_ID, taskId);
  return intent;
  }

  @NonNull
  public static Intent newIntentFromNote(
  @NonNull Context context,
  @NonNull String prefillTitle,
  @NonNull String prefillDescription
  ) {
  Intent intent = new Intent(context, AddEditTaskActivity.class);
  intent.putExtra(EXTRA_PREFILL_TITLE, prefillTitle);
  intent.putExtra(EXTRA_PREFILL_DESCRIPTION, prefillDescription);
  return intent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  binding = ActivityAddEditTaskBinding.inflate(getLayoutInflater());
  setContentView(binding.getRoot());

       viewModel = new ViewModelProvider(this).get(AddEditTaskViewModel.class);
       taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1L);

       setupToolbar();
       setupSubtasksRecyclerView();
       setupDateAndReminderPickers();
       setupChangeTracking();
       setupBackNavigationGuard();

       if (savedInstanceState != null) {
           restoreInstanceState(savedInstanceState);
       } else if (taskId <= 0L) {
           applyPrefillFromNoteIfPresent();
       }

       if (taskId > 0L) {
           binding.toolbarAddEditTask.setTitle(R.string.title_edit_task);
           MenuItem deleteItem = binding.toolbarAddEditTask.getMenu().findItem(R.id.action_delete_task);
           if (deleteItem != null) {
               deleteItem.setVisible(true);
           }
           binding.cbDetailCompleted.setVisibility(View.VISIBLE);
           viewModel.loadTaskIfNeeded(taskId);
       } else {
           binding.toolbarAddEditTask.setTitle(R.string.title_add_task);
       }

       viewModel.getLoadedTask().observe(this, task -> {
           if (task == null) {
               return;
           }
           existingTask = task;
           if (savedInstanceState == null) {
               populateTaskToViews(task);
           }
       });

       updateDueDateText();
       updateReminderText();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
  super.onSaveInstanceState(outState);
  if (selectedDueDateMillis != null) {
  outState.putLong(STATE_DUE_DATE, selectedDueDateMillis);
  }
  if (selectedReminderMillis != null) {
  outState.putLong(STATE_REMINDER_TIME, selectedReminderMillis);
  }
  outState.putString(STATE_SUBTASKS_JSON, SubtaskJsonHelper.toJson(subtaskAdapter.getItems()));
  outState.putBoolean(STATE_HAS_EDITED, hasUnsavedEdits);
  outState.putBoolean(STATE_PROMPTED_EXACT_ALARM, hasPromptedExactAlarm);
  }

  private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
  if (savedInstanceState.containsKey(STATE_DUE_DATE)) {
  selectedDueDateMillis = savedInstanceState.getLong(STATE_DUE_DATE);
  }
  if (savedInstanceState.containsKey(STATE_REMINDER_TIME)) {
  selectedReminderMillis = savedInstanceState.getLong(STATE_REMINDER_TIME);
  }
  String subtasksJson = savedInstanceState.getString(STATE_SUBTASKS_JSON, "[]");
  subtaskAdapter.setItems(SubtaskJsonHelper.fromJson(subtasksJson));
  hasUnsavedEdits = savedInstanceState.getBoolean(STATE_HAS_EDITED, false);
  hasPromptedExactAlarm = savedInstanceState.getBoolean(STATE_PROMPTED_EXACT_ALARM, false);
  }

  private void applyPrefillFromNoteIfPresent() {
  String prefillTitle = getIntent().getStringExtra(EXTRA_PREFILL_TITLE);
  String prefillDesc = getIntent().getStringExtra(EXTRA_PREFILL_DESCRIPTION);
  if (prefillTitle != null || prefillDesc != null) {
  isPopulatingData = true;
  binding.etTaskTitle.setText(prefillTitle != null ? prefillTitle : "");
  binding.etTaskDescription.setText(prefillDesc != null ? prefillDesc : "");
  isPopulatingData = false;
  hasUnsavedEdits = true;
  }
  }

  private void setupToolbar() {
  binding.toolbarAddEditTask.setNavigationOnClickListener(v -> handleExitWithConfirmation());
  binding.toolbarAddEditTask.setOnMenuItemClickListener(item -> {
  int id = item.getItemId();
  if (id == R.id.action_save_task) {
  attemptSaveTask();
  return true;
  } else if (id == R.id.action_delete_task) {
  confirmAndDeleteTask();
  return true;
  }
  return false;
  });
  }

  private void setupSubtasksRecyclerView() {
  subtaskAdapter = new SubtaskAdapter(() -> {
  if (!isPopulatingData) {
  hasUnsavedEdits = true;
  }
  });
  binding.rvSubtasks.setLayoutManager(new LinearLayoutManager(this));
  binding.rvSubtasks.setAdapter(subtaskAdapter);

       binding.btnAddSubtask.setOnClickListener(v -> addSubtaskFromInput());
       binding.etNewSubtask.setOnEditorActionListener((v, actionId, event) -> {
           if (actionId == EditorInfo.IME_ACTION_DONE) {
               addSubtaskFromInput();
               return true;
           }
           return false;
       });
  }

  private void addSubtaskFromInput() {
  String raw = binding.etNewSubtask.getText() != null
  ? binding.etNewSubtask.getText().toString().trim()
  : "";
  if (raw.isEmpty()) {
  return;
  }
  subtaskAdapter.addItem(new SubtaskItem(raw, false));
  binding.etNewSubtask.setText("");
  hasUnsavedEdits = true;
  }

  private void setupDateAndReminderPickers() {
  binding.btnPickDueDate.setOnClickListener(v -> showDateTimePicker(
  selectedDueDateMillis,
  pickedMillis -> {
  selectedDueDateMillis = pickedMillis;
  hasUnsavedEdits = true;

                   // UC 3.15 rẽ nhánh 2: Đồng bộ thời điểm nhắc nhở nếu thời hạn mới lùi về sớm hơn
                   Long adjustedReminder = ReminderScheduler.resolveReminderAfterDueDateChange(
                           selectedReminderMillis,
                           pickedMillis,
                           System.currentTimeMillis()
                   );
                   if (!Objects.equals(adjustedReminder, selectedReminderMillis)) {
                       selectedReminderMillis = adjustedReminder;
                       Toast.makeText(
                               this,
                               R.string.msg_reminder_adjusted_to_due,
                               Toast.LENGTH_SHORT
                       ).show();
                       updateReminderText();
                   }
                   updateDueDateText();
               }
       ));

       binding.btnClearDueDate.setOnClickListener(v -> {
           selectedDueDateMillis = null;
           hasUnsavedEdits = true;
           updateDueDateText();
       });

       binding.btnPickReminder.setOnClickListener(v -> handlePickReminderClick());

       binding.btnClearReminder.setOnClickListener(v -> {
           selectedReminderMillis = null;
           hasUnsavedEdits = true;
           updateReminderText();
       });
  }

  /**
  * Kiểm tra quyền Thông báo (Android 13+) đúng lúc người dùng muốn đặt nhắc nhở (UC 3.13 rẽ nhánh 2).
    */
    private void handlePickReminderClick() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    boolean granted = ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED;

         if (!granted) {
             if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                 new MaterialAlertDialogBuilder(this)
                         .setTitle(R.string.dialog_permission_notification_title)
                         .setMessage(R.string.dialog_permission_notification_msg)
                         .setNegativeButton(R.string.action_cancel, null)
                         .setPositiveButton(android.R.string.ok, (dialog, which) ->
                                 notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                         .show();
             } else {
                 notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
             }
             return;
         }
    }
    proceedToReminderPicker();
    }

  private void proceedToReminderPicker() {
  showDateTimePicker(
  selectedReminderMillis != null ? selectedReminderMillis : selectedDueDateMillis,
  pickedMillis -> {
  if (pickedMillis <= System.currentTimeMillis()) {
  Toast.makeText(this, R.string.error_reminder_in_past, Toast.LENGTH_SHORT).show();
  return;
  }
  selectedReminderMillis = pickedMillis;
  hasUnsavedEdits = true;
  updateReminderText();

                   // Android 12+ (API 31+): Nếu chưa có quyền báo thức chính xác, gợi ý mở Cài đặt
                   if (!ReminderScheduler.canScheduleExactAlarms(this) && !hasPromptedExactAlarm) {
                       hasPromptedExactAlarm = true;
                       showExactAlarmSettingsDialog();
                   }
               }
       );
  }

  private void showNotificationSettingsDialog() {
  new MaterialAlertDialogBuilder(this)
  .setTitle(R.string.dialog_permission_notification_title)
  .setMessage(R.string.dialog_permission_notification_msg)
  .setNegativeButton(R.string.action_cancel, null)
  .setPositiveButton(R.string.action_open_settings, (dialog, which) -> openAppNotificationSettings())
  .show();
  }

  private void showExactAlarmSettingsDialog() {
  new MaterialAlertDialogBuilder(this)
  .setTitle(R.string.dialog_permission_exact_alarm_title)
  .setMessage(R.string.dialog_permission_exact_alarm_msg)
  .setNegativeButton(R.string.action_cancel, null)
  .setPositiveButton(R.string.action_open_settings, (dialog, which) -> openExactAlarmSettings())
  .show();
  }

  private void openAppNotificationSettings() {
  Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
  intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
  try {
  startActivity(intent);
  } catch (Exception e) {
  Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
  fallback.setData(Uri.parse("package:" + getPackageName()));
  startActivity(fallback);
  }
  }

  private void openExactAlarmSettings() {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
  Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
  intent.setData(Uri.parse("package:" + getPackageName()));
  try {
  startActivity(intent);
  } catch (Exception e) {
  Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
  fallback.setData(Uri.parse("package:" + getPackageName()));
  startActivity(fallback);
  }
  }
  }

  private interface OnDateTimePickedListener {
  void onDateTimePicked(long epochMillis);
  }

  private void showDateTimePicker(
  @Nullable Long initialMillis,
  @NonNull OnDateTimePickedListener callback
  ) {
  long baseMillis = (initialMillis != null && initialMillis > 0L)
  ? initialMillis
  : System.currentTimeMillis() + 3_600_000L;
  ZonedDateTime baseZdt = Instant.ofEpochMilli(baseMillis).atZone(ZoneId.systemDefault());

       MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
               .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
               .build();

       datePicker.addOnPositiveButtonClickListener(utcDateMillis -> {
           MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                   .setTimeFormat(TimeFormat.CLOCK_24H)
                   .setHour(baseZdt.getHour())
                   .setMinute(baseZdt.getMinute())
                   .build();

           timePicker.addOnPositiveButtonClickListener(view -> {
               long combined = DateTimeUtils.combineUtcDateAndLocalTime(
                       utcDateMillis,
                       timePicker.getHour(),
                       timePicker.getMinute(),
                       ZoneId.systemDefault()
               );
               callback.onDateTimePicked(combined);
           });

           timePicker.show(getSupportFragmentManager(), "tag_time_picker");
       });

       datePicker.show(getSupportFragmentManager(), "tag_date_picker");
  }

  private void setupChangeTracking() {
  TextWatcher watcher = new TextWatcher() {
  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               if (!isPopulatingData) {
                   binding.tilTaskTitle.setError(null);
                   hasUnsavedEdits = true;
               }
           }

           @Override
           public void afterTextChanged(Editable s) {
           }
       };
       binding.etTaskTitle.addTextChangedListener(watcher);
       binding.etTaskDescription.addTextChangedListener(watcher);

       binding.chipGroupPriority.setOnCheckedStateChangeListener((group, checkedIds) -> {
           if (!isPopulatingData) {
               hasUnsavedEdits = true;
           }
       });
       binding.chipGroupTaskCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
           if (!isPopulatingData) {
               hasUnsavedEdits = true;
           }
       });
       binding.cbDetailCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
           if (!isPopulatingData) {
               hasUnsavedEdits = true;
           }
       });
  }

  private void setupBackNavigationGuard() {
  getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
  @Override
  public void handleOnBackPressed() {
  handleExitWithConfirmation();
  }
  });
  }

  private void handleExitWithConfirmation() {
  if (!hasActualChanges()) {
  finish();
  return;
  }
  new MaterialAlertDialogBuilder(this)
  .setTitle(R.string.dialog_discard_title)
  .setMessage(R.string.dialog_discard_message)
  .setNegativeButton(R.string.action_keep_editing, null)
  .setPositiveButton(R.string.action_discard, (dialog, which) -> finish())
  .show();
  }

  private boolean hasActualChanges() {
  String title = getTextSafely(binding.etTaskTitle);
  String desc = getTextSafely(binding.etTaskDescription);
  String subtasksJson = SubtaskJsonHelper.toJson(subtaskAdapter.getItems());

       if (existingTask == null) {
           return hasUnsavedEdits && (!title.isEmpty()
                   || !desc.isEmpty()
                   || selectedDueDateMillis != null
                   || selectedReminderMillis != null
                   || !subtasksJson.equals("[]"));
       }
       return !title.equals(existingTask.getTitle())
               || !desc.equals(existingTask.getDescription())
               || getSelectedPriorityValue() != existingTask.getPriority()
               || getSelectedCategoryValue() != existingTask.getCategory()
               || !Objects.equals(selectedDueDateMillis, existingTask.getDueDate())
               || !Objects.equals(selectedReminderMillis, existingTask.getReminderTime())
               || !subtasksJson.equals(existingTask.getSubtasksJson())
               || binding.cbDetailCompleted.isChecked() != existingTask.isCompleted();
  }

  private void populateTaskToViews(@NonNull TaskEntity task) {
  isPopulatingData = true;
  binding.etTaskTitle.setText(task.getTitle());
  binding.etTaskDescription.setText(task.getDescription());
  binding.cbDetailCompleted.setChecked(task.isCompleted());

       Priority priority = Priority.fromValue(task.getPriority());
       if (priority == Priority.HIGH) {
           binding.chipPriorityHigh.setChecked(true);
       } else if (priority == Priority.LOW) {
           binding.chipPriorityLow.setChecked(true);
       } else {
           binding.chipPriorityMedium.setChecked(true);
       }

       TaskCategory category = TaskCategory.fromValue(task.getCategory());
       switch (category) {
           case STUDY:
               binding.chipTaskCatStudy.setChecked(true);
               break;
           case WORK:
               binding.chipTaskCatWork.setChecked(true);
               break;
           case SHOPPING:
               binding.chipTaskCatShopping.setChecked(true);
               break;
           case OTHER:
               binding.chipTaskCatOther.setChecked(true);
               break;
           case PERSONAL:
           default:
               binding.chipTaskCatPersonal.setChecked(true);
               break;
       }

       selectedDueDateMillis = task.getDueDate();
       selectedReminderMillis = task.getReminderTime();
       updateDueDateText();
       updateReminderText();

       List<SubtaskItem> subtasks = SubtaskJsonHelper.fromJson(task.getSubtasksJson());
       subtaskAdapter.setItems(subtasks);

       isPopulatingData = false;
       hasUnsavedEdits = false;
  }

  private void updateDueDateText() {
  if (selectedDueDateMillis != null && selectedDueDateMillis > 0L) {
  binding.tvSelectedDueDate.setText(DateTimeUtils.formatDateTime(selectedDueDateMillis));
  binding.btnClearDueDate.setVisibility(View.VISIBLE);
  } else {
  binding.tvSelectedDueDate.setText(R.string.due_date_not_set);
  binding.btnClearDueDate.setVisibility(View.GONE);
  }
  }

  private void updateReminderText() {
  if (selectedReminderMillis != null && selectedReminderMillis > 0L) {
  binding.tvSelectedReminder.setText(DateTimeUtils.formatDateTime(selectedReminderMillis));
  binding.btnClearReminder.setVisibility(View.VISIBLE);
  } else {
  binding.tvSelectedReminder.setText(R.string.reminder_not_set);
  binding.btnClearReminder.setVisibility(View.GONE);
  }
  }

  private int getSelectedPriorityValue() {
  int checkedId = binding.chipGroupPriority.getCheckedChipId();
  if (checkedId == R.id.chip_priority_high) {
  return Priority.HIGH.getValue();
  } else if (checkedId == R.id.chip_priority_low) {
  return Priority.LOW.getValue();
  }
  return Priority.MEDIUM.getValue();
  }

  private int getSelectedCategoryValue() {
  int checkedId = binding.chipGroupTaskCategory.getCheckedChipId();
  if (checkedId == R.id.chip_task_cat_study) {
  return TaskCategory.STUDY.getValue();
  } else if (checkedId == R.id.chip_task_cat_work) {
  return TaskCategory.WORK.getValue();
  } else if (checkedId == R.id.chip_task_cat_shopping) {
  return TaskCategory.SHOPPING.getValue();
  } else if (checkedId == R.id.chip_task_cat_other) {
  return TaskCategory.OTHER.getValue();
  }
  return TaskCategory.PERSONAL.getValue();
  }

  private void attemptSaveTask() {
  // Nếu người dùng đang gõ dở 1 công việc con mà quên bấm nút "Thêm mục", tự động thêm vào danh sách
  addSubtaskFromInput();

       String title = getTextSafely(binding.etTaskTitle);
       String description = getTextSafely(binding.etTaskDescription);
       long now = System.currentTimeMillis();

       // Chỉ bắt buộc kiểm tra reminderTime > now nếu người dùng vừa đặt mới hoặc thay đổi giờ nhắc
       boolean isReminderModifiedOrNew = selectedReminderMillis != null
               && (existingTask == null || !selectedReminderMillis.equals(existingTask.getReminderTime()));

       TaskValidationError error = ValidationUtils.validateTaskInput(
               title,
               description,
               selectedReminderMillis,
               isReminderModifiedOrNew,
               now
       );

       if (error == TaskValidationError.EMPTY_TITLE) {
           binding.tilTaskTitle.setError(getString(R.string.error_task_title_empty));
           binding.etTaskTitle.requestFocus();
           return;
       }
       if (error == TaskValidationError.TITLE_TOO_LONG) {
           binding.tilTaskTitle.setError(getString(R.string.error_title_too_long));
           binding.etTaskTitle.requestFocus();
           return;
       }
       if (error == TaskValidationError.DESCRIPTION_TOO_LONG) {
           Toast.makeText(this, R.string.error_description_too_long, Toast.LENGTH_SHORT).show();
           return;
       }
       if (error == TaskValidationError.REMINDER_IN_PAST) {
           Toast.makeText(this, R.string.error_reminder_in_past, Toast.LENGTH_SHORT).show();
           return;
       }

       boolean isCompleted = existingTask != null && binding.cbDetailCompleted.isChecked();
       String subtasksJson = SubtaskJsonHelper.toJson(subtaskAdapter.getItems());

       viewModel.saveTask(
               existingTask,
               title,
               description,
               getSelectedPriorityValue(),
               getSelectedCategoryValue(),
               selectedDueDateMillis,
               selectedReminderMillis,
               subtasksJson,
               isCompleted,
               () -> {
                   Toast.makeText(this, R.string.msg_task_saved, Toast.LENGTH_SHORT).show();
                   finish();
               }
       );
  }

  private void confirmAndDeleteTask() {
  if (existingTask == null) {
  return;
  }
  new MaterialAlertDialogBuilder(this)
  .setTitle(R.string.dialog_delete_task_title)
  .setMessage(R.string.dialog_delete_task_message)
  .setNegativeButton(R.string.action_cancel, null)
  .setPositiveButton(R.string.action_delete, (dialog, which) ->
  viewModel.deleteTask(existingTask, () -> {
  Toast.makeText(this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show();
  finish();
  }))
  .show();
  }

  @NonNull
  private static String getTextSafely(@NonNull com.google.android.material.textfield.TextInputEditText editText) {
  return editText.getText() != null ? editText.getText().toString().trim() : "";
  }
  }
