@echo off
echo ==========================================
echo 🚀 Starting Backend Server (Spring Boot)...
echo ==========================================

cd backend
call gradlew.bat bootRun
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Backend start failed!
    pause
    exit /b %ERRORLEVEL%
)
pause

