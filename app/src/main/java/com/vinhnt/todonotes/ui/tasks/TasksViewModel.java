package com.vinhnt.todonotes.ui.tasks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.model.Priority;
import com.vinhnt.todonotes.data.model.TaskCategory;
import com.vinhnt.todonotes.data.model.TaskFilterType;
import com.vinhnt.todonotes.data.model.TaskSortOption;
import com.vinhnt.todonotes.data.repository.TaskRepository;
import com.vinhnt.todonotes.util.DateTimeUtils;
import com.vinhnt.todonotes.util.PreferencesManager;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
* ViewModel quản lý trạng thái danh sách công việc, thống kê tiến độ, bộ lọc đa tiêu chí và sắp xếp.
  */
  public class TasksViewModel extends AndroidViewModel {

  /**
  * Dữ liệu thống kê hiển thị trên Hero Progress Card (UC 3.1 Điểm mở rộng).
    */
    public static class TaskProgressStats {
    public final int totalCount;
    public final int completedCount;
    public final int overdueCount;
    public final int percentage;

    public TaskProgressStats(int totalCount, int completedCount, int overdueCount) {
    this.totalCount = totalCount;
    this.completedCount = completedCount;
    this.overdueCount = overdueCount;
    this.percentage = totalCount == 0 ? 0 : Math.round((completedCount * 100f) / totalCount);
    }
    }

  private final TaskRepository taskRepository;
  private final PreferencesManager preferencesManager;

  private final LiveData<List<TaskEntity>> allTasksSource;
  private final MutableLiveData<TaskFilterType> filterTypeLiveData = new MutableLiveData<>(TaskFilterType.ALL);
  // -1 đại diện cho "Tất cả nhãn"
  private final MutableLiveData<Integer> categoryFilterLiveData = new MutableLiveData<>(-1);
  private final MutableLiveData<String> searchQueryLiveData = new MutableLiveData<>("");
  private final MutableLiveData<TaskSortOption> sortOptionLiveData;
  private final MutableLiveData<Boolean> sortAscendingLiveData;

  private final MediatorLiveData<List<TaskEntity>> displayedTasksLiveData = new MediatorLiveData<>();
  private final MediatorLiveData<TaskProgressStats> progressStatsLiveData = new MediatorLiveData<>();

  public TasksViewModel(@NonNull Application application) {
  super(application);
  this.taskRepository = TaskRepository.getInstance(application);
  this.preferencesManager = PreferencesManager.getInstance(application);

       TaskSortOption savedSort = TaskSortOption.fromPrefValue(preferencesManager.getTaskSortOption());
       this.sortOptionLiveData = new MutableLiveData<>(savedSort);
       this.sortAscendingLiveData = new MutableLiveData<>(preferencesManager.isTaskSortAscending());

       this.allTasksSource = taskRepository.observeAllTasks();

       displayedTasksLiveData.addSource(allTasksSource, tasks -> recomputeDisplayedTasks());
       displayedTasksLiveData.addSource(filterTypeLiveData, filter -> recomputeDisplayedTasks());
       displayedTasksLiveData.addSource(categoryFilterLiveData, cat -> recomputeDisplayedTasks());
       displayedTasksLiveData.addSource(searchQueryLiveData, query -> recomputeDisplayedTasks());
       displayedTasksLiveData.addSource(sortOptionLiveData, sort -> recomputeDisplayedTasks());
       displayedTasksLiveData.addSource(sortAscendingLiveData, asc -> recomputeDisplayedTasks());

       progressStatsLiveData.addSource(allTasksSource, this::recomputeStats);
  }

  @NonNull
  public LiveData<List<TaskEntity>> getDisplayedTasks() {
  return displayedTasksLiveData;
  }

  @NonNull
  public LiveData<TaskProgressStats> getProgressStats() {
  return progressStatsLiveData;
  }

  @NonNull
  public TaskFilterType getCurrentFilterType() {
  TaskFilterType current = filterTypeLiveData.getValue();
  return current != null ? current : TaskFilterType.ALL;
  }

  public int getCurrentCategoryFilter() {
  Integer cat = categoryFilterLiveData.getValue();
  return cat != null ? cat : -1;
  }

  @NonNull
  public TaskSortOption getCurrentSortOption() {
  TaskSortOption sort = sortOptionLiveData.getValue();
  return sort != null ? sort : TaskSortOption.CREATED_DATE;
  }

  public boolean isSortAscending() {
  Boolean asc = sortAscendingLiveData.getValue();
  return asc != null && asc;
  }

  public boolean hasActiveFilterOrSearch() {
  String q = searchQueryLiveData.getValue();
  boolean hasQuery = q != null && !q.trim().isEmpty();
  return getCurrentFilterType() != TaskFilterType.ALL
  || getCurrentCategoryFilter() != -1
  || hasQuery;
  }

  public void setFilterType(@NonNull TaskFilterType filterType) {
  if (filterType != filterTypeLiveData.getValue()) {
  filterTypeLiveData.setValue(filterType);
  }
  }

  public void setCategoryFilter(int categoryValue) {
  Integer current = categoryFilterLiveData.getValue();
  if (current == null || current != categoryValue) {
  categoryFilterLiveData.setValue(categoryValue);
  }
  }

  public void setSearchQuery(@Nullable String query) {
  String safe = query == null ? "" : query;
  if (!safe.equals(searchQueryLiveData.getValue())) {
  searchQueryLiveData.setValue(safe);
  }
  }

  public void setSortOption(@NonNull TaskSortOption sortOption) {
  preferencesManager.setTaskSortOption(sortOption.getPrefValue());
  sortOptionLiveData.setValue(sortOption);
  }

  public void toggleSortDirection() {
  boolean newAsc = !isSortAscending();
  preferencesManager.setTaskSortAscending(newAsc);
  sortAscendingLiveData.setValue(newAsc);
  }

  public void addQuickTask(@NonNull String title, @Nullable String description) {
  long now = System.currentTimeMillis();
  TaskEntity entity = new TaskEntity(
  title.trim(),
  description == null ? "" : description.trim(),
  null,
  null,
  Priority.MEDIUM.getValue(),
  TaskCategory.PERSONAL.getValue(),
  "[]",
  false,
  now,
  now
  );
  taskRepository.insertTask(entity, null);
  }

  public void toggleTaskCompleted(@NonNull TaskEntity task, boolean isCompleted) {
  taskRepository.toggleTaskCompletion(task, isCompleted, null);
  }

  public void deleteTask(@NonNull TaskEntity task) {
  taskRepository.deleteTask(task, null);
  }

  public void restoreDeletedTask(@NonNull TaskEntity deletedTask) {
  taskRepository.restoreDeletedTask(deletedTask, null);
  }

  private void recomputeStats(@Nullable List<TaskEntity> allTasks) {
  progressStatsLiveData.setValue(computeProgressStats(allTasks, System.currentTimeMillis()));
  }

  @NonNull
  public static TaskProgressStats computeProgressStats(@Nullable List<TaskEntity> allTasks, long nowMillis) {
  if (allTasks == null || allTasks.isEmpty()) {
  return new TaskProgressStats(0, 0, 0);
  }
  int total = allTasks.size();
  int completed = 0;
  int overdue = 0;
  for (TaskEntity task : allTasks) {
  if (task == null) {
  continue;
  }
  if (task.isCompleted()) {
  completed++;
  } else if (DateTimeUtils.isOverdue(false, task.getDueDate(), nowMillis)) {
  overdue++;
  }
  }
  return new TaskProgressStats(total, completed, overdue);
  }

  private void recomputeDisplayedTasks() {
  List<TaskEntity> source = allTasksSource.getValue();
  if (source == null) {
  displayedTasksLiveData.setValue(Collections.emptyList());
  return;
  }
  List<TaskEntity> result = filterAndSortTasks(
  source,
  getCurrentFilterType(),
  getCurrentCategoryFilter(),
  searchQueryLiveData.getValue(),
  getCurrentSortOption(),
  isSortAscending(),
  System.currentTimeMillis(),
  ZoneId.systemDefault()
  );
  displayedTasksLiveData.setValue(result);
  }

  /**
  * Hàm nạp chồng giữ tương thích với chiều sắp xếp mặc định (ascending = false).
    */
    @NonNull
    public static List<TaskEntity> filterAndSortTasks(
    @Nullable List<TaskEntity> source,
    @NonNull TaskFilterType filterType,
    int categoryFilter,
    @Nullable String keyword,
    @NonNull TaskSortOption sortOption,
    long nowMillis,
    @NonNull ZoneId zoneId
    ) {
    return filterAndSortTasks(
    source,
    filterType,
    categoryFilter,
    keyword,
    sortOption,
    false,
    nowMillis,
    zoneId
    );
    }

  /**
  * Hàm thuần (pure static method) phục vụ lọc và sắp xếp công việc (có hỗ trợ đảo chiều).
    */
    @NonNull
    public static List<TaskEntity> filterAndSortTasks(
    @Nullable List<TaskEntity> source,
    @NonNull TaskFilterType filterType,
    int categoryFilter,
    @Nullable String keyword,
    @NonNull TaskSortOption sortOption,
    boolean reverseDirection,
    long nowMillis,
    @NonNull ZoneId zoneId
    ) {
    if (source == null || source.isEmpty()) {
    return Collections.emptyList();
    }
    String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    List<TaskEntity> filtered = new ArrayList<>();

    for (TaskEntity task : source) {
    if (task == null) {
    continue;
    }
    // 1. Lọc theo trạng thái (UC 3.11)
    boolean matchesStatus;
    switch (filterType) {
    case ACTIVE:
    matchesStatus = !task.isCompleted();
    break;
    case COMPLETED:
    matchesStatus = task.isCompleted();
    break;
    case OVERDUE:
    matchesStatus = DateTimeUtils.isOverdue(task.isCompleted(), task.getDueDate(), nowMillis);
    break;
    case TODAY:
    matchesStatus = task.getDueDate() != null
    && DateTimeUtils.isSameDay(task.getDueDate(), nowMillis, zoneId);
    break;
    case ALL:
    default:
    matchesStatus = true;
    break;
    }
    if (!matchesStatus) {
    continue;
    }

         // 2. Lọc theo nhãn phân loại
         if (categoryFilter >= 0 && task.getCategory() != categoryFilter) {
             continue;
         }

         // 3. Lọc theo từ khóa tìm kiếm trong tab
         if (!normalizedKeyword.isEmpty()) {
             String titleLower = task.getTitle().toLowerCase(Locale.ROOT);
             String descLower = task.getDescription().toLowerCase(Locale.ROOT);
             if (!titleLower.contains(normalizedKeyword) && !descLower.contains(normalizedKeyword)) {
                 continue;
             }
         }

         filtered.add(task.copy());
    }

    // 4. Sắp xếp (UC 3.12): Luôn ưu tiên việc chưa hoàn thành lên trên việc đã hoàn thành
    int directionMultiplier = reverseDirection ? -1 : 1;
    Comparator<TaskEntity> comparator = (a, b) -> {
    if (a.isCompleted() != b.isCompleted()) {
    return a.isCompleted() ? 1 : -1;
    }
    switch (sortOption) {
    case DUE_DATE: {
    Long dueA = a.getDueDate();
    Long dueB = b.getDueDate();
    if (dueA == null && dueB == null) {
    return Long.compare(b.getCreatedAt(), a.getCreatedAt());
    }
    // Luôn đẩy các công việc không đặt hạn chót (dueDate == null) xuống cuối danh sách
    if (dueA == null) return 1;
    if (dueB == null) return -1;
    int cmp = Long.compare(dueA, dueB) * directionMultiplier;
    return cmp != 0 ? cmp : Integer.compare(b.getPriority(), a.getPriority());
    }
    case PRIORITY: {
    int cmp = Integer.compare(b.getPriority(), a.getPriority()) * directionMultiplier;
    return cmp != 0 ? cmp : Long.compare(b.getCreatedAt(), a.getCreatedAt());
    }
    case CREATED_DATE:
    default:
    return Long.compare(b.getCreatedAt(), a.getCreatedAt()) * directionMultiplier;
    }
    };

    filtered.sort(comparator);
    return filtered;
    }
    }
