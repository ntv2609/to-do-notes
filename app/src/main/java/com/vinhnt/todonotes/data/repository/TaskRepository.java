package com.vinhnt.todonotes.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.vinhnt.todonotes.data.local.AppDatabase;
import com.vinhnt.todonotes.data.local.dao.TaskDao;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.receiver.ReminderScheduler;
import com.vinhnt.todonotes.util.AppExecutors;

import java.util.List;

/**
* Lớp trung gian điều phối truy xuất dữ liệu công việc giữa ViewModel và TaskDao trên luồng nền AppExecutors,
* đồng thời đồng bộ trạng thái lịch nhắc nhở AlarmManager (BR-04, BR-05, UC 3.13 - 3.15).
  */
  public class TaskRepository {

  public interface Callback<T> {
  void onComplete(T result);
  }

  private static final Object LOCK = new Object();
  private static volatile TaskRepository instance;

  private final TaskDao taskDao;
  private final AppExecutors executors;
  @Nullable
  private final Context appContext;

  public TaskRepository(@NonNull TaskDao taskDao, @NonNull AppExecutors executors) {
  this(taskDao, executors, null);
  }

  public TaskRepository(
  @NonNull TaskDao taskDao,
  @NonNull AppExecutors executors,
  @Nullable Context appContext
  ) {
  this.taskDao = taskDao;
  this.executors = executors;
  this.appContext = appContext != null ? appContext.getApplicationContext() : null;
  }

  @NonNull
  public static TaskRepository getInstance(@NonNull Context context) {
  if (instance == null) {
  synchronized (LOCK) {
  if (instance == null) {
  Context applicationContext = context.getApplicationContext();
  AppDatabase db = AppDatabase.getInstance(applicationContext);
  instance = new TaskRepository(db.taskDao(), AppExecutors.getInstance(), applicationContext);
  }
  }
  }
  return instance;
  }

  @NonNull
  public LiveData<List<TaskEntity>> observeAllTasks() {
  return taskDao.observeAllTasks();
  }

  @NonNull
  public LiveData<TaskEntity> observeTaskById(long taskId) {
  return taskDao.observeTaskById(taskId);
  }

  @NonNull
  public LiveData<List<TaskEntity>> searchTasks(@NonNull String keyword) {
  return taskDao.searchTasks(keyword.trim());
  }

  public void getTaskById(long taskId, @NonNull Callback<TaskEntity> callback) {
  executors.diskIO().execute(() -> {
  TaskEntity task = taskDao.getTaskByIdSync(taskId);
  executors.mainThread().execute(() -> callback.onComplete(task));
  });
  }

  public void insertTask(@NonNull TaskEntity task, @Nullable Callback<Long> callback) {
  executors.diskIO().execute(() -> {
  long now = System.currentTimeMillis();
  if (task.getCreatedAt() <= 0L) {
  task.setCreatedAt(now);
  }
  task.setUpdatedAt(now);
  long rowId = taskDao.insertTask(task);
  task.setId(rowId);
  syncReminderForTask(task);
  if (callback != null) {
  executors.mainThread().execute(() -> callback.onComplete(rowId));
  }
  });
  }

  public void updateTask(@NonNull TaskEntity task, @Nullable Callback<Integer> callback) {
  executors.diskIO().execute(() -> {
  task.setUpdatedAt(System.currentTimeMillis());
  int rows = taskDao.updateTask(task);
  syncReminderForTask(task);
  if (callback != null) {
  executors.mainThread().execute(() -> callback.onComplete(rows));
  }
  });
  }

  public void toggleTaskCompletion(@NonNull TaskEntity task, boolean isCompleted, @Nullable Callback<Void> callback) {
  executors.diskIO().execute(() -> {
  long now = System.currentTimeMillis();
  Long completedAt = isCompleted ? now : null;
  taskDao.updateCompletionStatus(task.getId(), isCompleted, completedAt, now);

           if (appContext != null) {
               if (isCompleted) {
                   // BR-04: Đánh dấu hoàn thành -> hủy báo thức và đóng thông báo nếu đang hiển thị
                   ReminderScheduler.cancelReminder(appContext, task.getId());
                   ReminderScheduler.cancelNotification(appContext, task.getId());
               } else {
                   // BR-04: Bỏ đánh dấu hoàn thành -> đặt lại báo thức nếu reminderTime vẫn còn ở tương lai
                   TaskEntity fresh = taskDao.getTaskByIdSync(task.getId());
                   if (fresh != null && ReminderScheduler.shouldScheduleReminder(fresh, now)) {
                       taskDao.updateReminderFired(fresh.getId(), false, now);
                       fresh.setReminderFired(false);
                       ReminderScheduler.scheduleReminder(appContext, fresh);
                   }
               }
           }

           if (callback != null) {
               executors.mainThread().execute(() -> callback.onComplete(null));
           }
       });
  }

  public void deleteTask(@NonNull TaskEntity task, @Nullable Callback<Void> callback) {
  executors.diskIO().execute(() -> {
  taskDao.deleteTask(task);
  if (appContext != null) {
  ReminderScheduler.cancelReminder(appContext, task.getId());
  ReminderScheduler.cancelNotification(appContext, task.getId());
  }
  if (callback != null) {
  executors.mainThread().execute(() -> callback.onComplete(null));
  }
  });
  }

  /**
  * Chèn lại bản ghi công việc vừa xóa khi người dùng bấm "Hoàn tác" (Undo - BR-05)
  * và khôi phục lịch nhắc nhở nếu vẫn còn hạn ở tương lai.
    */
    public void restoreDeletedTask(@NonNull TaskEntity deletedTask, @Nullable Callback<Long> callback) {
    executors.diskIO().execute(() -> {
    long rowId = taskDao.insertTask(deletedTask);
    deletedTask.setId(rowId);
    syncReminderForTask(deletedTask);
    if (callback != null) {
    executors.mainThread().execute(() -> callback.onComplete(rowId));
    }
    });
    }

  private void syncReminderForTask(@NonNull TaskEntity task) {
  if (appContext == null) {
  return;
  }
  long now = System.currentTimeMillis();
  if (ReminderScheduler.shouldScheduleReminder(task, now)) {
  ReminderScheduler.scheduleReminder(appContext, task);
  } else {
  ReminderScheduler.cancelReminder(appContext, task.getId());
  if (task.isCompleted() || task.getReminderTime() == null) {
  ReminderScheduler.cancelNotification(appContext, task.getId());
  }
  }
  }
  }
