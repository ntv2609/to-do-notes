package com.vinhnt.todonotes.ui.main;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.vinhnt.todonotes.R;
import com.vinhnt.todonotes.databinding.ActivityMainBinding;
import com.vinhnt.todonotes.ui.notes.AddEditNoteActivity;
import com.vinhnt.todonotes.ui.notes.NotesFragment;
import com.vinhnt.todonotes.ui.tasks.AddEditTaskActivity;
import com.vinhnt.todonotes.ui.tasks.TasksFragment;

/**
 * Activity chính điều phối Toolbar, BottomNavigationView (Công việc & Ghi chú nhanh) và nút FAB.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_TASKS_FRAGMENT = "tag_tasks_fragment";
    private static final String TAG_NOTES_FRAGMENT = "tag_notes_fragment";
    private static final String KEY_SELECTED_TAB = "key_selected_tab";

    private ActivityMainBinding binding;
    private int currentTabId = R.id.nav_tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            currentTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.nav_tasks);
        }

        setupToolbar();
        setupBottomNavigation();
        setupFab();

        if (savedInstanceState == null) {
            showTasksTab();
        } else {
            updateToolbarForCurrentTab();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, currentTabId);
    }

    private void setupToolbar() {
        binding.toolbarMain.setOnMenuItemClickListener(this::onToolbarMenuItemClicked);
    }

    private boolean onToolbarMenuItemClicked(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_sort) {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (current instanceof TasksFragment) {
                ((TasksFragment) current).showSortOptionsDialog();
            }
            return true;
        } else if (itemId == R.id.action_toggle_layout) {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (current instanceof NotesFragment) {
                ((NotesFragment) current).toggleLayoutMode();
                updateToggleLayoutIcon(item, ((NotesFragment) current).isGridView());
            }
            return true;
        } else if (itemId == R.id.action_search) {
            Toast.makeText(this, R.string.title_search, Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_settings) {
            Toast.makeText(this, R.string.title_settings, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setSelectedItemId(currentTabId);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tasks) {
                currentTabId = R.id.nav_tasks;
                showTasksTab();
                return true;
            } else if (itemId == R.id.nav_notes) {
                currentTabId = R.id.nav_notes;
                showNotesTab();
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            if (currentTabId == R.id.nav_tasks) {
                startActivity(AddEditTaskActivity.newIntentForAdd(this));
            } else if (currentTabId == R.id.nav_notes) {
                startActivity(AddEditNoteActivity.newIntentForAdd(this));
            }
        });
    }

    private void showTasksTab() {
        updateToolbarForCurrentTab();
        Fragment existing = getSupportFragmentManager().findFragmentByTag(TAG_TASKS_FRAGMENT);
        if (existing == null) {
            existing = new TasksFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, existing, TAG_TASKS_FRAGMENT)
                .commit();
    }

    private void showNotesTab() {
        updateToolbarForCurrentTab();
        Fragment existing = getSupportFragmentManager().findFragmentByTag(TAG_NOTES_FRAGMENT);
        if (existing == null) {
            existing = new NotesFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, existing, TAG_NOTES_FRAGMENT)
                .commit();
    }

    private void updateToolbarForCurrentTab() {
        MenuItem sortItem = binding.toolbarMain.getMenu().findItem(R.id.action_sort);
        MenuItem toggleItem = binding.toolbarMain.getMenu().findItem(R.id.action_toggle_layout);

        if (currentTabId == R.id.nav_notes) {
            binding.toolbarMain.setTitle(R.string.nav_notes);
            if (sortItem != null) {
                sortItem.setVisible(false);
            }
            if (toggleItem != null) {
                toggleItem.setVisible(true);
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                boolean isGrid = (current instanceof NotesFragment) && ((NotesFragment) current).isGridView();
                updateToggleLayoutIcon(toggleItem, isGrid);
            }
        } else {
            binding.toolbarMain.setTitle(R.string.nav_tasks);
            if (sortItem != null) {
                sortItem.setVisible(true);
            }
            if (toggleItem != null) {
                toggleItem.setVisible(false);
            }
        }
    }

    private void updateToggleLayoutIcon(@NonNull MenuItem item, boolean isGrid) {
        if (isGrid) {
            item.setIcon(R.drawable.ic_list_view);
            item.setTitle(R.string.action_toggle_layout);
        } else {
            item.setIcon(R.drawable.ic_grid_view);
            item.setTitle(R.string.action_toggle_layout);
        }
    }
}