@echo off
cd /d %~dp0

echo ============================================
echo   0701_my_project
============================================
echo.

REM check python
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python not found. Please install Python 3.10+ and check Add Python to PATH
    pause
    exit /b 1
)

if not exist .venv (
    echo [1/3] Creating venv...
    python -m venv .venv
    if errorlevel 1 (
        echo [ERROR] Failed to create venv
        pause
        exit /b 1
    )
)

echo [2/3] Activating venv...
call .venv\Scripts\activate.bat >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Failed to activate venv
    pause
    exit /b 1
)

echo [3/3] Installing deps...
python -m pip install -q --upgrade pip >nul 2>&1
python -m pip install -q -r requirements.txt >nul 2>&1

echo.
echo Running at http://127.0.0.1:8000
echo.

python app.py

echo.
echo Server stopped. Press any key to close...
pause >nul
