package com.vinhnt.todonotes.ui.notes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.databinding.FragmentNotesBinding;

/**
 * Fragment hiển thị tab Ghi chú nhanh với giao diện thẻ 6 màu, hỗ trợ ghim, tìm kiếm, đổi bố cục Lưới 2 cột/1 cột và Hoàn tác xóa.
 */
public class NotesFragment extends Fragment implements NoteAdapter.OnNoteClickListener {

    private FragmentNotesBinding binding;
    private NotesViewModel viewModel;
    private NoteAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        setupRecyclerView();
        setupSearchInput();
        observeViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new NoteAdapter(this);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        binding.rvNotes.setLayoutManager(layoutManager);
        binding.rvNotes.setAdapter(adapter);
    }

    private void setupSearchInput() {
        binding.etSearchNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (viewModel != null) {
                    viewModel.setSearchQuery(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void observeViewModel() {
        viewModel.getIsGridView().observe(getViewLifecycleOwner(), isGrid -> {
            int spanCount = Boolean.TRUE.equals(isGrid) ? 2 : 1;
            layoutManager.setSpanCount(spanCount);
            layoutManager.invalidateSpanAssignments();
        });

        viewModel.getNotesList().observe(getViewLifecycleOwner(), notes -> {
            if (notes == null || notes.isEmpty()) {
                binding.rvNotes.setVisibility(View.GONE);
                binding.layoutEmptyNotes.setVisibility(View.VISIBLE);
            } else {
                binding.rvNotes.setVisibility(View.VISIBLE);
                binding.layoutEmptyNotes.setVisibility(View.GONE);
                adapter.submitList(notes, () -> {
                    if (layoutManager != null) {
                        layoutManager.invalidateSpanAssignments();
                    }
                });
            }
        });
    }

    public void toggleLayoutMode() {
        if (viewModel != null) {
            viewModel.toggleLayoutMode();
        }
    }

    public boolean isGridView() {
        return viewModel != null && Boolean.TRUE.equals(viewModel.getIsGridView().getValue());
    }

    @Override
    public void onNoteClick(@NonNull NoteEntity note) {
        if (getContext() != null) {
            startActivity(AddEditNoteActivity.newIntentForEdit(getContext(), note.getId()));
        }
    }

    @Override
    public void onPinClick(@NonNull NoteEntity note) {
        if (viewModel != null) {
            viewModel.toggleNotePinned(note);
        }
    }

    @Override
    public void onDeleteClick(@NonNull NoteEntity note) {
        if (getContext() == null) {
            return;
        }

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.dialog_delete_note_title)
                .setMessage(R.string.dialog_delete_note_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> performDelete(note))
                .show();
    }

    private void performDelete(@NonNull NoteEntity note) {
        if (viewModel == null || binding == null) {
            return;
        }

        viewModel.deleteNoteWithUndo(note, result -> {
            if (binding != null && isAdded()) {
                Snackbar.make(binding.getRoot(), R.string.msg_note_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_undo, v -> viewModel.undoDeleteNote(null))
                        .show();
            }
        });
    }
}