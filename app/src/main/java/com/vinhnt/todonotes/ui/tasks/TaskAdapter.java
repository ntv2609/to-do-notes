package com.vinhnt.todonotes.ui.tasks;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
* Adapter hiển thị danh sách công việc dùng ListAdapter + DiffUtil chuẩn hiệu năng cao.
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

           // 1. Tiêu đề & hiệu ứng gạch ngang khi đã hoàn thành (docs/DESIGN.md 4.2)
           binding.tvTaskTitle.setText(task.getTitle());
           if (task.isCompleted()) {
               binding.tvTaskTitle.setPaintFlags(
                       binding.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
               );
               binding.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.color_text_disabled));
               binding.cardTaskItem.setAlpha(0.78f);
           } else {
               binding.tvTaskTitle.setPaintFlags(
                       binding.tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
               );
               binding.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.color_text_primary));
               binding.cardTaskItem.setAlpha(1.0f);
           }

           // 2. Mô tả ngắn
           String desc = task.getDescription().trim();
           if (desc.isEmpty()) {
               binding.tvTaskDescription.setVisibility(View.GONE);
           } else {
               binding.tvTaskDescription.setVisibility(View.VISIBLE);
               binding.tvTaskDescription.setText(desc);
           }

           // 3. Checkbox: Gỡ listener trước khi setChecked để tránh callback giả khi cuộn RecyclerView
           binding.cbTaskCompleted.setOnCheckedChangeListener(null);
           binding.cbTaskCompleted.setChecked(task.isCompleted());
           binding.cbTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
               if (buttonView.isPressed()) {
                   listener.onTaskCheckChanged(task, isChecked);
               }
           });

           // 4. Huy hiệu Độ ưu tiên (Priority Badge)
           bindPriorityBadge(context, Priority.fromValue(task.getPriority()));

           // 5. Huy hiệu Nhãn phân loại (Category Badge)
           bindCategoryBadge(context, TaskCategory.fromValue(task.getCategory()));

           // 6. Tiến độ Công việc con (Subtasks)
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

           // 7. Thời hạn hoàn thành & Biểu tượng Nhắc nhở
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

           // 8. Sự kiện bấm vào thẻ & bấm nút xóa
           binding.cardTaskItem.setOnClickListener(v -> listener.onTaskClicked(task));
           binding.btnDeleteTask.setOnClickListener(v -> listener.onTaskDeleteClicked(task));
       }

       private void bindPriorityBadge(@NonNull Context context, @NonNull Priority priority) {
           int textRes;
           int textColorRes;
           int bgColorRes;
           switch (priority) {
               case HIGH:
                   textRes = R.string.priority_high;
                   textColorRes = R.color.priority_high_text;
                   bgColorRes = R.color.priority_high_bg;
                   break;
               case LOW:
                   textRes = R.string.priority_low;
                   textColorRes = R.color.priority_low_text;
                   bgColorRes = R.color.priority_low_bg;
                   break;
               case MEDIUM:
               default:
                   textRes = R.string.priority_medium;
                   textColorRes = R.color.priority_medium_text;
                   bgColorRes = R.color.priority_medium_bg;
                   break;
           }
           binding.tvBadgePriority.setText(textRes);
           binding.tvBadgePriority.setTextColor(ContextCompat.getColor(context, textColorRes));
           binding.tvBadgePriority.setBackgroundTintList(
                   ColorStateList.valueOf(ContextCompat.getColor(context, bgColorRes))
           );
       }

       private void bindCategoryBadge(@NonNull Context context, @NonNull TaskCategory category) {
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
                   ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_primary_container))
           );
       }
  }
  }
