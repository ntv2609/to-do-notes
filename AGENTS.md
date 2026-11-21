# QUY TẮC LÀM VIỆC DÀNH CHO AI TRONG DỰ ÁN (AGENTS.md)

Tài liệu này là bộ luật bắt buộc phải đính kèm vào đầu mỗi cuộc trò chuyện (Chat hoặc Tasks) ở Giai đoạn 5 (Build). Mọi phản hồi sinh code phải tuân thủ nghiêm ngặt các quy định dưới đây.

## 1. THÔNG TIN KỸ THUẬT CỐ ĐỊNH
- **Package gốc:** `com.vinhnt.todonotes`
- **Ngôn ngữ:** Java (JDK 17). **TUYỆT ĐỐI CẤM** xuất file `.kt` (Kotlin) hoặc dùng Jetpack Compose.
- **Giao diện:** XML Layout + `ViewBinding` + Material Components (`com.google.android.material`).
- **Kiến trúc:** `UI (Activity/Fragment/Adapter)` -> `ViewModel (AndroidViewModel/ViewModel + LiveData)` -> `Repository` -> `Room DAO + AppExecutors`.
- **Nguồn sự thật duy nhất:** `docs/SPEC.md` và `docs/DECISIONS.md`. Không tự ý thêm/bớt trường dữ liệu hoặc đổi quy tắc nghiệp vụ nếu chưa cập nhật hai file này.

## 2. QUY ƯỚC ĐẶT TÊN & CHẤT LƯỢNG CODE
1. **Java Classes:** `PascalCase` (`TaskEntity`, `TaskDao`, `TaskRepository`, `TasksViewModel`, `AddEditTaskActivity`).
2. **Layout XML:** `activity_*.xml`, `fragment_*.xml`, `item_*.xml`, `dialog_*.xml`, `layout_*.xml`.
3. **Resource IDs:** `snake_case` có tiền tố loại view (`tv_task_title`, `cb_task_completed`, `btn_save`, `rv_tasks`, `chip_filter_all`, `til_title`, `et_title`).
4. **Chuỗi văn bản & Màu sắc:**
    - **CẤM** hardcode chữ hiển thị cho người dùng trong file `.java` hoặc `.xml` layout. Mọi chuỗi phải khai báo đồng thời trong `app/src/main/res/values/strings.xml` (Tiếng Việt) và `app/src/main/res/values-en/strings.xml` (Tiếng Anh).
    - Màu sắc khai báo trong `values/colors.xml` và `values-night/colors.xml`.
5. **An toàn luồng & Bộ nhớ:**
    - **CẤM** gọi `.allowMainThreadQueries()` trong code sản phẩm chính (chỉ cho phép trong Instrumentation Test).
    - Trong `Fragment`, phải giải phóng `binding = null` ở `onDestroyView()`.
    - Mọi `PendingIntent` của `AlarmManager` và `Notification` phải có cờ `PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE`.

## 3. ĐỊNH DẠNG XUẤT FILE BẮT BUỘC (DÙNG CHO SCRIPT TÁCH CODE)
1. Toàn bộ các file tạo mới hoặc chỉnh sửa trong một lượt phải nằm trong **DUY NHẤT MỘT khối code markdown** dùng hàng rào **BỐN dấu backtick (` ```` `)**.
2. Dòng đầu tiên bên trong khối: `# MANIFEST: N` (với `N` là số lượng file xuất ra).
3. Mỗi file bắt đầu bằng đúng một dòng `# File: duong/dan/tuong/doi` (dùng dấu `/`, tính từ thư mục gốc dự án), theo sau là **TOÀN BỘ NỘI DUNG ĐẦY ĐỦ** của file đó.
4. Dòng cuối cùng bên trong khối: `# END_OF_BATCH`.
5. **TUYỆT ĐỐI CẤM:**
    - Cấm viết rút gọn kiểu `// ... giữ nguyên ...`, `// TODO`, `/* phần còn lại như cũ */`.
    - Cấm xuất bản vá (diff). Sửa 1 dòng trong file cũ cũng phải xuất lại toàn bộ file đó.
    - Cấm để bất kỳ dòng code hay bình luận nào bên trong nội dung file bắt đầu bằng chuỗi `# File:`.
6. Mọi lời giải thích, báo cáo tiến độ, danh sách hàm đã sửa để đối chiếu `git diff`, và hướng dẫn chạy thử phải đặt **BÊN NGOÀI** khối bốn dấu backtick.

## 4. TRUNG THỰC VỀ KIỂM THỬ & LỆNH GRADLE
- AI không có Android SDK hay máy ảo. **CẤM** tuyên bố "đã build/chạy/test thành công".
- Luôn ghi rõ trạng thái: **CHƯA CHẠY TRÊN MÁY THẬT/EMULATOR** và cung cấp các lệnh để người dùng chạy trong Terminal của Android Studio:
    - Kiểm tra biên dịch Debug: `./gradlew assembleDebug` (Windows: `.\gradlew.bat assembleDebug`)
    - Chạy Unit Test (JUnit): `./gradlew testDebugUnitTest` (Windows: `.\gradlew.bat testDebugUnitTest`)
    - Chạy Instrumentation Test (Room/Android): `./gradlew connectedDebugAndroidTest` (Windows: `.\gradlew.bat connectedDebugAndroidTest`)
    - Kiểm tra Lint: `./gradlew lintDebug` (Windows: `.\gradlew.bat lintDebug`)
- Mỗi khi sửa `AndroidManifest.xml`, `build.gradle(.kts)` hoặc `libs.versions.toml`, bắt buộc nhắc người dùng bấm **"Sync Project with Gradle Files"** trong Android Studio.

## 5. ĐỊNH NGHĨA "HOÀN THÀNH" (DEFINITION OF DONE) CHO MỖI STEP
Một Step chỉ được coi là xong khi:
1. Toàn bộ file trong phạm vi Step được xuất đầy đủ 100%, không thiếu import, không hardcode chuỗi UI.
2. Có kèm Unit Test hoặc Instrumentation Test tương ứng với tầng vừa viết.
3. Có báo cáo 7 mục chuẩn ở ngoài khối code (bao gồm **Bản tóm tắt trạng thái <= 10 dòng** ở cuối).
4. Người dùng xác nhận đã chạy lệnh Gradle / chạy trên máy ảo Pixel 7 không lỗi và gõ `"OK Step tiếp theo"`.
