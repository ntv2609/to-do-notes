# ĐẶC TẢ YÊU CẦU PHẦN MỀM (SOFTWARE REQUIREMENTS SPECIFICATION) - v1.0

- **Tên đề tài:** Xây dựng ứng dụng thiết bị di động cho phép quản lý công việc hằng ngày kết hợp với ghi chú nhanh cho người dùng cá nhân trên nền tảng Java và Android.
- **Tên ứng dụng:** TodoNotes (Quản lý Công việc & Ghi chú nhanh)
- **Package Name:** `com.vinhnt.todonotes`
- **Phiên bản tài liệu:** 1.0 (Baseline đã chốt)
- **Ngày chốt:** 2026-11-15
- **Quy mô thực hiện:** Nhóm 3 thành viên — Thời gian: 14 ngày (15/11/2026 – 29/11/2026)

---

## 1. TỔNG QUAN & PHẠM VI SẢN PHẨM (MOSCOW)

### 1.1. Must Have (Bắt buộc có trong MVP - Đáp ứng 100% 15 Use Case gốc)
- **M-01 (UC 3.1):** Xem danh sách công việc lưu cục bộ, hiển thị thẻ tổng quan tiến độ (Tổng số, Đã xong, Quá hạn, % hoàn thành).
- **M-02 (UC 3.2):** Thêm công việc mới với kiểm tra dữ liệu đầu vào (Tiêu đề bắt buộc, Mô tả, Thời hạn, Độ ưu tiên, Nhãn phân loại, Công việc con, Nhắc nhở).
- **M-03 (UC 3.3):** Xem chi tiết và chỉnh sửa công việc trực tiếp trên một màn hình thống nhất; tự động đồng bộ lại lịch nhắc nhở nếu đổi thời hạn/giờ nhắc; cảnh báo nếu thoát khi có thay đổi chưa lưu.
- **M-04 (UC 3.4):** Xóa công việc có hộp thoại xác nhận (`MaterialAlertDialog`), tự động hủy báo thức liên kết, kèm thanh `Snackbar` cho phép Hoàn tác (Undo) trong 5 giây.
- **M-05 (UC 3.5):** Đánh dấu hoàn thành / chưa hoàn thành công việc tức thì từ danh sách hoặc màn hình chi tiết; tự động hủy nhắc nhở khi hoàn thành và đặt lại nhắc nhở (nếu còn trong tương lai) khi bỏ hoàn thành.
- **M-06 (UC 3.6):** Tạo ghi chú nhanh (Tiêu đề hoặc Nội dung không được đồng thời để trống), chọn màu thẻ, ghim ghi chú.
- **M-07 (UC 3.7):** Xem danh sách ghi chú và xem chi tiết ghi chú; hỗ trợ chuyển đổi bố cục Danh sách (1 cột) và Lưới (2 cột so le - Staggered Grid).
- **M-08 (UC 3.8):** Chỉnh sửa ghi chú trực tiếp, kiểm tra không rỗng hoàn toàn, cảnh báo thoát khi chưa lưu.
- **M-09 (UC 3.9):** Xóa ghi chú có hộp thoại xác nhận kèm `Snackbar` Hoàn tác (Undo).
- **M-10 (UC 3.10):** Tìm kiếm từ khóa ngoại tuyến theo thời gian thực trên cả Công việc và Ghi chú (hỗ trợ lọc ngay trên từng tab và màn hình Tìm kiếm tổng hợp `SearchActivity` hiển thị cả hai nhóm kết quả).
- **M-11 (UC 3.11):** Lọc công việc theo trạng thái (Tất cả, Chưa hoàn thành, Đã hoàn thành, Quá hạn, Hôm nay) và theo Nhãn phân loại (Category).
- **M-12 (UC 3.12):** Sắp xếp công việc theo Thời gian tạo, Thời hạn hoàn thành (`dueDate`), hoặc Mức độ ưu tiên (`priority`), hỗ trợ đảo chiều Tăng dần / Giảm dần và lưu tùy chọn vào `SharedPreferences`.
- **M-13 (UC 3.13, 3.14, 3.15):** Thiết lập, thay đổi, hủy và đồng bộ trạng thái nhắc nhở ngoại tuyến bằng `AlarmManager`, xử lý quyền Android 12/13+, khôi phục lịch nhắc khi khởi động lại máy (`BOOT_COMPLETED`) hoặc đổi giờ hệ thống.

