# BỘ QUY CHUẨN THIẾT KẾ GIAO DIỆN (UI/UX DESIGN SYSTEM) - v1.0

- **Dự án:** TodoNotes — Quản lý Công việc & Ghi chú nhanh
- **Hệ thống thiết kế nền tảng:** Material Design 3 (`Theme.Material3.DayNight.NoActionBar`)
- **Phong cách chủ đạo:** **Minimalism + Soft Glass** (Tối giản, thoáng đãng, bề mặt thẻ bán trong suốt 88%–92% Alpha, viền mảnh `1dp` sắc nét như cạnh kính, bo góc lớn `16dp–24dp`, đổ bóng mềm nhẹ).
- **Ràng buộc kỹ thuật:** Tương thích 100% từ `minSdk 26` (Android 8.0) trở lên; **KHÔNG** dùng Neumorphism, **KHÔNG** phụ thuộc `RenderEffect` blur của Android 12+, **KHÔNG** dùng thư viện UI bên thứ ba, **KHÔNG** nhúng file font hay ảnh bitmap nhị phân (chỉ dùng font hệ thống `sans-serif` và Vector Drawable XML).

---

## 0. ĐỀ XUẤT 3 PHƯƠNG ÁN MÀU CHỦ ĐẠO (PRIMARY ACCENT PALETTES)

| Phương án | Tên phong cách | Light Primary | Dark Primary | Light Primary Container | Dark Primary Container | Đặc trưng cảm xúc & Gợi ý lựa chọn |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **PA 1 (Mặc định khuyến nghị)** | **Indigo Slate** (Xanh Chàm Hiện Đại) | `#4F46E5` | `#818CF8` | `#E0E7FF` | `#1E1B4B` | Sắc nét, chuyên nghiệp, tạo chiều sâu "Soft Glass" đẹp nhất trên nền xám băng (`#F8FAFC`) và xanh đêm (`#0B0F19`). |
| **PA 2** | **Emerald Sage** (Xanh Ngọc Điềm Tĩnh) | `#0D9488` | `#2DD4BF` | `#CCFBF1` | `#134E4A` | Dịu mắt khi nhìn lâu, tạo cảm giác thư giãn, cân bằng cho ứng dụng cá nhân hằng ngày. |
| **PA 3** | **Warm Terracotta** (Cam Đất Năng Động) | `#D97706` | `#FBBF24` | `#FEF3C7` | `#451A03` | Ấm áp, giàu năng lượng, gần gũi như sổ tay giấy truyền thống kết hợp mặt kính hiện đại. |

> **Lưu ý:** Toàn bộ bảng mã màu chi tiết ở **Mục 1** bên dưới đang sử dụng **Phương án 1 (Indigo Slate)** làm chuẩn mặc định. Nếu chọn Phương án 2 hoặc 3, chỉ cần thay 4 mã `colorPrimary` / `colorPrimaryContainer` tương ứng từ bảng trên vào `colors.xml`.

---

## 1. HỆ THỐNG MÀU SẮC (COLOR SYSTEM — ĐẠT CHUẨN TƯƠNG PHẢN WCAG AA >= 4.5:1)

### 1.1. Bảng màu Nền tảng, Bề mặt Soft Glass & Văn bản (Light & Dark Mode)

Kỹ thuật tạo hiệu ứng **Soft Glass trên Android 8.0+ (`minSdk 26`)**:
- Không dùng blur động gây giật khung hình khi cuộn `RecyclerView`.
- Kết hợp **Nền màn hình có sắc độ tinh tế (`background`)** + **Bề mặt thẻ bán trong suốt (`surface_glass` có kênh Alpha `E6` = 90% hoặc `F0` = 94%)** + **Viền kính mảnh `1dp` (`stroke_glass`)**.

