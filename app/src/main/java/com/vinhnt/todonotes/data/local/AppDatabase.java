package com.vinhnt.todonotes.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.vinhnt.todonotes.data.local.dao.NoteDao;
import com.vinhnt.todonotes.data.local.dao.TaskDao;
import com.vinhnt.todonotes.data.local.entity.NoteEntity;
import com.vinhnt.todonotes.data.local.entity.TaskEntity;

/**
* Cơ sở dữ liệu Room cục bộ của ứng dụng TodoNotes (Singleton).
* Bật exportSchema = true và định nghĩa sẵn danh sách Migrations (không dùng fallbackToDestructiveMigration).
  */
  @Database(
  entities = {TaskEntity.class, NoteEntity.class},
  version = 1,
  exportSchema = true
  )
  public abstract class AppDatabase extends RoomDatabase {

  public static final String DATABASE_NAME = "todonotes_database.db";
  private static final Object LOCK = new Object();
  private static volatile AppDatabase instance;

  public abstract TaskDao taskDao();

  public abstract NoteDao noteDao();

  /**
  * Ví dụ Migration mẫu (từ 1 -> 2 khi cần mở rộng schema về sau) để tuyệt đối không mất dữ liệu người dùng.
    */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
    // Dự phòng cho nâng cấp schema tương lai
    }
    };

  @NonNull
  public static AppDatabase getInstance(@NonNull Context context) {
  if (instance == null) {
  synchronized (LOCK) {
  if (instance == null) {
  instance = Room.databaseBuilder(
  context.getApplicationContext(),
  AppDatabase.class,
  DATABASE_NAME
  )
  .addMigrations(MIGRATION_1_2)
  .build();
  }
  }
  }
  return instance;
  }
  }
