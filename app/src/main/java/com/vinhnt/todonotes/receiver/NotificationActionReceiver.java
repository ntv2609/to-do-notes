package com.vinhnt.todonotes.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vinhnt.todonotes.data.local.AppDatabase;
import com.vinhnt.todonotes.data.local.dao.TaskDao;
import com.vinhnt.todonotes.util.AppExecutors;

/**
* BroadcastReceiver xử lý nút thao tác nhanh "Hoàn thành ngay" trực tiếp trên thanh thông báo (S-04).
  */
  public class NotificationActionReceiver extends BroadcastReceiver {

  private static final String TAG = "NotificationActionRcv";
  public static final String ACTION_COMPLETE_TASK = "com.vinhnt.todonotes.action.COMPLETE_TASK";

  @Override
  public void onReceive(Context context, Intent intent) {
  if (context == null || intent == null) {
  return;
  }
  if (!ACTION_COMPLETE_TASK.equals(intent.getAction())) {
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
               long now = System.currentTimeMillis();
               taskDao.updateCompletionStatus(taskId, true, now, now);
               ReminderScheduler.cancelReminder(appContext, taskId);
               ReminderScheduler.cancelNotification(appContext, taskId);
           } catch (Exception e) {
               Log.e(TAG, "Error completing task from notification action, taskId=" + taskId, e);
           } finally {
               pendingResult.finish();
           }
       });
  }
  }