| Tên biến trong `colors.xml` | Vai trò thiết kế | Mã Hex (Light Mode) | Mã Hex (Dark Mode) | Tỷ lệ tương phản (Chữ/Nền) |
| :--- | :--- | :--- | :--- | :--- |
| `color_primary` | Màu nhấn chính (FAB, Checkbox, nút Lưu, trạng thái Active) | `#4F46E5` | `#818CF8` | `6.2:1` (Light) / `7.4:1` (Dark) |
| `color_on_primary` | Chữ/Icon nằm trên nền `color_primary` | `#FFFFFF` | `#0F172A` | `> 7.0:1` |
| `color_primary_container` | Nền Chip đang chọn, nền huy hiệu, vùng nhấn nhẹ | `#E0E7FF` | `#1E1B4B` | `8.5:1` |
| `color_on_primary_container` | Chữ/Icon trên `color_primary_container` | `#1E1B4B` | `#E0E7FF` | `8.5:1` |
| `color_secondary` | Màu nhấn phụ (Subtasks, thống kê phụ, icon công cụ) | `#0EA5E9` | `#38BDF8` | `4.6:1` |
| `color_background` | Nền tổng thể của màn hình (`android:colorBackground`) | `#F4F6FB` | `#0B0F19` | Nền gốc tôn thẻ kính nổi bật |
| `color_surface` | Bề mặt đặc chuẩn (Toolbar, BottomNavigation, Dialog) | `#FFFFFF` | `#111827` | `> 12.0:1` |
| `color_surface_glass` | **Bề mặt thẻ Soft Glass** (92% Alpha trên Light, 88% trên Dark) | `#EBF8FAFC` | `#E01E293B` | Tạo độ trong nhẹ trên nền |
| `color_surface_elevated` | Bề mặt thẻ nổi cao (Progress Card, Search Bar) | `#FFFFFF` | `#1E293B` | `> 10.0:1` |
| `color_glass_stroke` | **Viền kính 1dp** của thẻ Công việc, thẻ Ghi chú, ô Tìm kiếm | `#CBD5E1` | `#334155` | Viền mảnh phản sáng cạnh thẻ |
| `color_text_primary` | Chữ tiêu đề chính, nội dung quan trọng (`onSurface`) | `#0F172A` | `#F8FAFC` | `15.8:1` (Light) / `16.1:1` (Dark) |
| `color_text_secondary` | Chữ mô tả phụ, ngày giờ, phụ chú (`onSurfaceVariant`) | `#475569` | `#94A3B8` | `7.1:1` (Light) / `6.8:1` (Dark) |
| `color_text_disabled` | Chữ công việc đã hoàn thành (gạch ngang) / gợi ý `hint` | `#64748B` | `#64748B` | `4.5:1` |
| `color_divider` | Đường kẻ phân cách mỏng bên trong danh sách con | `#E2E8F0` | `#1E293B` | Nhẹ nhàng, không gây rối mắt |

### 1.2. Bảng màu Độ ưu tiên (Priority) & Trạng thái Quá hạn (Overdue / Error)

Mỗi mức độ ưu tiên sử dụng cặp **Màu chữ/viền đậm + Màu nền huy hiệu (Badge Container)** riêng cho chế độ Sáng và Tối để đảm bảo độ tương phản luôn `>= 4.8:1`:

| Mức độ / Trạng thái | Tên biến (`colors.xml`) | Light Mode (Chữ/Icon) | Light Mode (Nền Badge) | Dark Mode (Chữ/Icon) | Dark Mode (Nền Badge) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Ưu tiên Thấp (`LOW = 0`)** | `priority_low_text` / `_bg` | `#0369A1` (Xanh lam đậm) | `#E0F2FE` | `#7DD3FC` (Xanh băng sáng) | `#0C4A6E` |
| **Ưu tiên Trung bình (`MEDIUM = 1`)** | `priority_medium_text` / `_bg` | `#B45309` (Hổ phách đậm) | `#FEF3C7` | `#FCD34D` (Vàng hổ phách) | `#78350F` |
| **Ưu tiên Cao (`HIGH = 2`)** | `priority_high_text` / `_bg` | `#BE123C` (Đỏ hồng ngọc) | `#FFE4E6` | `#FDA4AF` (Hồng san hô sáng) | `#881337` |
| **Quá hạn / Lỗi (`OVERDUE`)** | `status_overdue_text` / `_bg` | `#B91C1C` (Đỏ cảnh báo) | `#FEE2E2` | `#FCA5A5` (Đỏ sáng dịu) | `#7F1D1D` |
| **Hoàn thành (`COMPLETED`)** | `status_completed_text` / `_bg` | `#15803D` (Xanh lá đậm) | `#DCFCE7` | `#86EFAC` (Xanh bạc hà) | `#14532D` |

