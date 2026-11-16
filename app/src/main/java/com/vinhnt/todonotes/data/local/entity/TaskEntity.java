package com.vinhnt.todonotes.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
* Thực thể Room ánh xạ bảng "tasks" lưu trữ công việc hằng ngày (theo docs/SPEC.md Mục 2.1).
  */
  @Entity(
  tableName = "tasks",
  indices = {
  @Index(name = "idx_tasks_completed_due", value = {"is_completed", "due_date"}),
  @Index(name = "idx_tasks_priority", value = {"priority"}),
  @Index(name = "idx_tasks_category", value = {"category"}),
  @Index(name = "idx_tasks_reminder", value = {"is_completed", "reminder_time"})
  }
  )
  public class TaskEntity {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  private long id;

  @NonNull
  @ColumnInfo(name = "title")
  private String title = "";

  @NonNull
  @ColumnInfo(name = "description")
  private String description = "";

  @Nullable
  @ColumnInfo(name = "due_date")
  private Long dueDate;

  @Nullable
  @ColumnInfo(name = "reminder_time")
  private Long reminderTime;

  @ColumnInfo(name = "reminder_fired", defaultValue = "0")
  private boolean reminderFired;

  @ColumnInfo(name = "priority", defaultValue = "1")
  private int priority = 1;

  @ColumnInfo(name = "category", defaultValue = "0")
  private int category = 0;

  @NonNull
  @ColumnInfo(name = "subtasks_json", defaultValue = "'[]'")
  private String subtasksJson = "[]";

  @ColumnInfo(name = "is_completed", defaultValue = "0")
  private boolean completed;

  @Nullable
  @ColumnInfo(name = "completed_at")
  private Long completedAt;

  @ColumnInfo(name = "created_at")
  private long createdAt;

  @ColumnInfo(name = "updated_at")
  private long updatedAt;

  /**
  * Constructor mặc định bắt buộc cho Room.
    */
    public TaskEntity() {
    }

  /**
  * Constructor tiện ích tạo mới công việc trong code ứng dụng và kiểm thử.
    */
    @Ignore
    public TaskEntity(
    @NonNull String title,
    @NonNull String description,
    @Nullable Long dueDate,
    @Nullable Long reminderTime,
    int priority,
    int category,
    @NonNull String subtasksJson,
    boolean completed,
    long createdAt,
    long updatedAt
    ) {
    this.title = title;
    this.description = description;
    this.dueDate = dueDate;
    this.reminderTime = reminderTime;
    this.reminderFired = false;
    this.priority = priority;
    this.category = category;
    this.subtasksJson = subtasksJson;
    this.completed = completed;
    this.completedAt = completed ? updatedAt : null;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    }

  /**
  * Tạo bản sao (deep copy) để sử dụng an toàn với ListAdapter + DiffUtil và chức năng Hoàn tác.
    */
    @NonNull
    public TaskEntity copy() {
    TaskEntity clone = new TaskEntity();
    clone.id = this.id;
    clone.title = this.title;
    clone.description = this.description;
    clone.dueDate = this.dueDate;
    clone.reminderTime = this.reminderTime;
    clone.reminderFired = this.reminderFired;
    clone.priority = this.priority;
    clone.category = this.category;
    clone.subtasksJson = this.subtasksJson;
    clone.completed = this.completed;
    clone.completedAt = this.completedAt;
    clone.createdAt = this.createdAt;
    clone.updatedAt = this.updatedAt;
    return clone;
    }

  public long getId() {
  return id;
  }

  public void setId(long id) {
  this.id = id;
  }

  @NonNull
  public String getTitle() {
  return title;
  }

  public void setTitle(@NonNull String title) {
  this.title = title;
  }

  @NonNull
  public String getDescription() {
  return description;
  }

  public void setDescription(@NonNull String description) {
  this.description = description;
  }

  @Nullable
  public Long getDueDate() {
  return dueDate;
  }

  public void setDueDate(@Nullable Long dueDate) {
  this.dueDate = dueDate;
  }

  @Nullable
  public Long getReminderTime() {
  return reminderTime;
  }

  public void setReminderTime(@Nullable Long reminderTime) {
  this.reminderTime = reminderTime;
  }

  public boolean isReminderFired() {
  return reminderFired;
  }

  public void setReminderFired(boolean reminderFired) {
  this.reminderFired = reminderFired;
  }

  public int getPriority() {
  return priority;
  }

  public void setPriority(int priority) {
  this.priority = priority;
  }

  public int getCategory() {
  return category;
  }

  public void setCategory(int category) {
  this.category = category;
  }

  @NonNull
  public String getSubtasksJson() {
  return subtasksJson;
  }

  public void setSubtasksJson(@NonNull String subtasksJson) {
  this.subtasksJson = subtasksJson;
  }

  public boolean isCompleted() {
  return completed;
  }

  public void setCompleted(boolean completed) {
  this.completed = completed;
  }

  @Nullable
  public Long getCompletedAt() {
  return completedAt;
  }

  public void setCompletedAt(@Nullable Long completedAt) {
  this.completedAt = completedAt;
  }

  public long getCreatedAt() {
  return createdAt;
  }

  public void setCreatedAt(long createdAt) {
  this.createdAt = createdAt;
  }

  public long getUpdatedAt() {
  return updatedAt;
  }

  public void setUpdatedAt(long updatedAt) {
  this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
  if (this == o) return true;
  if (o == null || getClass() != o.getClass()) return false;
  TaskEntity that = (TaskEntity) o;
  return id == that.id
  && reminderFired == that.reminderFired
  && priority == that.priority
  && category == that.category
  && completed == that.completed
  && createdAt == that.createdAt
  && updatedAt == that.updatedAt
  && Objects.equals(title, that.title)
  && Objects.equals(description, that.description)
  && Objects.equals(dueDate, that.dueDate)
  && Objects.equals(reminderTime, that.reminderTime)
  && Objects.equals(subtasksJson, that.subtasksJson)
  && Objects.equals(completedAt, that.completedAt);
  }

  @Override
  public int hashCode() {
  return Objects.hash(
  id, title, description, dueDate, reminderTime, reminderFired,
  priority, category, subtasksJson, completed, completedAt, createdAt, updatedAt
  );
  }
  }
