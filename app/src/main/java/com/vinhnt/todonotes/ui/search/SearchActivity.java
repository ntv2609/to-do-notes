package com.vinhnt.todonotes.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.databinding.ActivitySearchBinding;
import com.vinhnt.todonotes.ui.notes.AddEditNoteActivity;
import com.vinhnt.todonotes.ui.notes.NoteAdapter;
import com.vinhnt.todonotes.ui.search.SearchViewModel.SearchResultsState;
import com.vinhnt.todonotes.ui.search.SearchViewModel.SearchScope;
import com.vinhnt.todonotes.ui.tasks.AddEditTaskActivity;
import com.vinhnt.todonotes.ui.tasks.TaskAdapter;

/**
* Màn hình Tìm kiếm Tổng hợp Công việc & Ghi chú nhanh (UC 3.10).
  */
  public class SearchActivity extends AppCompatActivity
  implements TaskAdapter.OnTaskInteractionListener, NoteAdapter.OnNoteClickListener {

  private ActivitySearchBinding binding;
  private SearchViewModel viewModel;
  private TaskAdapter taskAdapter;
  private NoteAdapter noteAdapter;

  @NonNull
  public static Intent newIntent(@NonNull Context context) {
  return new Intent(context, SearchActivity.class);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  binding = ActivitySearchBinding.inflate(getLayoutInflater());
  setContentView(binding.getRoot());

       viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

       setupToolbar();
       setupRecyclerViews();
       setupSearchInput();
       setupScopeChips();
       observeViewModel();

       if (savedInstanceState == null) {
           focusSearchInput();
       }
  }

  private void setupToolbar() {
  binding.toolbarSearch.setNavigationOnClickListener(v -> finish());
  }

  private void setupRecyclerViews() {
  taskAdapter = new TaskAdapter(this);
  binding.rvSearchTasks.setLayoutManager(new LinearLayoutManager(this));
  binding.rvSearchTasks.setAdapter(taskAdapter);

       noteAdapter = new NoteAdapter(this);
       binding.rvSearchNotes.setLayoutManager(new LinearLayoutManager(this));
       binding.rvSearchNotes.setAdapter(noteAdapter);
  }

  private void setupSearchInput() {
  binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               viewModel.setQuery(s != null ? s.toString() : "");
           }

           @Override
           public void afterTextChanged(Editable s) {
           }
       });
  }

  private void setupScopeChips() {
  binding.chipGroupSearchScope.setOnCheckedStateChangeListener((group, checkedIds) -> {
  if (checkedIds.isEmpty()) {
  return;
  }
  int checkedId = checkedIds.get(0);
  if (checkedId == R.id.chip_scope_tasks) {
  viewModel.setScope(SearchScope.TASKS_ONLY);
  } else if (checkedId == R.id.chip_scope_notes) {
  viewModel.setScope(SearchScope.NOTES_ONLY);
  } else {
  viewModel.setScope(SearchScope.ALL);
  }
  });
  }

  private void observeViewModel() {
  viewModel.getSearchResults().observe(this, this::renderSearchResults);
  }

  private void renderSearchResults(@Nullable SearchResultsState state) {
  if (state == null) {
  return;
  }
  taskAdapter.submitList(state.tasks);
  noteAdapter.submitList(state.notes);

       boolean hasTasks = !state.tasks.isEmpty();
       boolean hasNotes = !state.notes.isEmpty();

       binding.tvSectionTasks.setVisibility(hasTasks ? View.VISIBLE : View.GONE);
       binding.rvSearchTasks.setVisibility(hasTasks ? View.VISIBLE : View.GONE);
       if (hasTasks) {
           binding.tvSectionTasks.setText(getString(R.string.search_section_tasks, state.tasks.size()));
       }

       binding.tvSectionNotes.setVisibility(hasNotes ? View.VISIBLE : View.GONE);
       binding.rvSearchNotes.setVisibility(hasNotes ? View.VISIBLE : View.GONE);
       if (hasNotes) {
           binding.tvSectionNotes.setText(getString(R.string.search_section_notes, state.notes.size()));
       }

       boolean isEmpty = state.isEmpty();
       binding.layoutEmptySearch.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
       binding.scrollSearchResults.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
  }

  private void focusSearchInput() {
  binding.etSearchQuery.requestFocus();
  binding.etSearchQuery.postDelayed(() -> {
  InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
  if (imm != null) {
  imm.showSoftInput(binding.etSearchQuery, InputMethodManager.SHOW_IMPLICIT);
  }
  }, 150L);
  }

  // --- TaskAdapter.OnTaskInteractionListener ---

  @Override
  public void onTaskClicked(@NonNull TaskEntity task) {
  startActivity(AddEditTaskActivity.newIntentForEdit(this, task.getId()));
  }

  @Override
  public void onTaskCheckChanged(@NonNull TaskEntity task, boolean isCompleted) {
  viewModel.toggleTaskCompleted(task, isCompleted);
  }

  @Override
  public void onTaskDeleteClicked(@NonNull TaskEntity task) {
  TaskEntity backup = task.copy();
  new MaterialAlertDialogBuilder(this)
  .setTitle(R.string.dialog_delete_task_title)
  .setMessage(R.string.dialog_delete_task_message)
  .setNegativeButton(R.string.action_cancel, null)
  .setPositiveButton(R.string.action_delete, (dialog, which) -> {
  viewModel.deleteTask(task);
  Snackbar.make(binding.searchRoot, R.string.msg_task_deleted, Snackbar.LENGTH_LONG)
  .setAction(R.string.action_undo, v -> viewModel.restoreDeletedTask(backup))
  .show();
  })
  .show();
  }

  // --- NoteAdapter.OnNoteClickListener ---

  @Override
  public void onNoteClick(@NonNull NoteEntity note) {
  startActivity(AddEditNoteActivity.newIntentForEdit(this, note.getId()));
  }

  @Override
  public void onPinClick(@NonNull NoteEntity note) {
  viewModel.toggleNotePinned(note);
  }

  @Override
  public void onDeleteClick(@NonNull NoteEntity note) {
  NoteEntity backup = note.copy();
  new MaterialAlertDialogBuilder(this)
  .setTitle(R.string.dialog_delete_note_title)
  .setMessage(R.string.dialog_delete_note_message)
  .setNegativeButton(R.string.action_cancel, null)
  .setPositiveButton(R.string.action_delete, (dialog, which) -> {
  viewModel.deleteNote(note);
  Snackbar.make(binding.searchRoot, R.string.msg_note_deleted, Snackbar.LENGTH_LONG)
  .setAction(R.string.action_undo, v -> viewModel.restoreDeletedNote(backup))
  .show();
  })
  .show();
  }
  }
