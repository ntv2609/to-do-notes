package com.vinhnt.todonotes.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
* Quản lý hồ luồng (Thread Pool) toàn cục cho truy xuất cơ sở dữ liệu Room và cập nhật giao diện.
  */
  public class AppExecutors {
  private static final Object LOCK = new Object();
  private static volatile AppExecutors instance;

  private final Executor diskIO;
  private final Executor mainThread;

  public AppExecutors(@NonNull Executor diskIO, @NonNull Executor mainThread) {
  this.diskIO = diskIO;
  this.mainThread = mainThread;
  }

  public static AppExecutors getInstance() {
  if (instance == null) {
  synchronized (LOCK) {
  if (instance == null) {
  instance = new AppExecutors(
  Executors.newFixedThreadPool(4),
  new MainThreadExecutor()
  );
  }
  }
  }
  return instance;
  }

  @NonNull
  public Executor diskIO() {
  return diskIO;
  }

  @NonNull
  public Executor mainThread() {
  return mainThread;
  }

  private static class MainThreadExecutor implements Executor {
  private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

       @Override
       public void execute(@NonNull Runnable command) {
           mainThreadHandler.post(command);
       }
  }
  }
