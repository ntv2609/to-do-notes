package com.vinhnt.todonotes.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.vinhnt.todonotes.data.local.dao.TaskDao;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;

import java.util.List;

/**
* Tiện ích quản lý lịch nhắc nhở ngoại tuyến bằng AlarmManager (UC 3.13, 3.14, 3.15).
* Hỗ trợ an toàn Android 8.0 – 15+ (có fallback sang setWindow khi chưa có quyền Exact Alarm trên Android 12+).
  */
  public final class ReminderScheduler {

  private static final String TAG = "ReminderScheduler";

  public static final String ACTION_TASK_REMINDER = "com.vinhnt.todonotes.action.TASK_REMINDER";
  public static final String EXTRA_TASK_ID = "extra_task_id";
  private static final long FALLBACK_WINDOW_MILLIS = 60_000L;

  private ReminderScheduler() {
  // Không cho phép khởi tạo trực tiếp
  }

  /**
  * Kiểm tra điều kiện nghiệp vụ xem một công việc có cần đặt lịch nhắc nhở hay không.
    */
    public static boolean shouldScheduleReminder(@Nullable TaskEntity task, long nowMillis) {
    if (task == null || task.getId() <= 0L || task.isCompleted()) {
    return false;
    }
    Long reminderTime = task.getReminderTime();
    return reminderTime != null && reminderTime > nowMillis;
    }

  /**
  * Đồng bộ thời điểm nhắc nhở khi người dùng thay đổi thời hạn hoàn thành (BR-02 & UC 3.15 rẽ nhánh 2).
  * - Nếu chưa đặt nhắc nhở -> trả về null.
  * - Nếu hạn chót mới >= giờ nhắc hiện tại -> trả về giờ nhắc hiện tại.
  * - Nếu hạn chót mới < giờ nhắc hiện tại và hạn chót mới còn ở tương lai -> điều chỉnh giờ nhắc về đúng hạn chót mới.
  * - Nếu hạn chót mới < giờ nhắc hiện tại nhưng hạn chót mới đã ở quá khứ -> hủy nhắc nhở (trả về null).
      */
      @Nullable
      public static Long resolveReminderAfterDueDateChange(
      @Nullable Long currentReminderMillis,
      long newDueDateMillis,
      long nowMillis
      ) {
      if (currentReminderMillis == null) {
      return null;
      }
      if (newDueDateMillis >= currentReminderMillis) {
      return currentReminderMillis;
      }
      return newDueDateMillis > nowMillis ? newDueDateMillis : null;
      }

  /**
  * Sinh mã requestCode / notificationId ổn định (không âm) từ taskId.
    */
    public static int buildRequestCode(long taskId) {
    return (int) (Math.abs(taskId) % Integer.MAX_VALUE);
    }

  /**
  * Kiểm tra thiết bị có cho phép đặt báo thức chính xác hay không (Android 12+ / API 31+).
    */
    public static boolean canScheduleExactAlarms(@NonNull Context context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
    return true;
    }
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    return alarmManager != null && alarmManager.canScheduleExactAlarms();
    }

  /**
  * Đặt hoặc cập nhật lịch nhắc nhở cho công việc.
  * Nếu công việc không đủ điều kiện (đã hoàn thành, không có giờ nhắc, hoặc giờ nhắc đã qua), tự động hủy lịch cũ.
  *
  * @return true nếu đã đăng ký lịch thành công với AlarmManager.
    */
    public static boolean scheduleReminder(@NonNull Context context, @Nullable TaskEntity task) {
    if (task == null || task.getId() <= 0L) {
    return false;
    }
    long now = System.currentTimeMillis();
    if (!shouldScheduleReminder(task, now)) {
    cancelReminder(context, task.getId());
    return false;
    }

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    if (alarmManager == null) {
    Log.w(TAG, "AlarmManager unavailable, cannot schedule reminder for taskId=" + task.getId());
    return false;
    }

    Long reminderTime = task.getReminderTime();
    if (reminderTime == null) {
    return false;
    }

    PendingIntent pendingIntent = createReminderPendingIntent(
    context,
    task.getId(),
    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );
    if (pendingIntent == null) {
    return false;
    }

    try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    if (alarmManager.canScheduleExactAlarms()) {
    alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    reminderTime,
    pendingIntent
    );
    } else {
    Log.w(TAG, "Exact alarm permission not granted; falling back to setWindow for taskId=" + task.getId());
    alarmManager.setWindow(
    AlarmManager.RTC_WAKEUP,
    reminderTime,
    FALLBACK_WINDOW_MILLIS,
    pendingIntent
    );
    }
    } else {
    alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    reminderTime,
    pendingIntent
    );
    }
    return true;
    } catch (SecurityException e) {
    Log.e(TAG, "SecurityException scheduling exact alarm; falling back to setWindow", e);
    try {
    alarmManager.setWindow(
    AlarmManager.RTC_WAKEUP,
    reminderTime,
    FALLBACK_WINDOW_MILLIS,
    pendingIntent
    );
    return true;
    } catch (Exception fallbackEx) {
    Log.e(TAG, "Fallback setWindow failed for taskId=" + task.getId(), fallbackEx);
    return false;
    }
    }
    }

  /**
  * Hủy lịch nhắc nhở đã đăng ký trên AlarmManager (UC 3.4, 3.5, 3.14, 3.15).
    */
    public static void cancelReminder(@NonNull Context context, long taskId) {
    if (taskId <= 0L) {
    return;
    }
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    PendingIntent existing = createReminderPendingIntent(
    context,
    taskId,
    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
    );
    if (existing != null) {
    if (alarmManager != null) {
    alarmManager.cancel(existing);
    }
    existing.cancel();
    }
    }

  /**
  * Đóng thông báo đang hiển thị trên thanh trạng thái (nếu có) cho công việc tương ứng.
    */
    public static void cancelNotification(@NonNull Context context, long taskId) {
    if (taskId <= 0L) {
    return;
    }
    NotificationManagerCompat.from(context).cancel(buildRequestCode(taskId));
    }

  /**
  * Quét toàn bộ công việc chưa hoàn thành còn lịch nhắc ở tương lai và đăng ký lại với AlarmManager.
  * Bắt buộc gọi trên luồng nền (diskIO).
    */
    public static int rescheduleAllActiveRemindersSync(@NonNull Context context, @NonNull TaskDao taskDao) {
    long now = System.currentTimeMillis();
    List<TaskEntity> activeTasks = taskDao.getActiveTasksWithFutureRemindersSync(now);
    if (activeTasks == null || activeTasks.isEmpty()) {
    return 0;
    }
    int scheduledCount = 0;
    for (TaskEntity task : activeTasks) {
    if (scheduleReminder(context, task)) {
    scheduledCount++;
    }
    }
    return scheduledCount;
    }

  @Nullable
  private static PendingIntent createReminderPendingIntent(
  @NonNull Context context,
  long taskId,
  int flags
  ) {
  Intent intent = new Intent(context.getApplicationContext(), ReminderReceiver.class);
  intent.setAction(ACTION_TASK_REMINDER);
  intent.putExtra(EXTRA_TASK_ID, taskId);
  return PendingIntent.getBroadcast(
  context.getApplicationContext(),
  buildRequestCode(taskId),
  intent,
  flags
  );
  }
  }
