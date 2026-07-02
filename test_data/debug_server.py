#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""调试版本服务器"""
import json
import os
import sqlite3
from datetime import datetime
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlparse

BASE_DIR = Path(__file__).resolve().parent
PUBLIC_DIR = BASE_DIR / "public"
DATA_DIR = BASE_DIR / "data"

DB_TYPE = os.getenv("DB_TYPE", "sqlite").strip().lower()
DB_HOST = os.getenv("DB_HOST", "127.0.0.1")
DB_PORT = int(os.getenv("DB_PORT", "5432" if DB_TYPE == "postgres" else "3306"))
DB_NAME = os.getenv("DB_NAME", "accounting")
DB_USER = os.getenv("DB_USER", "")
DB_PASSWORD = os.getenv("DB_PASSWORD", "")
APP_HOST = os.getenv("APP_HOST", "0.0.0.0")
APP_PORT = int(os.getenv("APP_PORT", "8000"))

print(f"DEBUG SERVER STARTING")
print(f"APP_PORT: {APP_PORT}")
print(f"Current directory: {os.getcwd()}")

class DebugHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(PUBLIC_DIR), **kwargs)
    
    def log_message(self, format, *args):
        print(f"[LOG] {format % args}")

    def end_headers(self):
        self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def send_json(self, payload, status=200):
        body = json.dumps(payload, ensure_ascii=False, default=str).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        print(f"\n[DEBUG] do_GET called with path: {repr(self.path)}")
        parsed = urlparse(self.path)
        print(f"[DEBUG] parsed.path: {repr(parsed.path)}")
        
        print(f"[DEBUG] Comparing to '/api/export.xlsx': {parsed.path == '/api/export.xlsx'}")
        
        if parsed.path == "/api/health":
            print("[DEBUG] Matched /api/health")
            return self.send_json({"ok": True, "db_type": DB_TYPE})
        
        if parsed.path == "/api/export.csv":
            print("[DEBUG] Matched /api/export.csv")
            return self.send_json({"ok": True, "format": "csv"})
        
        if parsed.path == "/api/export.xlsx":
            print("[DEBUG] Matched /api/export.xlsx")
            return self.send_json({"ok": True, "format": "xlsx"})
        
        print(f"[DEBUG] No match, calling super().do_GET()")
        return super().do_GET()

if __name__ == "__main__":
    server = ThreadingHTTPServer(("127.0.0.1", APP_PORT), DebugHandler)
    print(f"Debug server running at http://127.0.0.1:{APP_PORT}")
    server.serve_forever()
