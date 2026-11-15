#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
tach_code.py - Tách khối code AI trả về (code_moi.md) rồi ghi vào dự án Android (Java).

Đặt file này ở thư mục gốc dự án, dán câu trả lời của AI vào code_moi.md rồi chạy:

  python tach_code.py                 kiểm tra + ghi (tự sao lưu bản cũ vào .backup_tach/<thời gian>/)
  python tach_code.py --dry-run       chỉ xem trước, KHÔNG ghi gì
  python tach_code.py --force         bỏ qua các cảnh báo chặn (output cụt, nghi rút gọn, placeholder...)
  python tach_code.py --src khac.md   đọc từ file khác

Định dạng AI phải trả về:
  # MANIFEST: <số file>
  # File: duong/dan/file1
  <nội dung đầy đủ>
  # File: duong/dan/file2
  <nội dung đầy đủ>
  # END_OF_BATCH
(Có hoặc không có hàng rào ``` bao ngoài đều được; chữ giải thích nằm ngoài MANIFEST..END_OF_BATCH bị bỏ qua.)
"""

import argparse
import difflib
import json
import re
import shutil
import sys
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path

try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

ROOT = Path(__file__).resolve().parent
DEFAULT_SRC = "code_moi.md"
BACKUP_DIR = ".backup_tach"
KEEP_BACKUPS = 10

EXCLUDE_DIRS = {
    ".git", ".gradle", ".idea", ".vscode", ".cxx", ".externalNativeBuild",
    BACKUP_DIR, "build", "captures", "node_modules", "__pycache__",
}
PROTECTED_FILES = {
    "gop_code.py", "tach_code.py", "code_moi.md", "tat_ca_code_cu.txt",
    "local.properties", "keystore.properties", "google-services.json",
    "gradlew", "gradlew.bat", "gradle-wrapper.jar",
}
ALLOWED_EXT = {
    ".java", ".xml", ".gradle", ".kts", ".toml", ".pro", ".properties",
    ".json", ".yml", ".yaml", ".sql", ".md", ".txt",
}
ALLOWED_NAMES = {".gitignore", ".gitattributes", ".editorconfig"}
CODE_EXT_FOR_PLACEHOLDER = {".java", ".xml", ".gradle", ".kts", ".toml", ".pro"}
SYNC_TRIGGERS = {"build.gradle", "build.gradle.kts", "settings.gradle", "settings.gradle.kts",
                 "libs.versions.toml", "gradle.properties", "androidmanifest.xml"}

FILE_HDR = re.compile(r"^#\s*File:\s*(.+?)\s*$")
MANIFEST = re.compile(r"^#\s*MANIFEST:\s*(\d+)\s*$")
END_MARK = re.compile(r"^#\s*END_OF_BATCH\s*$")
FENCE = re.compile(r"^\s*`{3,}[\w-]*\s*$")

COMMENT_PREFIX = re.compile(r"^\s*(//|/\*|\*|<!--|#)")
PLACEHOLDER_WORDS = re.compile(
    r"(?i)(giữ nguyên|phần còn lại|code cũ|như cũ|existing code|rest of|same as before|unchanged)"
)
ELLIPSIS_LINE = re.compile(r"^\s*(//|/\*|<!--|#)?\s*\.{3,}\s*(\*/|-->)?\s*$")


def parse(text):
    lines = text.replace("\r\n", "\n").replace("\r", "\n").split("\n")

    expected = None
    start = None
    for i, line in enumerate(lines):
        m = MANIFEST.match(line)
        if m:
            expected = int(m.group(1))
            start = i + 1
            break
    if start is None:
        start = len(lines)
        for i, line in enumerate(lines):
            if FILE_HDR.match(line):
                start = i
                break

    end = None
    for i in range(len(lines) - 1, start - 1, -1):
        if END_MARK.match(lines[i]):
            end = i
            break

    body = lines[start:end] if end is not None else lines[start:]
    if end is None:
        while body and (not body[-1].strip() or FENCE.match(body[-1])):
            body.pop()

    entries = []
    cur = None
    stray = [l for l in body[: next((i for i, l in enumerate(body) if FILE_HDR.match(l)), len(body))] if l.strip()]
    for line in body:
        m = FILE_HDR.match(line)
        if m:
            if cur:
                entries.append(cur)
            cur = [m.group(1), []]
        elif cur is not None:
            cur[1].append(line)
    if cur:
        entries.append(cur)
    return expected, end is not None, entries, stray


def clean_path(raw):
    p = raw.strip().strip("`").strip().strip("\"'").replace("\\", "/")
    while p.startswith("./"):
        p = p[2:]
    return p


def count_braces(java_text):
    t = re.sub(r"/\*.*?\*/", "", java_text, flags=re.S)
    t = re.sub(r"//[^\n]*", "", t)
    t = re.sub(r'"(?:\\.|[^"\\\n])*"', '""', t)
    t = re.sub(r"'(?:\\.|[^'\\\n])*'", "''", t)
    return t.count("{") - t.count("}")


def check_entry(rel, content):
    """Trả về (errors, warnings). errors chặn việc ghi file (trừ khi --force)."""
    errors, warns = [], []
    p = Path(rel)
    name, ext = p.name, p.suffix.lower()

    if not rel:
        return ["đường dẫn rỗng"], warns
    if rel.startswith("/") or re.match(r"^[A-Za-z]:", rel) or ".." in p.parts:
        return ["đường dẫn tuyệt đối hoặc chứa '..'"], warns
    if name in PROTECTED_FILES or name.lower().startswith(".env") or name.lower().endswith((".jks", ".keystore")):
        return ["file được bảo vệ (bí mật/công cụ), không cho AI ghi đè"], warns
    if any(part in EXCLUDE_DIRS for part in p.parts[:-1]):
        return ["nằm trong thư mục bị loại (build/.git/.gradle/...)"], warns
    if ext not in ALLOWED_EXT and name not in ALLOWED_NAMES:
        return [f"đuôi file '{ext or name}' không nằm trong danh sách cho phép (không Kotlin, không binary)"], warns
    target = (ROOT / rel).resolve(strict=False)
    try:
        target.relative_to(ROOT.resolve())
    except ValueError:
        return ["nằm ngoài thư mục dự án"], warns

    if not content.strip():
        return ["nội dung rỗng (sợ xoá trắng file)"], warns

    if ext in CODE_EXT_FOR_PLACEHOLDER:
        hits = []
        for no, line in enumerate(content.split("\n"), 1):
            if ELLIPSIS_LINE.match(line) or (COMMENT_PREFIX.match(line) and PLACEHOLDER_WORDS.search(line)):
                hits.append(f"dòng {no}: {line.strip()[:70]}")
        if hits:
            errors.append("nghi AI rút gọn/placeholder -> " + " | ".join(hits[:3]))

    if ext == ".xml":
        try:
            ET.fromstring(content.strip())
        except ET.ParseError as exc:
            errors.append(f"XML không hợp lệ: {exc}")
    elif ext == ".json":
        try:
            json.loads(content)
        except ValueError as exc:
            errors.append(f"JSON không hợp lệ: {exc}")
    elif ext == ".java":
        diff = count_braces(content)
        if diff != 0:
            warns.append(f"ngoặc {{ }} lệch {diff:+d}: có thể bị cắt cụt, kiểm tra kỹ khi build")

    if target.exists() and target.is_file():
        try:
            old = target.read_text(encoding="utf-8-sig", errors="replace")
        except Exception:
            old = ""
        if len(old) > 500 and len(content) < 0.5 * len(old):
            errors.append(f"file mới nhỏ hơn 50% file cũ ({len(content)} vs {len(old)} ký tự): nghi bị rút gọn")
    return errors, warns


def count_changes(old, new):
    added = removed = 0
    for line in difflib.unified_diff(old.split("\n"), new.split("\n"), lineterm="", n=0):
        if line.startswith("+") and not line.startswith("+++"):
            added += 1
        elif line.startswith("-") and not line.startswith("---"):
            removed += 1
    return added, removed


def prune_backups():
    base = ROOT / BACKUP_DIR
    if not base.exists():
        return
    dirs = sorted(d for d in base.iterdir() if d.is_dir())
    for d in dirs[:-KEEP_BACKUPS]:
        shutil.rmtree(d, ignore_errors=True)


def main():
    ap = argparse.ArgumentParser(description="Tách code AI trả về và ghi vào dự án")
    ap.add_argument("--src", default=DEFAULT_SRC)
    ap.add_argument("--dry-run", action="store_true", help="chỉ xem trước")
    ap.add_argument("--force", action="store_true", help="bỏ qua các lỗi chặn")
    args = ap.parse_args()

    src = ROOT / args.src
    if not src.exists():
        sys.exit(f"Không tìm thấy file nguồn: {src}")

    expected, has_end, entries, stray = parse(src.read_text(encoding="utf-8-sig"))

    if not entries:
        sys.exit("Không thấy dòng '# File: ...' nào. Kiểm tra lại nội dung code_moi.md.")
    if not has_end:
        msg = "Thiếu '# END_OF_BATCH': output của AI có thể bị cắt cụt."
        if not args.force:
            sys.exit("DỪNG. " + msg + " Chưa ghi file nào. Hãy yêu cầu AI xuất lại, hoặc dùng --force nếu chắc chắn.")
        print("CẢNH BÁO: " + msg)
    if expected is None:
        print("CẢNH BÁO: không có dòng '# MANIFEST: N', không đối chiếu được số file.")
    if stray:
        print(f"CẢNH BÁO: có {len(stray)} dòng chữ nằm trước file đầu tiên, đã bỏ qua.")

    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_root = ROOT / BACKUP_DIR / stamp
    seen = {}
    counts = {"new": 0, "changed": 0, "same": 0, "rejected": 0}
    changed_names = set()
    mode = "XEM TRƯỚC" if args.dry_run else "GHI"
    print(f"[{mode}] {len(entries)} file trong {src.name}\n")

    for raw_path, lines in entries:
        rel = clean_path(raw_path)
        content = "\n".join(lines).strip("\n") + "\n"
        errors, warns = check_entry(rel, content)

        if rel in seen:
            warns.append("trùng đường dẫn trong cùng đợt, bản sau sẽ đè bản trước")
        seen[rel] = True

        blocking = errors and not args.force
        for w in warns:
            print(f"  CẢNH BÁO  {rel}: {w}")
        if blocking:
            counts["rejected"] += 1
            for e in errors:
                print(f"  BỎ QUA    {rel}: {e}")
            continue
        for e in errors:
            print(f"  (--force) {rel}: {e}")

        target = ROOT / rel
        new_text = content
        if target.exists():
            old_bytes = target.read_bytes()
            old_text = old_bytes.decode("utf-8-sig", errors="replace")
            eol = "\r\n" if b"\r\n" in old_bytes else "\n"
            if old_text.replace("\r\n", "\n") == new_text:
                counts["same"] += 1
                print(f"  giữ nguyên {rel}")
                continue
            add, rem = count_changes(old_text.replace("\r\n", "\n"), new_text)
            print(f"  SỬA       {rel}  (+{add} / -{rem} dòng)")
            counts["changed"] += 1
            if not args.dry_run:
                dest = backup_root / rel
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(target, dest)
        else:
            eol = "\n"
            print(f"  MỚI       {rel}  ({len(new_text.splitlines())} dòng)")
            counts["new"] += 1

        changed_names.add(Path(rel).name.lower())
        if not args.dry_run:
            target.parent.mkdir(parents=True, exist_ok=True)
            with target.open("w", encoding="utf-8", newline=eol) as fh:
                fh.write(new_text)

    if not args.dry_run:
        prune_backups()

    print(
        f"\nKết quả: {counts['new']} mới, {counts['changed']} sửa, "
        f"{counts['same']} không đổi, {counts['rejected']} bị bỏ qua."
    )
    if expected is not None and expected != len(entries):
        print(f"CẢNH BÁO: MANIFEST báo {expected} file nhưng tách được {len(entries)}.")
    if counts["changed"] and not args.dry_run:
        print(f"Bản cũ của các file đã sửa nằm ở {BACKUP_DIR}/{stamp}/")
    if counts["rejected"]:
        print("Có file bị bỏ qua: đọc lý do ở trên, nhờ AI xuất lại đúng file đó, hoặc dùng --force nếu chắc chắn.")
    if changed_names & SYNC_TRIGGERS and not args.dry_run:
        print("Nhắc: build.gradle / libs.versions.toml / AndroidManifest.xml vừa đổi -> bấm 'Sync Project with Gradle Files' trong Android Studio.")

    gi = ROOT / ".gitignore"
    if gi.exists() and BACKUP_DIR not in gi.read_text(encoding="utf-8", errors="replace"):
        print(f"Gợi ý: thêm '{BACKUP_DIR}/', 'code_moi.md', 'tat_ca_code_cu.txt' vào .gitignore.")
    if not args.dry_run:
        print("Tiếp theo: git diff --stat  ->  Build/Run trong Android Studio  ->  commit nếu chạy ổn.")


if __name__ == "__main__":
    main()
