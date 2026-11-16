package com.vinhnt.todonotes.ui.notes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.data.repository.NoteRepository;

/**
 * ViewModel quản lý logic kiểm tra hợp lệ, tải và lưu trữ dữ liệu Ghi chú nhanh.
 */
public class AddEditNoteViewModel extends AndroidViewModel {

    public interface OnSaveCallback {
        void onSaved(long noteId);
    }

    private final NoteRepository repository;

    public AddEditNoteViewModel(@NonNull Application application) {
        super(application);
        this.repository = NoteRepository.getInstance(application);
    }

    @NonNull
    public LiveData<NoteEntity> getNoteById(long noteId) {
        return repository.observeNoteById(noteId);
    }

    public boolean isValidNote(@NonNull String title, @NonNull String content) {
        String cleanTitle = title.trim();
        String cleanContent = content.trim();
        if (cleanTitle.isEmpty() && cleanContent.isEmpty()) {
            return false;
        }
        return cleanTitle.length() <= 200 && cleanContent.length() <= 20000;
    }

    public void saveNote(
            long noteId,
            @NonNull String title,
            @NonNull String content,
            int colorIndex,
            boolean isPinned,
            @Nullable OnSaveCallback callback
    ) {
        String cleanTitle = title.trim();
        String cleanContent = content.trim();

        if (noteId > 0L) {
            repository.getNoteById(noteId, existing -> {
                long createdAt = (existing != null && existing.getCreatedAt() > 0L)
                        ? existing.getCreatedAt()
                        : System.currentTimeMillis();

                NoteEntity noteToUpdate = new NoteEntity(
                        cleanTitle,
                        cleanContent,
                        colorIndex,
                        isPinned,
                        createdAt,
                        System.currentTimeMillis()
                );
                noteToUpdate.setId(noteId);

                repository.updateNote(noteToUpdate, rows -> {
                    if (callback != null) {
                        callback.onSaved(noteId);
                    }
                });
            });
        } else {
            long now = System.currentTimeMillis();
            NoteEntity newNote = new NoteEntity(
                    cleanTitle,
                    cleanContent,
                    colorIndex,
                    isPinned,
                    now,
                    now
            );

            repository.insertNote(newNote, insertedId -> {
                if (callback != null) {
                    callback.onSaved(insertedId);
                }
            });
        }
    }

    public void deleteNote(@NonNull NoteEntity note, @Nullable NoteRepository.Callback<Void> callback) {
        repository.deleteNote(note, callback);
    }
}