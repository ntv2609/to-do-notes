package com.vinhnt.todonotes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
* Lớp chuyển tiếp tương thích ngược (nếu chưa xóa thủ công file gốc do template tạo).
* Activity chính thức của ứng dụng nằm tại {@link com.vinhnt.todonotes.ui.main.MainActivity}.
  */
  public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  startActivity(new Intent(this, com.vinhnt.todonotes.ui.main.MainActivity.class));
  finish();
  }
  }
