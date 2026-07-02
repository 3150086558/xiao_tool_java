#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
BASE_URL="${BASE_URL:-http://127.0.0.1:${APP_PORT:-8000}}"

echo "检查健康接口..."
curl -fsS "$BASE_URL/api/health" >/dev/null

echo "检查首页..."
curl -fsS "$BASE_URL/" | grep -q "小肖记账"

echo "检查新增账单..."
curl -fsS -X POST "$BASE_URL/api/records" \
  -H 'Content-Type: application/json' \
  -d '{"record_date":"2026-06-27","type":"expense","category":"测试","amount":1.23,"account":"测试账户","note":"smoke test"}' >/dev/null

echo "检查列表接口..."
curl -fsS "$BASE_URL/api/records" | grep -q "测试"

echo "检查汇总接口..."
curl -fsS "$BASE_URL/api/summary" | grep -q "expense"

echo "检查通过。"
