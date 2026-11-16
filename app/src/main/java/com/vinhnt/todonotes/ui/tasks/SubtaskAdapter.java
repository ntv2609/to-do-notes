package com.vinhnt.todonotes.ui.tasks;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.data.model.SubtaskItem;
import com.vinhnt.todonotes.databinding.ItemSubtaskBinding;

import java.util.ArrayList;
import java.util.List;

/**
* Adapter hiển thị danh sách các mục công việc con (Checklist) trong màn hình AddEditTaskActivity.
  */
  public class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.SubtaskViewHolder> {

  public interface OnSubtaskChangedListener {
  void onSubtasksModified();
  }

  private final List<SubtaskItem> items = new ArrayList<>();
  private final OnSubtaskChangedListener listener;

  public SubtaskAdapter(@NonNull OnSubtaskChangedListener listener) {
  this.listener = listener;
  }

  public void setItems(@NonNull List<SubtaskItem> newItems) {
  items.clear();
  for (SubtaskItem item : newItems) {
  items.add(new SubtaskItem(item.getTitle(), item.isDone()));
  }
  notifyDataSetChanged();
  }

  public void addItem(@NonNull SubtaskItem item) {
  items.add(item);
  notifyItemInserted(items.size() - 1);
  listener.onSubtasksModified();
  }

  @NonNull
  public List<SubtaskItem> getItems() {
  List<SubtaskItem> copy = new ArrayList<>(items.size());
  for (SubtaskItem item : items) {
  copy.add(new SubtaskItem(item.getTitle(), item.isDone()));
  }
  return copy;
  }

  @NonNull
  @Override
  public SubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
  ItemSubtaskBinding binding = ItemSubtaskBinding.inflate(
  LayoutInflater.from(parent.getContext()),
  parent,
  false
  );
  return new SubtaskViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull SubtaskViewHolder holder, int position) {
  SubtaskItem item = items.get(position);
  holder.binding.tvSubtaskTitle.setText(item.getTitle());
  applyStrikeThrough(holder, item.isDone());

       holder.binding.cbSubtaskDone.setOnCheckedChangeListener(null);
       holder.binding.cbSubtaskDone.setChecked(item.isDone());
       holder.binding.cbSubtaskDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
           int adapterPos = holder.getBindingAdapterPosition();
           if (adapterPos != RecyclerView.NO_POSITION) {
               items.get(adapterPos).setDone(isChecked);
               applyStrikeThrough(holder, isChecked);
               listener.onSubtasksModified();
           }
       });

       holder.binding.btnRemoveSubtask.setOnClickListener(v -> {
           int adapterPos = holder.getBindingAdapterPosition();
           if (adapterPos != RecyclerView.NO_POSITION) {
               items.remove(adapterPos);
               notifyItemRemoved(adapterPos);
               listener.onSubtasksModified();
           }
       });
  }

  @Override
  public int getItemCount() {
  return items.size();
  }

  private void applyStrikeThrough(@NonNull SubtaskViewHolder holder, boolean isDone) {
  if (isDone) {
  holder.binding.tvSubtaskTitle.setPaintFlags(
  holder.binding.tvSubtaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
  );
  holder.binding.tvSubtaskTitle.setTextColor(
  ContextCompat.getColor(holder.binding.getRoot().getContext(), R.color.color_text_disabled)
  );
  } else {
  holder.binding.tvSubtaskTitle.setPaintFlags(
  holder.binding.tvSubtaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
  );
  holder.binding.tvSubtaskTitle.setTextColor(
  ContextCompat.getColor(holder.binding.getRoot().getContext(), R.color.color_text_primary)
  );
  }
  }

  static class SubtaskViewHolder extends RecyclerView.ViewHolder {
  final ItemSubtaskBinding binding;

       SubtaskViewHolder(@NonNull ItemSubtaskBinding binding) {
           super(binding.getRoot());
           this.binding = binding;
       }
  }
  }