### 1.3. Bảng 6 Màu Thẻ Ghi chú Nhanh (Quick Note Card Colors — `colorIndex 0..5`)

- **Quy tắc quan trọng:** Không bao giờ dùng màu pastel sáng chói của Light Mode bê nguyên sang Dark Mode (vì chữ trắng trên nền vàng sáng sẽ mù chữ, còn chữ đen trong Dark Mode gây chói mắt).
- Cả 6 màu thẻ dưới đây đều dùng chung màu chữ chính `color_text_primary` (`#0F172A` ở Light Mode và `#F8FAFC` ở Dark Mode) với tỷ lệ tương phản đạt từ **7.5:1 đến 15.8:1**:

| `colorIndex` | Tên màu hiển thị | Biến nền thẻ (`colors.xml`) | Biến viền 1dp (`colors.xml`) | Light Mode (Nền / Viền) | Dark Mode (Nền kính trầm / Viền) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **`0`** | Mặc định (Kính trắng / Xám đá) | `note_color_0_bg` | `note_color_0_stroke` | `#FFFFFF` / `#CBD5E1` | `#1E293B` / `#334155` |
| **`1`** | Vàng Nắng Ấm (Warm Amber) | `note_color_1_bg` | `note_color_1_stroke` | `#FFFBEB` / `#FDE68A` | `#292112` / `#5C481D` |
| **`2`** | Xanh Bạc Hà (Mint Sage) | `note_color_2_bg` | `note_color_2_stroke` | `#ECFDF5` / `#A7F3D0` | `#112822` / `#1F4E40` |
| **`3`** | Xanh Bầu Trời (Sky Mist) | `note_color_3_bg` | `note_color_3_stroke` | `#F0F9FF` / `#BAE6FD` | `#112233` / `#1E3F5E` |
| **`4`** | Hồng Phấn Nhẹ (Rose Quartz) | `note_color_4_bg` | `note_color_4_stroke` | `#FFF1F2` / `#FECDD3` | `#2D161E` / `#592838` |
| **`5`** | Tím Oải Hương (Soft Lavender) | `note_color_5_bg` | `note_color_5_stroke` | `#F5F3FF` / `#DDD6FE` | `#201833` / `#423068` |

---

## 2. HỆ THỐNG KIỂU CHỮ (TYPOGRAPHY SYSTEM)

- **Font chữ:** Sử dụng hoàn toàn họ font hệ thống Android (`sans-serif` cho nội dung thường và `sans-serif-medium` cho tiêu đề/nhãn nhấn mạnh). Không nhúng file `.ttf`/`.otf`.
- **Đơn vị đo:** Bắt buộc dùng `sp` cho mọi kích thước chữ để tự động co giãn khi người dùng bật "Cỡ chữ lớn" trong Cài đặt Android.

| Vai trò hiển thị | Thuộc tính TextAppearance / Style | Font Family | Cỡ chữ (`sp`) | Độ đậm (`textStyle`) | Line Spacing Extra | Màu chữ mặc định |
| :--- | :--- | :--- | :---: | :--- | :---: | :--- |
| **Tiêu đề màn hình (Toolbar Title)** | `TextAppearance.TodoNotes.ToolbarTitle` | `sans-serif-medium` | `20sp` | `bold` | `2dp` | `color_text_primary` |
| **Chỉ số thống kê lớn (Progress %)** | `TextAppearance.TodoNotes.HeadlineMetric` | `sans-serif-medium` | `24sp` | `bold` | `2dp` | `color_primary` |
| **Tiêu đề mục (Task / Note Title)** | `TextAppearance.TodoNotes.ItemTitle` | `sans-serif-medium` | `16sp` | `normal` (500) | `3dp` | `color_text_primary` |
| **Ô nhập tiêu đề lớn (Add/Edit Title)** | `TextAppearance.TodoNotes.EditorTitle` | `sans-serif-medium` | `20sp` | `bold` | `4dp` | `color_text_primary` |
| **Nội dung chính (Mô tả / Ghi chú)** | `TextAppearance.TodoNotes.Body` | `sans-serif` | `14sp` | `normal` (400) | `4dp` | `color_text_secondary` (ở list) / `color_text_primary` (ở editor) |
| **Phụ chú & Thời gian (Caption / Meta)** | `TextAppearance.TodoNotes.Caption` | `sans-serif` | `12sp` | `normal` (400) | `2dp` | `color_text_secondary` |
| **Nhãn Huy hiệu & Chip (Badge / Chip)** | `TextAppearance.TodoNotes.Badge` | `sans-serif-medium` | `12sp` | `normal` (500) | `0dp` | Theo màu ngữ cảnh |

