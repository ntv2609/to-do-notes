package com.vinhnt.todonotes.receiver;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vinhnt.todonotes.data.local.AppDatabase;
import com.vinhnt.todonotes.util.AppExecutors;

/**
* BroadcastReceiver khôi phục toàn bộ lịch nhắc nhở hợp lệ còn hiệu lực sau khi thiết bị khởi động lại,
* đổi giờ/múi giờ, cập nhật ứng dụng hoặc được cấp quyền báo thức chính xác (UC 3.15 rẽ nhánh 3).
  */
  public class BootCompletedReceiver extends BroadcastReceiver {

  private static final String TAG = "BootCompletedReceiver";

  @Override
  public void onReceive(Context context, Intent intent) {
  if (context == null || intent == null || intent.getAction() == null) {
  return;
  }
  String action = intent.getAction();
  boolean isSupportedAction = Intent.ACTION_BOOT_COMPLETED.equals(action)
  || Intent.ACTION_TIME_CHANGED.equals(action)
  || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
  || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
  || AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED.equals(action);

       if (!isSupportedAction) {
           return;
       }

       final Context appContext = context.getApplicationContext();
       final PendingResult pendingResult = goAsync();

       AppExecutors.getInstance().diskIO().execute(() -> {
           try {
               int count = ReminderScheduler.rescheduleAllActiveRemindersSync(
                       appContext,
                       AppDatabase.getInstance(appContext).taskDao()
               );
               Log.i(TAG, "Rescheduled " + count + " active reminders on action=" + action);
           } catch (Exception e) {
               Log.e(TAG, "Failed to reschedule reminders on action=" + action, e);
           } finally {
               pendingResult.finish();
           }
       });
  }
  }
