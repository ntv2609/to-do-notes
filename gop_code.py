#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
gop_code.py - Gom source dự án Android (Java) thành MỘT file text để đính kèm cho AI.

Đặt file này ở thư mục gốc dự án (cùng cấp với settings.gradle / app/).

Cách dùng:
  python gop_code.py                       gom toàn bộ code (KHÔNG gồm tài liệu .md/.txt)
  python gop_code.py app/src/main/java/com/ten/app/data app/src/main/res/layout
                                           chỉ gom các thư mục/file được chỉ định (dùng cho từng step)
  python gop_code.py --docs                kèm cả tài liệu .md/.txt (SPEC, PLAN, AGENTS...)
  python gop_code.py --changed             chỉ file đã đổi so với commit gần nhất (cần git),
                                           dùng để đưa phần AI vừa sửa cho AI khác review
  python gop_code.py --no-tree             không in cây thư mục ở đầu file
  python gop_code.py --out ten_khac.txt    đổi tên file đầu ra (mặc định tat_ca_code_cu.txt)
  python gop_code.py --max-kb 200          bỏ qua file lớn hơn 200 KB (mặc định 400)
"""

import argparse
import os
import re
import subprocess
import sys
from pathlib import Path

try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

ROOT = Path(__file__).resolve().parent
DEFAULT_OUT = "tat_ca_code_cu.txt"

# Thư mục không bao giờ gom (build, cache, IDE, sao lưu của tach_code.py)
EXCLUDE_DIRS = {
    ".git", ".gradle", ".idea", ".vscode", ".cxx", ".externalNativeBuild",
    ".backup_tach", "build", "captures", "node_modules", "__pycache__",
}

# Thư mục chỉ gom khi bạn chỉ định rõ đường dẫn (vd: python gop_code.py app/schemas)
OPT_IN_DIRS = {"schemas"}  # Room export schema, thường rất lớn

# File không bao giờ gom (bí mật, tạp chất, chính các script)
EXCLUDE_FILES = {
    "gop_code.py", "tach_code.py", "code_moi.md", DEFAULT_OUT,
    "local.properties", "keystore.properties", "google-services.json",
    "gradlew", "gradlew.bat", ".DS_Store", "Thumbs.db",
}

CODE_EXT = {
    ".java", ".xml", ".gradle", ".kts", ".toml", ".pro", ".properties",
    ".json", ".yml", ".yaml", ".sql",
}
DOC_EXT = {".md", ".txt"}
SPECIAL_NAMES = {".gitignore", ".gitattributes", ".editorconfig"}

SECRET_PATTERNS = [
    (re.compile(r"AIza[0-9A-Za-z_\-]{30,}"), "khoá Google API"),
    (re.compile(r"sk-[A-Za-z0-9]{20,}"), "khoá dạng sk-..."),
    (re.compile(r"-----BEGIN [A-Z ]*PRIVATE KEY-----"), "private key"),
    (
        re.compile(
            r"(?i)(api[_-]?key|secret|passwd|password|token|storePassword|keyPassword)"
            r"\s*[=:]\s*[\"'][^\"']{6,}[\"']"
        ),
        "gán chuỗi bí mật trực tiếp trong code",
    ),
]


def is_excluded_name(name, skip_names):
    if name in skip_names:
        return True
    low = name.lower()
    if low.startswith(".env"):
        return True
    return low.endswith((".log", ".lock", ".jks", ".keystore", ".apk", ".aab"))


def matches_only(rel, only):
    if not only:
        return True
    for p in only:
        if rel == p or rel.startswith(p.rstrip("/") + "/"):
            return True
    return False


def git_changed():
    try:
        res = subprocess.run(
            ["git", "status", "--porcelain", "-uall"],
            cwd=ROOT, capture_output=True, text=True, encoding="utf-8", check=True,
        )
    except Exception as exc:
        sys.exit(f"Không chạy được git ({exc}). Hãy chạy `git init` trước hoặc bỏ --changed.")
    files = set()
    for line in res.stdout.splitlines():
        if len(line) < 4:
            continue
        path = line[3:]
        if " -> " in path:
            path = path.split(" -> ")[-1]
        files.add(path.strip().strip('"').replace("\\", "/"))
    return files


def read_text(fp):
    raw = fp.read_bytes()
    try:
        return raw.decode("utf-8-sig"), False
    except UnicodeDecodeError:
        return raw.decode("utf-8", errors="replace"), True


def main():
    ap = argparse.ArgumentParser(description="Gom code dự án Android (Java) cho AI")
    ap.add_argument("paths", nargs="*", help="thư mục/file cần gom (bỏ trống = toàn bộ)")
    ap.add_argument("--docs", action="store_true", help="kèm tài liệu .md/.txt")
    ap.add_argument("--changed", action="store_true", help="chỉ file đã đổi (git)")
    ap.add_argument("--no-tree", action="store_true", help="không in cây thư mục")
    ap.add_argument("--out", default=DEFAULT_OUT, help="tên file đầu ra")
    ap.add_argument("--max-kb", type=int, default=400, help="bỏ qua file lớn hơn N KB")
    args = ap.parse_args()

    out_path = ROOT / args.out
    skip_names = EXCLUDE_FILES | {Path(args.out).name}
    only = [p.replace("\\", "/").strip("/") for p in args.paths if p.strip()]
    if args.changed:
        only = sorted(set(only) | git_changed()) if only else sorted(git_changed())
        if not only:
            print("Không có file nào thay đổi so với commit gần nhất.")
            return

    all_files = []   # mọi file (để in cây thư mục)
    included = []    # (rel, text)
    warnings = []
    secret_hits = []
    marker_hits = []

    for cur, dirs, files in os.walk(ROOT):
        cur_path = Path(cur)
        dirs[:] = sorted(d for d in dirs if d not in EXCLUDE_DIRS)
        for fn in sorted(files):
            if is_excluded_name(fn, skip_names):
                continue
            fp = cur_path / fn
            rel = fp.relative_to(ROOT).as_posix()
            parts = Path(rel).parts

            opt_in_hit = any(p in OPT_IN_DIRS for p in parts[:-1])
            if opt_in_hit and not (only and matches_only(rel, only)):
                continue

            ext = fp.suffix.lower()
            wanted = (
                ext in CODE_EXT
                or fn in SPECIAL_NAMES
                or (args.docs and ext in DOC_EXT)
            )
            take = wanted and matches_only(rel, only)
            all_files.append((rel, take))
            if not take:
                continue

            size_kb = fp.stat().st_size / 1024
            if size_kb > args.max_kb:
                warnings.append(f"Bỏ qua {rel}: {size_kb:.0f} KB > {args.max_kb} KB")
                all_files[-1] = (rel, False)
                continue
            try:
                text, lossy = read_text(fp)
            except Exception as exc:
                warnings.append(f"Không đọc được {rel}: {exc}")
                all_files[-1] = (rel, False)
                continue
            if lossy:
                warnings.append(f"{rel}: không phải UTF-8, một số ký tự đã bị thay thế")
            text = text.replace("\r\n", "\n").replace("\r", "\n")
            included.append((rel, text))

            for lineno, line in enumerate(text.split("\n"), 1):
                if re.match(r"^#\s*File:", line):
                    marker_hits.append(f"{rel}:{lineno}")
                for pattern, label in SECRET_PATTERNS:
                    if pattern.search(line):
                        secret_hits.append(f"{rel}:{lineno} ({label})")
                        break

    included.sort(key=lambda it: it[0].lower())
    all_files.sort(key=lambda it: it[0].lower())

    with out_path.open("w", encoding="utf-8", newline="\n") as out:
        if not args.no_tree:
            out.write("# PROJECT_TREE\n")
            out.write("# [+] = nội dung được kèm bên dưới (sau dòng '# File: ...')\n")
            out.write("# [ ] = chỉ có tên file, CHƯA gửi nội dung (cần thì hỏi tôi)\n")
            shown_blank = 0
            for rel, take in all_files:
                if take:
                    out.write(f"[+] {rel}\n")
                elif shown_blank < 400:
                    out.write(f"[ ] {rel}\n")
                    shown_blank += 1
            if shown_blank >= 400:
                out.write("[ ] ... (còn nhiều file khác, đã rút gọn)\n")
            out.write("# END_PROJECT_TREE\n")
        for rel, text in included:
            out.write(f"\n# File: {rel}\n")
            out.write(text.rstrip())
            out.write("\n\n")

    total_chars = sum(len(t) for _, t in included)
    print(f"Đã gom {len(included)} file vào: {out_path.name}")
    print(f"Kích thước: {total_chars:,} ký tự (~{total_chars // 4:,} token ước tính)")
    biggest = sorted(included, key=lambda it: len(it[1]), reverse=True)[:5]
    if biggest:
        print("File lớn nhất:")
        for rel, text in biggest:
            print(f"  {len(text):>8,} ký tự  {rel}")
    if args.changed:
        print("Chế độ --changed: chỉ gồm file đổi so với commit gần nhất.")
    if not only and total_chars > 600_000:
        print("CẢNH BÁO: quá lớn. Hãy gom theo từng step: python gop_code.py <đường dẫn liên quan>")

    for w in warnings:
        print(f"CẢNH BÁO: {w}")
    if marker_hits:
        print("CẢNH BÁO: các dòng bắt đầu bằng '# File:' trong nội dung sẽ gây nhầm khi tách:")
        for h in marker_hits[:10]:
            print(f"  {h}")
    if secret_hits:
        print("\nCẢNH BÁO BẢO MẬT: có thể có bí mật trong code sắp gửi cho AI:")
        for h in secret_hits[:20]:
            print(f"  {h}")
        print("Chuyển chúng ra local.properties / BuildConfig rồi gom lại trước khi đính kèm.")


if __name__ == "__main__":
    main()