---

## 3. HÌNH HỌC BO GÓC, VIỀN KÍNH, ĐỘ CAO & LƯỚI KHOẢNG CÁCH (4/8/16/24DP GRID)

### 3.1. Quy chuẩn Bo góc & Viền Soft Glass (Shape & Stroke)
- **Thẻ thống kê tiến độ (Hero Progress Card):** Bo góc `20dp`, viền `1dp` (`color_glass_stroke`), `cardElevation = 0dp`, nền `color_surface_elevated`.
- **Thẻ Công việc (`item_task.xml`) & Thẻ Ghi chú (`item_note.xml`):** Bo góc `16dp`, viền `1dp` (`color_glass_stroke` hoặc `note_color_X_stroke`), `cardElevation = 1dp`, nền `color_surface_glass` (hoặc `note_color_X_bg`).
- **Ô nhập liệu & Thanh tìm kiếm (`TextInputLayout`, Search Bar):** Bo góc `16dp` (hoặc `24dp` dạng viên thuốc cho thanh tìm kiếm nhanh), viền `1dp`.
- **Chip lọc & Huy hiệu (Filter Chip / Priority Badge):** Bo góc `12dp` (tạo nét hiện đại, gọn gàng hơn kiểu bo tròn 50% cổ điển).
- **Nút hành động nổi (`ExtendedFloatingActionButton` / `FloatingActionButton`):** Bo góc `16dp`, `elevation = 4dp`.
- **Hộp thoại xác nhận (`MaterialAlertDialog`):** Bo góc `24dp`, viền `1dp`.

### 3.2. Quy chuẩn Lưới khoảng cách (Spacing Grid: `4dp / 8dp / 16dp / 24dp`)
- **Lề ngang màn hình (Screen Horizontal Padding):** `16dp` ở cả hai bên trái/phải.
- **Khoảng cách giữa 2 thẻ trong `RecyclerView`:** `8dp` (dọc) và `8dp` (ngang khi ở chế độ Lưới 2 cột).
- **Đệm bên trong thẻ (Card Internal Padding):** `16dp` (cho thẻ Công việc và thẻ Thống kê), `14dp–16dp` (cho thẻ Ghi chú).
- **Khoảng cách giữa các phần tử liên quan trong cùng 1 thẻ:** `4dp` (giữa Tiêu đề và Mô tả ngắn) và `8dp` (giữa Mô tả và hàng Huy hiệu ngày giờ/ưu tiên).
- **Khoảng cách giữa các khối chức năng lớn (Section Spacing):** `16dp` hoặc `24dp`.
- **Khoảng trống cuối danh sách (`RecyclerView` `paddingBottom`):** `88dp` kèm `android:clipToPadding="false"` để phần tử cuối cùng không bao giờ bị nút `FAB` che khuất.

---

## 4. ĐẶC TẢ CHI TIẾT TỪNG THÀNH PHẦN GIAO DIỆN (COMPONENT SPECIFICATIONS)

### 4.1. Thẻ Thống kê Tiến độ (`Hero Progress Card` — Đầu màn hình Công việc)
- Sử dụng `MaterialCardView` bo góc `20dp`, viền `1dp` `@color/color_glass_stroke`, nền `@color/color_surface_elevated`, đệm trong `16dp`.
- **Hàng trên:** Bên trái là tiêu đề *"Tiến độ công việc"* (`16sp`, `sans-serif-medium`) và dòng phụ *"X/Y hoàn thành • Z quá hạn"* (`12sp`); bên phải là con số phần trăm nổi bật (ví dụ `65%`, `24sp`, `bold`, màu `@color/color_primary`).
- **Hàng dưới (cách `12dp`):** Thanh `LinearProgressIndicator` cao `8dp`, bo tròn đầu (`trackCornerRadius="4dp"`), màu nền rãnh `@color/color_primary_container`, màu thanh chạy `@color/color_primary`.

