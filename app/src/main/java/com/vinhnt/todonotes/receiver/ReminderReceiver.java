package com.vinhnt.todonotes.receiver;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.TodoNotesApp;
import com.vinhnt.todonotes.data.local.AppDatabase;
import com.vinhnt.todonotes.data.local.dao.TaskDao;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.ui.tasks.AddEditTaskActivity;
import com.vinhnt.todonotes.util.AppExecutors;

/**
* BroadcastReceiver tiếp nhận báo thức đến hạn từ AlarmManager, kiểm tra lại trạng thái mới nhất
* trong Room DB và phát thông báo cục bộ kèm nút "Hoàn thành ngay" (UC 3.15).
  */
  public class ReminderReceiver extends BroadcastReceiver {

  private static final String TAG = "ReminderReceiver";

  @Override
  public void onReceive(Context context, Intent intent) {
  if (context == null || intent == null) {
  return;
  }
  long taskId = intent.getLongExtra(ReminderScheduler.EXTRA_TASK_ID, -1L);
  if (taskId <= 0L) {
  return;
  }

       final Context appContext = context.getApplicationContext();
       final PendingResult pendingResult = goAsync();

       AppExecutors.getInstance().diskIO().execute(() -> {
           try {
               TaskDao taskDao = AppDatabase.getInstance(appContext).taskDao();
               TaskEntity task = taskDao.getTaskByIdSync(taskId);

               // UC 3.15 rẽ nhánh 1: Công việc đã bị xóa, đã hoàn thành hoặc đã hủy nhắc nhở -> không phát thông báo
               if (task == null || task.isCompleted() || task.getReminderTime() == null) {
                   return;
               }

               long now = System.currentTimeMillis();
               taskDao.updateReminderFired(taskId, true, now);
               showTaskReminderNotification(appContext, task);
           } catch (Exception e) {
               Log.e(TAG, "Error handling task reminder broadcast for taskId=" + taskId, e);
           } finally {
               pendingResult.finish();
           }
       });
  }

  private void showTaskReminderNotification(@NonNull Context context, @NonNull TaskEntity task) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
  if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
  != PackageManager.PERMISSION_GRANTED) {
  Log.w(TAG, "POST_NOTIFICATIONS permission not granted; skipping notification for taskId=" + task.getId());
  return;
  }
  }

       int notificationId = ReminderScheduler.buildRequestCode(task.getId());

       // UC 3.15 bước 3: Nhấn vào thông báo mở thẳng màn hình chi tiết công việc (kèm back stack về MainActivity)
       Intent detailIntent = AddEditTaskActivity.newIntentForEdit(context, task.getId());
       PendingIntent contentPendingIntent = TaskStackBuilder.create(context)
               .addNextIntentWithParentStack(detailIntent)
               .getPendingIntent(
                       notificationId,
                       PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
               );

       // Nút thao tác nhanh "Hoàn thành ngay" trực tiếp trên Notification (S-04)
       Intent completeIntent = new Intent(context, NotificationActionReceiver.class);
       completeIntent.setAction(NotificationActionReceiver.ACTION_COMPLETE_TASK);
       completeIntent.putExtra(ReminderScheduler.EXTRA_TASK_ID, task.getId());
       PendingIntent completePendingIntent = PendingIntent.getBroadcast(
               context,
               notificationId,
               completeIntent,
               PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
       );

       String contentText = task.getTitle();
       String bigText = task.getDescription().trim().isEmpty()
               ? task.getTitle()
               : task.getTitle() + "\n" + task.getDescription().trim();

       NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TodoNotesApp.REMINDER_CHANNEL_ID)
               .setSmallIcon(R.drawable.ic_alarm)
               .setContentTitle(context.getString(R.string.notification_title_reminder))
               .setContentText(contentText)
               .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
               .setPriority(NotificationCompat.PRIORITY_HIGH)
               .setCategory(NotificationCompat.CATEGORY_REMINDER)
               .setAutoCancel(true)
               .setDefaults(NotificationCompat.DEFAULT_ALL)
               .addAction(
                       R.drawable.ic_task,
                       context.getString(R.string.notification_action_complete),
                       completePendingIntent
               );

       if (contentPendingIntent != null) {
           builder.setContentIntent(contentPendingIntent);
       }

       NotificationManagerCompat.from(context).notify(notificationId, builder.build());
  }
  }
