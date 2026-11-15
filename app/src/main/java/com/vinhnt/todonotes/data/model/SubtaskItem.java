package com.vinhnt.todonotes.data.model;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
* Mô hình dữ liệu cho một công việc con (Subtask / Checklist item) bên trong công việc chính.
  */
  public class SubtaskItem {
  @NonNull
  private String title;
  private boolean done;

  public SubtaskItem(@NonNull String title, boolean done) {
  this.title = title.trim();
  this.done = done;
  }

  @NonNull
  public String getTitle() {
  return title;
  }

  public void setTitle(@NonNull String title) {
  this.title = title.trim();
  }

  public boolean isDone() {
  return done;
  }

  public void setDone(boolean done) {
  this.done = done;
  }

  @Override
  public boolean equals(Object o) {
  if (this == o) return true;
  if (o == null || getClass() != o.getClass()) return false;
  SubtaskItem that = (SubtaskItem) o;
  return done == that.done && Objects.equals(title, that.title);
  }

  @Override
  public int hashCode() {
  return Objects.hash(title, done);
  }
  }
