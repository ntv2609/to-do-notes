package com.vinhnt.todonotes.ui.search;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.repository.NoteRepository;
import com.vinhnt.todonotes.data.repository.TaskRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
* ViewModel phục vụ màn hình Tìm kiếm tổng hợp cả Công việc lẫn Ghi chú nhanh (UC 3.10).
  */
  public class SearchViewModel extends AndroidViewModel {

  public enum SearchScope {
  ALL,
  TASKS_ONLY,
  NOTES_ONLY
  }

  public static class SearchResultsState {
  @NonNull
  public final List<TaskEntity> tasks;
  @NonNull
  public final List<NoteEntity> notes;
  @NonNull
  public final SearchScope scope;

       public SearchResultsState(
               @NonNull List<TaskEntity> tasks,
               @NonNull List<NoteEntity> notes,
               @NonNull SearchScope scope
       ) {
           this.tasks = tasks;
           this.notes = notes;
           this.scope = scope;
       }

       public boolean isEmpty() {
           return tasks.isEmpty() && notes.isEmpty();
       }
  }

  private final TaskRepository taskRepository;
  private final NoteRepository noteRepository;

  private final LiveData<List<TaskEntity>> allTasksSource;
  private final LiveData<List<NoteEntity>> allNotesSource;
  private final MutableLiveData<String> queryLiveData = new MutableLiveData<>("");
  private final MutableLiveData<SearchScope> scopeLiveData = new MutableLiveData<>(SearchScope.ALL);

  private final MediatorLiveData<SearchResultsState> searchResultsLiveData = new MediatorLiveData<>();

  public SearchViewModel(@NonNull Application application) {
  super(application);
  this.taskRepository = TaskRepository.getInstance(application);
  this.noteRepository = NoteRepository.getInstance(application);

       this.allTasksSource = taskRepository.observeAllTasks();
       this.allNotesSource = noteRepository.observeAllNotes();

       searchResultsLiveData.addSource(allTasksSource, tasks -> recomputeResults());
       searchResultsLiveData.addSource(allNotesSource, notes -> recomputeResults());
       searchResultsLiveData.addSource(queryLiveData, q -> recomputeResults());
       searchResultsLiveData.addSource(scopeLiveData, s -> recomputeResults());
  }

  @NonNull
  public LiveData<SearchResultsState> getSearchResults() {
  return searchResultsLiveData;
  }

  public void setQuery(@Nullable String query) {
  String safe = query == null ? "" : query;
  if (!safe.equals(queryLiveData.getValue())) {
  queryLiveData.setValue(safe);
  }
  }

  public void setScope(@NonNull SearchScope scope) {
  if (scope != scopeLiveData.getValue()) {
  scopeLiveData.setValue(scope);
  }
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

  public void toggleNotePinned(@NonNull NoteEntity note) {
  noteRepository.toggleNotePinned(note, !note.isPinned(), null);
  }

  public void deleteNote(@NonNull NoteEntity note) {
  noteRepository.deleteNote(note, null);
  }

  public void restoreDeletedNote(@NonNull NoteEntity deletedNote) {
  noteRepository.restoreDeletedNote(deletedNote, null);
  }

  private void recomputeResults() {
  SearchScope scope = scopeLiveData.getValue() != null ? scopeLiveData.getValue() : SearchScope.ALL;
  String query = queryLiveData.getValue();

       List<TaskEntity> matchedTasks = (scope == SearchScope.NOTES_ONLY)
               ? Collections.emptyList()
               : filterTasksByKeyword(allTasksSource.getValue(), query);

       List<NoteEntity> matchedNotes = (scope == SearchScope.TASKS_ONLY)
               ? Collections.emptyList()
               : filterNotesByKeyword(allNotesSource.getValue(), query);

       searchResultsLiveData.setValue(new SearchResultsState(matchedTasks, matchedNotes, scope));
  }

  /**
  * Lọc danh sách công việc theo từ khóa (khớp tiêu đề hoặc mô tả, không phân biệt chữ hoa/thường).
    */
    @NonNull
    public static List<TaskEntity> filterTasksByKeyword(
    @Nullable List<TaskEntity> source,
    @Nullable String keyword
    ) {
    if (source == null || source.isEmpty()) {
    return Collections.emptyList();
    }
    String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    List<TaskEntity> result = new ArrayList<>();
    for (TaskEntity task : source) {
    if (task == null) {
    continue;
    }
    if (normalized.isEmpty()) {
    result.add(task.copy());
    continue;
    }
    String title = task.getTitle().toLowerCase(Locale.ROOT);
    String desc = task.getDescription().toLowerCase(Locale.ROOT);
    if (title.contains(normalized) || desc.contains(normalized)) {
    result.add(task.copy());
    }
    }
    return result;
    }

  /**
  * Lọc danh sách ghi chú nhanh theo từ khóa (khớp tiêu đề hoặc nội dung, không phân biệt chữ hoa/thường).
    */
    @NonNull
    public static List<NoteEntity> filterNotesByKeyword(
    @Nullable List<NoteEntity> source,
    @Nullable String keyword
    ) {
    if (source == null || source.isEmpty()) {
    return Collections.emptyList();
    }
    String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    List<NoteEntity> result = new ArrayList<>();
    for (NoteEntity note : source) {
    if (note == null) {
    continue;
    }
    if (normalized.isEmpty()) {
    result.add(note.copy());
    continue;
    }
    String title = note.getTitle().toLowerCase(Locale.ROOT);
    String content = note.getContent().toLowerCase(Locale.ROOT);
    if (title.contains(normalized) || content.contains(normalized)) {
    result.add(note.copy());
    }
    }
    return result;
    }
    }
