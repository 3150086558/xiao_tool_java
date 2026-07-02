#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

# 优先使用项目内虚拟环境；没有就用系统 python3
if [ -d ".venv" ]; then
  . .venv/bin/activate
fi

python3 -m pip install -r requirements.txt
python3 app.py
