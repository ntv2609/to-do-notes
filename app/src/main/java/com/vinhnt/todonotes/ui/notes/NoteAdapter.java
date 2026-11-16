package com.vinhnt.todonotes.ui.notes;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.databinding.ItemNoteBinding;
import com.vinhnt.todonotes.util.DateTimeUtils;

/**
 * ListAdapter hiển thị danh sách Ghi chú nhanh thích ứng 6 màu thẻ Soft Glass (Light/Dark Mode).
 */
public class NoteAdapter extends ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(@NonNull NoteEntity note);
        void onPinClick(@NonNull NoteEntity note);
        void onDeleteClick(@NonNull NoteEntity note);
    }

    private static final DiffUtil.ItemCallback<NoteEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<NoteEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull NoteEntity oldItem, @NonNull NoteEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull NoteEntity oldItem, @NonNull NoteEntity newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Nullable
    private final OnNoteClickListener listener;

    public NoteAdapter(@Nullable OnNoteClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoteBinding binding = ItemNoteBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new NoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static int getBgColorResId(int colorIndex) {
        switch (colorIndex) {
            case 1:
                return R.color.note_color_1_bg;
            case 2:
                return R.color.note_color_2_bg;
            case 3:
                return R.color.note_color_3_bg;
            case 4:
                return R.color.note_color_4_bg;
            case 5:
                return R.color.note_color_5_bg;
            default:
                return R.color.note_color_0_bg;
        }
    }

    public static int getStrokeColorResId(int colorIndex) {
        switch (colorIndex) {
            case 1:
                return R.color.note_color_1_stroke;
            case 2:
                return R.color.note_color_2_stroke;
            case 3:
                return R.color.note_color_3_stroke;
            case 4:
                return R.color.note_color_4_stroke;
            case 5:
                return R.color.note_color_5_stroke;
            default:
                return R.color.note_color_0_stroke;
        }
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {

        private final ItemNoteBinding binding;

        NoteViewHolder(@NonNull ItemNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull NoteEntity note) {
            Context context = itemView.getContext();

            if (note.getTitle().trim().isEmpty()) {
                binding.tvNoteTitle.setVisibility(View.GONE);
            } else {
                binding.tvNoteTitle.setVisibility(View.VISIBLE);
                binding.tvNoteTitle.setText(note.getTitle());
            }

            if (note.getContent().trim().isEmpty()) {
                binding.tvNoteContent.setVisibility(View.GONE);
            } else {
                binding.tvNoteContent.setVisibility(View.VISIBLE);
                binding.tvNoteContent.setText(note.getContent());
            }

            String updatedTimeStr = DateTimeUtils.formatDateTime(note.getUpdatedAt());
            binding.tvNoteUpdatedAt.setText(context.getString(R.string.note_updated_at_format, updatedTimeStr));

            int bgColor = ContextCompat.getColor(context, getBgColorResId(note.getColorIndex()));
            int strokeColor = ContextCompat.getColor(context, getStrokeColorResId(note.getColorIndex()));
            binding.cardNote.setCardBackgroundColor(bgColor);
            binding.cardNote.setStrokeColor(strokeColor);

            if (note.isPinned()) {
                int pinColor = ContextCompat.getColor(context, R.color.color_primary);
                binding.ibPin.setImageTintList(ColorStateList.valueOf(pinColor));
                binding.ibPin.setContentDescription(context.getString(R.string.action_unpin));
            } else {
                int defaultColor = ContextCompat.getColor(context, R.color.color_text_secondary);
                binding.ibPin.setImageTintList(ColorStateList.valueOf(defaultColor));
                binding.ibPin.setContentDescription(context.getString(R.string.action_pin));
            }

            binding.cardNote.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNoteClick(note);
                }
            });

            binding.ibPin.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPinClick(note);
                }
            });

            binding.ibDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(note);
                }
            });
        }
    }
}