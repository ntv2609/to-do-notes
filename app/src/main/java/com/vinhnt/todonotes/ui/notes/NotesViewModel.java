package com.vinhnt.todonotes.ui.notes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.data.repository.NoteRepository;
import com.vinhnt.todonotes.util.PreferencesManager;

import java.util.List;

/**
 * ViewModel quản lý dữ liệu danh sách Ghi chú nhanh, tìm kiếm từ khóa, đổi bố cục Lưới/Danh sách và Hoàn tác xóa.
 */
public class NotesViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    private final PreferencesManager preferencesManager;

    private final MutableLiveData<String> searchQueryLiveData = new MutableLiveData<>("");
    private final MediatorLiveData<List<NoteEntity>> notesListLiveData = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> isGridViewLiveData = new MutableLiveData<>(false);

    private LiveData<List<NoteEntity>> currentSource = null;
    @Nullable
    private NoteEntity recentlyDeletedNote = null;

    public NotesViewModel(@NonNull Application application) {
        super(application);
        this.repository = NoteRepository.getInstance(application);
        this.preferencesManager = PreferencesManager.getInstance(application);
        this.isGridViewLiveData.setValue(preferencesManager.isNoteGridMode());

        notesListLiveData.addSource(searchQueryLiveData, query -> updateSource(query));
        updateSource("");
    }

    private void updateSource(@NonNull String query) {
        if (currentSource != null) {
            notesListLiveData.removeSource(currentSource);
        }
        if (query.trim().isEmpty()) {
            currentSource = repository.observeAllNotes();
        } else {
            currentSource = repository.searchNotes(query.trim());
        }
        notesListLiveData.addSource(currentSource, notes -> notesListLiveData.setValue(notes));
    }

    @NonNull
    public LiveData<List<NoteEntity>> getNotesList() {
        return notesListLiveData;
    }

    @NonNull
    public LiveData<Boolean> getIsGridView() {
        return isGridViewLiveData;
    }

    public void setSearchQuery(@NonNull String query) {
        searchQueryLiveData.setValue(query);
    }

    public void toggleLayoutMode() {
        boolean newValue = !Boolean.TRUE.equals(isGridViewLiveData.getValue());
        isGridViewLiveData.setValue(newValue);
        preferencesManager.setNoteGridMode(newValue);
    }

    public void toggleNotePinned(@NonNull NoteEntity note) {
        repository.toggleNotePinned(note, !note.isPinned(), null);
    }

    public void deleteNoteWithUndo(@NonNull NoteEntity note, @Nullable NoteRepository.Callback<Void> callback) {
        this.recentlyDeletedNote = note.copy();
        repository.deleteNote(note, callback);
    }

    public void undoDeleteNote(@Nullable NoteRepository.Callback<Long> callback) {
        if (recentlyDeletedNote != null) {
            repository.restoreDeletedNote(recentlyDeletedNote, callback);
            recentlyDeletedNote = null;
        }
    }
}