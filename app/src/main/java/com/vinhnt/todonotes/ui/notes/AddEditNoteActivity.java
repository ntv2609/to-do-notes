package com.vinhnt.todonotes.ui.notes;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.databinding.ActivityAddEditNoteBinding;
import com.vinhnt.todonotes.ui.tasks.AddEditTaskActivity;
import com.vinhnt.todonotes.util.DateTimeUtils;

import java.util.Objects;

/**
 * Màn hình Thêm mới / Chỉnh sửa Ghi chú nhanh (UC 3.6, 3.8), hỗ trợ chọn 6 màu thẻ, ghim,
 * nút "Chuyển thành công việc" (UC 3.8) và nhận văn bản chia sẻ từ ứng dụng ngoài (ACTION_SEND).
 */
public class AddEditNoteActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "extra_note_id";

    private static final String STATE_SELECTED_COLOR = "state_selected_color";
    private static final String STATE_IS_PINNED = "state_is_pinned";
    private static final String STATE_HAS_EDITED = "state_has_edited";

    private ActivityAddEditNoteBinding binding;
    private AddEditNoteViewModel viewModel;

    private long noteId = -1L;
    @Nullable
    private NoteEntity existingNote;

    private int selectedColorIndex = 0;
    private boolean isPinned = false;
    private boolean isPopulatingData = false;
    private boolean hasUnsavedEdits = false;

    @NonNull
    public static Intent newIntentForAdd(@NonNull Context context) {
        return new Intent(context, AddEditNoteActivity.class);
    }

    @NonNull
    public static Intent newIntentForEdit(@NonNull Context context, long noteId) {
        Intent intent = new Intent(context, AddEditNoteActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, noteId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AddEditNoteViewModel.class);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_NOTE_ID)) {
            noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1L);
        }

        setupToolbar();
        setupColorPalette();
        setupTextWatchers();
        setupBackPressedHandler();

        if (savedInstanceState != null) {
            selectedColorIndex = savedInstanceState.getInt(STATE_SELECTED_COLOR, 0);
            isPinned = savedInstanceState.getBoolean(STATE_IS_PINNED, false);
            hasUnsavedEdits = savedInstanceState.getBoolean(STATE_HAS_EDITED, false);
            updateColorSelectionUI();
        } else {
            handleIncomingIntent();
        }

        if (noteId > 0L) {
            loadExistingNote();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_COLOR, selectedColorIndex);
        outState.putBoolean(STATE_IS_PINNED, isPinned);
        outState.putBoolean(STATE_HAS_EDITED, hasUnsavedEdits);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarAddEditNote);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(noteId > 0L ? R.string.title_edit_note : R.string.title_add_note);
        }
        binding.toolbarAddEditNote.setNavigationOnClickListener(v -> handleBackNavigation());
    }

    private void setupColorPalette() {
        ImageButton[] colorButtons = new ImageButton[]{
                binding.btnColor0, binding.btnColor1, binding.btnColor2,
                binding.btnColor3, binding.btnColor4, binding.btnColor5
        };

        for (int i = 0; i < colorButtons.length; i++) {
            final int index = i;
            colorButtons[i].setOnClickListener(v -> {
                selectedColorIndex = index;
                hasUnsavedEdits = true;
                updateColorSelectionUI();
            });
        }
        updateColorSelectionUI();
    }

    private void updateColorSelectionUI() {
        ImageButton[] colorButtons = new ImageButton[]{
                binding.btnColor0, binding.btnColor1, binding.btnColor2,
                binding.btnColor3, binding.btnColor4, binding.btnColor5
        };

        for (int i = 0; i < colorButtons.length; i++) {
            if (i == selectedColorIndex) {
                colorButtons[i].setElevation(8f);
                colorButtons[i].setScaleX(1.15f);
                colorButtons[i].setScaleY(1.15f);
            } else {
                colorButtons[i].setElevation(0f);
                colorButtons[i].setScaleX(1.0f);
                colorButtons[i].setScaleY(1.0f);
            }
        }

        int bgColor = ContextCompat.getColor(this, NoteAdapter.getBgColorResId(selectedColorIndex));
        binding.getRoot().setBackgroundColor(bgColor);
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isPopulatingData) {
                    hasUnsavedEdits = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        binding.etNoteTitle.addTextChangedListener(watcher);
        binding.etNoteContent.addTextChangedListener(watcher);
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });
    }

    private void handleIncomingIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            String sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

            isPopulatingData = true;
            if (sharedSubject != null && !sharedSubject.trim().isEmpty()) {
                binding.etNoteTitle.setText(sharedSubject);
            }
            if (sharedText != null) {
                binding.etNoteContent.setText(sharedText);
            }
            isPopulatingData = false;
            hasUnsavedEdits = true;
        }
    }

    private void loadExistingNote() {
        viewModel.getNoteById(noteId).observe(this, note -> {
            if (note != null && existingNote == null) {
                existingNote = note;
                isPopulatingData = true;

                binding.etNoteTitle.setText(note.getTitle());
                binding.etNoteContent.setText(note.getContent());
                selectedColorIndex = note.getColorIndex();
                isPinned = note.isPinned();

                String updatedStr = DateTimeUtils.formatDateTime(note.getUpdatedAt());
                binding.tvNoteLastUpdated.setText(getString(R.string.note_updated_at_format, updatedStr));

                updateColorSelectionUI();
                invalidateOptionsMenu();
                isPopulatingData = false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_edit_note, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        if (deleteItem != null) {
            deleteItem.setVisible(noteId > 0L);
        }

        MenuItem pinItem = menu.findItem(R.id.action_pin);
        if (pinItem != null) {
            if (isPinned) {
                pinItem.setIcon(R.drawable.ic_pin);
                pinItem.setTitle(R.string.action_unpin);
            } else {
                pinItem.setIcon(R.drawable.ic_pin);
                pinItem.setTitle(R.string.action_pin);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            performSave();
            return true;
        } else if (id == R.id.action_pin) {
            isPinned = !isPinned;
            hasUnsavedEdits = true;
            invalidateOptionsMenu();
            Toast.makeText(this, isPinned ? R.string.action_pin : R.string.action_unpin, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_convert_to_task) {
            performConvertToTask();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDeleteNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performSave() {
        String title = Objects.requireNonNull(binding.etNoteTitle.getText()).toString();
        String content = Objects.requireNonNull(binding.etNoteContent.getText()).toString();

        if (!viewModel.isValidNote(title, content)) {
            Toast.makeText(this, R.string.error_note_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.saveNote(noteId, title, content, selectedColorIndex, isPinned, savedId -> {
            Toast.makeText(this, R.string.msg_note_saved, Toast.LENGTH_SHORT).show();
            hasUnsavedEdits = false;
            finish();
        });
    }

    private void performConvertToTask() {
        String title = Objects.requireNonNull(binding.etNoteTitle.getText()).toString().trim();
        String content = Objects.requireNonNull(binding.etNoteContent.getText()).toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, R.string.error_note_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        String taskTitle = title.isEmpty() ? content : title;
        String taskDescription = title.isEmpty() ? "" : content;

        Intent intent = AddEditTaskActivity.newIntentFromNote(this, taskTitle, taskDescription);
        startActivity(intent);
        Toast.makeText(this, R.string.msg_converted_note_to_task, Toast.LENGTH_SHORT).show();
    }

    private void confirmDeleteNote() {
        if (existingNote == null) {
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_note_title)
                .setMessage(R.string.dialog_delete_note_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    viewModel.deleteNote(existingNote, result -> {
                        Toast.makeText(this, R.string.msg_note_deleted, Toast.LENGTH_SHORT).show();
                        hasUnsavedEdits = false;
                        finish();
                    });
                })
                .show();
    }

    private void handleBackNavigation() {
        if (hasUnsavedEdits) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_discard_title)
                    .setMessage(R.string.dialog_discard_message)
                    .setNegativeButton(R.string.action_keep_editing, null)
                    .setPositiveButton(R.string.action_discard, (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }
}