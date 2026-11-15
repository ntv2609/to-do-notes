# KẾ HOẠCH TỔNG THỂ DỰ ÁN A–Z (PROJECT MASTER PLAN)

## 1. TECH STACK & PHIÊN BẢN CHUẨN
- **IDE:** Android Studio Quail 4 (2026.1.4 Patch 1) trên Windows 11.
- **Thiết bị kiểm thử chuẩn:** Máy ảo **Pixel 7** (khuyến nghị API 34 hoặc 35).
- **Ngôn ngữ & JDK:** Java 17 (`JavaVersion.VERSION_17`), dự án tạo từ template **Empty Views Activity (Java)**.
- **SDK:** `minSdk = 26` (Android 8.0), `targetSdk = 35` (hoặc giữ nguyên `targetSdk`/`compileSdk` mặc định do Quail 4 sinh ra khi tạo project).
- **Thư viện chuẩn (chỉ dùng nhóm AndroidX & Material chính thống):**
    - `androidx.appcompat:appcompat:1.7.0`
    - `com.google.android.material:material:1.12.0`
    - `androidx.constraintlayout:constraintlayout:2.2.0`
    - `androidx.recyclerview:recyclerview:1.3.2`
    - `androidx.lifecycle:lifecycle-viewmodel:2.8.7` & `androidx.lifecycle:lifecycle-livedata:2.8.7`
    - `androidx.room:room-runtime:2.6.1` & `annotationProcessor "androidx.room:room-compiler:2.6.1"`
    - Kiểm thử: `junit:junit:4.13.2`, `androidx.test.ext:junit:1.2.1`, `androidx.test:runner:1.6.2`, `androidx.room:room-testing:2.6.1`, `androidx.arch.core:core-testing:2.2.0`.
    - *(Lưu ý: Không cài thêm bất kỳ thư viện bên thứ 3 nào ngoài danh sách trên. Xử lý JSON cho Subtasks và Backup dùng `org.json` tích hợp sẵn trong Android SDK).*

---

## 2. KIẾN TRÚC TỔNG THỂ & CẤU TRÚC PACKAGE

```text
com.vinhnt.todonotes
├── TodoNotesApp.java                  // Application class: khởi tạo NotificationChannel, Theme, Ngôn ngữ
├── data
│   ├── local
│   │   ├── AppDatabase.java           // RoomDatabase singleton + Migrations
│   │   ├── dao
│   │   │   ├── TaskDao.java           // Truy vấn CRUD, Lọc, Tìm kiếm cho bảng tasks
│   │   │   └── NoteDao.java           // Truy vấn CRUD, Ghim, Tìm kiếm cho bảng notes
│   │   └── entity
│   │       ├── TaskEntity.java        // Thực thể bảng tasks
│   │       └── NoteEntity.java        // Thực thể bảng notes
│   ├── model
│   │   ├── Priority.java              // Enum/hằng số mức độ ưu tiên (LOW, MEDIUM, HIGH)
│   │   ├── TaskCategory.java          // Enum/hằng số nhãn phân loại (PERSONAL, STUDY, WORK, SHOPPING, OTHER)
│   │   ├── TaskFilterType.java        // Trạng thái lọc (ALL, ACTIVE, COMPLETED, OVERDUE, TODAY)
│   │   ├── TaskSortOption.java        // Tiêu chí sắp xếp (CREATED_DATE, DUE_DATE, PRIORITY)
│   │   └── SubtaskItem.java           // POJO biểu diễn 1 công việc con + trình chuyển đổi JSON
│   └── repository
│       ├── TaskRepository.java        // Điều phối TaskDao + AppExecutors + đồng bộ ReminderScheduler
│       ├── NoteRepository.java        // Điều phối NoteDao + AppExecutors
│       └── BackupRepository.java      // Xuất / Nhập toàn bộ Task & Note ra/từ JSON (SAF)
├── receiver
│   ├── ReminderScheduler.java         // Tiện ích đặt/hủy AlarmManager an toàn trên Android 8–15+
│   ├── ReminderReceiver.java          // Nhận Alarm -> kiểm tra DB -> phát Notification
│   ├── NotificationActionReceiver.java// Xử lý nút "Hoàn thành ngay" trực tiếp trên Notification
│   └── BootCompletedReceiver.java     // Nhận BOOT_COMPLETED, TIME_SET, TIMEZONE_CHANGED -> đặt lại lịch
├── ui
│   ├── main
│   │   └── MainActivity.java          // Chứa Toolbar, BottomNavigationView, FAB, điều phối 2 Fragment
│   ├── tasks
│   │   ├── TasksFragment.java         // Màn hình danh sách công việc + Thống kê + Lọc + Sắp xếp + Undo
│   │   ├── TaskAdapter.java           // ListAdapter + DiffUtil hiển thị thẻ công việc
│   │   ├── TasksViewModel.java        // Quản lý trạng thái danh sách, bộ lọc, sắp xếp, thống kê
│   │   ├── AddEditTaskActivity.java   // Màn hình Thêm / Xem chi tiết / Sửa công việc + Subtasks + Nhắc nhở
│   │   └── SubtaskAdapter.java        // Adapter cho danh sách công việc con trong màn hình chi tiết
│   ├── notes
│   │   ├── NotesFragment.java         // Màn hình danh sách/lưới ghi chú nhanh + Undo
│   │   ├── NoteAdapter.java           // ListAdapter + DiffUtil hiển thị thẻ ghi chú theo màu & trạng thái ghim
│   │   ├── NotesViewModel.java        // Quản lý trạng thái danh sách ghi chú, tìm kiếm, bố cục 1/2 cột
│   │   └── AddEditNoteActivity.java   // Màn hình Tạo / Sửa ghi chú + Chuyển thành công việc + Nhận chia sẻ
│   ├── search
│   │   └── SearchActivity.java        // Màn hình Tìm kiếm tổng hợp cả Công việc & Ghi chú (UC 3.10)
│   └── settings
│       └── SettingsActivity.java      // Cài đặt Giao diện Sáng/Tối, Ngôn ngữ Việt/Anh, Sao lưu/Khôi phục JSON
└── util
    ├── AppExecutors.java              // Quản lý luồng nền (diskIO, mainThread)
    ├── DateTimeUtils.java             // Định dạng ngày giờ, kiểm tra hôm nay/quá hạn
    ├── PreferencesManager.java        // Lưu tùy chọn sắp xếp, chế độ xem lưới, giao diện
    ├── SubtaskJsonHelper.java         // Parse/Serialize danh sách SubtaskItem bằng org.json
    └── ValidationUtils.java           // Kiểm tra hợp lệ dữ liệu đầu vào của Task và Note (dễ viết Unit Test)
```

