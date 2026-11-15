package com.vinhnt.todonotes.data.model;

/**
* Định nghĩa các mức độ ưu tiên của công việc (0 = Thấp, 1 = Trung bình, 2 = Cao).
  */
  public enum Priority {
  LOW(0),
  MEDIUM(1),
  HIGH(2);

  private final int value;

  Priority(int value) {
  this.value = value;
  }

  public int getValue() {
  return value;
  }

  public static Priority fromValue(int value) {
  for (Priority priority : values()) {
  if (priority.value == value) {
  return priority;
  }
  }
  return MEDIUM;
  }
  }
