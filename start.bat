@echo off
chcp 65001 >nul
echo ========================================
echo   组织权限管理与个人工具系统 - 一键启动
echo ========================================
echo.

echo [1/2] 启动后端服务 (端口 2222)...
start "后端服务" cmd /k "cd /d backend-java && mvnw.cmd spring-boot:run"

timeout /t 5 /nobreak >nul

echo [2/2] 启动前端服务 (端口 2221)...
start "前端服务" cmd /k "cd /d frontend && npm run dev"

echo.
echo ========================================
echo   启动完成！
echo   后端地址: http://localhost:2222
echo   前端地址: http://localhost:2221
echo ========================================
echo.
echo 两个命令行窗口已分别启动前后端服务
echo 请等待服务完全启动后访问浏览器
echo.
pause
