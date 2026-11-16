package com.vinhnt.todonotes.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.vinhnt.todonotes.data.local.dao.NoteDao;
import com.vinhnt.todonotes.data.local.dao.TaskDao;
import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.model.Priority;
import com.vinhnt.todonotes.data.model.TaskCategory;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
* Bộ kiểm thử Instrumentation Test cho TaskDao và NoteDao trên cơ sở dữ liệu Room in-memory.
  */
  @RunWith(AndroidJUnit4.class)
  public class AppDatabaseDaoTest {

  @Rule
  public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  private AppDatabase database;
  private TaskDao taskDao;
  private NoteDao noteDao;

  @Before
  public void setUp() {
  Context context = ApplicationProvider.getApplicationContext();
  database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
  .allowMainThreadQueries()
  .build();
  taskDao = database.taskDao();
  noteDao = database.noteDao();
  }

  @After
  public void tearDown() {
  database.close();
  }

  @Test
  public void taskDao_insertUpdateCompleteAndDelete_worksCorrectly() throws InterruptedException {
  long now = 1_763_200_000_000L;
  TaskEntity task = new TaskEntity(
  "Ôn tập Lập trình Android",
  "Chuẩn bị báo cáo Use Case 3.1 - 3.15",
  now + 86_400_000L,
  now + 3_600_000L,
  Priority.HIGH.getValue(),
  TaskCategory.STUDY.getValue(),
  "[{\"title\":\"Viết DAO\",\"done\":true}]",
  false,
  now,
  now
  );

       long id = taskDao.insertTask(task);
       assertTrue(id > 0L);

       TaskEntity loaded = taskDao.getTaskByIdSync(id);
       assertNotNull(loaded);
       assertEquals("Ôn tập Lập trình Android", loaded.getTitle());
       assertFalse(loaded.isCompleted());

       // Kiểm tra truy vấn nhắc nhở tương lai (UC 3.15)
       List<TaskEntity> activeReminders = taskDao.getActiveTasksWithFutureRemindersSync(now);
       assertEquals(1, activeReminders.size());

       // Đánh dấu hoàn thành công việc (UC 3.5) -> không còn nằm trong danh sách nhắc nhở chưa hoàn thành
       long completedTime = now + 1000L;
       taskDao.updateCompletionStatus(id, true, completedTime, completedTime);
       TaskEntity afterCompleted = taskDao.getTaskByIdSync(id);
       assertNotNull(afterCompleted);
       assertTrue(afterCompleted.isCompleted());
       assertEquals(Long.valueOf(completedTime), afterCompleted.getCompletedAt());
       assertTrue(taskDao.getActiveTasksWithFutureRemindersSync(now).isEmpty());

       // Xóa công việc (UC 3.4) và kiểm tra hoàn tác (Undo)
       TaskEntity backupCopy = afterCompleted.copy();
       taskDao.deleteTaskById(id);
       assertNull(taskDao.getTaskByIdSync(id));

       taskDao.insertTask(backupCopy);
       List<TaskEntity> allTasks = getOrAwaitValue(taskDao.observeAllTasks());
       assertEquals(1, allTasks.size());
       assertEquals(id, allTasks.get(0).getId());
  }

  @Test
  public void noteDao_insertPinAndSearch_ordersPinnedFirst() throws InterruptedException {
  long now = 1_763_200_000_000L;
  NoteEntity normalNote = new NoteEntity(
  "Danh sách mua sắm",
  "Mua cà phê và sổ tay",
  1,
  false,
  now,
  now + 5000L
  );
  NoteEntity pinnedNote = new NoteEntity(
  "Ý tưởng đồ án Android",
  "Kết hợp quản lý công việc với ghi chú nhanh",
  3,
  true,
  now,
  now + 1000L
  );

       noteDao.insertNote(normalNote);
       noteDao.insertNote(pinnedNote);

       List<NoteEntity> allNotes = getOrAwaitValue(noteDao.observeAllNotes());
       assertEquals(2, allNotes.size());
       // Dù normalNote có updatedAt mới hơn, pinnedNote (is_pinned = true) vẫn phải đứng đầu
       assertEquals("Ý tưởng đồ án Android", allNotes.get(0).getTitle());

       List<NoteEntity> searchResult = getOrAwaitValue(noteDao.searchNotes("ghi chú nhanh"));
       assertEquals(1, searchResult.size());
       assertEquals("Ý tưởng đồ án Android", searchResult.get(0).getTitle());
  }

  private static <T> T getOrAwaitValue(final LiveData<T> liveData) throws InterruptedException {
  final AtomicReference<T> data = new AtomicReference<>();
  final CountDownLatch latch = new CountDownLatch(1);
  Observer<T> observer = new Observer<T>() {
  @Override
  public void onChanged(T value) {
  data.set(value);
  latch.countDown();
  liveData.removeObserver(this);
  }
  };
  liveData.observeForever(observer);
  if (!latch.await(2, TimeUnit.SECONDS)) {
  throw new RuntimeException("LiveData value was never set.");
  }
  return data.get();
  }
  }