### 1.2. Should Have (Nên có - Tăng điểm đồ án & chất lượng thực tế)
- **S-01:** Danh sách công việc con (Checklist / Subtasks) ngay trong mỗi công việc.
- **S-02:** Chuyển đổi nhanh 1 chạm từ một Ghi chú sang Công việc mới (điền sẵn tiêu đề & mô tả).
- **S-03:** Nhận văn bản chia sẻ từ ứng dụng khác (`Intent.ACTION_SEND` kiểu `text/plain`) mở thẳng vào màn hình tạo Ghi chú nhanh.
- **S-04:** Thao tác nhanh **"Hoàn thành ngay"** (`Notification Action`) trực tiếp trên thông báo nhắc việc mà không cần mở ứng dụng.
- **S-05:** Màn hình Cài đặt (Settings): Chuyển đổi Sáng / Tối / Theo hệ thống, đổi ngôn ngữ Tiếng Việt / Tiếng Anh, Sao lưu (Export) & Khôi phục (Import) toàn bộ dữ liệu ra file `.json` qua Storage Access Framework (SAF).

### 1.3. Could Have (Có thể làm nếu dư thời gian)
- **C-01:** Vuốt ngang (Swipe) để đánh dấu hoàn thành nhanh công việc bên cạnh nút Checkbox.

### 1.4. Won't Have (Không làm trong phạm vi đồ án này)
- **W-01:** Không làm đăng nhập tài khoản, không đồng bộ máy chủ đám mây (giữ kiến trúc thuần Offline-first).
- **W-02:** Không làm công việc lặp lại theo chu kỳ phức tạp (RRULE) và không đính kèm file ảnh/âm thanh dung lượng lớn vào DB.

---

## 2. THIẾT KẾ CƠ SỞ DỮ LIỆU CỤC BỘ (ROOM SQLITE)

- **Tên Database:** `todonotes_database.db`
- **Phiên bản Schema khởi điểm:** `version = 1` (`exportSchema = true`)
- **Quy ước thời gian:** Mọi cột thời gian đều lưu dưới dạng `long` (Epoch milliseconds theo UTC). Giá trị `null` (dùng kiểu `Long` đối tượng trong Java) đại diện cho "Chưa thiết lập".

### 2.1. Thực thể `TaskEntity` (Bảng `tasks`)

| Tên cột (SQLite) | Trường Java | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `id` | `long` | `PRIMARY KEY AUTOINCREMENT` | Mã định danh duy nhất của công việc |
| `title` | `title` | `String` (`TEXT`) | `@NonNull` | Tiêu đề công việc (1 – 200 ký tự, đã `trim()`) |
| `description` | `description` | `String` (`TEXT`) | `@NonNull` (mặc định `""`) | Mô tả chi tiết công việc (tối đa 5.000 ký tự) |
| `due_date` | `dueDate` | `Long` (`INTEGER`) | Nullable | Thời hạn hoàn thành (Epoch ms). `null` nếu không có hạn |
| `reminder_time` | `reminderTime` | `Long` (`INTEGER`) | Nullable | Thời điểm phát thông báo nhắc nhở (Epoch ms) |
| `reminder_fired` | `reminderFired` | `boolean` (`INTEGER`) | `NOT NULL DEFAULT 0` | `true` nếu thông báo nhắc nhở đã được phát |
| `priority` | `priority` | `int` (`INTEGER`) | `NOT NULL DEFAULT 1` | `0` = Thấp, `1` = Trung bình, `2` = Cao |
| `category` | `category` | `int` (`INTEGER`) | `NOT NULL DEFAULT 0` | `0` = Cá nhân, `1` = Học tập, `2` = Công việc, `3` = Mua sắm, `4` = Khác |
| `subtasks_json` | `subtasksJson` | `String` (`TEXT`) | `@NonNull` (mặc định `"[]"`) | Mảng JSON các công việc con `[{"title":"...","done":false}]` |
| `is_completed` | `completed` | `boolean` (`INTEGER`) | `NOT NULL DEFAULT 0` | `false` = Chưa hoàn thành, `true` = Đã hoàn thành |
| `completed_at` | `completedAt` | `Long` (`INTEGER`) | Nullable | Thời điểm đánh dấu hoàn thành (Epoch ms) |
| `created_at` | `createdAt` | `long` (`INTEGER`) | `NOT NULL` | Thời điểm tạo bản ghi (Epoch ms) |
| `updated_at` | `updatedAt` | `long` (`INTEGER`) | `NOT NULL` | Thời điểm cập nhật gần nhất (Epoch ms) |

**Chỉ mục (`@Index`) trên bảng `tasks`:**
- `idx_tasks_completed_due`: `{"is_completed", "due_date"}`
- `idx_tasks_priority`: `{"priority"}`
- `idx_tasks_category`: `{"category"}`
- `idx_tasks_reminder`: `{"is_completed", "reminder_time"}`

