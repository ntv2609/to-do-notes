package com.vinhnt.todonotes.ui.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.model.Priority;
import com.vinhnt.todonotes.data.model.TaskCategory;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
* Kiểm thử đơn vị (JUnit 4) cho logic tìm kiếm tổng hợp Công việc & Ghi chú nhanh (UC 3.10).
  */
  public class SearchLogicTest {

  private List<TaskEntity> tasks;
  private List<NoteEntity> notes;

  @Before
  public void setUp() {
  TaskEntity t1 = new TaskEntity(
  "Hoàn thiện báo cáo Android",
  "Rà soát 15 Use Case",
  null, null,
  Priority.HIGH.getValue(),
  TaskCategory.STUDY.getValue(),
  "[]", false, 1000L, 1000L
  );
  t1.setId(1L);

       TaskEntity t2 = new TaskEntity(
               "Mua cáp sạc mới",
               "Cáp USB-C chuẩn PD",
               null, null,
               Priority.LOW.getValue(),
               TaskCategory.SHOPPING.getValue(),
               "[]", false, 2000L, 2000L
       );
       t2.setId(2L);
       tasks = Arrays.asList(t1, t2);

       NoteEntity n1 = new NoteEntity(
               "Ghi chú Android Room",
               "Xuất schema 1.json và viết migration",
               2, true, 1000L, 2000L
       );
       n1.setId(10L);

       NoteEntity n2 = new NoteEntity(
               "Thực đơn tuần",
               "Cơm tấm, phở bò",
               1, false, 1000L, 1500L
       );
       n2.setId(11L);
       notes = Arrays.asList(n1, n2);
  }

  @Test
  public void filterTasksAndNotes_emptyKeyword_returnsAllItems() {
  assertEquals(2, SearchViewModel.filterTasksByKeyword(tasks, "").size());
  assertEquals(2, SearchViewModel.filterTasksByKeyword(tasks, "   ").size());
  assertEquals(2, SearchViewModel.filterNotesByKeyword(notes, null).size());
  }

  @Test
  public void filterTasksAndNotes_matchingKeyword_isCaseInsensitiveAcrossTitleAndBody() {
  List<TaskEntity> matchedTasks = SearchViewModel.filterTasksByKeyword(tasks, "use case");
  assertEquals(1, matchedTasks.size());
  assertEquals(1L, matchedTasks.get(0).getId());

       List<NoteEntity> matchedNotes = SearchViewModel.filterNotesByKeyword(notes, "ANDROID");
       assertEquals(1, matchedNotes.size());
       assertEquals(10L, matchedNotes.get(0).getId());
  }

  @Test
  public void filterTasksAndNotes_nonMatchingKeyword_returnsEmptyLists() {
  assertTrue(SearchViewModel.filterTasksByKeyword(tasks, "xyz_khong_khop").isEmpty());
  assertTrue(SearchViewModel.filterNotesByKeyword(notes, "xyz_khong_khop").isEmpty());
  }
  }
