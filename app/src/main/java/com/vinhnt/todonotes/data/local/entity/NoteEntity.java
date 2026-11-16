package com.vinhnt.todonotes.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
* Thực thể Room ánh xạ bảng "notes" lưu trữ ghi chú nhanh (theo docs/SPEC.md Mục 2.2).
  */
  @Entity(
  tableName = "notes",
  indices = {
  @Index(name = "idx_notes_pinned_updated", value = {"is_pinned", "updated_at"})
  }
  )
  public class NoteEntity {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  private long id;

  @NonNull
  @ColumnInfo(name = "title")
  private String title = "";

  @NonNull
  @ColumnInfo(name = "content")
  private String content = "";

  @ColumnInfo(name = "color_index", defaultValue = "0")
  private int colorIndex = 0;

  @ColumnInfo(name = "is_pinned", defaultValue = "0")
  private boolean pinned = false;

  @ColumnInfo(name = "created_at")
  private long createdAt;

  @ColumnInfo(name = "updated_at")
  private long updatedAt;

  /**
  * Constructor mặc định bắt buộc cho Room.
    */
    public NoteEntity() {
    }

  /**
  * Constructor tiện ích tạo mới ghi chú nhanh trong code ứng dụng và kiểm thử.
    */
    @Ignore
    public NoteEntity(
    @NonNull String title,
    @NonNull String content,
    int colorIndex,
    boolean pinned,
    long createdAt,
    long updatedAt
    ) {
    this.title = title;
    this.content = content;
    this.colorIndex = Math.max(0, Math.min(5, colorIndex));
    this.pinned = pinned;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    }

  /**
  * Tạo bản sao (deep copy) phục vụ ListAdapter + DiffUtil và chức năng Hoàn tác.
    */
    @NonNull
    public NoteEntity copy() {
    NoteEntity clone = new NoteEntity();
    clone.id = this.id;
    clone.title = this.title;
    clone.content = this.content;
    clone.colorIndex = this.colorIndex;
    clone.pinned = this.pinned;
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
  public String getContent() {
  return content;
  }

  public void setContent(@NonNull String content) {
  this.content = content;
  }

  public int getColorIndex() {
  return colorIndex;
  }

  public void setColorIndex(int colorIndex) {
  this.colorIndex = Math.max(0, Math.min(5, colorIndex));
  }

  public boolean isPinned() {
  return pinned;
  }

  public void setPinned(boolean pinned) {
  this.pinned = pinned;
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
  NoteEntity that = (NoteEntity) o;
  return id == that.id
  && colorIndex == that.colorIndex
  && pinned == that.pinned
  && createdAt == that.createdAt
  && updatedAt == that.updatedAt
  && Objects.equals(title, that.title)
  && Objects.equals(content, that.content);
  }

  @Override
  public int hashCode() {
  return Objects.hash(id, title, content, colorIndex, pinned, createdAt, updatedAt);
  }
  }