### 2.2. Thực thể `NoteEntity` (Bảng `notes`)

| Tên cột (SQLite) | Trường Java | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `id` | `long` | `PRIMARY KEY AUTOINCREMENT` | Mã định danh duy nhất của ghi chú |
| `title` | `title` | `String` (`TEXT`) | `@NonNull` (mặc định `""`) | Tiêu đề ghi chú (tối đa 200 ký tự) |
| `content` | `content` | `String` (`TEXT`) | `@NonNull` (mặc định `""`) | Nội dung văn bản ghi chú (tối đa 20.000 ký tự) |
| `color_index` | `colorIndex` | `int` (`INTEGER`) | `NOT NULL DEFAULT 0` | Mã màu thẻ từ `0` đến `5` (Mặc định, Vàng ấm, Xanh lá, Xanh dương, Hồng, Tím) |
| `is_pinned` | `pinned` | `boolean` (`INTEGER`) | `NOT NULL DEFAULT 0` | `true` = Ghim lên đầu danh sách |
| `created_at` | `createdAt` | `long` (`INTEGER`) | `NOT NULL` | Thời điểm tạo ghi chú (Epoch ms) |
| `updated_at` | `updatedAt` | `long` (`INTEGER`) | `NOT NULL` | Thời điểm chỉnh sửa gần nhất (Epoch ms) |

**Chỉ mục (`@Index`) trên bảng `notes`:**
- `idx_notes_pinned_updated`: `{"is_pinned", "updated_at"}`

---

## 3. QUY TẮC NGHIỆP VỤ CHI TIẾT (BUSINESS RULES)

1. **BR-01 (Kiểm tra hợp lệ Công việc - UC 3.2, 3.3):**
    - `title.trim()` không được rỗng và không vượt quá 200 ký tự. Nếu rỗng, hiển thị lỗi trực tiếp trên `TextInputLayout` và không lưu.
    - `description` tối đa 5.000 ký tự.
    - Khi tạo mới hoặc sửa thời điểm nhắc nhở (`reminderTime`), nếu người dùng bật nhắc nhở thì `reminderTime` bắt buộc phải lớn hơn thời gian hiện tại của thiết bị (`reminderTime > System.currentTimeMillis()`).
2. **BR-02 (Quan hệ giữa Thời hạn `dueDate` và Nhắc nhở `reminderTime` - UC 3.13, 3.15):**
    - Công việc có thể có `dueDate` mà không có `reminderTime`, hoặc có `reminderTime` mà không có `dueDate`.
    - Khi người dùng chọn `dueDate` mà chưa đặt `reminderTime`, giao diện cho phép bật nhanh tùy chọn "Nhắc đúng hạn" hoặc "Nhắc trước 15 phút / 1 giờ".
    - Nếu công việc đang có cả `dueDate` và `reminderTime`, khi người dùng chỉnh sửa `dueDate` lùi về thời điểm sớm hơn `reminderTime` hiện tại (UC 3.15 rẽ nhánh 2): Hệ thống hiển thị cảnh báo và tự động cập nhật `reminderTime = dueDate` (nếu `dueDate > now`) hoặc tắt nhắc nhở nếu người dùng chọn không nhắc.
3. **BR-03 (Trạng thái Quá hạn - Overdue):**
    - Công việc được coi là **Quá hạn** khi và chỉ khi: `!isCompleted && dueDate != null && dueDate < System.currentTimeMillis()`.
    - Trạng thái Quá hạn được tính toán động khi hiển thị giao diện và khi lọc, không lưu cứng thành cột dư thừa trong DB để tránh lệch trạng thái khi thời gian trôi qua.
4. **BR-04 (Đồng bộ trạng thái Hoàn thành & Nhắc nhở - UC 3.5, 3.15):**
    - Khi chuyển công việc sang `isCompleted = true`: Cập nhật `completedAt = now`, gọi `ReminderScheduler.cancelReminder(taskId)` để hủy báo thức trên hệ điều hành.
    - Khi chuyển công việc từ Đã hoàn thành về `isCompleted = false`: Đặt `completedAt = null`. Nếu `reminderTime != null && reminderTime > now`, đặt lại báo thức và gán `reminderFired = false`.
