@echo off
cd /d %~dp0
python -m pip install --upgrade pip
python -m pip install pyinstaller
pyinstaller --noconfirm --name "小肖记账" --add-data "public;public" app.py
pause