### 4.2. Thẻ Công việc (`Task Card` — `item_task.xml`)
- `MaterialCardView` bo góc `16dp`, viền `1dp` `@color/color_glass_stroke`, nền `@color/color_surface_glass`, hiệu ứng chạm `android:foreground="?attr/selectableItemBackground"`.
- **Cấu trúc 3 cột ngang trong `ConstraintLayout`:**
    1. **Trái:** `MaterialCheckBox` (`id: cb_task_completed`), vùng chạm `48dp x 48dp`, căn giữa theo trục dọc của dòng tiêu đề.
    2. **Giữa (chiếm phần lớn chiều ngang):**
        - Dòng 1: `tv_task_title` (`16sp`, tối đa 2 dòng `ellipsize="end"`). Khi `isCompleted == true`, bật `Paint.STRIKE_THRU_TEXT_FLAG` và đổi màu chữ sang `@color/color_text_disabled`, giảm `alpha` toàn thẻ xuống `0.78f`.
        - Dòng 2 (ẩn `GONE` nếu mô tả rỗng): `tv_task_description` (`14sp`, tối đa 2 dòng, màu `@color/color_text_secondary`).
        - Dòng 3 (Hàng мета-data & Huy hiệu, cách trên `8dp`): Hiển thị gọn gàng:
            - Huy hiệu **Độ ưu tiên** (`tv_badge_priority`: nền `@color/priority_*_bg`, chữ `@color/priority_*_text`, đệm ngang `8dp`, dọc `2dp`, bo góc `8dp`).
            - Huy hiệu **Nhãn phân loại** (`tv_badge_category`: nền `@color/color_primary_container`, chữ `@color/color_on_primary_container`).
            - Nhãn **Thời hạn (`tv_due_date`)**: Nếu quá hạn (`DateTimeUtils.isOverdue`), đổi chữ và nền sang `@color/status_overdue_text` / `@color/status_overdue_bg` kèm chữ *"Quá hạn"*.
            - Biểu tượng chuông nhỏ (`ic_reminder`) nếu công việc có hẹn giờ nhắc nhở, và chỉ số công việc con (ví dụ `☑ 1/3`) nếu có Subtasks.
    3. **Phải:** Nút xóa nhanh `ImageButton` (`id: btn_delete_task`, kích thước `48dp x 48dp`, nền `?attr/selectableItemBackgroundBorderless`, icon `@drawable/ic_delete`, tint `@color/color_text_secondary`).

### 4.3. Thẻ Ghi chú Nhanh (`Note Card` — `item_note.xml`)
- `MaterialCardView` bo góc `16dp`, viền `1dp` động theo `note.getColorIndex()` (`note_color_0_stroke` .. `note_color_5_stroke`), nền động theo `note_color_0_bg` .. `note_color_5_bg`.
- **Hàng 1:** Tiêu đề ghi chú (`tv_note_title`, `16sp`, `sans-serif-medium`, tối đa 2 dòng) nằm bên trái; nút Ghim (`btn_pin_note`, `48dp x 48dp`, icon `@drawable/ic_pin`) nằm góc phải trên. Khi `isPinned == true`, icon Ghim đổi màu sang `@color/color_primary` và hiện nhãn nhỏ *"Đã ghim"*.
- **Hàng 2:** Nội dung trích dẫn (`tv_note_content`, `14sp`, tối đa 6 dòng ở chế độ Lưới 2 cột hoặc 3 dòng ở chế độ Danh sách 1 cột, `ellipsize="end"`). Nếu tiêu đề trống, dòng nội dung được đôn lên vị trí chính.
- **Hàng 3 (Footer):** Thời gian cập nhật (`tv_note_date`, `12sp`, màu `@color/color_text_secondary`) ở bên trái và nút Xóa (`btn_delete_note`, `48dp x 48dp`) ở góc phải dưới.