5. **BR-05 (Xóa và Hoàn tác - UC 3.4, 3.9):**
    - Mọi thao tác xóa Công việc hoặc Ghi chú đều phải qua hộp thoại xác nhận `MaterialAlertDialog`.
    - Sau khi người dùng bấm "Xóa" trên hộp thoại: bản ghi bị xóa khỏi Room DB (kèm hủy báo thức nếu là Công việc), đồng thời màn hình danh sách hiện `Snackbar` có nút **"Hoàn tác" (Undo)**. Nếu người dùng bấm "Hoàn tác", hệ thống chèn lại bản ghi gốc (giữ nguyên `id`) vào Room DB và khôi phục báo thức nếu `reminderTime > now`.
6. **BR-06 (Kiểm tra hợp lệ Ghi chú nhanh - UC 3.6, 3.8):**
    - Ghi chú hợp lệ khi ` !title.trim().isEmpty() || !content.trim().isEmpty() `.
    - Nếu cả tiêu đề và nội dung đều trống, hệ thống báo lỗi "Vui lòng nhập tiêu đề hoặc nội dung ghi chú" và không lưu.
    - Nếu tiêu đề trống nhưng nội dung có chữ, khi hiển thị trên danh sách sẽ hiển thị phần nội dung làm dòng chính.
7. **BR-07 (Chuyển Ghi chú thành Công việc - UC 3.8 Điểm mở rộng):**
    - Tại màn hình chỉnh sửa ghi chú, khi bấm nút "Chuyển thành công việc", hệ thống mở `AddEditTaskActivity` với `title` lấy từ tiêu đề ghi chú (nếu tiêu đề ghi chú trống thì lấy dòng đầu tiên của nội dung, cắt tối đa 100 ký tự) và `description` lấy từ toàn bộ nội dung ghi chú. Ghi chú gốc vẫn được giữ nguyên (không tự ý xóa).

---

## 4. KIẾN TRÚC MÀN HÌNH & LUỒNG ĐIỀU HƯỚNG (UI/UX)

1. **`MainActivity`:**
    - Thanh công cụ phía trên (`MaterialToolbar`): Tiêu đề màn hình, nút mở **Tìm kiếm tổng hợp (`SearchActivity`)**, nút **Chuyển chế độ lưới/danh sách** (khi ở tab Ghi chú) hoặc nút **Sắp xếp** (khi ở tab Công việc), và nút mở **Cài đặt (`SettingsActivity`)**.
    - Khu vực nội dung chính (`FragmentContainerView` hoặc `ViewPager2` không vuốt): Chứa `TasksFragment` và `NotesFragment`.
    - Thanh điều hướng dưới đáy (`BottomNavigationView`): 2 tab **"Công việc"** và **"Ghi chú nhanh"**.
    - Nút hành động nổi (`FloatingActionButton`): Đổi nhãn/biểu tượng theo tab đang mở (Thêm công việc / Tạo ghi chú).
2. **`TasksFragment` (Màn hình Quản lý Công việc):**
    - Thẻ thống kê tiến độ (Progress Card): Hiển thị số việc đã hoàn thành / tổng số việc, số việc quá hạn và thanh `LinearProgressIndicator`.
    - Thanh tìm kiếm nhanh trong tab + Dãy `ChipGroup` lọc theo Trạng thái (`Tất cả`, `Chưa xong`, `Đã xong`, `Quá hạn`, `Hôm nay`) và lọc theo Nhãn (`Tất cả nhãn`, `Cá nhân`, `Học tập`, `Công việc`, `Mua sắm`, `Khác`).
    - Danh sách `RecyclerView` (`TaskAdapter` kế thừa `ListAdapter` + `DiffUtil`).
    - `EmptyStateView`: Hiển thị hình minh họa vector và nút gợi ý khi danh sách trống hoặc không khớp bộ lọc.
3. **`AddEditTaskActivity` (Màn hình Thêm / Xem chi tiết / Sửa Công việc):**
    - Nhập Tiêu đề, Mô tả, chọn Độ ưu tiên (`ChipGroup`), chọn Nhãn phân loại (`ChipGroup`), chọn Ngày & Giờ hạn chót (`MaterialDatePicker` + `MaterialTimePicker`), bật/chọn Ngày & Giờ nhắc nhở, thêm/xóa/tích chọn các mục Công việc con (Subtasks).
    - Menu góc trên: Nút Lưu, nút Xóa (chỉ hiện khi đang xem/sửa công việc đã có).
    - Bắt sự kiện `OnBackPressedCallback`: Nếu dữ liệu đã bị sửa đổi so với ban đầu mà chưa bấm Lưu, hiện hộp thoại hỏi "Hủy thay đổi / Tiếp tục chỉnh sửa".
