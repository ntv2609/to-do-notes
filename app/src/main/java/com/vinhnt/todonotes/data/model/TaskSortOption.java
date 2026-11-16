package com.vinhnt.todonotes.data.model;

import com.vinhnt.todonotes.util.PreferencesManager;

/**
* Các tiêu chí sắp xếp danh sách công việc (UC 3.12).
  */
  public enum TaskSortOption {
  CREATED_DATE(PreferencesManager.SORT_CREATED_DATE),
  DUE_DATE(PreferencesManager.SORT_DUE_DATE),
  PRIORITY(PreferencesManager.SORT_PRIORITY);

  private final int prefValue;

  TaskSortOption(int prefValue) {
  this.prefValue = prefValue;
  }

  public int getPrefValue() {
  return prefValue;
  }

  public static TaskSortOption fromPrefValue(int value) {
  for (TaskSortOption option : values()) {
  if (option.prefValue == value) {
  return option;
  }
  }
  return CREATED_DATE;
  }
  }