### 4.4. Thanh Chip Lọc (`Filter ChipGroup`)
- Đặt trong `HorizontalScrollView` (`scrollbars="none"`), chiều cao mỗi `Chip` đạt chuẩn chạm `48dp` (nhờ `chipMinTouchTargetSize="48dp"`).
- Sử dụng style `Widget.Material3.Chip.Filter`:
    - Khi chưa chọn: Nền `@color/color_surface_glass`, viền `1dp` `@color/color_glass_stroke`, chữ `@color/color_text_secondary`.
    - Khi đang chọn (`checked`): Nền `@color/color_primary_container`, viền `1dp` `@color/color_primary`, chữ `@color/color_on_primary_container`.

### 4.5. Ô Nhập liệu (`TextInputLayout` / Search Bar)
- Trong màn hình Thêm/Sửa công việc: Dùng style `Widget.Material3.TextInputLayout.OutlinedBox` với góc bo `14dp`, nền hộp `@color/color_surface_glass`, viền mặc định `@color/color_glass_stroke`, viền khi focus `@color/color_primary` (`2dp`).
- Trong màn hình Ghi chú nhanh (`AddEditNoteActivity`): Thiết kế dạng trang giấy phẳng tối giản (không đóng khung hộp cứng nhắc), ô tiêu đề lớn `20sp` ở trên, đường kẻ mảnh `1dp` và vùng soạn thảo nội dung rộng mở bên dưới; dãy 6 nút tròn chọn màu thẻ (`48dp x 48dp` mỗi nút) đặt ngay phía trên vùng soạn thảo.

### 4.6. Thanh Điều hướng Dưới (`BottomNavigationView`) & Nút `FAB`
- `BottomNavigationView`: Nền `@color/color_surface`, có đường viền mảnh `1dp` `@color/color_divider` ở cạnh trên thay cho bóng đổ dày; chỉ báo mục đang chọn (`itemActiveIndicatorStyle`) màu `@color/color_primary_container`.
- `FloatingActionButton` (`id: fab_add`): Đặt góc phải dưới, cách `BottomNavigationView` `16dp` và cách lề phải `16dp`, màu nền `@color/color_primary`, màu icon `@color/color_on_primary`, góc bo `16dp`.

### 4.7. `Snackbar` Hoàn tác & Hộp thoại Xác nhận (`MaterialAlertDialog`)
- **Snackbar:** Neo phía trên `fab_add` (`setAnchorView(binding.fabAdd)`) để không bao giờ đè lên nút Thêm mới hay `BottomNavigationView`. Nút hành động `"Hoàn tác"` nổi bật dễ bấm.
- **Hộp thoại (`MaterialAlertDialogBuilder`):** Dùng cho xác nhận Xóa (UC 3.4, 3.9), cảnh báo thoát khi chưa lưu, và chọn tiêu chí Sắp xếp (UC 3.12). Nút Xóa mang màu cảnh báo `@color/status_overdue_text`.

### 4.8. Trạng thái Rỗng (`EmptyStateView`)
- Căn giữa vùng danh sách, gồm:
    1. Hình tròn nền mềm (`72dp x 72dp`, nền `@color/color_primary_container`) chứa icon Vector (`36dp x 36dp`, tint `@color/color_primary`).
    2. Tiêu đề trạng thái rỗng (`16sp`, `sans-serif-medium`, màu `@color/color_text_primary`, ví dụ: *"Chưa có công việc nào"*).
    3. Dòng hướng dẫn gợi ý (`14sp`, màu `@color/color_text_secondary`, căn giữa, lề ngang `32dp`, ví dụ: *"Nhấn nút + ở góc dưới để tạo công việc đầu tiên của bạn"*).

---

## 5. ÁNH XẠ SANG THUỘC TÍNH THEME MATERIAL 3 (`themes.xml` & `themes.xml (night)`)

Để việc cài đặt ở **Step `M0-S2`** diễn ra chính xác và đồng bộ trên toàn bộ ứng dụng, các thuộc tính trong `res/values/themes.xml` và `res/values-night/themes.xml` được ánh xạ trực tiếp như sau:

