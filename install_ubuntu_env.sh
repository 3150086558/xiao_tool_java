#!/usr/bin/env bash
set -e

echo "安装 Python、pip、venv、PostgreSQL/MySQL 客户端依赖..."
sudo apt update
sudo apt install -y python3 python3-pip python3-venv build-essential libpq-dev default-libmysqlclient-dev pkg-config

echo "创建 Python 虚拟环境..."
cd "$(dirname "$0")"
python3 -m venv .venv
. .venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt

echo "环境安装完成。"
echo "下一步：复制 .env.example 为 .env，配置数据库，然后执行 ./run_server.sh"
