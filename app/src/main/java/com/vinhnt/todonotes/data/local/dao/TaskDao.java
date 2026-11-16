package com.vinhnt.todonotes.data.local.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vinhnt.todonotes.data.local.entity.TaskEntity;

import java.util.List;

/**
* Giao diện truy xuất dữ liệu (DAO) cho bảng "tasks".
  */
  @Dao
  public interface TaskDao {

  @Query("SELECT * FROM tasks ORDER BY is_completed ASC, created_at DESC")
  LiveData<List<TaskEntity>> observeAllTasks();

  @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
  LiveData<TaskEntity> observeTaskById(long taskId);

  @Nullable
  @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
  TaskEntity getTaskByIdSync(long taskId);

  @Query("SELECT * FROM tasks WHERE title LIKE '%' || :keyword || '%' COLLATE NOCASE " +
  "OR description LIKE '%' || :keyword || '%' COLLATE NOCASE " +
  "ORDER BY is_completed ASC, updated_at DESC")
  LiveData<List<TaskEntity>> searchTasks(@NonNull String keyword);

  /**
  * Lấy danh sách công việc chưa hoàn thành còn lịch nhắc nhở trong tương lai
  * để khôi phục báo thức sau khi khởi động lại máy (UC 3.15 rẽ nhánh 3).
    */
    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND reminder_time IS NOT NULL AND reminder_time > :nowMillis")
    List<TaskEntity> getActiveTasksWithFutureRemindersSync(long nowMillis);

  /**
  * Lấy toàn bộ công việc đồng bộ trên luồng nền phục vụ chức năng Sao lưu JSON (Backup).
    */
    @Query("SELECT * FROM tasks ORDER BY created_at ASC")
    List<TaskEntity> getAllTasksSync();

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insertTask(@NonNull TaskEntity task);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertTasks(@NonNull List<TaskEntity> tasks);

  @Update
  int updateTask(@NonNull TaskEntity task);

  @Delete
  int deleteTask(@NonNull TaskEntity task);

  @Query("DELETE FROM tasks WHERE id = :taskId")
  int deleteTaskById(long taskId);

  @Query("UPDATE tasks SET is_completed = :completed, completed_at = :completedAt, updated_at = :updatedAt WHERE id = :taskId")
  int updateCompletionStatus(long taskId, boolean completed, @Nullable Long completedAt, long updatedAt);

  @Query("UPDATE tasks SET reminder_fired = :fired, updated_at = :updatedAt WHERE id = :taskId")
  int updateReminderFired(long taskId, boolean fired, long updatedAt);
  }