---

## 3. SƠ ĐỒ THỰC THỂ DỮ LIỆU (ERD) & SƠ ĐỒ MÀN HÌNH

### 3.1. Sơ đồ ERD (Cơ sở dữ liệu cục bộ `todonotes_database.db`)
```text
+-----------------------------------+          (Chuyển đổi 1 chạm          +-----------------------------------+
|               tasks               |           tạo bản ghi mới)           |               notes               |
+-----------------------------------+ -----------------------------------> +-----------------------------------+
| PK  id             : INTEGER      |                                      | PK  id             : INTEGER      |
|     title          : TEXT NOT NULL|                                      |     title          : TEXT NOT NULL|
|     description    : TEXT NOT NULL|                                      |     content        : TEXT NOT NULL|
|     due_date       : INTEGER NULL |                                      |     color_index    : INTEGER      |
|     reminder_time  : INTEGER NULL |                                      |     is_pinned      : INTEGER (0/1)|
|     reminder_fired : INTEGER (0/1)|                                      |     created_at     : INTEGER      |
|     priority       : INTEGER (0-2)|                                      |     updated_at     : INTEGER      |
|     category       : INTEGER (0-4)|                                      +-----------------------------------+
|     subtasks_json  : TEXT NOT NULL|
|     is_completed   : INTEGER (0/1)|
|     completed_at   : INTEGER NULL |
|     created_at     : INTEGER      |
|     updated_at     : INTEGER      |
+-----------------------------------+
```

### 3.2. Sơ đồ luồng điều hướng màn hình
```text
                        +---------------------------------------+
                        |             MainActivity              |
                        |  (Toolbar + BottomNav + Dynamic FAB)  |
                        +---------------------------------------+
                           /                |                \
        +-----------------+                 |                 +-----------------+
        v                                   v                                   v
+--------------------+            +-------------------+               +--------------------+
|   TasksFragment    |            |   NotesFragment   |               |  Toolbar Actions   |
| (Thống kê, Lọc,    |            | (Danh sách / Lưới |               +--------------------+
|  Sắp xếp, DS Việc) |            |  2 cột, Ghim)     |                  /              \
+--------------------+            +-------------------+                 v                v
        |                                   |                 +------------------+ +------------------+
        v (Thêm / Xem / Sửa)                v (Tạo / Sửa)     |  SearchActivity  | | SettingsActivity |
+--------------------+            +-------------------+       | (Tìm chung cả    | | (Theme, Ngôn ngữ,|
|AddEditTaskActivity |<-----------|AddEditNoteActivity|<------|  Việc & Ghi chú) | |  Backup JSON)    |
+--------------------+ (Chuyển    +-------------------+       +------------------+ +------------------+
        ^               thành việc)         ^
        |                                   |
 (Mở từ Notification)              (Nhận ACTION_SEND
  ReminderReceiver                  từ ứng dụng khác)
```

---

## 4. LỘ TRÌNH MILESTONE & PHÂN BỔ THỜI GIAN (14 NGÀY — NHÓM 3 NGƯỜI)

