package com.vinhnt.todonotes.ui.tasks;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.model.TaskCategory;
import com.vinhnt.todonotes.data.model.TaskFilterType;
import com.vinhnt.todonotes.data.model.TaskSortOption;
import com.vinhnt.todonotes.databinding.FragmentTasksBinding;

import java.util.List;

/**
* Fragment quản lý danh sách Công việc hằng ngày (UC 3.1, 3.4, 3.5, 3.11, 3.12).
  */
  public class TasksFragment extends Fragment implements TaskAdapter.OnTaskInteractionListener {

  private FragmentTasksBinding binding;
  private TasksViewModel viewModel;
  private TaskAdapter taskAdapter;

  @Nullable
  @Override
  public View onCreateView(
  @NonNull LayoutInflater inflater,
  @Nullable ViewGroup container,
  @Nullable Bundle savedInstanceState
  ) {
  binding = FragmentTasksBinding.inflate(inflater, container, false);
  return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
  super.onViewCreated(view, savedInstanceState);
  viewModel = new ViewModelProvider(this).get(TasksViewModel.class);

       setupRecyclerView();
       setupFilterChips();
       setupSearchInput();
       observeViewModel();
  }

  private void setupRecyclerView() {
  taskAdapter = new TaskAdapter(this);
  binding.rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
  binding.rvTasks.setHasFixedSize(false);
  binding.rvTasks.setAdapter(taskAdapter);
  }

  private void setupFilterChips() {
  binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
  if (checkedIds.isEmpty()) {
  return;
  }
  int checkedId = checkedIds.get(0);
  if (checkedId == R.id.chip_status_active) {
  viewModel.setFilterType(TaskFilterType.ACTIVE);
  } else if (checkedId == R.id.chip_status_completed) {
  viewModel.setFilterType(TaskFilterType.COMPLETED);
  } else if (checkedId == R.id.chip_status_overdue) {
  viewModel.setFilterType(TaskFilterType.OVERDUE);
  } else if (checkedId == R.id.chip_status_today) {
  viewModel.setFilterType(TaskFilterType.TODAY);
  } else {
  viewModel.setFilterType(TaskFilterType.ALL);
  }
  });

       binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
           if (checkedIds.isEmpty()) {
               return;
           }
           int checkedId = checkedIds.get(0);
           if (checkedId == R.id.chip_cat_personal) {
               viewModel.setCategoryFilter(TaskCategory.PERSONAL.getValue());
           } else if (checkedId == R.id.chip_cat_study) {
               viewModel.setCategoryFilter(TaskCategory.STUDY.getValue());
           } else if (checkedId == R.id.chip_cat_work) {
               viewModel.setCategoryFilter(TaskCategory.WORK.getValue());
           } else if (checkedId == R.id.chip_cat_shopping) {
               viewModel.setCategoryFilter(TaskCategory.SHOPPING.getValue());
           } else if (checkedId == R.id.chip_cat_other) {
               viewModel.setCategoryFilter(TaskCategory.OTHER.getValue());
           } else {
               viewModel.setCategoryFilter(-1);
           }
       });
  }

  private void setupSearchInput() {
  binding.etSearchTasks.addTextChangedListener(new TextWatcher() {
  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               viewModel.setSearchQuery(s != null ? s.toString() : "");
           }

           @Override
           public void afterTextChanged(Editable s) {
           }
       });
  }

  private void observeViewModel() {
  viewModel.getProgressStats().observe(getViewLifecycleOwner(), stats -> {
  if (stats == null || binding == null) {
  return;
  }
  binding.tvProgressSummary.setText(getString(
  R.string.progress_summary_format,
  stats.completedCount,
  stats.totalCount,
  stats.overdueCount
  ));
  binding.tvProgressPercent.setText(getString(
  R.string.progress_percentage_format,
  stats.percentage
  ));
  binding.progressBarTasks.setProgressCompat(stats.percentage, true);
  });

       viewModel.getDisplayedTasks().observe(getViewLifecycleOwner(), this::renderTaskList);
  }

  private void renderTaskList(@Nullable List<TaskEntity> tasks) {
  if (binding == null) {
  return;
  }
  taskAdapter.submitList(tasks);
  boolean isEmpty = tasks == null || tasks.isEmpty();
  binding.layoutEmptyTasks.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
  binding.rvTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

       if (isEmpty) {
           if (viewModel.hasActiveFilterOrSearch()) {
               binding.tvEmptyTasksTitle.setText(R.string.empty_tasks_filter_title);
               binding.tvEmptyTasksSubtitle.setText(R.string.empty_tasks_filter_subtitle);
           } else {
               binding.tvEmptyTasksTitle.setText(R.string.empty_tasks_title);
               binding.tvEmptyTasksSubtitle.setText(R.string.empty_tasks_subtitle);
           }
       }
  }

  /**
  * Hiển thị hộp thoại Sắp xếp công việc kèm tùy chọn đảo chiều (UC 3.12 & Điểm mở rộng).
    */
    public void showSortOptionsDialog() {
    if (!isAdded()) {
    return;
    }
    String[] options = new String[]{
    getString(R.string.sort_by_created_date),
    getString(R.string.sort_by_due_date),
    getString(R.string.sort_by_priority)
    };
    int currentSelection = viewModel.getCurrentSortOption().getPrefValue();

    new MaterialAlertDialogBuilder(requireContext())
    .setTitle(R.string.sort_dialog_title)
    .setSingleChoiceItems(options, currentSelection, (dialog, which) -> {
    TaskSortOption chosen = TaskSortOption.fromPrefValue(which);
    viewModel.setSortOption(chosen);
    dialog.dismiss();
    })
    .setNeutralButton(R.string.action_sort, (dialog, which) -> viewModel.toggleSortDirection())
    .setNegativeButton(R.string.action_cancel, null)
    .show();
    }

  /**
  * Mở màn hình Thêm công việc mới đầy đủ (UC 3.2).
    */
    public void openAddTaskScreen() {
    if (!isAdded()) {
    return;
    }
    startActivity(AddEditTaskActivity.newIntentForAdd(requireContext()));
    }

  @Override
  public void onTaskClicked(@NonNull TaskEntity task) {
  // UC 3.1 bước 3 & UC 3.3: Mở màn hình Xem chi tiết & Chỉnh sửa công việc
  startActivity(AddEditTaskActivity.newIntentForEdit(requireContext(), task.getId()));
  }

  @Override
  public void onTaskCheckChanged(@NonNull TaskEntity task, boolean isCompleted) {
  viewModel.toggleTaskCompleted(task, isCompleted);
  }

  @Override
  public void onTaskDeleteClicked(@NonNull TaskEntity task) {
  // UC 3.4: Bắt buộc có hộp thoại xác nhận trước khi xóa
  TaskEntity backupCopy = task.copy();
  new MaterialAlertDialogBuilder(requireContext())
  .setTitle(R.string.dialog_delete_task_title)
  .setMessage(R.string.dialog_delete_task_message)
  .setNegativeButton(R.string.action_cancel, null)
  .setPositiveButton(R.string.action_delete, (dialog, which) -> {
  viewModel.deleteTask(task);
  if (binding != null) {
  View anchor = requireActivity().findViewById(R.id.fab_add);
  Snackbar snackbar = Snackbar.make(
  binding.getRoot(),
  R.string.msg_task_deleted,
  Snackbar.LENGTH_LONG
  );
  if (anchor != null) {
  snackbar.setAnchorView(anchor);
  }
  snackbar.setAction(R.string.action_undo, v -> viewModel.restoreDeletedTask(backupCopy));
  snackbar.show();
  }
  })
  .show();
  }

  @Override
  public void onDestroyView() {
  super.onDestroyView();
  binding = null;
  }
  }
