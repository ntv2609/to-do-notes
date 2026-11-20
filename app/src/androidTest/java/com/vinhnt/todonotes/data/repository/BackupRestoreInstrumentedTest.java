package com.vinhnt.todonotes.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.vinhnt.todonotes.data.local.AppDatabase;
import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.model.Priority;
import com.vinhnt.todonotes.data.model.TaskCategory;
import com.vinhnt.todonotes.data.repository.BackupRepository.BackupPayload;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

/**
* Kiểm thử Instrumentation Test cho chức năng Sao lưu (Serialize) & Khôi phục (Parse + Import Transaction) JSON.
  */
  @RunWith(AndroidJUnit4.class)
  public class BackupRestoreInstrumentedTest {

  private AppDatabase database;

  @Before
  public void setUp() {
  Context context = ApplicationProvider.getApplicationContext();
  database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
  .allowMainThreadQueries()
  .build();
  }

  @After
  public void tearDown() {
  database.close();
  }

  @Test
  public void serializeAndRestoreBackup_roundTripPreservesAllTasksAndNotes() throws JSONException {
  long now = 1_763_700_000_000L;
  TaskEntity task = new TaskEntity(
  "Đóng gói APK nộp đồ án",
  "Kiểm tra đủ 15 Use Case và chụp ảnh báo cáo",
  now + 86_400_000L,
  now + 3_600_000L,
  Priority.HIGH.getValue(),
  TaskCategory.STUDY.getValue(),
  "[{\"title\":\"Chạy testDebugUnitTest\",\"done\":true}]",
  false,
  now,
  now
  );
  task.setId(5L);

       NoteEntity note = new NoteEntity(
               "Ghi chú bảo vệ đồ án",
               "Demo tạo việc, nhắc nhở, chuyển ghi chú thành việc và xuất JSON",
               4,
               true,
               now,
               now
       );
       note.setId(9L);

       String json = BackupRepository.serializeToJson(
               Collections.singletonList(task),
               Collections.singletonList(note),
               now
       );

       BackupPayload parsed = BackupRepository.parseFromJson(json);
       BackupRepository.importPayloadSync(database, parsed);

       List<TaskEntity> restoredTasks = database.taskDao().getAllTasksSync();
       List<NoteEntity> restoredNotes = database.noteDao().getAllNotesSync();

       assertEquals(1, restoredTasks.size());
       assertEquals(task, restoredTasks.get(0));

       assertEquals(1, restoredNotes.size());
       assertEquals(note, restoredNotes.get(0));
  }

  @Test
  public void parseFromJson_corruptedOrMissingArrays_throwsJsonException() {
  try {
  BackupRepository.parseFromJson("{\"app\":\"TodoNotes\"}");
  fail("Expected JSONException when required arrays are missing");
  } catch (JSONException expected) {
  assertTrue(expected.getMessage() != null && !expected.getMessage().isEmpty());
  }
  }
  }
