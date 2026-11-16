package com.vinhnt.todonotes.ui.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.model.Priority;
import com.vinhnt.todonotes.data.model.TaskCategory;
import com.vinhnt.todonotes.data.model.TaskFilterType;
import com.vinhnt.todonotes.data.model.TaskSortOption;
import com.vinhnt.todonotes.ui.tasks.TasksViewModel.TaskProgressStats;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

/**
* Kiểm thử đơn vị (JUnit 4) cho logic lọc đa tiêu chí (UC 3.11), sắp xếp (UC 3.12)
* và tính toán thống kê tiến độ (UC 3.1) trong TasksViewModel.
  */
  public class TaskFilterSortLogicTest {

  private static final ZoneId ZONE_HANOI = ZoneId.of("Asia/Ho_Chi_Minh");
  private static final long NOW = 1_763_280_000_000L; // Mốc tham chiếu cố định

  private TaskEntity taskOverdueHighStudy;
  private TaskEntity taskTodayLowPersonal;
  private TaskEntity taskNoDueMediumWork;
  private TaskEntity taskCompletedHighStudy;

  private List<TaskEntity> sampleTasks;

  @Before
  public void setUp() {
  taskOverdueHighStudy = createTask(
  1L,
  "Nộp báo cáo Use Case",
  "Môn Lập trình Android",
  NOW - 3_600_000L, // Quá hạn 1 giờ trước (cùng ngày hôm nay)
  Priority.HIGH.getValue(),
  TaskCategory.STUDY.getValue(),
  false,
  NOW - 10_000L
  );

       taskTodayLowPersonal = createTask(
               2L,
               "Tập thể dục buổi chiều",
               "Chạy bộ 3km",
               NOW + 7_200_000L, // Còn hạn trong ngày hôm nay (+2 giờ)
               Priority.LOW.getValue(),
               TaskCategory.PERSONAL.getValue(),
               false,
               NOW - 5_000L
       );

       taskNoDueMediumWork = createTask(
               3L,
               "Dọn dẹp email công việc",
               "Phân loại hộp thư đến",
               null, // Không đặt hạn chót
               Priority.MEDIUM.getValue(),
               TaskCategory.WORK.getValue(),
               false,
               NOW - 1_000L
       );

       taskCompletedHighStudy = createTask(
               4L,
               "Cài đặt Android Studio Quail 4",
               "Cấu hình máy ảo Pixel 7",
               NOW - 86_400_000L,
               Priority.HIGH.getValue(),
               TaskCategory.STUDY.getValue(),
               true, // Đã hoàn thành
               NOW - 20_000L
       );

       sampleTasks = Arrays.asList(
               taskOverdueHighStudy,
               taskTodayLowPersonal,
               taskNoDueMediumWork,
               taskCompletedHighStudy
       );
  }

  @Test
  public void computeProgressStats_countsTotalCompletedAndOverdueCorrectly() {
  TaskProgressStats stats = TasksViewModel.computeProgressStats(sampleTasks, NOW);
  assertEquals(4, stats.totalCount);
  assertEquals(1, stats.completedCount);
  // Chỉ công việc chưa hoàn thành và dueDate < NOW mới tính là quá hạn
  assertEquals(1, stats.overdueCount);
  assertEquals(25, stats.percentage);
  }

  @Test
  public void filterByStatus_activeCompletedAndOverdue_returnsExpectedTasks() {
  List<TaskEntity> active = TasksViewModel.filterAndSortTasks(
  sampleTasks, TaskFilterType.ACTIVE, -1, "", TaskSortOption.CREATED_DATE, NOW, ZONE_HANOI
  );
  assertEquals(3, active.size());

       List<TaskEntity> completed = TasksViewModel.filterAndSortTasks(
               sampleTasks, TaskFilterType.COMPLETED, -1, "", TaskSortOption.CREATED_DATE, NOW, ZONE_HANOI
       );
       assertEquals(1, completed.size());
       assertEquals(4L, completed.get(0).getId());

       List<TaskEntity> overdue = TasksViewModel.filterAndSortTasks(
               sampleTasks, TaskFilterType.OVERDUE, -1, "", TaskSortOption.CREATED_DATE, NOW, ZONE_HANOI
       );
       assertEquals(1, overdue.size());
       assertEquals(1L, overdue.get(0).getId());
  }

  @Test
  public void filterByCategoryAndKeyword_combinesBothCriteria() {
  List<TaskEntity> studyAndroid = TasksViewModel.filterAndSortTasks(
  sampleTasks,
  TaskFilterType.ALL,
  TaskCategory.STUDY.getValue(),
  "pixel 7",
  TaskSortOption.CREATED_DATE,
  NOW,
  ZONE_HANOI
  );
  assertEquals(1, studyAndroid.size());
  assertEquals(4L, studyAndroid.get(0).getId());
  }

  @Test
  public void sortByDueDate_placesNullDueDatesAtBottomOfActiveTasks() {
  List<TaskEntity> sorted = TasksViewModel.filterAndSortTasks(
  sampleTasks,
  TaskFilterType.ALL,
  -1,
  "",
  TaskSortOption.DUE_DATE,
  false,
  NOW,
  ZONE_HANOI
  );

       assertEquals(4, sorted.size());
       // Thứ tự kỳ vọng:
       // 1. taskOverdueHighStudy (chưa xong, hạn sớm nhất)
       // 2. taskTodayLowPersonal (chưa xong, hạn muộn hơn)
       // 3. taskNoDueMediumWork (chưa xong, dueDate == null luôn nằm cuối nhóm chưa xong)
       // 4. taskCompletedHighStudy (đã hoàn thành luôn nằm dưới cùng)
       assertEquals(1L, sorted.get(0).getId());
       assertEquals(2L, sorted.get(1).getId());
       assertEquals(3L, sorted.get(2).getId());
       assertEquals(4L, sorted.get(3).getId());
  }

  @Test
  public void sortByPriority_defaultAndReversed_ordersCorrectly() {
  List<TaskEntity> highToLow = TasksViewModel.filterAndSortTasks(
  sampleTasks,
  TaskFilterType.ACTIVE,
  -1,
  "",
  TaskSortOption.PRIORITY,
  false,
  NOW,
  ZONE_HANOI
  );
  // HIGH (id=1) -> MEDIUM (id=3) -> LOW (id=2)
  assertEquals(1L, highToLow.get(0).getId());
  assertEquals(3L, highToLow.get(1).getId());
  assertEquals(2L, highToLow.get(2).getId());

       List<TaskEntity> lowToHigh = TasksViewModel.filterAndSortTasks(
               sampleTasks,
               TaskFilterType.ACTIVE,
               -1,
               "",
               TaskSortOption.PRIORITY,
               true,
               NOW,
               ZONE_HANOI
       );
       // LOW (id=2) -> MEDIUM (id=3) -> HIGH (id=1)
       assertEquals(2L, lowToHigh.get(0).getId());
       assertEquals(3L, lowToHigh.get(1).getId());
       assertEquals(1L, lowToHigh.get(2).getId());
  }

  @Test
  public void filterWithNoMatchingKeyword_returnsEmptyList() {
  List<TaskEntity> empty = TasksViewModel.filterAndSortTasks(
  sampleTasks,
  TaskFilterType.ALL,
  -1,
  "tu_khoa_khong_ton_tai_999",
  TaskSortOption.CREATED_DATE,
  NOW,
  ZONE_HANOI
  );
  assertTrue(empty.isEmpty());
  }

  private static TaskEntity createTask(
  long id,
  String title,
  String description,
  Long dueDate,
  int priority,
  int category,
  boolean completed,
  long createdAt
  ) {
  TaskEntity t = new TaskEntity(
  title,
  description,
  dueDate,
  null,
  priority,
  category,
  "[]",
  completed,
  createdAt,
  createdAt
  );
  t.setId(id);
  return t;
  }
  }