| Thuộc tính Material 3 (`attr`) | Giá trị ánh xạ trong `themes.xml` | Tác dụng trên toàn hệ thống |
| :--- | :--- | :--- |
| `parent` | `Theme.Material3.DayNight.NoActionBar` | Tự động chuyển đổi Light/Dark và dùng `MaterialToolbar` tùy chỉnh |
| `colorPrimary` | `@color/color_primary` | Màu chủ đạo cho FAB, Checkbox, Switch, con trỏ văn bản, ProgressBar |
| `colorOnPrimary` | `@color/color_on_primary` | Màu chữ/icon trên nền `colorPrimary` |
| `colorPrimaryContainer` | `@color/color_primary_container` | Nền của Filter Chip đang chọn, Active Indicator của BottomNav |
| `colorOnPrimaryContainer` | `@color/color_on_primary_container` | Màu chữ/icon trên `colorPrimaryContainer` |
| `colorSecondary` | `@color/color_secondary` | Màu nhấn phụ |
| `android:colorBackground` | `@color/color_background` | Màu nền gốc của mọi Activity |
| `colorSurface` | `@color/color_surface` | Màu bề mặt của Toolbar, BottomNavigation, Dialog, Menu |
| `colorOnSurface` | `@color/color_text_primary` | Màu chữ chính mặc định trên mọi bề mặt |
| `colorOnSurfaceVariant` | `@color/color_text_secondary` | Màu chữ phụ, icon chưa kích hoạt, nhãn gợi ý |
| `colorOutline` | `@color/color_glass_stroke` | Màu viền `1dp` mặc định của Card, Chip, Outlined TextField |
| `colorError` | `@color/status_overdue_text` | Màu báo lỗi của `TextInputLayout` khi bỏ trống tiêu đề hoặc sai giờ nhắc |
| `android:statusBarColor` | `@color/color_surface` | Đồng bộ màu thanh trạng thái với `MaterialToolbar` |
| `android:windowLightStatusBar` | `true` (Light) / `false` (Dark) | Đảm bảo icon pin/sóng/giờ trên thanh trạng thái luôn rõ nét |

---

## 6. QUY TẮC TRỢ NĂNG & KHẢ NĂNG TIẾP CẬN (ACCESSIBILITY RULES)

1. **Kích thước vùng chạm tối thiểu (`48dp x 48dp`):**
    - Mọi phần tử tương tác (`MaterialCheckBox`, nút Xóa `ImageButton`, nút Ghim `ImageButton`, 6 nút tròn chọn màu thẻ ghi chú, nút Xóa công việc con, các `Chip` lọc) bắt buộc có `minWidth="48dp"` và `minHeight="48dp"` (hoặc dùng `padding` mở rộng vùng chạm đạt đủ `48dp`).
2. **Mô tả nội dung cho trình đọc màn hình (`android:contentDescription`):**
    - Mọi `ImageView` / `ImageButton` / `FloatingActionButton` có ý nghĩa thao tác đều phải gán `android:contentDescription="@string/cd_..."` song ngữ Việt/Anh (ví dụ: `@string/cd_add_task`, `@string/cd_delete_task`, `@string/cd_pin_note`, `@string/cd_toggle_grid_view`).
    - Các icon thuần túy trang trí đi kèm cạnh nhãn văn bản (như icon lịch nhỏ nằm sát bên cạnh chuỗi ngày hạn chót) đặt `android:importantForAccessibility="no"` để TalkBack không đọc lặp.
3. **Thích ứng cỡ chữ lớn (Large Font Scaling & Reflow):**
    - Không khóa cứng chiều cao (`layout_height="40dp"`) cho các thẻ chứa văn bản (`MaterialCardView`, dòng tiêu đề công việc/ghi chú). Luôn dùng `layout_height="wrap_content"` kết hợp `minHeight` để khi người dùng tăng cỡ chữ hệ thống lên `130%–150%`, chữ không bị cắt mất chân.
4. **Không truyền tải thông tin chỉ bằng màu sắc (Color-blind Safe):**
    - Độ ưu tiên không chỉ phân biệt bằng màu Đỏ/Vàng/Xanh mà luôn hiển thị rõ nhãn chữ (`Cao` / `Trung bình` / `Thấp`).
    - Trạng thái Quá hạn luôn đi kèm nhãn chữ (`Quá hạn`) bên cạnh màu cảnh báo.
    - Ô chọn màu thẻ ghi chú trong `AddEditNoteActivity` luôn hiển thị dấu tích (`✓`) hoặc viền đậm bao quanh màu đang được chọn.
