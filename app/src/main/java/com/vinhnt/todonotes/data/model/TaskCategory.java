package com.vinhnt.todonotes.data.model;

/**
* Định nghĩa các nhãn phân loại (Category) của công việc.
  */
  public enum TaskCategory {
  PERSONAL(0),
  STUDY(1),
  WORK(2),
  SHOPPING(3),
  OTHER(4);

  private final int value;

  TaskCategory(int value) {
  this.value = value;
  }

  public int getValue() {
  return value;
  }

  public static TaskCategory fromValue(int value) {
  for (TaskCategory category : values()) {
  if (category.value == value) {
  return category;
  }
  }
  return PERSONAL;
  }
  }
