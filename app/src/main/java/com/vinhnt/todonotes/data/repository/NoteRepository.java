package com.vinhnt.todonotes.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.vinhnt.todonotes.data.local.AppDatabase;
import com.vinhnt.todonotes.data.local.dao.NoteDao;
import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.util.AppExecutors;

import java.util.List;

/**
* Lớp trung gian điều phối truy xuất dữ liệu ghi chú nhanh giữa ViewModel và NoteDao trên luồng nền AppExecutors.
  */
  public class NoteRepository {

  public interface Callback<T> {
  void onComplete(T result);
  }

  private static final Object LOCK = new Object();
  private static volatile NoteRepository instance;

  private final NoteDao noteDao;
  private final AppExecutors executors;

  public NoteRepository(@NonNull NoteDao noteDao, @NonNull AppExecutors executors) {
  this.noteDao = noteDao;
  this.executors = executors;
  }

  @NonNull
  public static NoteRepository getInstance(@NonNull Context context) {
  if (instance == null) {
  synchronized (LOCK) {
  if (instance == null) {
  AppDatabase db = AppDatabase.getInstance(context);
  instance = new NoteRepository(db.noteDao(), AppExecutors.getInstance());
  }
  }
  }
  return instance;
  }

  @NonNull
  public LiveData<List<NoteEntity>> observeAllNotes() {
  return noteDao.observeAllNotes();
  }

  @NonNull
  public LiveData<NoteEntity> observeNoteById(long noteId) {
  return noteDao.observeNoteById(noteId);
  }

  @NonNull
  public LiveData<List<NoteEntity>> searchNotes(@NonNull String keyword) {
  return noteDao.searchNotes(keyword.trim());
  }

  public void getNoteById(long noteId, @NonNull Callback<NoteEntity> callback) {
  executors.diskIO().execute(() -> {
  NoteEntity note = noteDao.getNoteByIdSync(noteId);
  executors.mainThread().execute(() -> callback.onComplete(note));
  });
  }

  public void insertNote(@NonNull NoteEntity note, @Nullable Callback<Long> callback) {
  executors.diskIO().execute(() -> {
  long now = System.currentTimeMillis();
  if (note.getCreatedAt() <= 0L) {
  note.setCreatedAt(now);
  }
  note.setUpdatedAt(now);
  long rowId = noteDao.insertNote(note);
  note.setId(rowId);
  if (callback != null) {
  executors.mainThread().execute(() -> callback.onComplete(rowId));
  }
  });
  }

  public void updateNote(@NonNull NoteEntity note, @Nullable Callback<Integer> callback) {
  executors.diskIO().execute(() -> {
  note.setUpdatedAt(System.currentTimeMillis());
  int rows = noteDao.updateNote(note);
  if (callback != null) {
  executors.mainThread().execute(() -> callback.onComplete(rows));
  }
  });
  }

  public void toggleNotePinned(@NonNull NoteEntity note, boolean isPinned, @Nullable Callback<Void> callback) {
  executors.diskIO().execute(() -> {
  long now = System.currentTimeMillis();
  noteDao.updatePinnedStatus(note.getId(), isPinned, now);
  if (callback != null) {
  executors.mainThread().execute(() -> callback.onComplete(null));
  }
  });
  }

  public void deleteNote(@NonNull NoteEntity note, @Nullable Callback<Void> callback) {
  executors.diskIO().execute(() -> {
  noteDao.deleteNote(note);
  if (callback != null) {
  executors.mainThread().execute(() -> callback.onComplete(null));
  }
  });
  }

  /**
  * Chèn lại bản ghi ghi chú vừa xóa khi người dùng bấm "Hoàn tác" (Undo - BR-05).
    */
    public void restoreDeletedNote(@NonNull NoteEntity deletedNote, @Nullable Callback<Long> callback) {
    executors.diskIO().execute(() -> {
    long rowId = noteDao.insertNote(deletedNote);
    if (callback != null) {
    executors.mainThread().execute(() -> callback.onComplete(rowId));
    }
    });
    }
    }