4. **`NotesFragment` (Màn hình Quản lý Ghi chú nhanh):**
    - Thanh tìm kiếm nhanh trong tab + Nút lọc Ghi chú đã ghim.
    - `RecyclerView` hỗ trợ đổi động giữa `LinearLayoutManager` (1 cột) và `StaggeredGridLayoutManager` (2 cột).
    - `EmptyStateView` khi chưa có ghi chú.
5. **`AddEditNoteActivity` (Màn hình Tạo / Xem chi tiết / Sửa Ghi chú & Nhận chia sẻ văn bản):**
    - Thanh công cụ: Nút Quay lại, nút Ghim (`isPinned`), nút Chuyển thành công việc, nút Xóa (nếu đang sửa), nút Lưu.
    - Bộ chọn 6 màu nền thẻ (`ColorPickerRow`).
    - Ô nhập Tiêu đề, nhãn hiển thị thời gian cập nhật gần nhất, ô nhập Nội dung đa dòng.
    - Hỗ trợ nhận `Intent.ACTION_SEND` (`text/plain`) từ trình duyệt/ứng dụng khác để điền sẵn nội dung.
6. **`SearchActivity` (Màn hình Tìm kiếm Tổng hợp - UC 3.10):**
    - Ô nhập từ khóa tự động focus kèm nút xóa nhanh từ khóa.
    - Hiển thị kết quả tìm kiếm trực tiếp từ cả bảng `tasks` và bảng `notes` (phân khu rõ ràng hoặc lọc bằng Chip `Tất cả`, `Công việc`, `Ghi chú`). Bấm vào mục nào sẽ mở màn hình chi tiết tương ứng.
7. **`SettingsActivity` (Màn hình Cài đặt & Sao lưu dữ liệu):**
    - Chọn Giao diện (Theo hệ thống / Sáng / Tối).
    - Chọn Ngôn ngữ hiển thị (Tiếng Việt / Tiếng Anh).
    - Trạng thái quyền Thông báo & Báo thức chính xác (kèm nút mở nhanh Cài đặt hệ thống nếu bị tắt).
    - Sao lưu dữ liệu ra file JSON (`ACTION_CREATE_DOCUMENT`) và Khôi phục dữ liệu từ file JSON (`ACTION_OPEN_DOCUMENT`).

---

## 5. YÊU CẦU PHI CHỨC NĂNG & KỸ THUẬT ANDROID

1. **Hiệu năng & Đa luồng:** Tuyệt đối không truy vấn Room DB trên Main Thread. Mọi thao tác ghi (`insert`, `update`, `delete`) và truy vấn một lần chạy qua `AppExecutors` (`ExecutorService` 4 luồng cho DB I/O); truy vấn danh sách dùng `LiveData` do Room tự quản lý luồng nền.
2. **Xử lý xoay màn hình & Process Death:** Lưu trạng thái bộ lọc/từ khóa tìm kiếm trong `ViewModel` + `SavedStateHandle`; lưu trạng thái form đang nhập dở trong `onSaveInstanceState`.
3. **Thông báo & Báo thức trên Android 12/13/14/15:**
    - Tạo `NotificationChannel` (`IMPORTANCE_HIGH`) ngay khi khởi chạy `TodoNotesApp`.
    - Xin quyền `POST_NOTIFICATIONS` trên Android 13+ (API 33+) đúng thời điểm người dùng bật nhắc nhở lần đầu.
    - Kiểm tra `AlarmManager.canScheduleExactAlarms()` trên Android 12+ (API 31+); nếu chưa được cấp quyền, hiển thị hộp thoại giải thích và mở `ACTION_REQUEST_SCHEDULE_EXACT_ALARM`, đồng thời có cơ chế fallback an toàn về `setWindow()` để ứng dụng không bao giờ bị văng (`SecurityException`).
4. **Bảo mật & Quyền riêng tư:**
    - Chỉ khai báo các quyền thực sự cần thiết: `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `VIBRATE`.
    - Đặt `android:allowBackup="false"` trong `AndroidManifest.xml` (người dùng chủ động sao lưu qua tính năng Xuất JSON).
5. **Đa ngôn ngữ, Dark Mode & Accessibility:**
    - 100% chuỗi hiển thị cho người dùng nằm trong `res/values/strings.xml` (Tiếng Việt mặc định) và `res/values-en/strings.xml` (Tiếng Anh).
    - Có đủ bảng màu `res/values/colors.xml` và `res/values-night/colors.xml` (kể cả 6 màu thẻ ghi chú cũng tự động đổi tông trầm dịu mắt khi sang Dark Mode).
    - Mọi nút chỉ có icon (`ImageButton`, `FloatingActionButton`) đều có `android:contentDescription` và kích thước vùng chạm tối thiểu `48dp x 48dp`.
