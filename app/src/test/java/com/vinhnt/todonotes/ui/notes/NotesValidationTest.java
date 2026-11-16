package com.vinhnt.todonotes.ui.notes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.vinhnt.todonotes.data.local.entity.NoteEntity;

import org.junit.Test;

/**
 * Unit Test kiểm tra logic kiểm tra hợp lệ tiêu đề/nội dung ghi chú và chỉ số màu thẻ.
 */
public class NotesValidationTest {

    @Test
    public void testEmptyNote_IsInvalid() {
        String title = "   ";
        String content = "";
        boolean valid = !title.trim().isEmpty() || !content.trim().isEmpty();
        assertFalse("Ghi chú trống cả tiêu đề lẫn nội dung phải không hợp lệ", valid);
    }

    @Test
    public void testTitleOnly_IsValid() {
        String title = "Ý tưởng dự án mới";
        String content = "";
        boolean valid = !title.trim().isEmpty() || !content.trim().isEmpty();
        assertTrue("Ghi chú chỉ có tiêu đề phải hợp lệ", valid);
    }

    @Test
    public void testContentOnly_IsValid() {
        String title = "";
        String content = "Ghi nhanh danh sách đồ cần mua ở siêu thị";
        boolean valid = !title.trim().isEmpty() || !content.trim().isEmpty();
        assertTrue("Ghi chú chỉ có nội dung phải hợp lệ", valid);
    }

    @Test
    public void testNoteColorIndex_Clamping() {
        NoteEntity noteUnder = new NoteEntity("Title", "Content", -5, false, 0L, 0L);
        assertEquals(0, noteUnder.getColorIndex());

        NoteEntity noteOver = new NoteEntity("Title", "Content", 10, false, 0L, 0L);
        assertEquals(5, noteOver.getColorIndex());
    }
}