| Milestone | Tên giai đoạn | Các Step | Thời gian dự kiến | Nội dung chính |
| :--- | :--- | :--- | :--- | :--- |
| **M0** | Khởi tạo & Khung nền | `M0-S1`, `M0-S2` | Ngày 1 | Tạo project trong Quail 4, cấu hình Gradle + ViewBinding + Room, thiết lập màu sắc Sáng/Tối, chuỗi đa ngôn ngữ Việt/Anh, Vector Icons và `AppExecutors`. |
| **M1** | Walking Skeleton | `M1-S1`, `M1-S2` | Ngày 2 – 3 | Xây dựng hoàn chỉnh tầng Room DB (`TaskEntity`, `NoteEntity`, `TaskDao`, `NoteDao`, `AppDatabase`) + Unit/DAO Test + Luồng dọc đầu tiên: hiển thị danh sách công việc và thêm nhanh công việc chạy được trên máy ảo Pixel 7. |
| **M2** | Quản lý Công việc hoàn chỉnh | `M2-S1`, `M2-S2`, `M2-S3` | Ngày 4 – 6 | Hoàn thiện `AddEditTaskActivity` (Độ ưu tiên, Nhãn, Hạn chót, Subtasks), Chỉnh sửa, Xóa + Undo, Đánh dấu hoàn thành, Thống kê tiến độ, Lọc & Sắp xếp (UC 3.1 – 3.5, 3.11, 3.12). |
| **M3** | Quản lý Ghi chú nhanh & Kết hợp | `M3-S1`, `M3-S2` | Ngày 7 – 8 | Hoàn thiện `NotesFragment` (Danh sách/Lưới 2 cột, Ghim, 6 màu thẻ), `AddEditNoteActivity`, Xóa + Undo, Chuyển Ghi chú thành Công việc, Nhận văn bản chia sẻ `ACTION_SEND` (UC 3.6 – 3.9). |
| **M4** | Nhắc nhở & Báo thức ngoại tuyến | `M4-S1`, `M4-S2` | Ngày 9 – 10 | Xây dựng `ReminderScheduler`, `ReminderReceiver`, nút "Hoàn thành ngay" trên Notification, `BootCompletedReceiver`, xin quyền Android 12/13+ (UC 3.13 – 3.15). |
| **M5** | Tìm kiếm tổng hợp, Cài đặt & Sao lưu | `M5-S1`, `M5-S2` | Ngày 11 – 12 | Xây dựng `SearchActivity` (UC 3.10), `SettingsActivity` (Dark mode, Tiếng Việt/Anh, Xuất/Nhập JSON qua SAF), ProGuard/R8, kiểm thử tổng thể. |
| **GĐ 6** | Đóng gói & Báo cáo (Buffer) | Theo yêu cầu | Ngày 13 – 14 | Thời gian đệm (buffer), xuất APK, chụp ảnh màn hình, hoàn thiện Báo cáo Word/PDF theo đúng barem. |

---

## 5. BẢNG QUẢN TRỊ RỦI RO KỸ THUẬT

| Rủi ro | Mức độ | Giải pháp phòng ngừa ngay từ thiết kế |
| :--- | :--- | :--- |
| Lệch phiên bản AGP/Gradle trên Android Studio Quail 4 | Cao | Không ghi đè file `gradle-wrapper.properties` hay phiên bản plugin AGP ở thư mục gốc; chỉ thêm các dependency thư viện chuẩn vào `app/build.gradle` (hoặc `build.gradle.kts`). |
| Crash `SecurityException` khi đặt báo thức trên Android 12–15 | Cao | Trong `ReminderScheduler`, luôn kiểm tra `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S` và `alarmManager.canScheduleExactAlarms()`. Nếu `false`, tự động fallback sang `alarmManager.setWindow(...)` và nhắc người dùng cấp quyền. |
| Lỗi `IllegalStateException: Cannot access database on the main thread` | Cao | Mọi hàm ghi dữ liệu trong `TaskRepository` / `NoteRepository` đều bọc trong `AppExecutors.getInstance().diskIO().execute(...)`. |
| Mất dữ liệu đang nhập khi xoay ngang màn hình | Trung bình | Sử dụng `ViewModel` và `onSaveInstanceState` trong `AddEditTaskActivity` / `AddEditNoteActivity`. |

---

## 6. CHECKLIST TRƯỚC KHI NỘP BÀI (PRE-SUBMISSION CHECKLIST)
- [ ] Chạy `.\gradlew.bat testDebugUnitTest` báo **BUILD SUCCESSFUL** (100% test pass).
- [ ] Chạy `.\gradlew.bat connectedDebugAndroidTest` trên máy ảo Pixel 7 báo **BUILD SUCCESSFUL**.
- [ ] Chạy `.\gradlew.bat lintDebug` không có lỗi Fatal/Error.
- [ ] Kiểm tra đủ 15 Use Case trên máy ảo Pixel 7 (bao gồm cả trường hợp nhập rỗng, hủy giữa chừng, xóa + hoàn tác).
- [ ] Xoay ngang/dọc màn hình ở tất cả các Activity không bị văng app hay mất nội dung đang nhập.
- [ ] Chuyển đổi giao diện Sáng / Tối và ngôn ngữ Tiếng Việt / Tiếng Anh hoạt động mượt mà.
- [ ] Đặt nhắc nhở sau 1 phút -> Khóa màn hình máy ảo -> Thông báo nổ đúng giờ -> Bấm nút "Hoàn thành ngay" trên thông báo -> Công việc tự động chuyển sang Đã hoàn thành.
