package com.vinhnt.todonotes.ui.tasks;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.data.model.Priority;
import com.vinhnt.todonotes.data.model.SubtaskItem;
import com.vinhnt.todonotes.data.model.TaskCategory;
import com.vinhnt.todonotes.databinding.ItemTaskBinding;
import com.vinhnt.todonotes.util.DateTimeUtils;
import com.vinhnt.todonotes.util.SubtaskJsonHelper;

import java.util.List;

/**
* Adapter hien thi danh sach cong viec voi ListAdapter + DiffUtil.
  */
  public class TaskAdapter extends ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder> {

  public interface OnTaskInteractionListener {
  void onTaskClicked(@NonNull TaskEntity task);

       void onTaskCheckChanged(@NonNull TaskEntity task, boolean isCompleted);

       void onTaskDeleteClicked(@NonNull TaskEntity task);
  }

  private final OnTaskInteractionListener listener;

  public TaskAdapter(@NonNull OnTaskInteractionListener listener) {
  super(DIFF_CALLBACK);
  this.listener = listener;
  }

  private static final DiffUtil.ItemCallback<TaskEntity> DIFF_CALLBACK =
  new DiffUtil.ItemCallback<TaskEntity>() {
  @Override
  public boolean areItemsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
  return oldItem.getId() == newItem.getId();
  }

               @Override
               public boolean areContentsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
                   return oldItem.equals(newItem);
               }
           };

  @NonNull
  @Override
  public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
  ItemTaskBinding binding = ItemTaskBinding.inflate(
  LayoutInflater.from(parent.getContext()),
  parent,
  false
  );
  return new TaskViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
  holder.bind(getItem(position), listener);
  }

  static class TaskViewHolder extends RecyclerView.ViewHolder {

       private final ItemTaskBinding binding;

       TaskViewHolder(@NonNull ItemTaskBinding binding) {
           super(binding.getRoot());
           this.binding = binding;
       }

       void bind(@NonNull TaskEntity task, @NonNull OnTaskInteractionListener listener) {
           Context context = binding.getRoot().getContext();

           // 1. Tieu de va trang thai hoan thanh
           binding.tvTaskTitle.setText(task.getTitle());
           if (task.isCompleted()) {
               binding.tvTaskTitle.setPaintFlags(
                       binding.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
               );
               binding.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.color_text_disabled));
               binding.cardTaskItem.setAlpha(0.72f);
           } else {
               binding.tvTaskTitle.setPaintFlags(
                       binding.tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
               );
               binding.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.color_text_primary));
               binding.cardTaskItem.setAlpha(1.0f);
           }

           // 2. Mo ta ngan
           String desc = task.getDescription().trim();
           if (desc.isEmpty()) {
               binding.tvTaskDescription.setVisibility(View.GONE);
           } else {
               binding.tvTaskDescription.setVisibility(View.VISIBLE);
               binding.tvTaskDescription.setText(desc);
           }

           // 3. Checkbox kem phan hoi rung nhe khi nguoi dung cham
           binding.cbTaskCompleted.setOnCheckedChangeListener(null);
           binding.cbTaskCompleted.setChecked(task.isCompleted());
           binding.cbTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
               if (buttonView.isPressed()) {
                   buttonView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                   listener.onTaskCheckChanged(task, isChecked);
               }
           });

           // 4. Do uu tien dang cham tron nho + chu
           bindPriorityIndicator(context, Priority.fromValue(task.getPriority()));

           // 5. Nhan phan loai trung tinh
           bindCategoryTag(context, TaskCategory.fromValue(task.getCategory()));

           // 6. Tien do cong viec con
           List<SubtaskItem> subtasks = SubtaskJsonHelper.fromJson(task.getSubtasksJson());
           if (subtasks.isEmpty()) {
               binding.tvBadgeSubtasks.setVisibility(View.GONE);
           } else {
               int doneCount = SubtaskJsonHelper.countCompleted(subtasks);
               binding.tvBadgeSubtasks.setVisibility(View.VISIBLE);
               binding.tvBadgeSubtasks.setText(
                       context.getString(R.string.subtask_progress_format, doneCount, subtasks.size())
               );
           }

           // 7. Han chot va bieu tuong nhac nho
           Long dueDate = task.getDueDate();
           Long reminderTime = task.getReminderTime();
           boolean hasDue = dueDate != null && dueDate > 0L;
           boolean hasReminder = reminderTime != null && reminderTime > 0L && !task.isCompleted();

           if (!hasDue && !hasReminder) {
               binding.layoutTaskTime.setVisibility(View.GONE);
           } else {
               binding.layoutTaskTime.setVisibility(View.VISIBLE);
               if (hasDue) {
                   binding.tvTaskDueDate.setVisibility(View.VISIBLE);
                   String formattedDue = DateTimeUtils.formatDateTime(dueDate);
                   boolean overdue = DateTimeUtils.isOverdue(
                           task.isCompleted(),
                           dueDate,
                           System.currentTimeMillis()
                   );
                   if (overdue) {
                       binding.tvTaskDueDate.setText(
                               context.getString(R.string.badge_overdue) + " • " + formattedDue
                       );
                       binding.tvTaskDueDate.setTextColor(
                               ContextCompat.getColor(context, R.color.status_overdue_text)
                       );
                   } else {
                       binding.tvTaskDueDate.setText(formattedDue);
                       binding.tvTaskDueDate.setTextColor(
                               ContextCompat.getColor(context, R.color.color_text_secondary)
                       );
                   }
               } else {
                   binding.tvTaskDueDate.setVisibility(View.VISIBLE);
                   binding.tvTaskDueDate.setText(DateTimeUtils.formatDateTime(reminderTime));
                   binding.tvTaskDueDate.setTextColor(
                           ContextCompat.getColor(context, R.color.color_text_secondary)
                   );
               }

               binding.ivTaskReminder.setVisibility(hasReminder ? View.VISIBLE : View.GONE);
           }

           // 8. Su kien bam vao the va nut xoa
           binding.cardTaskItem.setOnClickListener(v -> listener.onTaskClicked(task));
           binding.btnDeleteTask.setOnClickListener(v -> listener.onTaskDeleteClicked(task));
       }

       private void bindPriorityIndicator(@NonNull Context context, @NonNull Priority priority) {
           int textRes;
           int colorRes;
           switch (priority) {
               case HIGH:
                   textRes = R.string.priority_high;
                   colorRes = R.color.priority_high_text;
                   break;
               case LOW:
                   textRes = R.string.priority_low;
                   colorRes = R.color.priority_low_text;
                   break;
               case MEDIUM:
               default:
                   textRes = R.string.priority_medium;
                   colorRes = R.color.priority_medium_text;
                   break;
           }
           int resolvedColor = ContextCompat.getColor(context, colorRes);
           binding.tvBadgePriority.setText(textRes);
           binding.tvBadgePriority.setTextColor(resolvedColor);
           TextViewCompat.setCompoundDrawableTintList(
                   binding.tvBadgePriority,
                   ColorStateList.valueOf(resolvedColor)
           );
       }

       private void bindCategoryTag(@NonNull Context context, @NonNull TaskCategory category) {
           int labelRes;
           switch (category) {
               case STUDY:
                   labelRes = R.string.category_study;
                   break;
               case WORK:
                   labelRes = R.string.category_work;
                   break;
               case SHOPPING:
                   labelRes = R.string.category_shopping;
                   break;
               case OTHER:
                   labelRes = R.string.category_other;
                   break;
               case PERSONAL:
               default:
                   labelRes = R.string.category_personal;
                   break;
           }
           binding.tvBadgeCategory.setText(labelRes);
           binding.tvBadgeCategory.setBackgroundTintList(
                   ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_surface_elevated))
           );
       }
  }
  }
