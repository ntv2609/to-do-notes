package com.vinhnt.todonotes.ui.tasks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.repository.TaskRepository;

/**
* ViewModel phục vụ màn hình AddEditTaskActivity: tải công việc cần sửa, lưu mới, cập nhật và xóa.
  */
  public class AddEditTaskViewModel extends AndroidViewModel {

  private final TaskRepository taskRepository;
  private final MutableLiveData<TaskEntity> loadedTaskLiveData = new MutableLiveData<>();
  private boolean isTaskLoaded = false;

  public AddEditTaskViewModel(@NonNull Application application) {
  super(application);
  this.taskRepository = TaskRepository.getInstance(application);
  }

  @NonNull
  public LiveData<TaskEntity> getLoadedTask() {
  return loadedTaskLiveData;
  }

  public void loadTaskIfNeeded(long taskId) {
  if (taskId <= 0L || isTaskLoaded) {
  return;
  }
  isTaskLoaded = true;
  taskRepository.getTaskById(taskId, loadedTaskLiveData::setValue);
  }

  public void saveTask(
  @Nullable TaskEntity existingTask,
  @NonNull String title,
  @NonNull String description,
  int priority,
  int category,
  @Nullable Long dueDateMillis,
  @Nullable Long reminderTimeMillis,
  @NonNull String subtasksJson,
  boolean isCompleted,
  @NonNull Runnable onSavedCallback
  ) {
  long now = System.currentTimeMillis();
  if (existingTask == null) {
  TaskEntity newTask = new TaskEntity(
  title.trim(),
  description.trim(),
  dueDateMillis,
  reminderTimeMillis,
  priority,
  category,
  subtasksJson,
  isCompleted,
  now,
  now
  );
  taskRepository.insertTask(newTask, rowId -> onSavedCallback.run());
  } else {
  TaskEntity updated = existingTask.copy();
  updated.setTitle(title.trim());
  updated.setDescription(description.trim());
  updated.setPriority(priority);
  updated.setCategory(category);
  updated.setDueDate(dueDateMillis);

           // Nếu người dùng đổi giờ nhắc nhở mới ở tương lai, đặt lại cờ reminderFired = false
           if (reminderTimeMillis != null && !reminderTimeMillis.equals(existingTask.getReminderTime())) {
               updated.setReminderFired(false);
           }
           updated.setReminderTime(reminderTimeMillis);
           updated.setSubtasksJson(subtasksJson);
           updated.setCompleted(isCompleted);
           updated.setCompletedAt(isCompleted
                   ? (existingTask.getCompletedAt() != null ? existingTask.getCompletedAt() : now)
                   : null);
           updated.setUpdatedAt(now);

           taskRepository.updateTask(updated, rows -> onSavedCallback.run());
       }
  }

  public void deleteTask(@NonNull TaskEntity task, @NonNull Runnable onDeletedCallback) {
  taskRepository.deleteTask(task, unused -> onDeletedCallback.run());
  }
  }
