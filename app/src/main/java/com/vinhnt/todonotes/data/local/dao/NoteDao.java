package com.vinhnt.todonotes.data.local.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vinhnt.todonotes.data.local.entity.NoteEntity;

import java.util.List;

/**
* Giao diện truy xuất dữ liệu (DAO) cho bảng "notes".
  */
  @Dao
  public interface NoteDao {

  @Query("SELECT * FROM notes ORDER BY is_pinned DESC, updated_at DESC")
  LiveData<List<NoteEntity>> observeAllNotes();

  @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
  LiveData<NoteEntity> observeNoteById(long noteId);

  @Nullable
  @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
  NoteEntity getNoteByIdSync(long noteId);

  @Query("SELECT * FROM notes WHERE title LIKE '%' || :keyword || '%' COLLATE NOCASE " +
  "OR content LIKE '%' || :keyword || '%' COLLATE NOCASE " +
  "ORDER BY is_pinned DESC, updated_at DESC")
  LiveData<List<NoteEntity>> searchNotes(@NonNull String keyword);

  /**
  * Lấy toàn bộ ghi chú đồng bộ trên luồng nền phục vụ chức năng Sao lưu JSON (Backup).
    */
    @Query("SELECT * FROM notes ORDER BY created_at ASC")
    List<NoteEntity> getAllNotesSync();

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insertNote(@NonNull NoteEntity note);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertNotes(@NonNull List<NoteEntity> notes);

  @Update
  int updateNote(@NonNull NoteEntity note);

  @Delete
  int deleteNote(@NonNull NoteEntity note);

  @Query("DELETE FROM notes WHERE id = :noteId")
  int deleteNoteById(long noteId);

  @Query("UPDATE notes SET is_pinned = :pinned, updated_at = :updatedAt WHERE id = :noteId")
  int updatePinnedStatus(long noteId, boolean pinned, long updatedAt);
  }
