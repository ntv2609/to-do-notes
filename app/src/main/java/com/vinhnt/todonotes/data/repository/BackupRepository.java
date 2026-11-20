package com.vinhnt.todonotes.data.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinhnt.todonotes.data.local.AppDatabase;
import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;
import com.vinhnt.todonotes.receiver.ReminderScheduler;
import com.vinhnt.todonotes.util.AppExecutors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
* Xử lý Sao lưu (Export) và Khôi phục (Import) toàn bộ Công việc & Ghi chú nhanh ra tệp JSON
* thông qua Storage Access Framework (SAF) bằng org.json chuẩn Android SDK.
  */
  public class BackupRepository {

  private static final String TAG = "BackupRepository";
  private static final int BACKUP_SCHEMA_VERSION = 1;

  public static class BackupPayload {
  @NonNull
  public final List<TaskEntity> tasks;
  @NonNull
  public final List<NoteEntity> notes;

       public BackupPayload(@NonNull List<TaskEntity> tasks, @NonNull List<NoteEntity> notes) {
           this.tasks = tasks;
           this.notes = notes;
       }
  }

  public interface ExportCallback {
  void onSuccess();

       void onError(@NonNull Throwable error);
  }

  public interface ImportCallback {
  void onSuccess(int taskCount, int noteCount);

       void onInvalidFormat(@NonNull Throwable error);

       void onError(@NonNull Throwable error);
  }

  private final Context appContext;
  private final AppDatabase database;
  private final AppExecutors executors;

  public BackupRepository(@NonNull Context context) {
  this.appContext = context.getApplicationContext();
  this.database = AppDatabase.getInstance(this.appContext);
  this.executors = AppExecutors.getInstance();
  }

  /**
  * Xuất toàn bộ dữ liệu trong Room DB ra tệp JSON tại Uri do người dùng chọn qua SAF.
    */
    public void exportBackupToUri(@NonNull Uri destinationUri, @NonNull ExportCallback callback) {
    executors.diskIO().execute(() -> {
    try {
    List<TaskEntity> tasks = database.taskDao().getAllTasksSync();
    List<NoteEntity> notes = database.noteDao().getAllNotesSync();
    String json = serializeToJson(tasks, notes, System.currentTimeMillis());

             try (OutputStream out = appContext.getContentResolver().openOutputStream(destinationUri)) {
                 if (out == null) {
                     throw new IOException("Cannot open output stream for destination Uri");
                 }
                 out.write(json.getBytes(StandardCharsets.UTF_8));
                 out.flush();
             }

             executors.mainThread().execute(callback::onSuccess);
         } catch (Exception e) {
             Log.e(TAG, "Failed to export backup JSON", e);
             executors.mainThread().execute(() -> callback.onError(e));
         }
    });
    }

  /**
  * Đọc tệp JSON từ Uri, kiểm tra hợp lệ toàn bộ trước khi ghi giao dịch vào Room DB,
  * sau đó khôi phục lại các lịch nhắc nhở còn hạn ở tương lai.
    */
    public void importBackupFromUri(@NonNull Uri sourceUri, @NonNull ImportCallback callback) {
    executors.diskIO().execute(() -> {
    try {
    String jsonContent = readTextFromUri(sourceUri);
    BackupPayload payload = parseFromJson(jsonContent);
    importPayloadSync(database, payload);
    ReminderScheduler.rescheduleAllActiveRemindersSync(appContext, database.taskDao());

             int taskCount = payload.tasks.size();
             int noteCount = payload.notes.size();
             executors.mainThread().execute(() -> callback.onSuccess(taskCount, noteCount));
         } catch (JSONException e) {
             Log.w(TAG, "Invalid backup JSON format", e);
             executors.mainThread().execute(() -> callback.onInvalidFormat(e));
         } catch (Exception e) {
             Log.e(TAG, "Failed to import backup JSON", e);
             executors.mainThread().execute(() -> callback.onError(e));
         }
    });
    }

  /**
  * Ghi toàn bộ BackupPayload vào Room DB trong một giao dịch an toàn (Transaction).
    */
    public static void importPayloadSync(@NonNull AppDatabase db, @NonNull BackupPayload payload) {
    db.runInTransaction(() -> {
    if (!payload.tasks.isEmpty()) {
    db.taskDao().insertTasks(payload.tasks);
    }
    if (!payload.notes.isEmpty()) {
    db.noteDao().insertNotes(payload.notes);
    }
    });
    }

  /**
  * Chuyển danh sách Công việc và Ghi chú sang chuỗi JSON định dạng chuẩn của TodoNotes.
    */
    @NonNull
    public static String serializeToJson(
    @NonNull List<TaskEntity> tasks,
    @NonNull List<NoteEntity> notes,
    long exportedAtMillis
    ) throws JSONException {
    JSONObject root = new JSONObject();
    root.put("app", "TodoNotes");
    root.put("schemaVersion", BACKUP_SCHEMA_VERSION);
    root.put("exportedAt", exportedAtMillis);

    JSONArray tasksArray = new JSONArray();
    for (TaskEntity task : tasks) {
    JSONObject obj = new JSONObject();
    obj.put("id", task.getId());
    obj.put("title", task.getTitle());
    obj.put("description", task.getDescription());
    obj.put("dueDate", task.getDueDate() != null ? task.getDueDate() : JSONObject.NULL);
    obj.put("reminderTime", task.getReminderTime() != null ? task.getReminderTime() : JSONObject.NULL);
    obj.put("reminderFired", task.isReminderFired());
    obj.put("priority", task.getPriority());
    obj.put("category", task.getCategory());
    obj.put("subtasksJson", task.getSubtasksJson());
    obj.put("completed", task.isCompleted());
    obj.put("completedAt", task.getCompletedAt() != null ? task.getCompletedAt() : JSONObject.NULL);
    obj.put("createdAt", task.getCreatedAt());
    obj.put("updatedAt", task.getUpdatedAt());
    tasksArray.put(obj);
    }
    root.put("tasks", tasksArray);

    JSONArray notesArray = new JSONArray();
    for (NoteEntity note : notes) {
    JSONObject obj = new JSONObject();
    obj.put("id", note.getId());
    obj.put("title", note.getTitle());
    obj.put("content", note.getContent());
    obj.put("colorIndex", note.getColorIndex());
    obj.put("pinned", note.isPinned());
    obj.put("createdAt", note.getCreatedAt());
    obj.put("updatedAt", note.getUpdatedAt());
    notesArray.put(obj);
    }
    root.put("notes", notesArray);

    return root.toString(2);
    }

  /**
  * Phân tích và xác thực tệp JSON sao lưu trước khi nạp vào Room DB.
    */
    @NonNull
    public static BackupPayload parseFromJson(@NonNull String rawJson) throws JSONException {
    if (rawJson.trim().isEmpty()) {
    throw new JSONException("Empty backup JSON");
    }
    JSONObject root = new JSONObject(rawJson);
    if (!root.has("tasks") || !root.has("notes")) {
    throw new JSONException("Missing required 'tasks' or 'notes' arrays");
    }

    JSONArray tasksArray = root.getJSONArray("tasks");
    List<TaskEntity> tasks = new ArrayList<>(tasksArray.length());
    long now = System.currentTimeMillis();

    for (int i = 0; i < tasksArray.length(); i++) {
    JSONObject obj = tasksArray.getJSONObject(i);
    String title = obj.optString("title", "").trim();
    if (title.isEmpty()) {
    throw new JSONException("Task at index " + i + " has empty title");
    }
    TaskEntity task = new TaskEntity();
    long id = obj.optLong("id", 0L);
    if (id > 0L) {
    task.setId(id);
    }
    task.setTitle(title);
    task.setDescription(obj.optString("description", ""));
    task.setDueDate(obj.isNull("dueDate") ? null : obj.optLong("dueDate"));
    task.setReminderTime(obj.isNull("reminderTime") ? null : obj.optLong("reminderTime"));
    task.setReminderFired(obj.optBoolean("reminderFired", false));
    task.setPriority(obj.optInt("priority", 1));
    task.setCategory(obj.optInt("category", 0));
    task.setSubtasksJson(obj.optString("subtasksJson", "[]"));
    task.setCompleted(obj.optBoolean("completed", false));
    task.setCompletedAt(obj.isNull("completedAt") ? null : obj.optLong("completedAt"));
    task.setCreatedAt(obj.optLong("createdAt", now));
    task.setUpdatedAt(obj.optLong("updatedAt", now));
    tasks.add(task);
    }

    JSONArray notesArray = root.getJSONArray("notes");
    List<NoteEntity> notes = new ArrayList<>(notesArray.length());
    for (int i = 0; i < notesArray.length(); i++) {
    JSONObject obj = notesArray.getJSONObject(i);
    String title = obj.optString("title", "");
    String content = obj.optString("content", "");
    if (title.trim().isEmpty() && content.trim().isEmpty()) {
    throw new JSONException("Note at index " + i + " is empty");
    }
    NoteEntity note = new NoteEntity();
    long id = obj.optLong("id", 0L);
    if (id > 0L) {
    note.setId(id);
    }
    note.setTitle(title);
    note.setContent(content);
    note.setColorIndex(obj.optInt("colorIndex", 0));
    note.setPinned(obj.optBoolean("pinned", false));
    note.setCreatedAt(obj.optLong("createdAt", now));
    note.setUpdatedAt(obj.optLong("updatedAt", now));
    notes.add(note);
    }

    return new BackupPayload(tasks, notes);
    }

  @NonNull
  private String readTextFromUri(@NonNull Uri uri) throws IOException {
  StringBuilder sb = new StringBuilder();
  try (InputStream in = appContext.getContentResolver().openInputStream(uri)) {
  if (in == null) {
  throw new IOException("Cannot open input stream for source Uri");
  }
  try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
  String line;
  while ((line = reader.readLine()) != null) {
  sb.append(line).append('\n');
  }
  }
  }
  return sb.toString();
  }
  }
